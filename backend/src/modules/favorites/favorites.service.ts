import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { ErrorCodes } from '../../config/common/errors/error-codes';
import { FavoriteResponseDto } from './dto/favorite.response.dto';

@Injectable()
export class FavoritesService {
  constructor(private prisma: PrismaService) {}

  private async getFavoritesDeckOrThrow(currentUserId: string) {
    const deck = await this.prisma.deck.findFirst({
      where: {
        ownerUserId: currentUserId,
        deckType: 'USER',
        isDefault: true,
        deletedAt: null,
      },
    });
    if (!deck) {
      throw new NotFoundException({
        code: ErrorCodes.FAVORITES_DECK_NOT_FOUND,
        message: 'Favorites deck không tồn tại (cần được tạo khi đăng ký)',
      });
    }
    return deck;
  }

  async favorite(currentUserId: string, originalVocabularyId: string): Promise<FavoriteResponseDto> {
    const original = await this.prisma.vocabulary.findFirst({
      where: {
        id: originalVocabularyId,
        deletedAt: null,
      },
      include: {
        deck: true,
      },
    });
    if (!original) {
      throw new NotFoundException({
        code: ErrorCodes.VOCABULARY_NOT_FOUND,
        message: 'Vocabulary không tồn tại',
      });
    }

    if (
      original.deck.deckType === 'USER' &&
      original.deck.ownerUserId !== currentUserId
    ) {
      throw new ForbiddenException({
        code: ErrorCodes.VOCABULARY_FORBIDDEN,
        message: 'Bạn không có quyền favorite vocabulary này',
      });
    }

    const favoritesDeck = await this.getFavoritesDeckOrThrow(currentUserId);

    const existingCopy = await this.prisma.vocabulary.findFirst({
      where: {
        deckId: favoritesDeck.id,
        sourceVocabularyId: originalVocabularyId,
        deletedAt: null,
      },
    });
    if (existingCopy) {
      return {
        status: 'already_favorited',
        favoriteVocabularyId: existingCopy.id,
      };
    }

    const created = await this.prisma.vocabulary.create({
      data: {
        deckId: favoritesDeck.id,
        sourceVocabularyId: original.id,
        word: original.word,
        normalizedWord: original.normalizedWord,
        pronunciation: original.pronunciation,
        meaning: original.meaning,
        normalizedMeaning: original.normalizedMeaning,
        descriptionEn: original.descriptionEn,
        example: original.example,
        collocation: original.collocation,
        relatedWords: original.relatedWords,
        note: original.note,
        audioUrl: original.audioUrl,
        imageUrl: original.imageUrl,
        difficulty: original.difficulty,
        partOfSpeech: original.partOfSpeech,
      },
    });

    await this.recalculateDeckTotalWords(favoritesDeck.id);

    return {
      status: 'added',
      favoriteVocabularyId: created.id,
    };
  }

  async unfavorite(currentUserId: string, originalVocabularyId: string): Promise<void> {
    const favoritesDeck = await this.getFavoritesDeckOrThrow(currentUserId);

    const copy = await this.prisma.vocabulary.findFirst({
      where: {
        deckId: favoritesDeck.id,
        sourceVocabularyId: originalVocabularyId,
        deletedAt: null,
      },
    });
    if (!copy) {
      throw new BadRequestException({
        code: ErrorCodes.FAVORITE_COPY_NOT_FOUND,
        message: 'Vocabulary này chưa được favorite',
      });
    }

    await this.prisma.softDelete(this.prisma.vocabulary, copy.id);
    await this.recalculateDeckTotalWords(favoritesDeck.id);
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

