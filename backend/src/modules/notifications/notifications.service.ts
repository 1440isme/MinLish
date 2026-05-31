import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { UpdateNotificationSettingDto } from './dto/update-notification-setting.dto';
import { CreateDeviceTokenDto } from './dto/create-device-token.dto';

@Injectable()
export class NotificationsService {
  // constructor --> khai báo cần chìa khóa PrismaService để mở cửa MySQL
  constructor(private prisma: PrismaService) {}

  // 1. Logic lấy cấu hình - Tự động bù đắp dữ liệu mặc định nếu chưa có
  async getSettings(userId: string) {
    let settings = await this.prisma.notificationSetting.findUnique({
      where: { userId },
    });

    // Logic : Nếu User mới đăng ký chưa có dòng cấu hình, tự động tạo mới bản ghi mặc định
    if (!settings) {
      settings = await this.prisma.notificationSetting.create({
        data: {
          id: crypto.randomUUID(), // Sinh mã UUID ngẫu nhiên 36 ký tự
          userId: userId,
          dailyReminderEnabled: true,
          dailyReminderTime: new Date('1970-01-01T20:00:00Z'), // Mặc định 8 giờ tối theo chuẩn UTC/Prisma
          dueReviewReminderEnabled: true,
          pushEnabled: true,
          emailEnabled: false,
        },
      });
    }
    return settings;
  }

  // 2. Logic cập nhật cấu hình thông báo
  async updateSettings(userId: string, dto: UpdateNotificationSettingDto) {
    let parsedReminderTime: Date | undefined = undefined;

    // Logic : Chuyển chuỗi thô "06:30:00" từ Android thành một đối tượng Ngày-Giờ hợp lệ
    if (dto.dailyReminderTime) {
      if (dto.dailyReminderTime.includes('T')) {
        parsedReminderTime = new Date(dto.dailyReminderTime);
      } else {
        parsedReminderTime = new Date(`1970-01-01T${dto.dailyReminderTime}Z`);
      }
    }

    return this.prisma.notificationSetting.update({
      where: { userId },
      data: {
        dailyReminderEnabled: dto.dailyReminderEnabled,
        dailyReminderTime: parsedReminderTime, // Nạp biến Date đã bọc ngày khởi nguyên
        dueReviewReminderEnabled: dto.dueReviewReminderEnabled,
        pushEnabled: dto.pushEnabled,
        emailEnabled: dto.emailEnabled,
      },
    });
  }

  // 3. Logic đăng ký Token thiết bị của điện thoại thật
  async registerDeviceToken(userId: string, dto: CreateDeviceTokenDto) {
    // Tìm xem mã token này của User đã được lưu trong MySQL từ trước chưa
    const existingToken = await this.prisma.deviceToken.findFirst({
      where: { token: dto.token, userId },
    });

    if (existingToken) {
      // Nếu có rồi, chỉ làm mới thời gian sử dụng gần nhất
      return this.prisma.deviceToken.update({
        where: { id: existingToken.id },
        data: { isActive: true, lastUsedAt: new Date() },
      });
    }

    // Nếu là thiết bị mới hoàn toàn, tiến hành tạo mới dòng dữ liệu
    return this.prisma.deviceToken.create({
      data: {
        id: crypto.randomUUID(),
        userId,
        token: dto.token,
        platform: dto.platform || 'ANDROID',
        deviceName: dto.deviceName,
        isActive: true,
        lastUsedAt: new Date(),
      },
    });
  }
}