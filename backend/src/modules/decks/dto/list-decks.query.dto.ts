import { ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import { IsEnum, IsInt, IsOptional, IsString, Max, Min } from 'class-validator';

export enum DeckListType {
  SYSTEM = 'SYSTEM',
  USER = 'USER',
  ALL = 'ALL',
}

export class ListDecksQueryDto {
  @ApiPropertyOptional({
    enum: DeckListType,
    example: DeckListType.ALL,
  })
  @IsOptional()
  @IsEnum(DeckListType)
  type?: DeckListType;

  @ApiPropertyOptional({ example: 'uuid-level' })
  @IsOptional()
  @IsString()
  levelId?: string;

  @ApiPropertyOptional({ example: 'toeic' })
  @IsOptional()
  @IsString()
  search?: string;

  @ApiPropertyOptional({
    example: 1,
    description: '1-based page index',
  })
  @IsOptional()
  @Transform(({ value }) => Number(value))
  @IsInt()
  @Min(1)
  page?: number;

  @ApiPropertyOptional({
    example: 20,
    description: 'Items per page (max 50)',
  })
  @IsOptional()
  @Transform(({ value }) => Number(value))
  @IsInt()
  @Min(1)
  @Max(50)
  pageSize?: number;
}

