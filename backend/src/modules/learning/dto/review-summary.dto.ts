import { ApiProperty } from '@nestjs/swagger';
import { ReviewRating } from '../enums/review-rating.enum';

export class ReviewSummaryDto {
  @ApiProperty({ enum: ReviewRating, example: ReviewRating.GOOD })
  rating: ReviewRating;

  @ApiProperty({ example: 4 })
  quality: number;

  @ApiProperty({ example: true })
  isCorrect: boolean;

  @ApiProperty({ type: Date })
  reviewedAt: Date;

  constructor(partial: Partial<ReviewSummaryDto>) {
    Object.assign(this, partial);
  }
}

