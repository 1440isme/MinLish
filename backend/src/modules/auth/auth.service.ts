import {
  Injectable,
  ConflictException,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { randomBytes, createHash } from 'crypto';
import { PrismaService } from '../../prisma/prisma.service';
import { UsersService } from '../users/users.service';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import { GoogleLoginDto } from './dto/google-login.dto';
import { AuthResponseDto } from './dto/auth-response.dto';
import { UserEntity } from '../users/entities/user.entity';
import { ErrorCodes } from '../../config/common/errors/error-codes';

@Injectable()
export class AuthService {
  private readonly saltRounds = 10;

  constructor(
    private usersService: UsersService,
    private jwtService: JwtService,
    private prisma: PrismaService,
  ) {}

  async hashPassword(password: string): Promise<string> {
    return bcrypt.hash(password, this.saltRounds);
  }

  async verifyPassword(password: string, hash: string): Promise<boolean> {
    return bcrypt.compare(password, hash);
  }

  generateAccessToken(userId: string): string {
    return this.jwtService.sign({ sub: userId });
  }

  generateRefreshToken(): string {
    return randomBytes(64).toString('hex');
  }

  async storeRefreshToken(userId: string, token: string): Promise<void> {
    const tokenHash = createHash('sha256').update(token).digest('hex');
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + 30);

    await this.prisma.refreshToken.create({
      data: {
        userId,
        tokenHash,
        expiresAt,
      },
    });
  }

  async register(dto: RegisterDto): Promise<AuthResponseDto> {
    const existingUser = await this.usersService.findByEmail(dto.email);
    if (existingUser) {
      throw new ConflictException('Email đã được sử dụng');
    }

    const passwordHash = await this.hashPassword(dto.password);

    const user = await this.usersService.create({
      email: dto.email,
      passwordHash,
      fullName: dto.fullName,
    });

    const accessToken = this.generateAccessToken(user.id);
    const refreshToken = this.generateRefreshToken();
    await this.storeRefreshToken(user.id, refreshToken);

    return {
      accessToken,
      refreshToken,
      user: new UserEntity(user),
    };
  }

  async login(dto: LoginDto): Promise<AuthResponseDto> {
    const user = await this.usersService.findByEmail(dto.email);
    if (!user || !user.passwordHash) {
      throw new UnauthorizedException('Email hoặc mật khẩu không đúng');
    }

    if (!user.isActive) {
      throw new UnauthorizedException('Tài khoản đã bị vô hiệu hóa');
    }

    const isPasswordValid = await this.verifyPassword(
      dto.password,
      user.passwordHash,
    );
    if (!isPasswordValid) {
      throw new UnauthorizedException('Email hoặc mật khẩu không đúng');
    }

    await this.usersService.updateLastLogin(user.id);

    const accessToken = this.generateAccessToken(user.id);
    const refreshToken = this.generateRefreshToken();
    await this.storeRefreshToken(user.id, refreshToken);

    return {
      accessToken,
      refreshToken,
      user: new UserEntity(user),
    };
  }

  async refreshAccessToken(refreshToken: string): Promise<AuthResponseDto> {
    const tokenHash = createHash('sha256').update(refreshToken).digest('hex');

    const storedToken = await this.prisma.refreshToken.findFirst({
      where: {
        tokenHash,
        revokedAt: null,
      },
      include: { user: true },
    });

    if (!storedToken) {
      throw new UnauthorizedException('Refresh token không hợp lệ');
    }

    if (storedToken.expiresAt < new Date()) {
      throw new UnauthorizedException('Refresh token đã hết hạn');
    }

    if (!storedToken.user.isActive || storedToken.user.deletedAt) {
      throw new UnauthorizedException('Tài khoản không hợp lệ');
    }

    await this.prisma.refreshToken.update({
      where: { id: storedToken.id },
      data: { revokedAt: new Date() },
    });

    const accessToken = this.generateAccessToken(storedToken.userId);
    const newRefreshToken = this.generateRefreshToken();
    await this.storeRefreshToken(storedToken.userId, newRefreshToken);

    return {
      accessToken,
      refreshToken: newRefreshToken,
      user: new UserEntity(storedToken.user),
    };
  }

  async googleLogin(dto: GoogleLoginDto): Promise<AuthResponseDto> {
    let email: string;
    let fullName: string;
    let providerId: string;

    if (process.env.NODE_ENV !== 'production' && dto.idToken.startsWith('mock_google_token_')) {
      // Format: mock_google_token_{email}_{fullName}
      const parts = dto.idToken.split('_');
      email = parts[3] || 'mock_google_user@gmail.com';
      fullName = parts[4] || 'Mock Google User';
      providerId = `mock_google_provider_${email}`;
    } else {
      const url = `https://oauth2.googleapis.com/tokeninfo?id_token=${dto.idToken}`;
      try {
        const response = await fetch(url);
        if (!response.ok) {
          throw new UnauthorizedException({
            code: ErrorCodes.GOOGLE_AUTH_FAILED,
            message: 'Xác thực Google ID Token thất bại',
          });
        }
        const payload = (await response.json()) as any;
        email = payload.email;
        fullName = payload.name || payload.given_name || 'Google User';
        providerId = payload.sub;
      } catch (err) {
        throw new UnauthorizedException({
          code: ErrorCodes.GOOGLE_AUTH_FAILED,
          message: 'Không thể kết nối đến Google Auth API',
        });
      }
    }

    if (!email) {
      throw new UnauthorizedException({
        code: ErrorCodes.GOOGLE_AUTH_FAILED,
        message: 'Google ID Token không chứa email hợp lệ',
      });
    }

    let user = await this.prisma.user.findFirst({
      where: {
        email,
        deletedAt: null,
      },
    });

    if (user) {
      if (user.authProvider !== 'GOOGLE') {
        // Link the existing local account to Google provider
        user = await this.prisma.user.update({
          where: { id: user.id },
          data: {
            authProvider: 'GOOGLE',
            providerId,
          },
        });
      }
    } else {
      // Create a new user with Google provider
      user = await this.usersService.createGoogleUser({
        email,
        fullName,
        providerId,
      });
    }

    if (!user.isActive) {
      throw new UnauthorizedException('Tài khoản đã bị vô hiệu hóa');
    }

    await this.usersService.updateLastLogin(user.id);

    const accessToken = this.generateAccessToken(user.id);
    const refreshToken = this.generateRefreshToken();
    await this.storeRefreshToken(user.id, refreshToken);

    return {
      accessToken,
      refreshToken,
      user: new UserEntity(user),
    };
  }

  async getProfile(userId: string): Promise<UserEntity> {
    return this.usersService.getProfile(userId);
  }
}
