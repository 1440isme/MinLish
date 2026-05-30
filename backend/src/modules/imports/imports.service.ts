import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import type { Deck } from '@prisma/client';
import { PrismaService } from '../../prisma/prisma.service';
import { ErrorCodes } from '../../config/common/errors/error-codes';
import { ImportCsvResponseDto } from './dto/import-csv.response.dto';
import { VocabularyPartOfSpeech } from '../vocabularies/entities/vocabulary.entity';

function normalizeText(value: string): string {
  return value.trim().toLowerCase().replace(/\s+/g, ' ');
}

const VALID_PARTS_OF_SPEECH = new Set<string>(
  Object.values(VocabularyPartOfSpeech),
);

type CsvRow = Record<string, unknown>;

@Injectable()
export class ImportsService {
  constructor(private prisma: PrismaService) {}

  private parseCsv(buffer: Buffer): CsvRow[] {
    // Minimal RFC4180-ish CSV parser (supports quotes + commas).
    const text = buffer.toString('utf8').replace(/^\uFEFF/, '');
    const lines = text.split(/\r?\n/).filter((l) => l.trim().length > 0);
    if (lines.length === 0) return [];

    const parseLine = (line: string): string[] => {
      const out: string[] = [];
      let cur = '';
      let inQuotes = false;
      for (let i = 0; i < line.length; i++) {
        const ch = line[i];
        if (ch === '"') {
          if (inQuotes && line[i + 1] === '"') {
            cur += '"';
            i++;
          } else {
            inQuotes = !inQuotes;
          }
          continue;
        }
        if (ch === ',' && !inQuotes) {
          out.push(cur);
          cur = '';
          continue;
        }
        cur += ch;
      }
      out.push(cur);
      return out.map((v) => v.trim());
    };

    const headers = parseLine(lines[0]).map((h) => h.trim());
    const rows: CsvRow[] = [];
    for (let li = 1; li < lines.length; li++) {
      const cols = parseLine(lines[li]);
      const row: CsvRow = {};
      for (let hi = 0; hi < headers.length; hi++) {
        const key = headers[hi];
        if (!key) continue;
        row[key] = cols[hi] ?? '';
      }
      rows.push(row);
    }
    return rows;
  }

  private async getDeckOrThrow(deckId: string): Promise<Deck> {
    const deck = await this.prisma.deck.findFirst({
      where: { id: deckId, deletedAt: null },
    });
    if (!deck) {
      throw new NotFoundException({
        code: ErrorCodes.DECK_NOT_FOUND,
        message: 'Deck không tồn tại',
      });
    }
    return deck;
  }

  private parsePartOfSpeech(value: unknown): string | null | 'INVALID' {
    if (typeof value !== 'string') {
      return null;
    }

    const normalized = value.trim().toLowerCase();
    if (!normalized) {
      return null;
    }

    return VALID_PARTS_OF_SPEECH.has(normalized) ? normalized : 'INVALID';
  }

