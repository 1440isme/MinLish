import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsArray, IsEnum, IsInt, IsOptional, IsString, Max, Min, IsIn } from 'class-validator';
import { PracticeType } from '../entities/practice.entity';

export class CreatePracticeSessionDto {
  @ApiProperty({
    example: 'd3b07384-d113-4ec5-a5e6-ec8d10332f7a',
    description: 'UUID of the deck to practice',
  })
  @IsString()
  deckId: string;

  @ApiPropertyOptional({
    enum: ['ALL', 'LEARNED_ONLY'],
    example: 'LEARNED_ONLY',
    description: 'Scope of vocabularies to practice. Default is LEARNED_ONLY.',
  })
  @IsOptional()
  @IsString()
  @IsIn(['ALL', 'LEARNED_ONLY'])
  scope?: 'ALL' | 'LEARNED_ONLY';

  @ApiPropertyOptional({
    type: [String],
    enum: PracticeType,
    isArray: true,
    example: ['MULTIPLE_CHOICE', 'FILL_IN_BLANK', 'LISTENING'],
    description: 'Selected practice types. Default is all three.',
  })
  @IsOptional()
  @IsArray()
  @IsEnum(PracticeType, { each: true })
  practiceTypes?: PracticeType[];

  @ApiPropertyOptional({
    example: 10,
    description: 'Total number of questions. Default is 10.',
  })
  @IsOptional()
  @IsInt()
  @Min(1)
  @Max(200)
  totalQuestions?: number;
}
