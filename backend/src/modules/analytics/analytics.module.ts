import { Module } from '@nestjs/common';
import { AnalyticsController } from './analytics.controller';
import { AnalyticsService } from './analytics.service';

@Module({
  controllers: [AnalyticsController],
  providers: [AnalyticsService],
  exports: [AnalyticsService], // Xuất khẩu ra ngoài để sau này chèn cứu hộ vào file của Dev D
})
export class AnalyticsModule {}