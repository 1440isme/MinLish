import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import {
  Deck,
  DeckType,
  Prisma,
  ReviewCard,
  ReviewCardStatus,
  User,
  Vocabulary,
} from '@prisma/client';
import { ErrorCodes } from '../../config/common/errors/error-codes';
import { PrismaService } from '../../prisma/prisma.service';
import { DailyPlanResponseDto } from './dto/daily-plan-response.dto';
import { DueCardsResponseDto } from './dto/due-cards-response.dto';
import { GetDueCardsQueryDto } from './dto/get-due-cards-query.dto';
import { ReviewHistoryQueryDto } from './dto/review-history-query.dto';
import { ReviewHistoryResponseDto } from './dto/review-history-response.dto';
import { ReviewSummaryDto } from './dto/review-summary.dto';
import { StartLearningDeckQueryDto } from './dto/start-learning-deck-query.dto';
import { SubmitReviewDto } from './dto/submit-review.dto';
import { SubmitReviewResponseDto } from './dto/submit-review-response.dto';
import { VocabularyPreviewDto } from './dto/vocabulary-preview.dto';
import { ReviewCardMapper } from './mapper/review-card.mapper';
import { Sm2Service } from './sm2.service';

type PrismaTx = Prisma.TransactionClient;

const DEFAULT_DUE_LIMIT = 20;
const DEFAULT_HISTORY_LIMIT = 20;
const FUTURE_REVIEW_GRACE_MS = 5 * 60 * 1000;

