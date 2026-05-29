import {
  ClassSerializerInterceptor,
  Controller,
  Delete,
  HttpCode,
  Param,
  Post,
  UseInterceptors,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import type { User } from '@prisma/client';
import { CurrentUser } from '../../config/common/decorators/current-user.decorator';
import { FavoritesService } from './favorites.service';
import { FavoriteResponseDto } from './dto/favorite.response.dto';

@ApiTags('Favorites')
@ApiBearerAuth()
@UseInterceptors(ClassSerializerInterceptor)
@Controller('vocabularies')
export class FavoritesController {
  constructor(private favoritesService: FavoritesService) {}

  @Post(':id/favorite')
  @ApiOperation({ summary: 'Favorite a vocabulary (copy into Favorites deck)' })
  @ApiResponse({ status: 200, type: FavoriteResponseDto })
  async favorite(
    @CurrentUser() user: User,
    @Param('id') id: string,
  ): Promise<FavoriteResponseDto> {
    return this.favoritesService.favorite(user.id, id);
  }

  @Delete(':id/favorite')
  @HttpCode(204)
  @ApiOperation({ summary: 'Unfavorite a vocabulary (soft delete copy in Favorites)' })
  @ApiResponse({ status: 204 })
  async unfavorite(@CurrentUser() user: User, @Param('id') id: string): Promise<void> {
    return this.favoritesService.unfavorite(user.id, id);
  }
}

