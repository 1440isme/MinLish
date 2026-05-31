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

  @ApiPropertyOptional({
    example: '/əˈbæn.dən/',
    nullable: true,
  })
  pronunciation?: string | null;

  @ApiPropertyOptional({
    example: 'verb',
    nullable: true,
  })
  partOfSpeech?: string | null;

  @ApiPropertyOptional({
    example: 'To leave something behind permanently.',
    nullable: true,
  })
  descriptionEn?: string | null;

  @ApiPropertyOptional({
    example: 'He decided to abandon the old plan.',
    nullable: true,
  })
  example?: string | null;

  constructor(partial: Partial<VocabularyPreviewDto>) {
    Object.assign(this, partial);
  }
}

