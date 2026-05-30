import {
  Body,
  ClassSerializerInterceptor,
  Controller,
  Delete,
  Get,
  HttpCode,
  Param,
  Patch,
  Post,
  Query,
  UseInterceptors,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import type { User } from '@prisma/client';
import { CurrentUser } from '../../config/common/decorators/current-user.decorator';
import { PaginatedResponseDto } from '../../config/common/dto/pagination.dto';
import { VocabularyEntity } from './entities/vocabulary.entity';
import { VocabulariesService } from './vocabularies.service';
import { ListVocabulariesQueryDto } from './dto/list-vocabularies.query.dto';
import { CreateVocabularyDto } from './dto/create-vocabulary.dto';
import { UpdateVocabularyDto } from './dto/update-vocabulary.dto';

class VocabularyListResponseDto extends PaginatedResponseDto<VocabularyEntity> {}

@ApiTags('Vocabularies')
@ApiBearerAuth()
@UseInterceptors(ClassSerializerInterceptor)
@Controller()
export class VocabulariesController {
  constructor(private vocabulariesService: VocabulariesService) {}

  @Get('decks/:deckId/vocabularies')
  @ApiOperation({ summary: 'List vocabularies in deck' })
  @ApiResponse({
    status: 200,
    type: VocabularyListResponseDto,
  })
  async listByDeck(
    @CurrentUser() user: User,
    @Param('deckId') deckId: string,
    @Query() query: ListVocabulariesQueryDto,
  ): Promise<VocabularyListResponseDto> {
    const result = await this.vocabulariesService.listByDeck(
      user.id,
      deckId,
      query,
    );
    return {
      meta: {
        page: result.page,
        pageSize: result.pageSize,
        total: result.total,
      },
      items: result.items,
    };
  }

  @Post('decks/:deckId/vocabularies')
  @ApiOperation({ summary: 'Create vocabulary in a user deck (manual add)' })
  @ApiResponse({ status: 201, type: VocabularyEntity })
  async createInDeck(
    @CurrentUser() user: User,
    @Param('deckId') deckId: string,
    @Body() dto: CreateVocabularyDto,
  ): Promise<VocabularyEntity> {
    return this.vocabulariesService.createInDeck(user.id, deckId, dto);
  }

  @Patch('vocabularies/:id')
  @ApiOperation({ summary: 'Update a vocabulary (user deck only)' })
  @ApiResponse({ status: 200, type: VocabularyEntity })
  async update(
    @CurrentUser() user: User,
    @Param('id') id: string,
    @Body() dto: UpdateVocabularyDto,
  ): Promise<VocabularyEntity> {
    return this.vocabulariesService.update(user.id, id, dto);
  }

  @Delete('vocabularies/:id')
  @HttpCode(204)
  @ApiOperation({ summary: 'Soft delete a vocabulary (user deck only)' })
  @ApiResponse({ status: 204 })
  async delete(
    @CurrentUser() user: User,
    @Param('id') id: string,
  ): Promise<void> {
    return this.vocabulariesService.softDelete(user.id, id);
  }
}
