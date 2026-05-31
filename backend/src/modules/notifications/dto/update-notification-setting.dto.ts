import { IsBoolean, IsOptional, IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateNotificationSettingDto {
  @ApiProperty({ example: true, required: false })
  @IsBoolean()
  @IsOptional()
  dailyReminderEnabled?: boolean;

  @ApiProperty({ example: '06:30:00', required: false })
  @IsString()
  @IsOptional()
  dailyReminderTime?: string;

  @ApiProperty({ example: true, required: false })
  @IsBoolean()
  @IsOptional()
  dueReviewReminderEnabled?: boolean;

  @ApiProperty({ example: true, required: false })
  @IsBoolean()
  @IsOptional()
  pushEnabled?: boolean;

  @ApiProperty({ example: false, required: false })
  @IsBoolean()
  @IsOptional()
  emailEnabled?: boolean;
}