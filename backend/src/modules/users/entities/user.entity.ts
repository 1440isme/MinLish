import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose } from 'class-transformer';

// ----------------------------------------------------------------
// Enums (mirror Prisma enums)
// ----------------------------------------------------------------

export enum AuthProvider {
  LOCAL = 'LOCAL',
  GOOGLE = 'GOOGLE',
}

export enum LearningGoal {
  TOEIC = 'TOEIC',
  IELTS = 'IELTS',
}

// ----------------------------------------------------------------
// User Entity
// ----------------------------------------------------------------

export class UserEntity {
  @ApiProperty({
    example: 'uuid-v4',
    description: 'User ID (UUID)',
  })
  @Expose()
  id: string;

  @ApiProperty({ example: 'user@example.com' })
  @Expose()
  email: string;

  /** passwordHash is intentionally NOT @Expose() — never serialize to response */
  passwordHash?: string | null;

  @ApiProperty({ example: 'Nguyen Van A' })
  @Expose()
  fullName: string;

  @ApiPropertyOptional({ example: 'https://cdn.example.com/avatar.jpg' })
  @Expose()
  avatarUrl?: string | null;

  @ApiProperty({
    enum: AuthProvider,
    example: AuthProvider.LOCAL,
  })
  @Expose()
  authProvider: AuthProvider;

  @ApiPropertyOptional({ example: null })
  @Expose()
  providerId?: string | null;

  @ApiPropertyOptional({
    enum: LearningGoal,
    example: LearningGoal.TOEIC,
  })
  @Expose()
  learningGoal?: LearningGoal | null;

  @ApiPropertyOptional({ example: 'uuid-level' })
  @Expose()
  currentLevelId?: string | null;

  @ApiPropertyOptional({ example: 'uuid-level' })
  @Expose()
  targetLevelId?: string | null;

  @ApiProperty({
    example: 10,
    description: 'Số từ mới học mỗi ngày',
  })
  @Expose()
  dailyNewWordsGoal: number;

  @ApiProperty({ example: 'Asia/Ho_Chi_Minh' })
  @Expose()
  timezone: string;

  @ApiProperty({ example: true })
  @Expose()
  isActive: boolean;

  @ApiPropertyOptional({ type: Date })
  @Expose()
  lastLoginAt?: Date | null;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  deletedAt?: Date | null;

  constructor(partial: Partial<UserEntity>) {
    Object.assign(this, partial);
  }
}
