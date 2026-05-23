import { ApiProperty } from '@nestjs/swagger';
import { Expose } from 'class-transformer';

export class DailyActivityEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-user' })
  @Expose()
  userId: string;

  @ApiProperty({
    type: String,
    example: '2026-05-23',
    description: 'Ngày hoạt động (DATE)',
  })
  @Expose()
  activityDate: Date;

  @ApiProperty({
    example: 5,
    description: 'Số từ mới học hôm nay',
  })
  @Expose()
  newWordsCount: number;

  @ApiProperty({
    example: 20,
    description: 'Số từ đã ôn hôm nay',
  })
  @Expose()
  reviewWordsCount: number;

  @ApiProperty({
    example: 2,
    description: 'Số session luyện tập hôm nay',
  })
  @Expose()
  practiceSessionsCount: number;

  @ApiProperty({ example: 18 })
  @Expose()
  correctCount: number;

  @ApiProperty({ example: 7 })
  @Expose()
  wrongCount: number;

  @ApiProperty({
    example: 1800,
    description: 'Tổng số giây học hôm nay',
  })
  @Expose()
  totalLearningSeconds: number;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  constructor(partial: Partial<DailyActivityEntity>) {
    Object.assign(this, partial);
  }
}
