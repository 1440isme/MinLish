import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose } from 'class-transformer';

export enum DevicePlatform {
  ANDROID = 'ANDROID',
  IOS = 'IOS',
  WEB = 'WEB',
}

export class DeviceTokenEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-user' })
  @Expose()
  userId: string;

  /** Full FCM token — do NOT log */
  @ApiProperty({ example: 'fcm-token-string' })
  @Expose()
  token: string;

  @ApiProperty({
    enum: DevicePlatform,
    example: DevicePlatform.ANDROID,
  })
  @Expose()
  platform: DevicePlatform;

  @ApiPropertyOptional({ example: 'Samsung Galaxy S24' })
  @Expose()
  deviceName?: string | null;

  @ApiProperty({ example: true })
  @Expose()
  isActive: boolean;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  lastUsedAt?: Date | null;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  constructor(partial: Partial<DeviceTokenEntity>) {
    Object.assign(this, partial);
  }
}
