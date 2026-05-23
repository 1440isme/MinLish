import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose, Type } from 'class-transformer';
import { VocabularyEntity } from '../../vocabularies/entities/vocabulary.entity';

// ----------------------------------------------------------------
// Enums
// ----------------------------------------------------------------

export enum PracticeType {
  MULTIPLE_CHOICE = 'MULTIPLE_CHOICE',
  FILL_IN_BLANK = 'FILL_IN_BLANK',
  LISTENING = 'LISTENING',
}

export enum PracticeSessionStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export enum QuestionType {
  WORD_TO_MEANING = 'WORD_TO_MEANING',
  MEANING_TO_WORD = 'MEANING_TO_WORD',
  FILL_IN_BLANK = 'FILL_IN_BLANK',
  LISTENING_WORD = 'LISTENING_WORD',
}

// ----------------------------------------------------------------
// PracticeSession Entity
// ----------------------------------------------------------------

export class PracticeSessionEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-user' })
  @Expose()
  userId: string;

  @ApiPropertyOptional({
    example: 'uuid-deck',
    nullable: true,
  })
  @Expose()
  deckId?: string | null;

  @ApiProperty({
    enum: PracticeType,
    example: PracticeType.MULTIPLE_CHOICE,
  })
  @Expose()
  practiceType: PracticeType;

  @ApiProperty({ example: 10 })
  @Expose()
  totalQuestions: number;

  @ApiProperty({ example: 8 })
  @Expose()
  correctAnswers: number;

  @ApiProperty({ example: 2 })
  @Expose()
  wrongAnswers: number;

  @ApiProperty({
    example: 80.0,
    description: 'Tỉ lệ đúng (%)',
  })
  @Expose()
  accuracy: number;

  @ApiProperty({
    enum: PracticeSessionStatus,
    example: PracticeSessionStatus.COMPLETED,
  })
  @Expose()
  status: PracticeSessionStatus;

  @ApiProperty({ type: Date })
  @Expose()
  startedAt: Date;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  finishedAt?: Date | null;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  constructor(partial: Partial<PracticeSessionEntity>) {
    Object.assign(this, partial);
  }
}

// ----------------------------------------------------------------
// PracticeAnswer Entity
// ----------------------------------------------------------------

export class PracticeAnswerEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-session' })
  @Expose()
  sessionId: string;

  @ApiProperty({ example: 'uuid-user' })
  @Expose()
  userId: string;

  @ApiPropertyOptional({
    example: 'uuid-vocab',
    nullable: true,
  })
  @Expose()
  vocabularyId?: string | null;

  @ApiProperty({
    enum: QuestionType,
    example: QuestionType.WORD_TO_MEANING,
  })
  @Expose()
  questionType: QuestionType;

  @ApiProperty({ example: 'What does "appointment" mean?' })
  @Expose()
  questionText: string;

  @ApiPropertyOptional({
    example: ['cuộc hẹn', 'hợp đồng', 'đàm phán', 'mua hàng'],
    type: [String],
  })
  @Expose()
  optionsJson?: string[] | null;

  @ApiPropertyOptional({
    example: 'cuộc hẹn',
    nullable: true,
  })
  @Expose()
  userAnswer?: string | null;

  @ApiProperty({ example: 'cuộc hẹn' })
  @Expose()
  correctAnswer: string;

  @ApiProperty({ example: true })
  @Expose()
  isCorrect: boolean;

  @ApiProperty({ type: Date })
  @Expose()
  answeredAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  // Relation
  @ApiPropertyOptional({ type: () => VocabularyEntity })
  @Expose()
  @Type(() => VocabularyEntity)
  vocabulary?: VocabularyEntity;

  constructor(partial: Partial<PracticeAnswerEntity>) {
    Object.assign(this, partial);
  }
}
