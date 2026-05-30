import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { ReviewCardResponseDto } from './review-card-response.dto';
import { VocabularyPreviewDto } from './vocabulary-preview.dto';

export class DailyPlanResponseDto {
  @ApiPropertyOptional({ example: 'uuid-deck', nullable: true })
  deckId?: string | null;

  @ApiProperty({ example: 10 })
  newWordsGoal: number;

  @ApiProperty({ example: 24 })
  newWordsAvailable: number;

  @ApiProperty({ example: 8 })
  dueReviewCount: number;

  @ApiProperty({ type: () => [ReviewCardResponseDto] })
  dueCards: ReviewCardResponseDto[];

  @ApiProperty({ type: () => [VocabularyPreviewDto] })
  newWords: VocabularyPreviewDto[];

  constructor(partial: Partial<DailyPlanResponseDto>) {
    Object.assign(this, partial);
  }
}

