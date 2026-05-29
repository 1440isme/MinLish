import { ApiProperty } from '@nestjs/swagger';

export type FavoriteStatus = 'added' | 'already_favorited';

export class FavoriteResponseDto {
  @ApiProperty({
    example: 'added',
    description: 'added nếu mới copy vào Favorites, already_favorited nếu đã có',
  })
  status: FavoriteStatus;

  @ApiProperty({ example: 'uuid-v4' })
  favoriteVocabularyId: string;
}

