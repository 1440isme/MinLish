import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose, Type } from 'class-transformer';
import { DeckEntity } from '../../decks/entities/deck.entity';

export class RecentLearningDeckResponseDto {
  @ApiProperty({
    example: true,
    description: 'true nếu tìm thấy deck học gần nhất khả dụng của user',
  })
  @Expose()
  hasRecentDeck: boolean;

  @ApiPropertyOptional({
    type: () => DeckEntity,
    nullable: true,
  })
  @Expose()
  @Type(() => DeckEntity)
  deck?: DeckEntity | null;

  @ApiPropertyOptional({
    example: '2026-05-31T12:34:56.000Z',
    nullable: true,
  })
  @Expose()
  lastStudiedAt?: Date | null;

  @ApiProperty({
    example: 8,
    description: 'Số lượng thẻ đến hạn ôn trong deck gần nhất',
  })
  @Expose()
  dueReviewCount: number;

  @ApiProperty({
    example: 24,
    description: 'Số lượng từ mới còn lại trong deck gần nhất',
  })
  @Expose()
  newWordsAvailable: number;

  constructor(partial: Partial<RecentLearningDeckResponseDto>) {
    Object.assign(this, partial);
  }
}
