import { IsInt, IsOptional } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class TrackActivityDto {
  @ApiProperty({
    example: 1,
    description: 'Number of practicing sessions completed (usually 1)',
  })
  @IsInt()
  @IsOptional()
  practiceSessionsCount?: number;

  @ApiProperty({
    example: 8,
    description: 'Number of correct answers in the test',
  })
  @IsInt()
  @IsOptional()
  correctCount?: number;

  @ApiProperty({
    example: 2,
    description: 'Number of incorrect answers in the test',
  })
  @IsInt()
  @IsOptional()
  wrongCount?: number;

  @ApiProperty({
    example: 120,
    description: 'Total time for completing the test is measured in seconds.',
  })
  @IsInt()
  @IsOptional()
  totalLearningSeconds?: number;
}
