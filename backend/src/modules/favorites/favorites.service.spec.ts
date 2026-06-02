import { ForbiddenException } from '@nestjs/common';
import { FavoritesService } from './favorites.service';

describe('FavoritesService', () => {
  let service: FavoritesService;
  let prisma: any;

  beforeEach(() => {
    prisma = {
      deck: {
        findFirst: jest.fn(),
        update: jest.fn(),
      },
      vocabulary: {
        findFirst: jest.fn(),
        create: jest.fn(),
        count: jest.fn(),
      },
      softDelete: jest.fn(),
    };

    service = new FavoritesService(prisma);
  });

  it('copies a vocabulary into the favorites deck with sourceVocabularyId', async () => {
    prisma.vocabulary.findFirst
      .mockResolvedValueOnce({
        id: 'vocab-source',
        word: 'budget',
        normalizedWord: 'budget',
        pronunciation: '/ˈbʌdʒ.ɪt/',
        meaning: 'ngân sách',
        normalizedMeaning: 'ngân sách',
        descriptionEn: 'A financial plan',
        example: 'We planned the budget carefully.',
        collocation: 'annual budget',
        relatedWords: 'finance',
        note: 'TOEIC',
        audioUrl: null,
        imageUrl: null,
        difficulty: null,
        partOfSpeech: 'noun',
        deck: {
          id: 'system-deck',
          deckType: 'SYSTEM',
          ownerUserId: null,
        },
      })
      .mockResolvedValueOnce(null);
    prisma.deck.findFirst.mockResolvedValue({
      id: 'favorites-deck',
      ownerUserId: 'user-1',
      deckType: 'USER',
      isDefault: true,
      deletedAt: null,
    });
    prisma.vocabulary.create.mockResolvedValue({ id: 'favorite-copy-1' });
    prisma.vocabulary.count.mockResolvedValue(1);

    const result = await service.favorite('user-1', 'vocab-source');

    expect(prisma.vocabulary.create).toHaveBeenCalledWith(
      expect.objectContaining({
        data: expect.objectContaining({
          deckId: 'favorites-deck',
          sourceVocabularyId: 'vocab-source',
          word: 'budget',
        }),
      }),
    );
    expect(result).toEqual({
      status: 'added',
      favoriteVocabularyId: 'favorite-copy-1',
    });
  });

  it('forbids favoriting a private vocabulary from another user', async () => {
    prisma.vocabulary.findFirst.mockResolvedValue({
      id: 'vocab-private',
      deck: {
        id: 'user-deck-2',
        deckType: 'USER',
        ownerUserId: 'user-2',
      },
    });

    await expect(service.favorite('user-1', 'vocab-private')).rejects.toBeInstanceOf(
      ForbiddenException,
    );
  });

  it('returns already_favorited when a copy already exists', async () => {
    prisma.vocabulary.findFirst
      .mockResolvedValueOnce({
        id: 'vocab-source',
        word: 'budget',
        normalizedWord: 'budget',
        meaning: 'ngân sách',
        normalizedMeaning: 'ngân sách',
        deck: {
          id: 'system-deck',
          deckType: 'SYSTEM',
          ownerUserId: null,
        },
      })
      .mockResolvedValueOnce({
        id: 'favorite-copy-1',
      });
    prisma.deck.findFirst.mockResolvedValue({
      id: 'favorites-deck',
      ownerUserId: 'user-1',
      deckType: 'USER',
      isDefault: true,
      deletedAt: null,
    });

    const result = await service.favorite('user-1', 'vocab-source');

    expect(result).toEqual({
      status: 'already_favorited',
      favoriteVocabularyId: 'favorite-copy-1',
    });
  });

  it('soft deletes a favorite copy on unfavorite', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'favorites-deck',
      ownerUserId: 'user-1',
      deckType: 'USER',
      isDefault: true,
      deletedAt: null,
    });
    prisma.vocabulary.findFirst.mockResolvedValue({
      id: 'favorite-copy-1',
      deckId: 'favorites-deck',
      sourceVocabularyId: 'vocab-source',
    });
    prisma.vocabulary.count.mockResolvedValue(0);

    await service.unfavorite('user-1', 'vocab-source');

    expect(prisma.softDelete).toHaveBeenCalledWith(prisma.vocabulary, 'favorite-copy-1');
  });
});
