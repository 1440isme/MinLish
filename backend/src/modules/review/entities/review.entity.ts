import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose, Type } from 'class-transformer';
import { VocabularyEntity } from '../../vocabularies/entities/vocabulary.entity';

// ----------------------------------------------------------------
// Enums
// ----------------------------------------------------------------

export enum ReviewCardStatus {
  NEW = 'NEW',
  LEARNING = 'LEARNING',
  REVIEW = 'REVIEW',
  MASTERED = 'MASTERED',
  SUSPENDED = 'SUSPENDED',
}

export enum ReviewRating {
  AGAIN = 'AGAIN',
  HARD = 'HARD',
  GOOD = 'GOOD',
  EASY = 'EASY',
}

// ----------------------------------------------------------------
// ReviewCard Entity
// SM-2 SRS state: user_id + vocabulary_id (unique)
// ----------------------------------------------------------------

export class ReviewCardEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-user' })
  @Expose()
  userId: string;

  @ApiProperty({ example: 'uuid-vocab' })
  @Expose()
  vocabularyId: string;

  @ApiProperty({
    enum: ReviewCardStatus,
    example: ReviewCardStatus.NEW,
  })
  @Expose()
  status: ReviewCardStatus;

  @ApiProperty({
    example: 0,
    description: 'Số lần ôn liên tiếp thành công',
  })
  @Expose()
  repetition: number;

  @ApiProperty({
    example: 0,
    description: 'Khoảng cách ôn tập (ngày)',
  })
  @Expose()
  intervalDays: number;

  @ApiProperty({
    example: 2.5,
    description: 'Hệ số dễ SM-2 (min 1.3)',
  })
  @Expose()
  easeFactor: number;

  @ApiProperty({
    type: Date,
    description: 'Thời điểm đến hạn ôn tiếp theo',
  })
  @Expose()
  dueAt: Date;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  lastReviewedAt?: Date | null;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  firstLearnedAt?: Date | null;

  @ApiProperty({
    example: 0,
    description: 'Số lần quên (AGAIN)',
  })
  @Expose()
  lapses: number;

  @ApiProperty({ example: 0 })
  @Expose()
  totalReviews: number;

  @ApiProperty({ example: 0 })
  @Expose()
  correctReviews: number;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  // Relation - populated with include
  @ApiPropertyOptional({ type: () => VocabularyEntity })
  @Expose()
  @Type(() => VocabularyEntity)
  vocabulary?: VocabularyEntity;

  constructor(partial: Partial<ReviewCardEntity>) {
    Object.assign(this, partial);
  }
}

// ----------------------------------------------------------------
// ReviewLog Entity
// ----------------------------------------------------------------

export class ReviewLogEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-user' })
  @Expose()
  userId: string;

  @ApiProperty({ example: 'uuid-review-card' })
  @Expose()
  reviewCardId: string;

  @ApiProperty({ example: 'uuid-vocab' })
  @Expose()
  vocabularyId: string;

  @ApiProperty({
    enum: ReviewRating,
    example: ReviewRating.GOOD,
  })
  @Expose()
  rating: ReviewRating;

  @ApiProperty({
    example: 4,
    description: 'Quality 0–5 tương ứng AGAIN/HARD/GOOD/EASY',
  })
  @Expose()
  quality: number;

  @ApiProperty({ example: true })
  @Expose()
  isCorrect: boolean;

  @ApiProperty({ example: 0 })
  @Expose()
  oldRepetition: number;

  @ApiProperty({ example: 1 })
  @Expose()
  newRepetition: number;

  @ApiProperty({ example: 0 })
  @Expose()
  oldIntervalDays: number;

  @ApiProperty({ example: 1 })
  @Expose()
  newIntervalDays: number;

  @ApiProperty({ example: 2.5 })
  @Expose()
  oldEaseFactor: number;

  @ApiProperty({ example: 2.5 })
  @Expose()
  newEaseFactor: number;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  oldDueAt?: Date | null;

  @ApiProperty({ type: Date })
  @Expose()
  newDueAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  reviewedAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  constructor(partial: Partial<ReviewLogEntity>) {
    Object.assign(this, partial);
  }
}
