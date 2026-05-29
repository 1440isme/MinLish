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
import { ApiBearerAuth, ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import type { User } from '@prisma/client';
import { CurrentUser } from '../../config/common/decorators/current-user.decorator';
import { DecksService } from './decks.service';
import { DeckEntity } from './entities/deck.entity';
import { ListDecksQueryDto } from './dto/list-decks.query.dto';
import { DeckListResponseDto } from './dto/deck-list.response.dto';
import { CreateDeckDto } from './dto/create-deck.dto';
import { UpdateDeckDto } from './dto/update-deck.dto';

@ApiTags('Decks')
@ApiBearerAuth()
@Controller('decks')
@UseInterceptors(ClassSerializerInterceptor)
export class DecksController {
  constructor(private decksService: DecksService) {}

  @Get()
  @ApiOperation({ summary: 'List decks (system/user)' })
  @ApiResponse({
    status: 200,
    type: DeckListResponseDto,
  })
  async listDecks(
    @CurrentUser() user: User,
    @Query() query: ListDecksQueryDto,
  ): Promise<DeckListResponseDto> {
    const result = await this.decksService.listDecks(user.id, query);
    return {
      meta: {
        page: result.page,
        pageSize: result.pageSize,
        total: result.total,
      },
      items: result.items,
    };
  }

  @Get('favorites')
  @ApiOperation({ summary: 'Get Favorites deck of current user' })
  @ApiResponse({ status: 200, type: DeckEntity })
  async getFavoritesDeck(@CurrentUser() user: User): Promise<DeckEntity> {
    return this.decksService.getFavoritesDeck(user.id);
  }

  @Post()
  @ApiOperation({ summary: 'Create user deck' })
  @ApiResponse({ status: 201, type: DeckEntity })
  async createDeck(
    @CurrentUser() user: User,
    @Body() dto: CreateDeckDto,
  ): Promise<DeckEntity> {
    return this.decksService.createUserDeck(user.id, dto);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get deck detail' })
  @ApiResponse({ status: 200, type: DeckEntity })
  async getDeck(@CurrentUser() user: User, @Param('id') id: string): Promise<DeckEntity> {
    return this.decksService.getDeckById(user.id, id);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update user deck' })
  @ApiResponse({ status: 200, type: DeckEntity })
  async updateDeck(
    @CurrentUser() user: User,
    @Param('id') id: string,
    @Body() dto: UpdateDeckDto,
  ): Promise<DeckEntity> {
    return this.decksService.updateUserDeck(user.id, id, dto);
  }

  @Delete(':id')
  @HttpCode(204)
  @ApiOperation({ summary: 'Soft delete user deck' })
  @ApiResponse({ status: 204 })
  async deleteDeck(@CurrentUser() user: User, @Param('id') id: string): Promise<void> {
    return this.decksService.softDeleteUserDeck(user.id, id);
  }
}

