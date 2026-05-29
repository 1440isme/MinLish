import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsInt, IsOptional, IsString, Min } from 'class-validator';

export class SubmitAnswerDto {
  @ApiProperty({
    example: 0,
    description: '0-based index of the question in the session',
  })
  @IsInt()
  @Min(0)
  questionIndex: number;

  @ApiPropertyOptional({
    example: 'cuộc hẹn',
    description: 'Answer submitted by user. Empty string or omitted counts as Skip/Dont know',
  })
  @IsOptional()
  @IsString()
  userAnswer?: string;
}
