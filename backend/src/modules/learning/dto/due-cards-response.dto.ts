import { ApiProperty } from '@nestjs/swagger';
import { ReviewCardResponseDto } from './review-card-response.dto';

export class DueCardsResponseDto {
  @ApiProperty({ type: () => [ReviewCardResponseDto] })
  items: ReviewCardResponseDto[];

  @ApiProperty({ example: 5 })
  count: number;

  @ApiProperty({ example: 20 })
  limit: number;

  constructor(partial: Partial<DueCardsResponseDto>) {
    Object.assign(this, partial);
  }
}

