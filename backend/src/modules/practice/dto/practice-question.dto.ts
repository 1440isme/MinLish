import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose } from 'class-transformer';
import { QuestionType } from '../entities/practice.entity';

export class PracticeQuestionDto {
  @ApiProperty({ example: 0 })
  @Expose()
  index: number;

  @ApiProperty({
    enum: QuestionType,
    example: 'WORD_TO_MEANING',
  })
  @Expose()
  questionType: QuestionType;

  @ApiProperty({ example: 'What does "appointment" mean?' })
  @Expose()
  questionText: string;

  @ApiPropertyOptional({
    type: [String],
    example: ['cuộc hẹn', 'hợp đồng', 'đàm phán', 'mua hàng'],
  })
  @Expose()
  options?: string[];

  @ApiPropertyOptional({
    example: 'uuid-vocab',
    nullable: true,
  })
  @Expose()
  vocabularyId: string | null;

  @ApiProperty({ example: false })
  @Expose()
  answered: boolean;
}
