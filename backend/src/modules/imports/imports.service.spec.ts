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

  it('persists valid part_of_speech values from CSV rows', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: false,
      deletedAt: null,
    });
    prisma.importJob.create.mockResolvedValue({ id: 'job-3' });
    prisma.vocabulary.findMany.mockResolvedValue([]);
    prisma.vocabulary.createMany.mockResolvedValue({ count: 1 });
    prisma.vocabulary.count.mockResolvedValue(1);

    const csv = [
      'word,meaning,part_of_speech',
      'budget,ngân sách,noun',
    ].join('\n');

    const result = await service.importCsv(
      'user-1',
      'deck-1',
      Buffer.from(csv, 'utf8'),
      'with-pos.csv',
    );

    expect(result.successRows).toBe(1);
    expect(result.failedRows).toBe(0);
    expect(prisma.vocabulary.createMany).toHaveBeenCalledWith(
      expect.objectContaining({
        data: [
          expect.objectContaining({
            word: 'budget',
            partOfSpeech: 'noun',
          }),
        ],
      }),
    );
  });

  it('marks rows with invalid part_of_speech as failed without failing the entire file', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      deckType: 'USER',
      ownerUserId: 'user-1',
      isDefault: false,
      deletedAt: null,
    });
    prisma.importJob.create.mockResolvedValue({ id: 'job-4' });
    prisma.vocabulary.findMany.mockResolvedValue([]);
    prisma.vocabulary.createMany.mockResolvedValue({ count: 1 });
    prisma.vocabulary.count.mockResolvedValue(1);

    const csv = [
      'word,meaning,part_of_speech',
      'budget,ngân sách,n',
      'delay,trì hoãn,verb',
    ].join('\n');

    const result = await service.importCsv(
      'user-1',
      'deck-1',
      Buffer.from(csv, 'utf8'),
      'invalid-pos.csv',
    );

    expect(result.successRows).toBe(1);
    expect(result.failedRows).toBe(1);
    expect(result.status).toBe('PARTIAL_SUCCESS');
    expect(result.errors).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          row: 1,
          field: 'part_of_speech',
        }),
      ]),
    );
    expect(prisma.vocabulary.createMany).toHaveBeenCalledWith(
      expect.objectContaining({
        data: [
          expect.objectContaining({
            word: 'delay',
            partOfSpeech: 'verb',
          }),
        ],
      }),
    );
  });

  it('exports csv for an accessible deck with escaped values', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-1',
      name: 'Business English',
      deckType: 'SYSTEM',
      ownerUserId: null,
      isDefault: false,
      deletedAt: null,
    });
    prisma.vocabulary.findMany.mockResolvedValue([
      {
        word: 'budget',
        meaning: 'ngân sách',
        pronunciation: '/bud-get/',
        descriptionEn: 'planned "spending"',
        example: 'Keep, track',
        collocation: 'tight budget',
        relatedWords: 'finance',
        note: 'line 1\nline 2',
        partOfSpeech: 'noun',
      },
    ]);

    const result = await service.exportCsv('user-1', 'deck-1');
    const text = result.content.toString('utf8');

    expect(result.fileName).toBe('business_english_vocabularies.csv');
    expect(text).toContain(
      'word,meaning,pronunciation,description_en,example,collocation,related_words,note,part_of_speech',
    );
    expect(text).toContain('"planned ""spending"""');
    expect(text).toContain('"Keep, track"');
    expect(text).toContain('"line 1\nline 2"');
  });

  it('forbids exporting another user deck', async () => {
    prisma.deck.findFirst.mockResolvedValue({
      id: 'deck-2',
      name: 'Private Deck',
      deckType: 'USER',
      ownerUserId: 'user-2',
      isDefault: false,
      deletedAt: null,
    });

    await expect(service.exportCsv('user-1', 'deck-2')).rejects.toBeInstanceOf(
      ForbiddenException,
    );
  });
});
