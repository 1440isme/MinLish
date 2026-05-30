import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { ReviewRating } from '../enums/review-rating.enum';
import { VocabularyPreviewDto } from './vocabulary-preview.dto';

export class ReviewLogResponseDto {
  @ApiProperty({ example: 'uuid-review-log' })
  id: string;

  @ApiProperty({ example: 'uuid-review-card' })
  reviewCardId: string;

  @ApiProperty({ example: 'uuid-vocabulary' })
  vocabularyId: string;

  @ApiProperty({ enum: ReviewRating, example: ReviewRating.HARD })
  rating: ReviewRating;

  @ApiProperty({ example: 3 })
  quality: number;

  @ApiProperty({ example: false })
  isCorrect: boolean;

  @ApiProperty({ example: 0 })
  oldRepetition: number;

  @ApiProperty({ example: 1 })
  newRepetition: number;

  @ApiProperty({ example: 0 })
  oldIntervalDays: number;

  @ApiProperty({ example: 1 })
  newIntervalDays: number;

  @ApiProperty({ example: 2.5 })
  oldEaseFactor: number;

  @ApiProperty({ example: 2.36 })
  newEaseFactor: number;

  @ApiPropertyOptional({ type: Date, nullable: true })
  oldDueAt?: Date | null;

  @ApiProperty({ type: Date })
  newDueAt: Date;

  @ApiProperty({ type: Date })
  reviewedAt: Date;

  @ApiPropertyOptional({ type: () => VocabularyPreviewDto })
  vocabulary?: VocabularyPreviewDto;

  constructor(partial: Partial<ReviewLogResponseDto>) {
    Object.assign(this, partial);
  }
}