  private assertDeckImportable(currentUserId: string, deck: Deck): void {
    if (deck.deckType === 'SYSTEM') {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_FORBIDDEN,
        message: 'Không thể import vào system deck',
      });
    }

    if (deck.ownerUserId !== currentUserId) {
      throw new ForbiddenException({
        code: ErrorCodes.DECK_FORBIDDEN,
        message: 'Bạn không có quyền import vào deck này',
      });
    }

    if (deck.isDefault) {
      throw new ForbiddenException({
        code: ErrorCodes.IMPORT_FAVORITES_FORBIDDEN,
        message: 'Không thể import trực tiếp vào Favorites deck',
      });
    }
  }

  async importCsv(
    currentUserId: string,
    deckId: string,
    fileBuffer: Buffer,
    fileName: string,
  ): Promise<ImportCsvResponseDto> {
    if (!fileBuffer || fileBuffer.length === 0) {
      throw new BadRequestException({
        code: ErrorCodes.IMPORT_FILE_REQUIRED,
        message: 'File CSV là bắt buộc',
      });
    }

    const deck = await this.getDeckOrThrow(deckId);
    this.assertDeckImportable(currentUserId, deck);

    const importJob = await this.prisma.importJob.create({
      data: {
        userId: currentUserId,
        deckId,
        fileName,
        fileType: 'CSV',
        status: 'PROCESSING',
        startedAt: new Date(),
      },
    });

    let totalRows = 0;
    let successRows = 0;
    let duplicateRows = 0;
    let failedRows = 0;

    const errors: { row: number; field: string; message: string }[] = [];
    const duplicates: {
      row: number;
      word: string;
      meaning: string;
      reason: string;
    }[] = [];

    try {
      const records = this.parseCsv(fileBuffer);

      totalRows = records.length;

      // Dedup within file by normalizedWord+normalizedMeaning
      const seenInFile = new Set<string>();
      const candidateRows: Array<{
        rowNumber: number;
        word: string;
        meaning: string;
        pronunciation?: string | null;
        descriptionEn?: string | null;
        example?: string | null;
        collocation?: string | null;
        relatedWords?: string | null;
        note?: string | null;
        partOfSpeech?: string | null;
        normalizedWord: string;
        normalizedMeaning: string;
      }> = [];

      for (let idx = 0; idx < records.length; idx++) {
        const rowNumber = idx + 1;
        const r = records[idx];

        const word = typeof r.word === 'string' ? r.word : '';
        const meaning = typeof r.meaning === 'string' ? r.meaning : '';

        if (!word.trim()) {
          failedRows++;
          errors.push({
            row: rowNumber,
            field: 'word',
            message: 'Word is required',
          });
          continue;
        }
        if (!meaning.trim()) {
          failedRows++;
          errors.push({
            row: rowNumber,
            field: 'meaning',
            message: 'Meaning is required',
          });
          continue;
        }

        const partOfSpeech = this.parsePartOfSpeech(r.part_of_speech);
        if (partOfSpeech === 'INVALID') {
          failedRows++;
          errors.push({
            row: rowNumber,
            field: 'part_of_speech',
            message: `part_of_speech must be one of: ${Array.from(VALID_PARTS_OF_SPEECH).join(', ')}`,
          });
          continue;
        }

        const normalizedWord = normalizeText(word);
        const normalizedMeaning = normalizeText(meaning);
        const key = `${normalizedWord}||${normalizedMeaning}`;

        if (seenInFile.has(key)) {
          duplicateRows++;
          duplicates.push({
            row: rowNumber,
            word,
            meaning,
            reason: 'Duplicate in this file',
          });
          continue;
        }
        seenInFile.add(key);

        candidateRows.push({
          rowNumber,
          word: word.trim(),
          meaning: meaning.trim(),
          pronunciation:
            typeof r.pronunciation === 'string' ? r.pronunciation.trim() : null,
          descriptionEn:
            typeof r.description_en === 'string'
              ? r.description_en.trim()
              : null,
          example: typeof r.example === 'string' ? r.example.trim() : null,
          collocation:
            typeof r.collocation === 'string' ? r.collocation.trim() : null,
          relatedWords:
            typeof r.related_words === 'string' ? r.related_words.trim() : null,
          note: typeof r.note === 'string' ? r.note.trim() : null,
          partOfSpeech,
          normalizedWord,
          normalizedMeaning,
        });
      }

      // Batch query existing vocabularies in deck (avoid row-by-row)
      const OR_BATCH_SIZE = 300;
      const existingKeys = new Set<string>();

      for (let i = 0; i < candidateRows.length; i += OR_BATCH_SIZE) {
        const chunk = candidateRows.slice(i, i + OR_BATCH_SIZE);
        const or = chunk.map((c) => ({
          normalizedWord: c.normalizedWord,
          normalizedMeaning: c.normalizedMeaning,
        }));

        const existing = await this.prisma.vocabulary.findMany({
          where: {
            deckId,
            deletedAt: null,
            OR: or,
          },
          select: {
            normalizedWord: true,
            normalizedMeaning: true,
          },
        });

        for (const e of existing) {
          existingKeys.add(`${e.normalizedWord}||${e.normalizedMeaning}`);
        }
      }

      const toInsert: any[] = [];
      for (const c of candidateRows) {
        const key = `${c.normalizedWord}||${c.normalizedMeaning}`;
        if (existingKeys.has(key)) {
          duplicateRows++;
          duplicates.push({
            row: c.rowNumber,
            word: c.word,
            meaning: c.meaning,
            reason: 'Duplicate in this deck',
          });
          continue;
        }

        toInsert.push({
          deckId,
          sourceVocabularyId: null,
          word: c.word,
          normalizedWord: c.normalizedWord,
          pronunciation: c.pronunciation ?? null,
          meaning: c.meaning,
          normalizedMeaning: c.normalizedMeaning,
          descriptionEn: c.descriptionEn ?? null,
          example: c.example ?? null,
          collocation: c.collocation ?? null,
          relatedWords: c.relatedWords ?? null,
          note: c.note ?? null,
          audioUrl: null,
          imageUrl: null,
          difficulty: null,
          partOfSpeech: c.partOfSpeech ?? null,
        });
      }

      // Insert in batches
      const INSERT_BATCH_SIZE = 500;
      for (let i = 0; i < toInsert.length; i += INSERT_BATCH_SIZE) {
        const batch = toInsert.slice(i, i + INSERT_BATCH_SIZE);
        // createMany keeps it efficient; duplicates were filtered already
        const res = await this.prisma.vocabulary.createMany({
          data: batch,
        });
        successRows += res.count;
      }

      await this.recalculateDeckTotalWords(deckId);

      const status: 'COMPLETED' | 'PARTIAL_SUCCESS' =
        failedRows === 0 && duplicateRows === 0
          ? 'COMPLETED'
          : 'PARTIAL_SUCCESS';

      await this.prisma.importJob.update({
        where: { id: importJob.id },
        data: {
          status,
          totalRows,
          successRows,
          duplicateRows,
          failedRows,
          errorReportJson: { errors, duplicates },
          finishedAt: new Date(),
        },
      });

      return {
        importJobId: importJob.id,
        totalRows,
        successRows,
        duplicateRows,
        failedRows,
        status,
        duplicates,
        errors,
      };
    } catch (e) {
      await this.prisma.importJob.update({
        where: { id: importJob.id },
        data: {
          status: 'FAILED',
          totalRows,
          successRows,
          duplicateRows,
          failedRows,
          errorReportJson: { errors, duplicates, rawError: String(e) },
          finishedAt: new Date(),
        },
      });

      throw new BadRequestException({
        code: ErrorCodes.IMPORT_INVALID_CSV,
        message: 'File CSV không hợp lệ hoặc không thể parse',
      });
    }
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
