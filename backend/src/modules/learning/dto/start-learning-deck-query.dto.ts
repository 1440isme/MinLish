import { ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import { IsInt, IsOptional, Max, Min } from 'class-validator';

export class StartLearningDeckQueryDto {
  @ApiPropertyOptional({
    example: 10,
    description: 'Override the number of new words returned (max 100)',
  })
  @IsOptional()
  @Transform(({ value }) => Number(value))
  @IsInt()
  @Min(1)
  @Max(100)
  limitNewWords?: number;
}

