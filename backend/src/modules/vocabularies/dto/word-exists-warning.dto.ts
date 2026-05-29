import { ApiProperty } from '@nestjs/swagger';

export class ExistingVocabularyItemDto {
  @ApiProperty({ example: 'uuid-v4' })
  id: string;

  @ApiProperty({ example: 'charge' })
  word: string;

  @ApiProperty({ example: 'buộc tội' })
  meaning: string;
}

export class WordExistsWithDifferentMeaningDetailsDto {
  @ApiProperty({ example: 'WORD_EXISTS_WITH_DIFFERENT_MEANING' })
  code: string;

  @ApiProperty({
    example:
      'This word already exists with another meaning. Do you want to add a new meaning?',
  })
  message: string;

  @ApiProperty({
    type: ExistingVocabularyItemDto,
    isArray: true,
  })
  existingItems: ExistingVocabularyItemDto[];
}
