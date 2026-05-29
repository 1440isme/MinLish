import {
  Controller,
  Param,
  Post,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { ApiBearerAuth, ApiBody, ApiConsumes, ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import type { User } from '@prisma/client';
import { CurrentUser } from '../../config/common/decorators/current-user.decorator';
import { ImportsService } from './imports.service';
import { ImportCsvResponseDto } from './dto/import-csv.response.dto';

/** Minimal shape from Multer memory storage (avoids @types/multer dependency). */
interface UploadedCsvFile {
  buffer: Buffer;
  originalname: string;
}

@ApiTags('Imports')
@ApiBearerAuth()
@Controller('decks')
export class ImportsController {
  constructor(private importsService: ImportsService) {}

  @Post(':deckId/import-csv')
  @ApiOperation({ summary: 'Import vocabularies from CSV into a user deck' })
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        file: {
          type: 'string',
          format: 'binary',
        },
      },
      required: ['file'],
    },
  })
  @ApiResponse({ status: 200, type: ImportCsvResponseDto })
  @UseInterceptors(FileInterceptor('file'))
  async importCsv(
    @CurrentUser() user: User,
    @Param('deckId') deckId: string,
    @UploadedFile() file?: UploadedCsvFile,
  ): Promise<ImportCsvResponseDto> {
    const fileName = file?.originalname ?? 'import.csv';
    return this.importsService.importCsv(
      user.id,
      deckId,
      file?.buffer ?? Buffer.alloc(0),
      fileName,
    );
  }
}

