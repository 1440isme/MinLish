import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose, Type } from 'class-transformer';
import { LearningLevelEntity } from '../../levels/entities/learning-level.entity';

// ----------------------------------------------------------------
// Enums
// ----------------------------------------------------------------

export enum DeckType {
  SYSTEM = 'SYSTEM',
  USER = 'USER',
}

export enum DeckVisibility {
  PRIVATE = 'PRIVATE',
  PUBLIC = 'PUBLIC',
}

// ----------------------------------------------------------------
// Deck Entity
// ----------------------------------------------------------------

export class DeckEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiPropertyOptional({
    example: 'uuid-owner',
    nullable: true,
  })
  @Expose()
  ownerUserId?: string | null;

  @ApiPropertyOptional({
    example: 'uuid-level',
    nullable: true,
  })
  @Expose()
  learningLevelId?: string | null;

  @ApiProperty({
    enum: DeckType,
    example: DeckType.USER,
  })
  @Expose()
  deckType: DeckType;

  @ApiProperty({
    enum: DeckVisibility,
    example: DeckVisibility.PRIVATE,
  })
  @Expose()
  visibility: DeckVisibility;

  @ApiProperty({ example: 'My Vocabulary' })
  @Expose()
  name: string;

  @ApiProperty({ example: 'my vocabulary' })
  @Expose()
  normalizedName: string;

  @ApiPropertyOptional({ example: 'Bộ từ vựng cá nhân' })
  @Expose()
  description?: string | null;

  @ApiPropertyOptional({
    example: ['TOEIC', 'Business'],
    type: [String],
    isArray: true,
  })
  @Expose()
  tags?: string[] | null;

  @ApiPropertyOptional({ example: 'https://cdn.example.com/thumb.jpg' })
  @Expose()
  thumbnailUrl?: string | null;

  @ApiProperty({ example: 0 })
  @Expose()
  displayOrder: number;

  @ApiProperty({
    example: 50,
    description: 'Tổng số từ trong deck',
  })
  @Expose()
  totalWords: number;

  @ApiProperty({
    example: false,
    description: 'true nếu là deck Favorites mặc định',
  })
  @Expose()
  isDefault: boolean;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  deletedAt?: Date | null;

  // Relations - populated when using include
  @ApiPropertyOptional({ type: () => LearningLevelEntity })
  @Expose()
  @Type(() => LearningLevelEntity)
  learningLevel?: LearningLevelEntity;

  constructor(partial: Partial<DeckEntity>) {
    Object.assign(this, partial);
  }
}
