import { Body, Controller, Get, Patch, Post } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { NotificationsService } from './notifications.service';
import { CurrentUser } from '../../config/common/decorators/current-user.decorator';
import { UpdateNotificationSettingDto } from './dto/update-notification-setting.dto';
import { CreateDeviceTokenDto } from './dto/create-device-token.dto';

@ApiTags('Notifications')
@ApiBearerAuth()
@Controller('notifications')
export class NotificationsController {
  constructor(private readonly notificationsService: NotificationsService) {}

  @Get('settings')
  async getSettings(@CurrentUser() user: any) {
    // API này tự động được bảo vệ an toàn nhờ APP_GUARD
    return this.notificationsService.getSettings(user.id);
  }

  @Patch('settings')
  async updateSettings(@CurrentUser() user: any, @Body() dto: UpdateNotificationSettingDto) {
    return this.notificationsService.updateSettings(user.id, dto);
  }

  @Post('device-token')
  async registerDevice(@CurrentUser() user: any, @Body() dto: CreateDeviceTokenDto) {
    return this.notificationsService.registerDeviceToken(user.id, dto);
  }
}