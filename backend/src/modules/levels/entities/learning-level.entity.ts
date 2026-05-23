import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose, Type } from 'class-transformer';
import { LearningPathEntity } from '../../learning-paths/entities/learning-path.entity';

export class LearningLevelEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-learning-path' })
  @Expose()
  learningPathId: string;

  @ApiProperty({ example: 'TOEIC_600' })
  @Expose()
  code: string;

  @ApiProperty({ example: 'TOEIC 600+' })
  @Expose()
  name: string;

  @ApiPropertyOptional({ example: 'Từ vựng trung cấp cho mục tiêu TOEIC 600+' })
  @Expose()
  description?: string | null;

  @ApiPropertyOptional({
    example: 451,
    type: Number,
  })
  @Expose()
  minScore?: number | null;

  @ApiPropertyOptional({
    example: 600,
    type: Number,
  })
  @Expose()
  maxScore?: number | null;

  @ApiProperty({ example: 2 })
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

  // Relation - populated when using include
  @ApiPropertyOptional({ type: () => LearningPathEntity })
  @Expose()
  @Type(() => LearningPathEntity)
  learningPath?: LearningPathEntity;

  constructor(partial: Partial<LearningLevelEntity>) {
    Object.assign(this, partial);
  }
}
