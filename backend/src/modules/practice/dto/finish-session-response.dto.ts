import { ApiProperty } from '@nestjs/swagger';
import { Expose, Type } from 'class-transformer';
import { PracticeSessionEntity, PracticeAnswerEntity } from '../entities/practice.entity';

export class PracticeSessionSummaryDto {
  @ApiProperty({ example: 10 })
  @Expose()
  totalQuestions: number;

  @ApiProperty({ example: 8 })
  @Expose()
  correctAnswers: number;

  @ApiProperty({ example: 2 })
  @Expose()
  wrongAnswers: number;

  @ApiProperty({ example: 0 })
  @Expose()
  unanswered: number;

  @ApiProperty({
    example: 80.0,
    description: 'Accuracy percentage (0 to 100)',
  })
  @Expose()
  accuracy: number;

  @ApiProperty({
    example: 45,
    description: 'Time taken to complete the practice session in seconds',
  })
  @Expose()
  timeTakenSeconds: number;
}

export class FinishSessionResponseDto {
  @ApiProperty({ type: () => PracticeSessionEntity })
  @Expose()
  @Type(() => PracticeSessionEntity)
  session: PracticeSessionEntity;

  @ApiProperty({
    type: [PracticeAnswerEntity],
    description: 'All submitted and unsubmitted answers in the session',
  })
  @Expose()
  @Type(() => PracticeAnswerEntity)
  answers: PracticeAnswerEntity[];

  @ApiProperty({ type: () => PracticeSessionSummaryDto })
  @Expose()
  @Type(() => PracticeSessionSummaryDto)
  summary: PracticeSessionSummaryDto;
}
