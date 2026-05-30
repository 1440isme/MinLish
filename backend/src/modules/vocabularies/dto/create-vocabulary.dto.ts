import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import {
  IsBoolean,
  IsEnum,
  IsOptional,
  IsString,
  MaxLength,
} from 'class-validator';
import {
  VocabularyDifficulty,
  VocabularyPartOfSpeech,
} from '../entities/vocabulary.entity';

export class CreateVocabularyDto {
  @ApiProperty({ example: 'charge' })
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  @MaxLength(150)
  word: string;

  @ApiPropertyOptional({ example: '/tʃɑːrdʒ/' })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  @MaxLength(255)
  pronunciation?: string;

  @ApiProperty({ example: 'tính phí' })
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  meaning: string;

  @ApiPropertyOptional({ example: 'To ask an amount of money for a service.' })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  descriptionEn?: string;

  @ApiPropertyOptional({ example: 'The hotel will charge an extra fee.' })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  example?: string;

  @ApiPropertyOptional({ example: 'charge a fee; charge extra' })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  collocation?: string;

  @ApiPropertyOptional({ example: 'fee;cost;payment' })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  relatedWords?: string;

  @ApiPropertyOptional({ example: 'Business context' })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  note?: string;

  @ApiPropertyOptional({
    enum: VocabularyDifficulty,
    example: VocabularyDifficulty.MEDIUM,
  })
  @IsOptional()
  @IsEnum(VocabularyDifficulty)
  difficulty?: VocabularyDifficulty;

  @ApiPropertyOptional({
    enum: VocabularyPartOfSpeech,
    example: VocabularyPartOfSpeech.NOUN,
  })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsEnum(VocabularyPartOfSpeech)
  partOfSpeech?: VocabularyPartOfSpeech;

  @ApiPropertyOptional({
    example: false,
    description:
      'Set true only after client confirms adding same word with different meaning',
  })
  @IsOptional()
  @IsBoolean()
  allowSameWordDifferentMeaning?: boolean;
}
