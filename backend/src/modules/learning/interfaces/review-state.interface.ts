import { ReviewCardStatus, ReviewRating } from '@prisma/client';

export interface ReviewState {
  status: ReviewCardStatus;
  repetition: number;
  intervalDays: number;
  easeFactor: number;
  lapses: number;
  totalReviews: number;
  correctReviews: number;
  firstLearnedAt: Date | null;
}

export interface DeriveReviewStatusInput {
  previousStatus: ReviewCardStatus;
  quality: number;
  nextRepetition: number;
  nextIntervalDays: number;
}

export interface NextReviewState {
  rating: ReviewRating;
  quality: number;
  isCorrect: boolean;
  status: ReviewCardStatus;
  repetition: number;
  intervalDays: number;
  easeFactor: number;
  dueAt: Date;
  lastReviewedAt: Date;
  firstLearnedAt: Date;
  lapses: number;
  totalReviews: number;
  correctReviews: number;
}

