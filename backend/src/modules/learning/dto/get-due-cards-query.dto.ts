import { ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import { IsInt, IsOptional, IsString, Max, Min } from 'class-validator';

export class GetDueCardsQueryDto {
  @ApiPropertyOptional({
    example: 'uuid-deck',
    description: 'Filter due cards by deck',
  })
  @IsOptional()
  @IsString()
  deckId?: string;

  @ApiPropertyOptional({
    example: 20,
    description: 'Maximum cards to return (max 100)',
  })
  @IsOptional()
  @Transform(({ value }) => Number(value))
  @IsInt()
  @Min(1)
  @Max(100)
  limit?: number;
}

