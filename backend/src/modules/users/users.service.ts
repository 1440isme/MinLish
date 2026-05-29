import { Injectable, NotFoundException } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../../prisma/prisma.service';
import { UserEntity } from './entities/user.entity';

@Injectable()
export class UsersService {
  constructor(private prisma: PrismaService) {}

  async findByEmail(email: string): Promise<User | null> {
    return this.prisma.user.findUnique({
      where: {
        email,
        deletedAt: null,
      },
    });
  }

  async findById(id: string): Promise<User | null> {
    return this.prisma.user.findUnique({
      where: {
        id,
        deletedAt: null,
      },
    });
  }

  async create(data: {
    email: string;
    passwordHash: string;
    fullName: string;
  }): Promise<User> {
    return this.prisma.$transaction(async (tx) => {
      const user = await tx.user.create({
        data: {
          ...data,
          authProvider: 'LOCAL',
        },
      });

      await tx.notificationSetting.create({
        data: {
          userId: user.id,
        },
      });

      await tx.deck.create({
        data: {
          ownerUserId: user.id,
          deckType: 'USER',
          visibility: 'PRIVATE',
          name: 'Favorites',
          normalizedName: 'favorites',
          description: 'Bộ sưu tập từ vựng yêu thích',
          isDefault: true,
          totalWords: 0,
        },
      });

      return user;
    });
  }

  async createGoogleUser(data: {
    email: string;
    fullName: string;
    providerId: string;
  }): Promise<User> {
    return this.prisma.$transaction(async (tx) => {
      const user = await tx.user.create({
        data: {
          email: data.email,
          fullName: data.fullName,
          providerId: data.providerId,
          authProvider: 'GOOGLE',
        },
      });

      await tx.notificationSetting.create({
        data: {
          userId: user.id,
        },
      });

      await tx.deck.create({
        data: {
          ownerUserId: user.id,
          deckType: 'USER',
          visibility: 'PRIVATE',
          name: 'Favorites',
          normalizedName: 'favorites',
          description: 'Bộ sưu tập từ vựng yêu thích',
          isDefault: true,
          totalWords: 0,
        },
      });

      return user;
    });
  }

  async updateLastLogin(id: string): Promise<void> {
    await this.prisma.user.update({
      where: { id },
      data: { lastLoginAt: new Date() },
    });
  }

  async getProfile(userId: string): Promise<UserEntity> {
    const user = await this.findById(userId);
    if (!user) {
      throw new NotFoundException('User không tồn tại');
    }
    return new UserEntity(user);
  }

  async updateProfile(
    userId: string,
    data: Partial<User>,
  ): Promise<UserEntity> {
    const user = await this.prisma.user.update({
      where: { id: userId },
      data,
    });
    return new UserEntity(user);
  }
}
