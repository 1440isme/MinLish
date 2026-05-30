import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { PrismaService } from '../../prisma/prisma.service';
import { ErrorCodes } from '../../config/common/errors/error-codes';
import { DeckEntity } from './entities/deck.entity';
import { ListDecksQueryDto, DeckListType } from './dto/list-decks.query.dto';
import { CreateDeckDto } from './dto/create-deck.dto';
import { UpdateDeckDto } from './dto/update-deck.dto';

function normalizeText(value: string): string {
  return value.trim().toLowerCase().replace(/\s+/g, ' ');
}

/** Path is derived via learningLevel.learningPath — not stored on deck. */
const deckWithLearningLevelInclude = {
  learningLevel: {
    include: {
      learningPath: true,
    },
  },
} as const;

@Injectable()
export class DecksService {
  constructor(private prisma: PrismaService) {}

  private async assertActiveLearningLevelExists(levelId: string): Promise<void> {
    const level = await this.prisma.learningLevel.findFirst({
      where: {
        id: levelId,
        isActive: true,
      },
    });
    if (!level) {
      throw new BadRequestException({
        code: ErrorCodes.LEARNING_LEVEL_NOT_FOUND,
        message: 'Learning level không tồn tại hoặc không khả dụng',
      });
    }
  }

  /** Resolve optional level id for create (null = no level). */
  private async resolveLearningLevelIdForCreate(
    learningLevelId?: string,
  ): Promise<string | null> {
    if (!learningLevelId) {
      return null;
    }
    await this.assertActiveLearningLevelExists(learningLevelId);
    return learningLevelId;
  }

  /**
   * Resolve level for update.
   * undefined = do not change; null = clear; string = set (validated).
   */
  private async resolveLearningLevelIdForUpdate(
    learningLevelId: string | null | undefined,
  ): Promise<string | null | undefined> {
    if (learningLevelId === undefined) {
      return undefined;
    }
    if (learningLevelId === null) {
      return null;
    }
    await this.assertActiveLearningLevelExists(learningLevelId);
    return learningLevelId;
  }

  private toDeckEntity(deck: Record<string, unknown>): DeckEntity {
    return new DeckEntity(deck as any);
  }

  async listDecks(
    currentUserId: string,
    query: ListDecksQueryDto,
  ): Promise<{
    items: DeckEntity[];
    total: number;
    page: number;
    pageSize: number;
  }> {
    const type = query.type ?? DeckListType.ALL;
    const page = query.page ?? 1;
    const pageSize = query.pageSize ?? 20;

    const search = query.search?.trim();
    const searchFilter: Prisma.DeckWhereInput | undefined = search
      ? {
          OR: [
            { name: { contains: search } },
            { description: { contains: search } },
          ],
        }
      : undefined;

    const systemWhere: Prisma.DeckWhereInput = {
      deckType: 'SYSTEM',
      deletedAt: null,
      ...(query.levelId ? { learningLevelId: query.levelId } : {}),
      ...(searchFilter ?? {}),
    };

    const userWhere: Prisma.DeckWhereInput = {
      deckType: 'USER',
      ownerUserId: currentUserId,
      deletedAt: null,
      ...(query.levelId ? { learningLevelId: query.levelId } : {}),
      ...(searchFilter ?? {}),
    };

    const where: Prisma.DeckWhereInput =
      type === DeckListType.SYSTEM
        ? systemWhere
        : type === DeckListType.USER
          ? userWhere
          : {
              OR: [systemWhere, userWhere],
            };

    const [total, rows] = await this.prisma.$transaction([
      this.prisma.deck.count({ where }),
      this.prisma.deck.findMany({
        where,
        orderBy:
          type === DeckListType.USER
            ? [{ isDefault: 'desc' }, { updatedAt: 'desc' }]
            : [{ displayOrder: 'asc' }, { name: 'asc' }],
        skip: (page - 1) * pageSize,
        take: pageSize,
      }),
    ]);

    return {
      total,
      page,
      pageSize,
      items: rows.map((d) => new DeckEntity(d as any)),
    };
  }

