import { ApiProperty } from '@nestjs/swagger';
import { ReviewCardResponseDto } from './review-card-response.dto';
import { ReviewSummaryDto } from './review-summary.dto';

export class SubmitReviewResponseDto {
  @ApiProperty({ type: () => ReviewCardResponseDto })
  reviewCard: ReviewCardResponseDto;

  @ApiProperty({ type: () => ReviewSummaryDto })
  summary: ReviewSummaryDto;

  constructor(partial: Partial<SubmitReviewResponseDto>) {
    Object.assign(this, partial);
  }
}

