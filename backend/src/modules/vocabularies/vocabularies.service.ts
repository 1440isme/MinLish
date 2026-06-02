import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import type { Deck, Vocabulary } from '@prisma/client';
import { PrismaService } from '../../prisma/prisma.service';
import { ErrorCodes } from '../../config/common/errors/error-codes';
import { VocabularyEntity } from './entities/vocabulary.entity';
import { ListVocabulariesQueryDto } from './dto/list-vocabularies.query.dto';
import { CreateVocabularyDto } from './dto/create-vocabulary.dto';
import { UpdateVocabularyDto } from './dto/update-vocabulary.dto';

function normalizeText(value: string): string {
  return value.trim().toLowerCase().replace(/\s+/g, ' ');
}

@Injectable()
export class VocabulariesService {
  constructor(private prisma: PrismaService) {}

  private async getDeckOrThrow(deckId: string): Promise<Deck> {
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
    return deck;
  }

  private assertDeckReadable(currentUserId: string, deck: Deck): void {
    if (deck.deckType === 'USER' && deck.ownerUserId !== currentUserId) {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_FORBIDDEN,
        message: 'Bạn không có quyền truy cập deck này',
      });
    }
  }

  private assertDeckEditable(currentUserId: string, deck: Deck): void {
    if (deck.deckType === 'SYSTEM') {
      throw new ForbiddenException({
        code: ErrorCodes.SYSTEM_DECK_VOCAB_EDIT_FORBIDDEN,
        message: 'Không thể chỉnh sửa vocabulary trong system deck',
      });
    }

    if (deck.ownerUserId !== currentUserId) {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_FORBIDDEN,
        message: 'Bạn không có quyền chỉnh sửa deck này',
      });
    }

    if (deck.isDefault) {
      throw new ForbiddenException({
        code: ErrorCodes.FAVORITES_DECK_MANUAL_EDIT_FORBIDDEN,
        message: 'Không thể thêm/sửa từ trực tiếp trong Favorites deck (MVP)',
      });
    }
  }

  async listByDeck(
    currentUserId: string,
    deckId: string,
    query: ListVocabulariesQueryDto,
  ): Promise<{
    items: VocabularyEntity[];
    total: number;
    page: number;
    pageSize: number;
  }> {
    const deck = await this.getDeckOrThrow(deckId);
    this.assertDeckReadable(currentUserId, deck);

    const page = query.page ?? 1;
    const pageSize = query.pageSize ?? 20;
    const search = query.search?.trim();

    const where = {
      deckId,
      deletedAt: null,
      ...(search
        ? {
            OR: [
              { word: { contains: search } },
              { meaning: { contains: search } },
              { example: { contains: search } },
            ],
          }
        : {}),
    };

    const [total, rows] = await this.prisma.$transaction([
      this.prisma.vocabulary.count({ where }),
      this.prisma.vocabulary.findMany({
        where,
        orderBy: [{ word: 'asc' }, { id: 'asc' }],
        skip: (page - 1) * pageSize, // bỏ qua các bản ghi của những trang trước, để lấy đúng dữ liệu của trang hiện tại.
        take: pageSize,
      }),
    ]);

    return {
      total,
      page,
      pageSize,
      items: rows.map((v) => new VocabularyEntity(v as any)), // convert dữ liệu thô từ Prisma thành VocabularyEntity
    };
  }

  async createInDeck(
    currentUserId: string,
    deckId: string,
    dto: CreateVocabularyDto,
  ): Promise<VocabularyEntity> {
    const deck = await this.getDeckOrThrow(deckId);
    this.assertDeckEditable(currentUserId, deck);

    const normalizedWord = normalizeText(dto.word);
    const normalizedMeaning = normalizeText(dto.meaning);
    // kiểm tra trùng hoàn toàn
    const exactDuplicate = await this.prisma.vocabulary.findFirst({
      where: {
        deckId,
        normalizedWord,
        normalizedMeaning,
        deletedAt: null,
      },
    });
    if (exactDuplicate) {
      throw new BadRequestException({
        code: ErrorCodes.DUPLICATE_VOCABULARY,
        message: 'Từ này đã tồn tại trong deck',
      });
    }
    // kiểm tra cùng từ nhưng khác nghĩa
    const sameWordDifferentMeaning = await this.prisma.vocabulary.findMany({
      where: {
        deckId,
        normalizedWord,
        normalizedMeaning: { not: normalizedMeaning }, // tìm từ khác nghĩa khác với normalizedMeaning hiện tại
        deletedAt: null,
      },
      select: {
        id: true,
        word: true,
        meaning: true,
      },
      orderBy: { createdAt: 'desc' },
      take: 10,
    });

    if (
      sameWordDifferentMeaning.length > 0 &&
      dto.allowSameWordDifferentMeaning !== true
    ) {
      throw new BadRequestException({
        code: ErrorCodes.WORD_EXISTS_WITH_DIFFERENT_MEANING,
        message:
          'Từ này đã tồn tại trong deck với nghĩa khác. Nếu đây là nghĩa mới, bạn có thể xác nhận để thêm tiếp.',
        existingItems: sameWordDifferentMeaning,
      });
    }

    const created = await this.prisma.vocabulary.create({
      data: {
        deckId,
        sourceVocabularyId: null, // nghĩa là từ này thêm thủ công, ko phải copy/import từ khác
        word: dto.word.trim(),
        normalizedWord,
        pronunciation: dto.pronunciation?.trim() ?? null,
        meaning: dto.meaning.trim(),
        normalizedMeaning,
        descriptionEn: dto.descriptionEn?.trim() ?? null,
        example: dto.example?.trim() ?? null,
        collocation: dto.collocation?.trim() ?? null,
        relatedWords: dto.relatedWords?.trim() ?? null,
        note: dto.note?.trim() ?? null,
        audioUrl: null,
        imageUrl: null,
        difficulty: dto.difficulty ?? null,
        partOfSpeech: dto.partOfSpeech?.trim() ?? null,
      },
    });

    await this.recalculateDeckTotalWords(deckId); // cập nhật lại số từ

    return new VocabularyEntity(created as any); // trả dưới dạng VocabularyEntity
  }

  async update(
    currentUserId: string,
    vocabularyId: string,
    dto: UpdateVocabularyDto,
  ): Promise<VocabularyEntity> {
    const vocab = await this.prisma.vocabulary.findFirst({
      where: {
        id: vocabularyId,
        deletedAt: null,
      },
    });
    if (!vocab) {
      throw new NotFoundException({
        code: ErrorCodes.VOCABULARY_NOT_FOUND,
        message: 'Vocabulary không tồn tại',
      });
    }

    const deck = await this.getDeckOrThrow(vocab.deckId);
    this.assertDeckEditable(currentUserId, deck);

    const nextWord = dto.word?.trim();
    const nextMeaning = dto.meaning?.trim();
    const normalizedWord = nextWord
      ? normalizeText(nextWord)
      : vocab.normalizedWord;
    const normalizedMeaning = nextMeaning
      ? normalizeText(nextMeaning)
      : vocab.normalizedMeaning;

    const exactDuplicate = await this.prisma.vocabulary.findFirst({
      where: {
        deckId: vocab.deckId,
        normalizedWord,
        normalizedMeaning,
        deletedAt: null,
        NOT: { id: vocabularyId },
      },
    });
    if (exactDuplicate) {
      throw new BadRequestException({
        code: ErrorCodes.DUPLICATE_VOCABULARY,
        message: 'Từ này đã tồn tại trong deck',
      });
    }

    const updated = await this.prisma.vocabulary.update({
      where: { id: vocabularyId },
      data: {
        word: nextWord ?? undefined,
        normalizedWord: dto.word ? normalizedWord : undefined,
        pronunciation:
          dto.pronunciation !== undefined
            ? (dto.pronunciation?.trim() ?? null)
            : undefined,
        meaning: nextMeaning ?? undefined,
        normalizedMeaning: dto.meaning ? normalizedMeaning : undefined,
        descriptionEn:
          dto.descriptionEn !== undefined
            ? (dto.descriptionEn?.trim() ?? null)
            : undefined,
        example:
          dto.example !== undefined ? (dto.example?.trim() ?? null) : undefined,
        collocation:
          dto.collocation !== undefined
            ? (dto.collocation?.trim() ?? null)
            : undefined,
        relatedWords:
          dto.relatedWords !== undefined
            ? (dto.relatedWords?.trim() ?? null)
            : undefined,
        note: dto.note !== undefined ? (dto.note?.trim() ?? null) : undefined,
        difficulty:
          dto.difficulty !== undefined ? (dto.difficulty ?? null) : undefined,
        partOfSpeech:
          dto.partOfSpeech !== undefined
            ? (dto.partOfSpeech?.trim() ?? null)
            : undefined,
      },
    });

    return new VocabularyEntity(updated as any);
  }

  async softDelete(currentUserId: string, vocabularyId: string): Promise<void> {
    const vocab = await this.prisma.vocabulary.findFirst({
      where: {
        id: vocabularyId,
        deletedAt: null,
      },
    });
    if (!vocab) {
      throw new NotFoundException({
        code: ErrorCodes.VOCABULARY_NOT_FOUND,
        message: 'Vocabulary không tồn tại',
      });
    }

    const deck = await this.getDeckOrThrow(vocab.deckId);
    this.assertDeckEditable(currentUserId, deck);

    await this.prisma.softDelete(this.prisma.vocabulary, vocabularyId);
    await this.recalculateDeckTotalWords(vocab.deckId);
  }

  private async recalculateDeckTotalWords(deckId: string): Promise<void> {
    const count = await this.prisma.vocabulary.count({
      where: { deckId, deletedAt: null },
    });
    await this.prisma.deck.update({
      where: { id: deckId },
      data: { totalWords: count },
    });
  }
}
