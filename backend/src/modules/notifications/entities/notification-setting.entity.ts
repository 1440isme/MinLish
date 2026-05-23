import { ApiProperty } from '@nestjs/swagger';
import { Expose } from 'class-transformer';

export class NotificationSettingEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-user' })
  @Expose()
  userId: string;

  @ApiProperty({
    example: true,
    description: 'Bật nhắc học hàng ngày',
  })
  @Expose()
  dailyReminderEnabled: boolean;

  @ApiProperty({
    example: '20:00:00',
    description: 'Giờ nhắc học hàng ngày (HH:mm:ss)',
  })
  @Expose()
  dailyReminderTime: Date;

  @ApiProperty({
    example: true,
    description: 'Bật nhắc ôn từ đến hạn',
  })
  @Expose()
  dueReviewReminderEnabled: boolean;

  @ApiProperty({
    example: true,
    description: 'Bật push notification',
  })
  @Expose()
  pushEnabled: boolean;

  @ApiProperty({
    example: false,
    description: 'Bật email notification',
  })
  @Expose()
  emailEnabled: boolean;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  constructor(partial: Partial<NotificationSettingEntity>) {
    Object.assign(this, partial);
  }
}
