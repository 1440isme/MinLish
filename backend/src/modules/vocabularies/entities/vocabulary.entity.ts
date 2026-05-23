import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose } from 'class-transformer';

// ----------------------------------------------------------------
// Enums
// ----------------------------------------------------------------

export enum VocabularyDifficulty {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
}

// ----------------------------------------------------------------
// Vocabulary Entity
// ----------------------------------------------------------------

export class VocabularyEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-deck' })
  @Expose()
  deckId: string;

  @ApiPropertyOptional({
    example: 'uuid-original-vocab',
    nullable: true,
    description:
      'ID vocabulary gốc nếu đây là bản copy/favorite',
  })
  @Expose()
  sourceVocabularyId?: string | null;

  @ApiProperty({ example: 'appointment' })
  @Expose()
  word: string;

  @ApiProperty({ example: 'appointment' })
  @Expose()
  normalizedWord: string;

  @ApiPropertyOptional({ example: '/əˈpɔɪnt.mənt/' })
  @Expose()
  pronunciation?: string | null;

  @ApiProperty({ example: 'cuộc hẹn' })
  @Expose()
  meaning: string;

  @ApiProperty({ example: 'cuộc hẹn' })
  @Expose()
  normalizedMeaning: string;

  @ApiPropertyOptional({
    example: 'An arrangement to meet someone at a particular time and place.',
  })
  @Expose()
  descriptionEn?: string | null;

  @ApiPropertyOptional({ example: 'I have an appointment with the manager at 9 a.m.' })
  @Expose()
  example?: string | null;

  @ApiPropertyOptional({ example: 'make an appointment; doctor appointment' })
  @Expose()
  collocation?: string | null;

  @ApiPropertyOptional({ example: 'meeting; schedule' })
  @Expose()
  relatedWords?: string | null;

  @ApiPropertyOptional({ example: 'Common in TOEIC office contexts.' })
  @Expose()
  note?: string | null;

  @ApiPropertyOptional({
    example: null,
    description: 'Future - MVP dùng TTS',
  })
  @Expose()
  audioUrl?: string | null;

  @ApiPropertyOptional({ example: null })
  @Expose()
  imageUrl?: string | null;

  @ApiPropertyOptional({
    enum: VocabularyDifficulty,
    example: VocabularyDifficulty.MEDIUM,
  })
  @Expose()
  difficulty?: VocabularyDifficulty | null;

  @ApiPropertyOptional({ example: 'noun' })
  @Expose()
  partOfSpeech?: string | null;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  deletedAt?: Date | null;

  constructor(partial: Partial<VocabularyEntity>) {
    Object.assign(this, partial);
  }
}
