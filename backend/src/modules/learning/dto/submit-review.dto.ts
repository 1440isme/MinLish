import { ApiProperty } from '@nestjs/swagger';
import { IsEnum, IsISO8601, IsString } from 'class-validator';
import { ReviewRating } from '../enums/review-rating.enum';

export class SubmitReviewDto {
  @ApiProperty({ example: 'uuid-vocabulary' })
  @IsString()
  vocabularyId: string;

  @ApiProperty({
    enum: ReviewRating,
    example: ReviewRating.GOOD,
  })
  @IsEnum(ReviewRating)
  rating: ReviewRating;

  @ApiProperty({
    example: '2026-05-30T02:22:00.000Z',
    description: 'ISO 8601 timestamp in UTC',
  })
  @IsISO8601({ strict: true })
  reviewedAt: string;
}
