import { ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import {
  ArrayMaxSize,
  IsArray,
  IsOptional,
  IsString,
  MaxLength,
  ValidateIf,
} from 'class-validator';

export class UpdateDeckDto {
  @ApiPropertyOptional({ example: 'My Updated Deck' })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  @MaxLength(200)
  name?: string;

  @ApiPropertyOptional({ example: 'Updated description' })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  description?: string;

  @ApiPropertyOptional({
    example: ['TOEIC', 'Business'],
    isArray: true,
    type: [String],
  })
  @IsOptional()
  @IsArray()
  @ArrayMaxSize(30)
  @IsString({ each: true })
  tags?: string[];

  @ApiPropertyOptional({
    example: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
    description:
      'Gắn deck với learning level. Gửi null để bỏ gắn level. Path suy ra từ level (không gửi learningPathId).',
    nullable: true,
  })
  @IsOptional()
  @ValidateIf((_, value) => value !== null && value !== undefined)
  @IsString()
  learningLevelId?: string | null;
}