  async getDeckById(
    currentUserId: string,
    deckId: string,
  ): Promise<DeckEntity> {
    const deck = await this.prisma.deck.findFirst({
      where: {
        id: deckId,
        deletedAt: null,
      },
      include: deckWithLearningLevelInclude,
    });
    if (!deck) {
      throw new NotFoundException({
        code: ErrorCodes.DECK_NOT_FOUND,
        message: 'Deck không tồn tại',
      });
    }

    if (deck.deckType === 'USER' && deck.ownerUserId !== currentUserId) {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_FORBIDDEN,
        message: 'Bạn không có quyền truy cập deck này',
      });
    }

    return this.toDeckEntity(deck);
  }

  async getFavoritesDeck(currentUserId: string): Promise<DeckEntity> {
    const deck = await this.prisma.deck.findFirst({
      where: {
        ownerUserId: currentUserId,
        deckType: 'USER',
        isDefault: true,
        deletedAt: null,
      },
      include: deckWithLearningLevelInclude,
    });
    if (!deck) {
      throw new NotFoundException({
        code: ErrorCodes.FAVORITES_DECK_NOT_FOUND,
        message: 'Favorites deck không tồn tại (cần được tạo khi đăng ký)',
      });
    }
    return this.toDeckEntity(deck);
  }

  async createUserDeck(
    currentUserId: string,
    dto: CreateDeckDto,
  ): Promise<DeckEntity> {
    const normalizedName = normalizeText(dto.name);

    const existing = await this.prisma.deck.findFirst({
      where: {
        ownerUserId: currentUserId,
        deckType: 'USER',
        normalizedName,
        deletedAt: null,
      },
    });
    if (existing) {
      throw new BadRequestException({
        code: ErrorCodes.DECK_NAME_DUPLICATE,
        message: 'Tên deck đã tồn tại',
      });
    }

    const learningLevelId = await this.resolveLearningLevelIdForCreate(
      dto.learningLevelId,
    );

    const created = await this.prisma.deck.create({
      data: {
        ownerUserId: currentUserId,
        learningLevelId,
        deckType: 'USER',
        visibility: 'PRIVATE',
        name: dto.name.trim(),
        normalizedName,
        description: dto.description?.trim() ?? null,
        tags: dto.tags ?? Prisma.JsonNull,
        thumbnailUrl: null,
        displayOrder: 0,
        totalWords: 0,
        isDefault: false,
      },
      include: deckWithLearningLevelInclude,
    });

    return this.toDeckEntity(created);
  }

  async updateUserDeck(
    currentUserId: string,
    deckId: string,
    dto: UpdateDeckDto,
  ): Promise<DeckEntity> {
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

    if (deck.deckType === 'SYSTEM') {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_CANNOT_UPDATE_SYSTEM,
        message: 'Không thể sửa system deck',
      });
    }

    if (deck.ownerUserId !== currentUserId) {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_FORBIDDEN,
        message: 'Bạn không có quyền sửa deck này',
      });
    }

    if (deck.isDefault) {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_CANNOT_DELETE_FAVORITES,
        message: 'Không thể sửa Favorites deck trong MVP',
      });
    }

    const learningLevelId = await this.resolveLearningLevelIdForUpdate(
      dto.learningLevelId,
    );

    const normalizedName = dto.name ? normalizeText(dto.name) : undefined;
    if (normalizedName) {
      const existing = await this.prisma.deck.findFirst({
        where: {
          ownerUserId: currentUserId,
          deckType: 'USER',
          normalizedName,
          deletedAt: null,
          NOT: { id: deckId },
        },
      });
      if (existing) {
        throw new BadRequestException({
          code: ErrorCodes.DECK_NAME_DUPLICATE,
          message: 'Tên deck đã tồn tại',
        });
      }
    }

    const updated = await this.prisma.deck.update({
      where: { id: deckId },
      data: {
        name: dto.name ? dto.name.trim() : undefined,
        normalizedName,
        description: dto.description ? dto.description.trim() : undefined,
        tags: dto.tags ? dto.tags : undefined,
        ...(learningLevelId !== undefined
          ? { learningLevelId }
          : {}),
      },
      include: deckWithLearningLevelInclude,
    });

    return this.toDeckEntity(updated);
  }

  async softDeleteUserDeck(
    currentUserId: string,
    deckId: string,
  ): Promise<void> {
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

    if (deck.deckType === 'SYSTEM') {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_CANNOT_UPDATE_SYSTEM,
        message: 'Không thể xóa system deck',
      });
    }

    if (deck.ownerUserId !== currentUserId) {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_FORBIDDEN,
        message: 'Bạn không có quyền xóa deck này',
      });
    }

    if (deck.isDefault) {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_CANNOT_DELETE_FAVORITES,
        message: 'Không thể xóa Favorites deck',
      });
    }

    await this.prisma.softDelete(this.prisma.deck, deckId);
  }
}
