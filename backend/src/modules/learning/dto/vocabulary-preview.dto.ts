import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class VocabularyPreviewDto {
  @ApiProperty({ example: 'uuid-vocabulary' })
  id: string;

  @ApiProperty({ example: 'uuid-deck' })
  deckId: string;

  @ApiProperty({ example: 'abandon' })
  word: string;

  @ApiProperty({ example: 'từ bỏ' })
  meaning: string;

  @ApiPropertyOptional({ example: '/əˈbæn.dən/', nullable: true })
  pronunciation?: string | null;

  @ApiPropertyOptional({ example: 'verb', nullable: true })
  partOfSpeech?: string | null;

  constructor(partial: Partial<VocabularyPreviewDto>) {
    Object.assign(this, partial);
  }
}

