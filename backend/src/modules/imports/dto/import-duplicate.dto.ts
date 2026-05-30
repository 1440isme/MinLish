import { ApiProperty } from '@nestjs/swagger';

export class ImportDuplicateDto {
  @ApiProperty({ example: 12 })
  row: number;

  @ApiProperty({ example: 'appointment' })
  word: string;

  @ApiProperty({ example: 'cuộc hẹn' })
  meaning: string;

  @ApiProperty({ example: 'Duplicate in this deck' })
  reason: string;
}
