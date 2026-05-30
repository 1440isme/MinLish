import { ApiProperty } from '@nestjs/swagger';
import { ImportDuplicateDto } from './import-duplicate.dto';
import { ImportErrorDto } from './import-error.dto';

export type ImportJobStatus = 'COMPLETED' | 'FAILED' | 'PARTIAL_SUCCESS';

export class ImportCsvResponseDto {
  @ApiProperty({ example: 'uuid-v4' })
  importJobId: string;

  @ApiProperty({ example: 5000 })
  totalRows: number;

  @ApiProperty({ example: 4800 })
  successRows: number;

  @ApiProperty({ example: 150 })
  duplicateRows: number;

  @ApiProperty({ example: 50 })
  failedRows: number;

  @ApiProperty({
    example: 'PARTIAL_SUCCESS',
    description: 'COMPLETED nếu all-success, PARTIAL_SUCCESS nếu có skip/error',
  })
  status: ImportJobStatus;

  @ApiProperty({
    type: ImportDuplicateDto,
    isArray: true,
  })
  duplicates: ImportDuplicateDto[];

  @ApiProperty({
    type: ImportErrorDto,
    isArray: true,
  })
  errors: ImportErrorDto[];
}
