import { Injectable, Req } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { User, ReviewCardStatus } from '@prisma/client';
import { TrackActivityDto } from './dto/track-activity.dto';

@Injectable()
export class AnalyticsService {
  constructor(private readonly prisma: PrismaService) {}

  // 1. Ghi nhận số liệu khi làm xong bài Quiz
  async trackPracticeActivity(userId: string, dto: TrackActivityDto) {
    const now = new Date();

    // Tự động truy vấn lấy múi giờ của User từ Database để tự xử lý độc lập
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    const userTimezone = user?.timezone || 'UTC';

    const activityDate = this.toTimezoneDateOnly(now, userTimezone);

    // Sử dụng upsert: Nếu ngày hôm nay chưa có dòng nào thì insert (tạo mới), có rồi thì update (Cộng dồn)
    return this.prisma.dailyActivity.upsert({
      where: {
        userId_activityDate: { userId, activityDate },
      },
      create: {
        id: crypto.randomUUID(),
        userId,
        activityDate,
        newWordsCount: 0,
        reviewWordsCount: 0,
        practiceSessionsCount: dto.practiceSessionsCount || 1,
        correctCount: dto.correctCount || 0,
        wrongCount: dto.wrongCount || 0,
        totalLearningSeconds: dto.totalLearningSeconds || 0,
      },
      update: {
        // Lệnh { increment: X } tự động làm phép toán: Giá trị cũ trong DB + X, chống nghẽn luồng mạng
        practiceSessionsCount: { increment: dto.practiceSessionsCount || 1 },
        correctCount: { increment: dto.correctCount || 0 },
        wrongCount: { increment: dto.wrongCount || 0 },
        totalLearningSeconds: { increment: dto.totalLearningSeconds || 0 },
      },
    });
  }

    //2. Hàm tổng hợp số liệu trả về cho class DashBoardAnalyticsDto
  async getDashboardAnalytics(user: User) {
    const userId = user.id;

    // gom cụm dữ liệu tổng ( lệnh aggregate) từ trước tới nay của User trong bảng DailyActivity
    const aggregates = await this.prisma.dailyActivity.aggregate({
      where: { userId },
      _sum: {
        newWordsCount: true,
        reviewWordsCount: true,
        correctCount: true,
        wrongCount: true,
      },
    });

    // Tính tổng số từ đang nợ cần ôn tập hôm nay (Đồng bộ logic đếm của Dev C)
    const dueTodayCount = await this.prisma.reviewCard.count({
      where: {
        userId,
        dueAt: { lte: new Date() },
        status: { not: ReviewCardStatus.SUSPENDED },
        vocabulary: { deletedAt: null },
      },
    });

    // Lấy riêng số liệu hoạt động của ngày hôm nay để tính phần trăm tiến độ học từ mới
    const todayDate = this.toTimezoneDateOnly(new Date(), user.timezone);
    const todayActivity = await this.prisma.dailyActivity.findUnique({
      where: { userId_activityDate: { userId, activityDate: todayDate } },
    });

    const totalLearned = aggregates._sum.newWordsCount || 0;
    const totalReview = aggregates._sum.reviewWordsCount || 0;

    // Tính tỷ lệ chính xác Accuracy = (Số câu đúng / Tổng số câu đúng + sai) * 100
    const totalCorrect = aggregates._sum.correctCount || 0;
    const totalWrong = aggregates._sum.wrongCount || 0;
    const totalAnswers = totalCorrect + totalWrong;
    const accuracy = totalAnswers > 0 ? (totalCorrect / totalAnswers) * 100 : 0.0; // Mặc định 80% nếu chưa làm câu nào

    // Tính chuỗi ngày học liên tục (Streak) bằng thuật toán đếm lùi ngày
    const streak = await this.calculateStreak(userId, user.timezone);

    // Tính % tiến độ mục tiêu ngày = (Số từ mới đã học hôm nay / Mục tiêu ngày của User) * 100
    const todayNewWords = todayActivity ? todayActivity.newWordsCount : 0;
    const dailyGoal = user.dailyNewWordsGoal || 10;
    const progressPercent = Math.min(100, Math.round((todayNewWords / dailyGoal) * 100));

    //Trả về 7 trường dữ liệu mà class DashBoardAnalyticsDto trên android cần
    return {
      totalLearned,
      totalReview,
      dueToday: dueTodayCount,
      dailyGoal,
      accuracy: parseFloat(accuracy.toFixed(1)), // Lấy 1 chữ số thập phân (Ví dụ: 92.5)
      streak,
      progressPercent,
    };
  }

