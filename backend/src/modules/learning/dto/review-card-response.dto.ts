import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { ReviewCardStatus } from '../enums/review-card-status.enum';
import { VocabularyPreviewDto } from './vocabulary-preview.dto';

export class ReviewCardResponseDto {
  @ApiProperty({ example: 'uuid-review-card' })
  id: string;

  @ApiProperty({ example: 'uuid-user' })
  userId: string;

  @ApiProperty({ example: 'uuid-vocabulary' })
  vocabularyId: string;

  @ApiProperty({
    enum: ReviewCardStatus,
    example: ReviewCardStatus.REVIEW,
  })
  status: ReviewCardStatus;

  @ApiProperty({ example: 2 })
  repetition: number;

  @ApiProperty({ example: 6 })
  intervalDays: number;

  @ApiProperty({ example: 2.5 })
  easeFactor: number;

  @ApiProperty({ type: Date })
  dueAt: Date;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  lastReviewedAt?: Date | null;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  firstLearnedAt?: Date | null;

  @ApiProperty({ example: 0 })
  lapses: number;

  @ApiProperty({ example: 3 })
  totalReviews: number;

  @ApiProperty({ example: 2 })
  correctReviews: number;

  @ApiPropertyOptional({ type: () => VocabularyPreviewDto })
  vocabulary?: VocabularyPreviewDto;

  constructor(partial: Partial<ReviewCardResponseDto>) {
    Object.assign(this, partial);
  }
}
