import { ApiProperty } from '@nestjs/swagger';
import { ReviewLogResponseDto } from './review-log-response.dto';

export class ReviewHistoryResponseDto {
  @ApiProperty({ type: () => [ReviewLogResponseDto] })
  items: ReviewLogResponseDto[];

  @ApiProperty({ example: 20 })
  count: number;

  @ApiProperty({ example: 20 })
  limit: number;

  constructor(partial: Partial<ReviewHistoryResponseDto>) {
    Object.assign(this, partial);
  }
}

