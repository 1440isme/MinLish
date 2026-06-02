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
        practiceSessionsCount: true,
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
    const totalPractices = aggregates._sum.practiceSessionsCount || 0;

    // Tính chuỗi ngày học liên tục (Streak) bằng thuật toán đếm lùi ngày
    const streak = await this.calculateStreak(userId, user.timezone);
    // Tính % tiến độ mục tiêu ngày
    const todayNewWords = todayActivity ? todayActivity.newWordsCount : 0;
    const dailyGoal = user.dailyNewWordsGoal || 10;
    const progressPercent = Math.min(100, Math.round((todayNewWords / dailyGoal) * 100));

    // Xử lý logic mốc tuần hiện tại ( từ thứ 2 -> chủ nhật )
    const dayOfWeek = todayDate.getDay();
    const diffToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
    const startOfWeek = new Date(todayDate);
    startOfWeek.setDate(todayDate.getDate() - diffToMonday);

    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);

    // Kéo hoa động tuần này về kèm trường đếm số lượng
    const weekActivities = await this.prisma.dailyActivity.findMany({
      where: {
        userId,
        activityDate: { gte: startOfWeek, lte: endOfWeek },
      },
      select: {
          activityDate: true,
          practiceSessionsCount: true,
          correctCount: true,
          wrongCount: true
      },
    });

    const weekActivityMap = new Map<string, any>();
    weekActivities.forEach(act => {
      weekActivityMap.set(act.activityDate.toISOString().split('T')[0], act);
    });

    // Tính mảng Weekly Active Days (Cho Dashboard) và Weekly Practice Counts (Cho cột đồ thị Stats)
    const weeklyActiveDays: boolean[] = [];
    const weeklyPracticeCounts: number[] = [];

    let weekCorrect = 0;
    let weekWrong = 0;

    for (let i = 0; i < 7; i++) {
      const checkDate = new Date(startOfWeek);
      checkDate.setDate(startOfWeek.getDate() + i);
      const dateStr = checkDate.toISOString().split('T')[0];
      const act = weekActivityMap.get(dateStr);

      if (act) {
        const hasData = act.newWordsCount > 0 || act.reviewWordsCount > 0 || act.practiceSessionsCount > 0;
        weeklyActiveDays.push(hasData);
        weeklyPracticeCounts.push(act.practiceSessionsCount || 0); // Lấy số lượng bài làm từ DB bài tập tích lũy

        // Tích lũy câu đúng/sai để phục vụ tính toán Accuracy riêng tuần này
        weekCorrect += act.correctCount || 0;
        weekWrong += act.wrongCount || 0;
      } else {
        weeklyActiveDays.push(false);
        weeklyPracticeCounts.push(0); // Nếu ngày đó trống hoạt động, tự trả về 0 để reset tuần mới
      }
    }

    // Tính toán Accuracy trong tuần
    const weekTotalAnswers = weekCorrect + weekWrong;
    const accuracy = weekTotalAnswers > 0 ? (weekCorrect / weekTotalAnswers) * 100 : 0.0;

    // Quét lịch sử Accuracy của 4 tuần gần nhất để vẽ biểu đồ
    const weeklyAccuracyHistory: number[] = [];
    for (let w = 3; w >= 0; w--) {
      const wStart = new Date(startOfWeek);
      wStart.setDate(startOfWeek.getDate() - (w * 7));
      const wEnd = new Date(wStart);
      wEnd.setDate(wStart.getDate() + 6);

      const pastActivities = await this.prisma.dailyActivity.aggregate({
        where: {
          userId,
          activityDate: { gte: wStart, lte: wEnd }
        },
        _sum: {
          correctCount: true,
          wrongCount: true
        }
      });

      const pCorrect = pastActivities._sum.correctCount || 0;
      const pWrong = pastActivities._sum.wrongCount || 0;
      const pTotal = pCorrect + pWrong;
      const pAccuracy = pTotal > 0 ? (pCorrect / pTotal) * 100 : 0.0;
      weeklyAccuracyHistory.push(parseFloat(pAccuracy.toFixed(1)));
    }

    //Trả về 7 trường dữ liệu mà class DashBoardAnalyticsDto trên android cần
    return {
      totalLearned,
      totalReview,
      dueToday: dueTodayCount,
      dailyGoal,
      accuracy: parseFloat(accuracy.toFixed(1)), // Lấy 1 chữ số thập phân (Ví dụ: 92.5)
      streak,
      progressPercent,
      weeklyActiveDays,
      totalPractices,
      weeklyPracticeCounts,
      weeklyAccuracyHistory,
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
        where: { userId: userId, status: 'COMPLETED' },
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
        status: session.status,
        finishedAt: session.finishedAt ? new Date(session.finishedAt).getTime() : 0,
      }));
    }
}