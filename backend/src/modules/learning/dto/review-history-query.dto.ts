import { ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import { IsInt, IsOptional, IsString, Max, Min } from 'class-validator';

export class ReviewHistoryQueryDto {
  @ApiPropertyOptional({ example: 'uuid-vocabulary' })
  @IsOptional()
  @IsString()
  vocabularyId?: string;

  @ApiPropertyOptional({
    example: 20,
    description: 'Maximum logs to return',
  })
  @IsOptional()
  @Transform(({ value }) => Number(value))
  @IsInt()
  @Min(1)
  @Max(100)
  limit?: number;
}
