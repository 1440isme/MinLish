import { ApiProperty } from '@nestjs/swagger';
import { PaginatedResponseDto, PaginationMetaDto } from '../../../config/common/dto/pagination.dto';
import { DeckEntity } from '../entities/deck.entity';

export class DeckListResponseDto extends PaginatedResponseDto<DeckEntity> {
  @ApiProperty({
    type: PaginationMetaDto,
  })
  declare meta: PaginationMetaDto;

  @ApiProperty({
    type: DeckEntity,
    isArray: true,
  })
  declare items: DeckEntity[];
}

