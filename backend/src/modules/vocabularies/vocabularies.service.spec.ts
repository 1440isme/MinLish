import { BadRequestException, ForbiddenException } from '@nestjs/common';
import { VocabulariesService } from './vocabularies.service';

describe('VocabulariesService', () => {
  let service: VocabulariesService;
  let prisma: any;

  beforeEach(() => {
    prisma = {
      $transaction: jest.fn(),
      deck: {
        findFirst: jest.fn(),
        update: jest.fn(),
      },
      vocabulary: {
        count: jest.fn(),
        findMany: jest.fn(),
        findFirst: jest.fn(),
        create: jest.fn(),
        update: jest.fn(),
      },
      softDelete: jest.fn(),
    };

    service = new VocabulariesService(prisma);
  });

  it('returns duplicate exact error when the same vocabulary already exists', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: false,
    });
    prisma.vocabulary.findFirst.mockResolvedValueOnce({
      id: 'vocab-existing',
    });

    await expect(
      service.createInDeck('user-1', 'deck-1', {
        word: 'Budget',
        meaning: 'Ngân sách',
      } as any),
    ).rejects.toBeInstanceOf(BadRequestException);
  });

  it('warns when the same word exists with a different meaning', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: false,
    });
    prisma.vocabulary.findFirst.mockResolvedValueOnce(null);
    prisma.vocabulary.findMany.mockResolvedValueOnce([
      { id: 'vocab-1', word: 'budget', meaning: 'ngân sách' },
    ]);

    await expect(
      service.createInDeck('user-1', 'deck-1', {
        word: 'Budget',
        meaning: 'bản dự toán',
      } as any),
    ).rejects.toMatchObject({
      response: expect.objectContaining({
        code: 'WORD_EXISTS_WITH_DIFFERENT_MEANING',
        existingItems: [{ id: 'vocab-1', word: 'budget', meaning: 'ngân sách' }],
      }),
    });
  });

  it('creates vocabulary and recalculates deck totals', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: false,
    });
    prisma.vocabulary.findFirst.mockResolvedValueOnce(null);
    prisma.vocabulary.findMany.mockResolvedValueOnce([]);
    prisma.vocabulary.create.mockResolvedValue({
      id: 'vocab-1',
      deckId: 'deck-1',
      word: 'budget',
      normalizedWord: 'budget',
      pronunciation: '/ˈbʌdʒ.ɪt/',
      meaning: 'ngân sách',
      normalizedMeaning: 'ngân sách',
      descriptionEn: null,
      example: null,
      collocation: null,
      relatedWords: null,
      note: null,
      audioUrl: null,
      imageUrl: null,
      difficulty: null,
      partOfSpeech: 'noun',
    });
    prisma.vocabulary.count.mockResolvedValue(1);

    const result = await service.createInDeck('user-1', 'deck-1', {
      word: 'budget',
      pronunciation: '/ˈbʌdʒ.ɪt/',
      meaning: 'ngân sách',
      partOfSpeech: 'noun',
      allowSameWordDifferentMeaning: false,
    } as any);

    expect(result.word).toBe('budget');
    expect(prisma.deck.update).toHaveBeenCalledWith({
      where: { id: 'deck-1' },
      data: { totalWords: 1 },
    });
  });

  it('forbids manual edits in favorites deck', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'favorites-deck',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: true,
    });

    await expect(
      service.createInDeck('user-1', 'favorites-deck', {
        word: 'budget',
        meaning: 'ngân sách',
      } as any),
    ).rejects.toBeInstanceOf(ForbiddenException);
  });

  it('soft deletes vocabulary and refreshes deck totals', async () => {
    prisma.vocabulary.findFirst.mockResolvedValue({
      id: 'vocab-1',
      deckId: 'deck-1',
      deletedAt: null,
    });
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: false,
    });
    prisma.vocabulary.count.mockResolvedValue(2);

    await service.softDelete('user-1', 'vocab-1');

    expect(prisma.softDelete).toHaveBeenCalledWith(prisma.vocabulary, 'vocab-1');
    expect(prisma.deck.update).toHaveBeenCalledWith({
      where: { id: 'deck-1' },
      data: { totalWords: 2 },
    });
  });
});
