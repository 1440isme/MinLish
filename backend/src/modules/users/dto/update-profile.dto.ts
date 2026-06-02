import { ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsString,
  IsOptional,
  MinLength,
  MaxLength,
  IsEnum,
  IsInt,
  Min,
  Max,
  IsUrl,
} from 'class-validator';
import { LearningGoal } from '@prisma/client';

export class UpdateProfileDto {
  @ApiPropertyOptional({
    example: 'Nguyen Van B',
    description: 'Họ và tên đầy đủ',
  })
  @IsOptional()
  @IsString()
  @MinLength(2, { message: 'Họ tên phải có ít nhất 2 ký tự' })
  @MaxLength(150, { message: 'Họ tên không được quá 150 ký tự' })
  fullName?: string;

  @ApiPropertyOptional({
    example: 'https://cdn.example.com/avatar.jpg',
    description: 'URL ảnh đại diện',
  })
  @IsOptional()
  @IsUrl({}, { message: 'Avatar URL không hợp lệ' })
  avatarUrl?: string;

  @ApiPropertyOptional({
    example: 'Asia/Ho_Chi_Minh',
    description: 'Múi giờ',
  })
  @IsOptional()
  @IsString()
  timezone?: string;

  @ApiPropertyOptional({
    enum: LearningGoal,
    example: LearningGoal.TOEIC,
    description: 'Mục tiêu học tập',
  })
  @IsOptional()
  @IsEnum(LearningGoal, { message: 'Learning goal không hợp lệ' })
  learningGoal?: LearningGoal;

  @ApiPropertyOptional({
    example: 15,
    minimum: 1,
    maximum: 100,
    description: 'Số từ mới học mỗi ngày',
  })
  @IsOptional()
  @IsInt({ message: 'Số từ mới mỗi ngày phải là số nguyên' })
  @Min(1, { message: 'Số từ mới mỗi ngày phải ít nhất 1' })
  @Max(100, { message: 'Số từ mới mỗi ngày không được quá 100' })
  dailyNewWordsGoal?: number;

  @ApiPropertyOptional({
    example: 'uuid-level-current',
    description: 'ID level hiện tại của người học (TOEIC/IELTS)',
  })
  @IsOptional()
  @IsString()
  currentLevelId?: string;

  @ApiPropertyOptional({
    example: 'uuid-level-target',
    description: 'ID level mục tiêu của người học (TOEIC/IELTS)',
  })
  @IsOptional()
  @IsString()
  targetLevelId?: string;
}
