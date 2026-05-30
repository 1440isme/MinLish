import { ForbiddenException } from '@nestjs/common';
import { ImportsService } from './imports.service';

describe('ImportsService', () => {
  let service: ImportsService;
  let prisma: any;

  beforeEach(() => {
    prisma = {
      deck: {
        findFirst: jest.fn(),
        update: jest.fn(),
      },
      importJob: {
        create: jest.fn(),
        update: jest.fn(),
      },
      vocabulary: {
        findMany: jest.fn(),
        createMany: jest.fn(),
        count: jest.fn(),
      },
    };

    service = new ImportsService(prisma);
  });

  it('forbids importing directly into the favorites deck', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'favorites-deck',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: true,
      deletedAt: null,
    });

    await expect(
      service.importCsv(
        'user-1',
        'favorites-deck',
        Buffer.from('word,meaning\nbudget,ngân sách'),
        'favorites.csv',
      ),
    ).rejects.toBeInstanceOf(ForbiddenException);
  });

  it('imports with partial success for invalid rows and duplicates in the same file', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: false,
      deletedAt: null,
    });
    prisma.importJob.create.mockResolvedValue({ id: 'job-1' });
    prisma.vocabulary.findMany.mockResolvedValue([]);
    prisma.vocabulary.createMany.mockResolvedValue({ count: 1 });
    prisma.vocabulary.count.mockResolvedValue(1);

    const csv = [
      'word,meaning',
      'budget,ngân sách',
      'budget,ngân sách',
      'delay,',
    ].join('\n');

    const result = await service.importCsv(
      'user-1',
      'deck-1',
      Buffer.from(csv, 'utf8'),
      'sample.csv',
    );

    expect(result.successRows).toBe(1);
    expect(result.duplicateRows).toBe(1);
    expect(result.failedRows).toBe(1);
    expect(result.status).toBe('PARTIAL_SUCCESS');
  });

  it('skips duplicates already present in the target deck', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: false,
      deletedAt: null,
    });
    prisma.importJob.create.mockResolvedValue({ id: 'job-2' });
    prisma.vocabulary.findMany.mockResolvedValue([
      { normalizedWord: 'budget', normalizedMeaning: 'ngân sách' },
    ]);
    prisma.vocabulary.createMany.mockResolvedValue({ count: 1 });
    prisma.vocabulary.count.mockResolvedValue(2);

    const csv = [
      'word,meaning',
      'budget,ngân sách',
      'delay,trì hoãn',
    ].join('\n');

    const result = await service.importCsv(
      'user-1',
      'deck-1',
      Buffer.from(csv, 'utf8'),
      'duplicates.csv',
    );

    expect(result.successRows).toBe(1);
    expect(result.duplicateRows).toBe(1);
    expect(result.failedRows).toBe(0);
    expect(prisma.vocabulary.createMany).toHaveBeenCalledWith(
      expect.objectContaining({
        data: [
          expect.objectContaining({
            word: 'delay',
            meaning: 'trì hoãn',
          }),
        ],
      }),
    );
  });
});
