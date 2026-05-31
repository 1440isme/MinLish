import { Body, Controller, Get, Post, Req } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { AnalyticsService } from './analytics.service';
import { CurrentUser } from '../../config/common/decorators/current-user.decorator';
import { TrackActivityDto } from './dto/track-activity.dto';
import type { User } from '@prisma/client';

@ApiTags('Analytics')
@ApiBearerAuth()
@Controller('analytics')
export class AnalyticsController {
    constructor(private readonly analyticsService: AnalyticsService) {}

    @Get('dashboard')
    async getDashboardAnalytics(@CurrentUser() user: User) {
        // Tự động nhận diện User từ mã Token của nhóm để tính toán Streak riêng cho người đó
        return this.analyticsService.getDashboardAnalytics(user);
    }

      @Post('track-practice')
    async trackPracticeActivity(@CurrentUser() user: User, @Body() dto: TrackActivityDto) {
        // Nhận số câu đúng/sai từ bài thực hành để cộng dồn vào bảng DailyActivity
        return this.analyticsService.trackPracticeActivity(user.id, dto);
    }

    @Get('history')
    async getRemoteHistory(@Req() req: any) {
      // Lấy userId từ mã Token bảo mật của người dùng đăng nhập
      const userId = req.user.id;

      // Triệu hồi service bốc mảng dữ liệu trả về cho Android
      return this.analyticsService.getPracticeHistory(userId);
    }
}