import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose } from 'class-transformer';

export class LearningPathEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({
    example: 'TOEIC',
    description: 'Mã định danh duy nhất',
  })
  @Expose()
  code: string;

  @ApiProperty({ example: 'TOEIC' })
  @Expose()
  name: string;

  @ApiPropertyOptional({ example: 'Lộ trình học từ vựng TOEIC' })
  @Expose()
  description?: string | null;

  @ApiProperty({ example: 1 })
  @Expose()
  displayOrder: number;

  @ApiProperty({ example: true })
  @Expose()
  isActive: boolean;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  constructor(partial: Partial<LearningPathEntity>) {
    Object.assign(this, partial);
  }
}
