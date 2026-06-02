import { ReviewCardStatus, ReviewRating } from '@prisma/client';
import { Sm2Service } from './sm2.service';

describe('Sm2Service', () => {
  let service: Sm2Service;

  beforeEach(() => {
    service = new Sm2Service();
  });

  const baseState = {
    status: ReviewCardStatus.NEW,
    repetition: 0,
    intervalDays: 0,
    easeFactor: 2.5,
    lapses: 0,
    totalReviews: 0,
    correctReviews: 0,
    firstLearnedAt: null,
  };

  it('schedules AGAIN after 1 minute for the first review of a new card', () => {
    const reviewedAt = new Date('2026-05-31T09:00:00.000Z');

    const result = service.calculateNextReviewState(
      baseState,
      ReviewRating.AGAIN,
      reviewedAt,
    );

    expect(result.status).toBe(ReviewCardStatus.LEARNING);
    expect(result.repetition).toBe(0);
    expect(result.intervalDays).toBe(0);
    expect(result.dueAt.toISOString()).toBe('2026-05-31T09:01:00.000Z');
  });

  it('schedules HARD after 10 minutes for the first review of a new card', () => {
    const reviewedAt = new Date('2026-05-31T09:00:00.000Z');

    const result = service.calculateNextReviewState(
      baseState,
      ReviewRating.HARD,
      reviewedAt,
    );

    expect(result.status).toBe(ReviewCardStatus.LEARNING);
    expect(result.repetition).toBe(1);
    expect(result.intervalDays).toBe(0);
    expect(result.dueAt.toISOString()).toBe('2026-05-31T09:10:00.000Z');
  });

  it('schedules GOOD after 30 minutes for the first review of a new card', () => {
    const reviewedAt = new Date('2026-05-31T09:00:00.000Z');

    const result = service.calculateNextReviewState(
      baseState,
      ReviewRating.GOOD,
      reviewedAt,
    );

    expect(result.status).toBe(ReviewCardStatus.LEARNING);
    expect(result.repetition).toBe(1);
    expect(result.intervalDays).toBe(0);
    expect(result.dueAt.toISOString()).toBe('2026-05-31T09:30:00.000Z');
  });

  it('schedules EASY after 1 day for the first review of a new card', () => {
    const reviewedAt = new Date('2026-05-31T09:00:00.000Z');

    const result = service.calculateNextReviewState(
      baseState,
      ReviewRating.EASY,
      reviewedAt,
    );

    expect(result.status).toBe(ReviewCardStatus.LEARNING);
    expect(result.repetition).toBe(1);
    expect(result.intervalDays).toBe(1);
    expect(result.dueAt.toISOString()).toBe('2026-06-01T09:00:00.000Z');
  });

  it('keeps the existing SM-2 day-based schedule after the first review', () => {
    const reviewedAt = new Date('2026-06-01T09:00:00.000Z');

    const result = service.calculateNextReviewState(
      {
        ...baseState,
        status: ReviewCardStatus.LEARNING,
        repetition: 1,
        intervalDays: 0,
        totalReviews: 1,
        correctReviews: 1,
        firstLearnedAt: new Date('2026-05-31T09:00:00.000Z'),
      },
      ReviewRating.GOOD,
      reviewedAt,
    );

    expect(result.status).toBe(ReviewCardStatus.REVIEW);
    expect(result.repetition).toBe(2);
    expect(result.intervalDays).toBe(6);
    expect(result.dueAt.toISOString()).toBe('2026-06-07T09:00:00.000Z');
  });
});