@Injectable()
export class LearningService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly sm2Service: Sm2Service,
  ) {}

  async startDeck(
    user: User,
    deckId: string,
    query: StartLearningDeckQueryDto,
  ): Promise<DailyPlanResponseDto> {
    await this.getDeckOrThrow(user.id, deckId);
    const goal = query.limitNewWords ?? user.dailyNewWordsGoal;
    return this.buildDailyPlan(user, deckId, goal);
  }

  async ensureReviewCardExists(
    user: User,
    vocabularyId: string,
  ): Promise<any> {
    const vocabulary = await this.getAccessibleVocabularyOrThrow(
      user.id,
      vocabularyId,
    );
    const reviewCard = await this.findOrCreateReviewCard(
      this.prisma,
      user.id,
      vocabulary.id,
    );

    return ReviewCardMapper.toReviewCardResponse({
      ...reviewCard,
      vocabulary,
    });
  }

  async getDailyPlan(
    user: User,
    query: GetDueCardsQueryDto,
  ): Promise<DailyPlanResponseDto> {
    if (query.deckId) {
      await this.getDeckOrThrow(user.id, query.deckId);
    }

    return this.buildDailyPlan(user, query.deckId ?? null, user.dailyNewWordsGoal);
  }

  async getDueCards(
    user: User,
    query: GetDueCardsQueryDto,
  ): Promise<DueCardsResponseDto> {
    if (query.deckId) {
      await this.getDeckOrThrow(user.id, query.deckId);
    }

    const limit = query.limit ?? DEFAULT_DUE_LIMIT;
    const dueCards = await this.prisma.reviewCard.findMany({
      where: this.buildDueCardsWhere(user.id, query.deckId),
      include: {
        vocabulary: true,
      },
      orderBy: [{ dueAt: 'asc' }, { id: 'asc' }],
      take: limit,
    });

    const items = dueCards.map((card) =>
      ReviewCardMapper.toReviewCardResponse(card),
    );

    return new DueCardsResponseDto({
      items,
      count: items.length,
      limit,
    });
  }

  async submitReview(
    user: User,
    dto: SubmitReviewDto,
  ): Promise<SubmitReviewResponseDto> {
    const reviewedAt = this.normalizeReviewedAt(dto.reviewedAt);
    const vocabulary = await this.getAccessibleVocabularyOrThrow(
      user.id,
      dto.vocabularyId,
    );

    const result = await this.prisma.$transaction(async (tx) => {
      const reviewCard = await this.findOrCreateReviewCard(
        tx,
        user.id,
        vocabulary.id,
      );

      if (reviewCard.status === ReviewCardStatus.SUSPENDED) {
        throw new ConflictException({
          code: ErrorCodes.REVIEW_CARD_SUSPENDED,
          message: 'Review card đang bị tạm ngưng',
        });
      }

      const nextState = this.sm2Service.calculateNextReviewState(
        {
          status: reviewCard.status,
          repetition: reviewCard.repetition,
          intervalDays: reviewCard.intervalDays,
          easeFactor: Number(reviewCard.easeFactor),
          lapses: reviewCard.lapses,
          totalReviews: reviewCard.totalReviews,
          correctReviews: reviewCard.correctReviews,
          firstLearnedAt: reviewCard.firstLearnedAt,
        },
        dto.rating,
        reviewedAt,
      );

      const updatedCard = await tx.reviewCard.update({
        where: { id: reviewCard.id },
        data: {
          status: nextState.status,
          repetition: nextState.repetition,
          intervalDays: nextState.intervalDays,
          easeFactor: nextState.easeFactor,
          dueAt: nextState.dueAt,
          lastReviewedAt: nextState.lastReviewedAt,
          firstLearnedAt: nextState.firstLearnedAt,
          lapses: nextState.lapses,
          totalReviews: nextState.totalReviews,
          correctReviews: nextState.correctReviews,
        },
      });

      await tx.reviewLog.create({
        data: {
          userId: user.id,
          reviewCardId: reviewCard.id,
          vocabularyId: vocabulary.id,
          rating: dto.rating,
          quality: nextState.quality,
          isCorrect: nextState.isCorrect,
          oldRepetition: reviewCard.repetition,
          newRepetition: nextState.repetition,
          oldIntervalDays: reviewCard.intervalDays,
          newIntervalDays: nextState.intervalDays,
          oldEaseFactor: reviewCard.easeFactor,
          newEaseFactor: nextState.easeFactor,
          oldDueAt: reviewCard.dueAt,
          newDueAt: nextState.dueAt,
          reviewedAt,
        },
      });

      await this.upsertDailyActivity(tx, user, reviewedAt, {
        isCorrect: nextState.isCorrect,
        isFirstLearn: reviewCard.firstLearnedAt === null,
      });

      return {
        updatedCard,
        summary: new ReviewSummaryDto({
          rating: dto.rating,
          quality: nextState.quality,
          isCorrect: nextState.isCorrect,
          reviewedAt,
        }),
      };
    });

    return new SubmitReviewResponseDto({
      reviewCard: ReviewCardMapper.toReviewCardResponse({
        ...result.updatedCard,
        vocabulary,
      }),
      summary: result.summary,
    });
  }

  async getReviewHistory(
    user: User,
    query: ReviewHistoryQueryDto,
  ): Promise<ReviewHistoryResponseDto> {
    if (query.vocabularyId) {
      await this.getAccessibleVocabularyOrThrow(user.id, query.vocabularyId);
    }

    const limit = query.limit ?? DEFAULT_HISTORY_LIMIT;
    const logs = await this.prisma.reviewLog.findMany({
      where: {
        userId: user.id,
        ...(query.vocabularyId ? { vocabularyId: query.vocabularyId } : {}),
      },
      include: {
        vocabulary: true,
      },
      orderBy: [{ reviewedAt: 'desc' }, { id: 'desc' }],
      take: limit,
    });

    const items = logs.map((log) => ReviewCardMapper.toReviewLogResponse(log));

    return new ReviewHistoryResponseDto({
      items,
      count: items.length,
      limit,
    });
  }

  private async buildDailyPlan(
    user: User,
    deckId: string | null,
    requestedNewWordsGoal: number,
  ): Promise<DailyPlanResponseDto> {
    const newWordsGoal = Math.min(Math.max(requestedNewWordsGoal, 1), 100);
    const dueWhere = this.buildDueCardsWhere(user.id, deckId);
    const newWordsWhere = this.buildNewWordsWhere(user.id, deckId);

    const [dueReviewCount, dueCards, newWordsAvailable, newWords] =
      await this.prisma.$transaction([
        this.prisma.reviewCard.count({ where: dueWhere }),
        this.prisma.reviewCard.findMany({
          where: dueWhere,
          include: { vocabulary: true },
          orderBy: [{ dueAt: 'asc' }, { id: 'asc' }],
          take: DEFAULT_DUE_LIMIT,
        }),
        this.prisma.vocabulary.count({ where: newWordsWhere }),
        this.prisma.vocabulary.findMany({
          where: newWordsWhere,
          orderBy: [{ createdAt: 'asc' }, { id: 'asc' }],
          take: newWordsGoal,
        }),
      ]);

    return new DailyPlanResponseDto({
      deckId,
      newWordsGoal,
      newWordsAvailable,
      dueReviewCount,
      dueCards: dueCards.map((card) => ReviewCardMapper.toReviewCardResponse(card)),
      newWords: newWords.map((vocabulary) =>
        ReviewCardMapper.toVocabularyPreview(vocabulary),
      ),
    });
  }

  private buildDueCardsWhere(
    currentUserId: string,
    deckId?: string | null,
  ): Prisma.ReviewCardWhereInput {
    return {
      userId: currentUserId,
      dueAt: { lte: new Date() },
      status: { not: ReviewCardStatus.SUSPENDED },
      vocabulary: {
        deletedAt: null,
        ...(deckId
          ? { deckId }
          : {
              deck: {
                deletedAt: null,
                OR: [
                  { deckType: DeckType.SYSTEM },
                  { deckType: DeckType.USER, ownerUserId: currentUserId },
                ],
              },
            }),
      },
    };
  }

  private buildNewWordsWhere(
    currentUserId: string,
    deckId?: string | null,
  ): Prisma.VocabularyWhereInput {
    return {
      deletedAt: null,
      ...(deckId
        ? { deckId }
        : {
            deck: {
              deletedAt: null,
              OR: [
                { deckType: DeckType.SYSTEM },
                { deckType: DeckType.USER, ownerUserId: currentUserId },
              ],
            },
          }),
      reviewCards: {
        none: {
          userId: currentUserId,
        },
      },
    };
  }

  private async getDeckOrThrow(
    currentUserId: string,
    deckId: string,
  ): Promise<Deck> {
    const deck = await this.prisma.deck.findFirst({
      where: {
        id: deckId,
        deletedAt: null,
      },
    });

    if (!deck) {
      throw new NotFoundException({
        code: ErrorCodes.DECK_NOT_FOUND,
        message: 'Deck không tồn tại',
      });
    }

    if (deck.deckType === DeckType.USER && deck.ownerUserId !== currentUserId) {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_FORBIDDEN,
        message: 'Bạn không có quyền truy cập deck này',
      });
    }

    return deck;
  }

  private async getAccessibleVocabularyOrThrow(
    currentUserId: string,
    vocabularyId: string,
  ): Promise<VocabularyPreviewDto & Vocabulary> {
    const vocabulary = await this.prisma.vocabulary.findFirst({
      where: {
        id: vocabularyId,
      },
    });

    if (!vocabulary) {
      throw new NotFoundException({
        code: ErrorCodes.VOCABULARY_NOT_FOUND,
        message: 'Vocabulary không tồn tại',
      });
    }

    if (vocabulary.deletedAt) {
      throw new BadRequestException({
        code: ErrorCodes.VOCABULARY_INACTIVE,
        message: 'Vocabulary đã bị vô hiệu hóa hoặc xóa mềm',
      });
    }

    const deck = await this.getDeckOrThrow(currentUserId, vocabulary.deckId);
    if (deck.deletedAt) {
      throw new ForbiddenException({
        code: ErrorCodes.VOCABULARY_FORBIDDEN,
        message: 'Bạn không có quyền truy cập vocabulary này',
      });
    }

    return vocabulary as VocabularyPreviewDto & Vocabulary;
  }

  private async findOrCreateReviewCard(
    tx: PrismaTx | PrismaService,
    userId: string,
    vocabularyId: string,
  ): Promise<ReviewCard> {
    const now = new Date();
    return tx.reviewCard.upsert({
      where: {
        userId_vocabularyId: {
          userId,
          vocabularyId,
        },
      },
      update: {},
      create: {
        userId,
        vocabularyId,
        status: ReviewCardStatus.NEW,
        repetition: 0,
        intervalDays: 0,
        easeFactor: 2.5,
        dueAt: now,
      },
    });
  }

  private normalizeReviewedAt(reviewedAt: string): Date {
    const parsed = new Date(reviewedAt);
    if (Number.isNaN(parsed.getTime())) {
      throw new BadRequestException({
        code: ErrorCodes.REVIEWED_AT_INVALID,
        message: 'reviewedAt không hợp lệ',
      });
    }

    if (parsed.getTime() > Date.now() + FUTURE_REVIEW_GRACE_MS) {
      throw new BadRequestException({
        code: ErrorCodes.REVIEWED_AT_INVALID,
        message: 'reviewedAt không được nằm quá xa trong tương lai',
      });
    }

    return parsed;
  }

  private async upsertDailyActivity(
    tx: PrismaTx,
    user: User,
    reviewedAt: Date,
    input: { isCorrect: boolean; isFirstLearn: boolean },
  ): Promise<void> {
    const activityDate = this.toTimezoneDateOnly(reviewedAt, user.timezone);

    await tx.dailyActivity.upsert({
      where: {
        userId_activityDate: {
          userId: user.id,
          activityDate,
        },
      },
      create: {
        userId: user.id,
        activityDate,
        newWordsCount: input.isFirstLearn ? 1 : 0,
        reviewWordsCount: 1,
        practiceSessionsCount: 0,
        correctCount: input.isCorrect ? 1 : 0,
        wrongCount: input.isCorrect ? 0 : 1,
        totalLearningSeconds: 0,
      },
      update: {
        newWordsCount: { increment: input.isFirstLearn ? 1 : 0 },
        reviewWordsCount: { increment: 1 },
        correctCount: { increment: input.isCorrect ? 1 : 0 },
        wrongCount: { increment: input.isCorrect ? 0 : 1 },
      },
    });
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
}
