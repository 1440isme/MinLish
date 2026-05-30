import {
  Prisma,
  ReviewCard as PrismaReviewCard,
  ReviewLog as PrismaReviewLog,
  Vocabulary as PrismaVocabulary,
} from '@prisma/client';
import { ReviewCardResponseDto } from '../dto/review-card-response.dto';
import { ReviewLogResponseDto } from '../dto/review-log-response.dto';
import { VocabularyPreviewDto } from '../dto/vocabulary-preview.dto';

type ReviewCardWithVocabulary = PrismaReviewCard & {
  vocabulary?: PrismaVocabulary | null;
};

type ReviewLogWithVocabulary = PrismaReviewLog & {
  vocabulary?: PrismaVocabulary | null;
};

function toNumber(value: Prisma.Decimal | number): number {
  return typeof value === 'number' ? value : Number(value);
}

export class ReviewCardMapper {
  static toVocabularyPreview(vocabulary: PrismaVocabulary): VocabularyPreviewDto {
    return new VocabularyPreviewDto({
      id: vocabulary.id,
      deckId: vocabulary.deckId,
      word: vocabulary.word,
      meaning: vocabulary.meaning,
      pronunciation: vocabulary.pronunciation,
      partOfSpeech: vocabulary.partOfSpeech,
    });
  }

  static toReviewCardResponse(
    card: ReviewCardWithVocabulary | PrismaReviewCard,
  ): ReviewCardResponseDto {
    return new ReviewCardResponseDto({
      id: card.id,
      userId: card.userId,
      vocabularyId: card.vocabularyId,
      status: card.status,
      repetition: card.repetition,
      intervalDays: card.intervalDays,
      easeFactor: toNumber(card.easeFactor),
      dueAt: card.dueAt,
      lastReviewedAt: card.lastReviewedAt,
      firstLearnedAt: card.firstLearnedAt,
      lapses: card.lapses,
      totalReviews: card.totalReviews,
      correctReviews: card.correctReviews,
      vocabulary:
        'vocabulary' in card && card.vocabulary
          ? this.toVocabularyPreview(card.vocabulary)
          : undefined,
    });
  }

  static toReviewLogResponse(
    log: ReviewLogWithVocabulary | PrismaReviewLog,
  ): ReviewLogResponseDto {
    return new ReviewLogResponseDto({
      id: log.id,
      reviewCardId: log.reviewCardId,
      vocabularyId: log.vocabularyId,
      rating: log.rating,
      quality: log.quality,
      isCorrect: log.isCorrect,
      oldRepetition: log.oldRepetition,
      newRepetition: log.newRepetition,
      oldIntervalDays: log.oldIntervalDays,
      newIntervalDays: log.newIntervalDays,
      oldEaseFactor: toNumber(log.oldEaseFactor),
      newEaseFactor: toNumber(log.newEaseFactor),
      oldDueAt: log.oldDueAt,
      newDueAt: log.newDueAt,
      reviewedAt: log.reviewedAt,
      vocabulary:
        'vocabulary' in log && log.vocabulary
          ? this.toVocabularyPreview(log.vocabulary)
          : undefined,
    });
  }
}