  //---------
  // Các hàm tiện ích hỗ trợ xử lý bên trong service

  private async calculateStreak(userId: string, timezone: string): Promise<number> {
    // Lấy toàn bộ danh sách ngày hoạt động có học từ hoặc có làm Quiz, xếp từ mới tới cũ
    const activities = await this.prisma.dailyActivity.findMany({
      where: {
        userId,
        OR: [
          { newWordsCount: { gt: 0 } },
          { reviewWordsCount: { gt: 0 } },
          { practiceSessionsCount: { gt: 0 } },
        ],
      },
      select: { activityDate: true },
      orderBy: { activityDate: 'desc' },
    });

    if (activities.length === 0) return 0;

    // Chuyển mảng Object thành mảng chuỗi chữ "YYYY-MM-DD" để so sánh cho nhanh
    const activeDatesSet = new Set(
      activities.map((a) => a.activityDate.toISOString().split('T')[0]),
    );

    const todayDate = this.toTimezoneDateOnly(new Date(), timezone);
    const todayStr = todayDate.toISOString().split('T')[0];

    // Tạo chuỗi ngày hôm qua
    const yesterday = new Date(todayDate);
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayStr = yesterday.toISOString().split('T')[0];

    //Logic Nếu cả hôm nay và hôm qua người dùng đều lười không học bài -> Streak đứt về 0
    if (!activeDatesSet.has(todayStr) && !activeDatesSet.has(yesterdayStr)) {
      return 0;
    }

    let streakCount = 0;
    // Bắt đầu vòng lặp kiểm tra: Xuất phát từ ngày có hoạt động gần nhất (hôm nay hoặc hôm qua)
    const currentCheckDate = activeDatesSet.has(todayStr) ? todayDate : yesterday;

    while (true) {
      const checkStr = currentCheckDate.toISOString().split('T')[0];
      if (activeDatesSet.has(checkStr)) {
        streakCount++;
        // Lùi mốc kiểm tra về quá khứ 1 ngày lịch để chạy lượt lặp tiếp theo
        currentCheckDate.setDate(currentCheckDate.getDate() - 1);
      } else {
        // Phát hiện một ngày bị bỏ trống hoạt động -> Đứt chuỗi, thoát vòng lặp ngay
        break;
      }
    }

    return streakCount;
  }

  private toTimezoneDateOnly(date: Date, timezone: string): Date {
    const formatter = new Intl.DateTimeFormat('en-CA', {
      timeZone: timezone || 'UTC',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
    const parts = formatter.formatToParts(date);
    const year = parts.find((part) => part.type === 'year')?.value ?? '1970';
    const month = parts.find((part) => part.type === 'month')?.value ?? '01';
    const day = parts.find((part) => part.type === 'day')?.value ?? '01';
    return new Date(`${year}-${month}-${day}T00:00:00.000Z`);
  }

    //hàm 3
    async getPracticeHistory(userId: string) {
      // Prisma lấy dữ liệu từ bảng lưu lịch sử làm bài tập, include thêm tên deck
      const sessions = await this.prisma.practiceSession.findMany({
        where: { userId: userId },
        include: { deck: { select: { name: true } } },
        orderBy: { finishedAt: 'desc' }, // Sắp xếp bài mới làm xếp lên đỉnh
        take: 5, // Chỉ lấy đúng 5 trận gần nhất để nhẹ máy
      });

    return sessions.map(session => ({
        id: session.id,
        deckId: session.deckId,
        deckName: session.deck?.name || 'Quick Practice',
        practiceType: session.practiceType, // Ví dụ: "MULTIPLE_CHOICE"
        totalQuestions: session.totalQuestions,
        correctAnswers: session.correctAnswers,

        finishedAt: session.finishedAt ? new Date(session.finishedAt).getTime() : 0,
      }));
    }
}