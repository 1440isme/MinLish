import { Injectable } from '@nestjs/common';
import { ReviewCardStatus, ReviewRating } from '@prisma/client';
import {
  DeriveReviewStatusInput,
  NextReviewState,
  ReviewState,
} from './interfaces/review-state.interface';

function addDays(date: Date, days: number): Date {
  const next = new Date(date);
  next.setUTCDate(next.getUTCDate() + days);
  return next;
}

function addMinutes(date: Date, minutes: number): Date {
  const next = new Date(date);
  next.setUTCMinutes(next.getUTCMinutes() + minutes);
  return next;
}

@Injectable()
export class Sm2Service {
  mapRatingToQuality(rating: ReviewRating): 0 | 3 | 4 | 5 {
    switch (rating) {
      case ReviewRating.AGAIN:
        return 0;
      case ReviewRating.HARD:
        return 3;
      case ReviewRating.GOOD:
        return 4;
      case ReviewRating.EASY:
        return 5;
      default:
        return 0;
    }
  }

  deriveReviewStatus(input: DeriveReviewStatusInput): ReviewCardStatus {
    if (input.previousStatus === ReviewCardStatus.SUSPENDED) {
      return ReviewCardStatus.SUSPENDED;
    }

    if (input.quality < 3) {
      return ReviewCardStatus.LEARNING;
    }

    if (input.nextIntervalDays >= 30) {
      return ReviewCardStatus.MASTERED;
    }

    if (input.nextRepetition <= 1) {
      return ReviewCardStatus.LEARNING;
    }

    return ReviewCardStatus.REVIEW;
  }

  calculateNextReviewState(
    current: ReviewState,
    rating: ReviewRating,
    reviewedAt: Date,
  ): NextReviewState {
    const quality = this.mapRatingToQuality(rating);
    const previousIntervalDays = current.intervalDays;
    let repetition = current.repetition;
    let intervalDays = current.intervalDays;
    let easeFactor = Number(current.easeFactor);
    let dueAt: Date;

    if (current.status === ReviewCardStatus.NEW && current.totalReviews === 0) {
      const firstReviewSchedule = this.getFirstReviewSchedule(rating, reviewedAt);
      repetition = firstReviewSchedule.repetition;
      intervalDays = firstReviewSchedule.intervalDays;
      dueAt = firstReviewSchedule.dueAt;
    } else if (quality < 3) {
      repetition = 0;
      intervalDays = 1;
      dueAt = addDays(reviewedAt, intervalDays);
    } else {
      if (repetition === 0) {
        intervalDays = 1;
      } else if (repetition === 1) {
        intervalDays = 6;
      } else {
        intervalDays = Math.round(previousIntervalDays * easeFactor);
      }

      repetition += 1;

      if (rating === ReviewRating.HARD) {
        intervalDays = Math.max(
          1,
          Math.round(Math.max(previousIntervalDays, 1) * 1.2),
        );
      }

      if (rating === ReviewRating.EASY) {
        intervalDays = Math.round(intervalDays * 1.3);
      }

      dueAt = addDays(reviewedAt, intervalDays);
    }

    easeFactor =
      easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
    easeFactor = Math.max(1.3, Number(easeFactor.toFixed(2)));

    const isCorrect = quality >= 3;
    const firstLearnedAt = current.firstLearnedAt ?? reviewedAt;
    const lapses =
      quality < 3 && current.status !== ReviewCardStatus.NEW
        ? current.lapses + 1
        : current.lapses;

    return {
      rating,
      quality,
      isCorrect,
      status: this.deriveReviewStatus({
        previousStatus: current.status,
        quality,
        nextRepetition: repetition,
        nextIntervalDays: intervalDays,
      }),
      repetition,
      intervalDays,
      easeFactor,
      dueAt,
      lastReviewedAt: reviewedAt,
      firstLearnedAt,
      lapses,
      totalReviews: current.totalReviews + 1,
      correctReviews: current.correctReviews + (isCorrect ? 1 : 0),
    };
  }

  private getFirstReviewSchedule(
    rating: ReviewRating,
    reviewedAt: Date,
  ): { repetition: number; intervalDays: number; dueAt: Date } {
    switch (rating) {
      case ReviewRating.AGAIN:
        return {
          repetition: 0,
          intervalDays: 0,
          dueAt: addMinutes(reviewedAt, 1),
        };
      case ReviewRating.HARD:
        return {
          repetition: 1,
          intervalDays: 0,
          dueAt: addMinutes(reviewedAt, 10),
        };
      case ReviewRating.GOOD:
        return {
          repetition: 1,
          intervalDays: 0,
          dueAt: addMinutes(reviewedAt, 30),
        };
      case ReviewRating.EASY:
        return {
          repetition: 1,
          intervalDays: 1,
          dueAt: addDays(reviewedAt, 1),
        };
      default:
        return {
          repetition: 0,
          intervalDays: 0,
          dueAt: addMinutes(reviewedAt, 1),
        };
    }
  }
}

