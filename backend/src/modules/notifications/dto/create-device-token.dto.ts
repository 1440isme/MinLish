import { IsEnum, IsNotEmpty, IsOptional, IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

enum DevicePlatform {
  ANDROID = 'ANDROID',
  IOS = 'IOS',
}

export class CreateDeviceTokenDto {
  @ApiProperty({ example: 'fcm_token_xyz_123456' })
  @IsString()
  @IsNotEmpty()
  token: string;

  @ApiProperty({ example: 'ANDROID', enum: DevicePlatform, required: false })
  @IsEnum(DevicePlatform)
  @IsOptional()
  platform?: DevicePlatform;

  @ApiProperty({ example: 'Samsung Galaxy S24 Ultra', required: false })
  @IsString()
  @IsOptional()
  deviceName?: string;
}