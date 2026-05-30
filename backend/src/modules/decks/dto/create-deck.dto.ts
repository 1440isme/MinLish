import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import {
  IsArray,
  IsOptional,
  IsString,
  MaxLength,
  ArrayMaxSize,
} from 'class-validator';

export class CreateDeckDto {
  @ApiProperty({ example: 'My Business Words' })
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  @MaxLength(200)
  name: string;

  @ApiPropertyOptional({ example: 'Từ vựng business tự học' })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  description?: string;

  @ApiPropertyOptional({
    example: ['Business', 'Office'],
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
      'Learning level tùy chọn. Path (TOEIC/IELTS) suy ra từ level — không gửi learningPathId.',
  })
  @IsOptional()
  @IsString()
  learningLevelId?: string;
}

