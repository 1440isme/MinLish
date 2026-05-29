import { ApiProperty } from '@nestjs/swagger';

export class ImportErrorDto {
  @ApiProperty({ example: 20 })
  row: number;

  @ApiProperty({ example: 'meaning' })
  field: string;

  @ApiProperty({ example: 'Meaning is required' })
  message: string;
}

