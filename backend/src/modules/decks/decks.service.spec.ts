import { BadRequestException, ForbiddenException } from '@nestjs/common';
import { DecksService } from './decks.service';
import { DeckListType } from './dto/list-decks.query.dto';

describe('DecksService', () => {
  let service: DecksService;
  let prisma: any;

  beforeEach(() => {
    prisma = {
      $transaction: jest.fn(),
      learningLevel: {
        findFirst: jest.fn(),
      },
      deck: {
        count: jest.fn(),
        findMany: jest.fn(),
        findFirst: jest.fn(),
        create: jest.fn(),
      update: jest.fn(),
      },
      softDelete: jest.fn(),
    };

    service = new DecksService(prisma);
  });

  it('lists system and user decks with pagination metadata', async () => {
    const rows = [
      { id: 'deck-1', name: 'Toeic 600', deckType: 'SYSTEM', deletedAt: null },
      { id: 'deck-2', name: 'My Deck', deckType: 'USER', deletedAt: null },
    ];
    prisma.$transaction.mockResolvedValue([2, rows]);

    const result = await service.listDecks('user-1', {
      type: DeckListType.ALL,
      page: 1,
      pageSize: 20,
    });

    expect(result.total).toBe(2);
    expect(result.items).toHaveLength(2);
    expect(prisma.$transaction).toHaveBeenCalled();
  });

  it('creates a user deck with normalized name', async () => {
    prisma.deck.findFirst.mockResolvedValue(null);
    prisma.deck.create.mockResolvedValue({
      id: 'deck-1',
      ownerUserId: 'user-1',
      deckType: 'USER',
      name: '  My Deck  '.trim(),
      normalizedName: 'my deck',
      description: 'Sample',
      tags: ['toeic'],
      isDefault: false,
      totalWords: 0,
      learningLevel: null,
    });

    const result = await service.createUserDeck('user-1', {
      name: '  My Deck  ',
      description: 'Sample',
      tags: ['toeic'],
    });

    expect(prisma.deck.create).toHaveBeenCalledWith(
      expect.objectContaining({
        data: expect.objectContaining({
          normalizedName: 'my deck',
          deckType: 'USER',
          ownerUserId: 'user-1',
        }),
      }),
    );
    expect(result.name).toBe('My Deck');
  });

  it('rejects duplicate user deck name', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'existing-deck',
      normalizedName: 'my deck',
    });

    await expect(
      service.createUserDeck('user-1', {
        name: 'My   Deck',
      }),
    ).rejects.toBeInstanceOf(BadRequestException);
  });

  it('forbids editing the favorites deck', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'favorites-deck',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: true,
      deletedAt: null,
    });

    await expect(
      service.updateUserDeck('user-1', 'favorites-deck', {
        name: 'New Name',
      }),
    ).rejects.toBeInstanceOf(ForbiddenException);
  });

  it('soft deletes a user deck when allowed', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: false,
      deletedAt: null,
    });

    await service.softDeleteUserDeck('user-1', 'deck-1');

    expect(prisma.softDelete).toHaveBeenCalledWith(prisma.deck, 'deck-1');
  });
});
