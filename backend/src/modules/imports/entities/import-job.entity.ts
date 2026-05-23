import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose } from 'class-transformer';

// ----------------------------------------------------------------
// Enums
// ----------------------------------------------------------------

export enum ImportFileType {
  CSV = 'CSV',
  XLSX = 'XLSX',
}

export enum ImportStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  PARTIAL_SUCCESS = 'PARTIAL_SUCCESS',
}

// ----------------------------------------------------------------
// ImportJob Entity
// ----------------------------------------------------------------

export class ImportJobEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-user' })
  @Expose()
  userId: string;

  @ApiProperty({ example: 'uuid-deck' })
  @Expose()
  deckId: string;

  @ApiProperty({ example: 'vocabulary_batch.csv' })
  @Expose()
  fileName: string;

  @ApiProperty({
    enum: ImportFileType,
    example: ImportFileType.CSV,
  })
  @Expose()
  fileType: ImportFileType;

  @ApiProperty({
    enum: ImportStatus,
    example: ImportStatus.COMPLETED,
  })
  @Expose()
  status: ImportStatus;

  @ApiProperty({
    example: 100,
    description: 'Tổng số dòng trong file',
  })
  @Expose()
  totalRows: number;

  @ApiProperty({
    example: 95,
    description: 'Số dòng import thành công',
  })
  @Expose()
  successRows: number;

  @ApiProperty({
    example: 3,
    description: 'Số dòng trùng, bị skip',
  })
  @Expose()
  duplicateRows: number;

  @ApiProperty({
    example: 2,
    description: 'Số dòng lỗi',
  })
  @Expose()
  failedRows: number;

  @ApiPropertyOptional({
    example: { errors: [], duplicates: [] },
    description: 'Chi tiết lỗi từng dòng',
  })
  @Expose()
  errorReportJson?: Record<string, any> | null;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  startedAt?: Date | null;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  finishedAt?: Date | null;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  @ApiProperty({ type: Date })
  @Expose()
  updatedAt: Date;

  constructor(partial: Partial<ImportJobEntity>) {
    Object.assign(this, partial);
  }
}
