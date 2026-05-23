import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Expose } from 'class-transformer';

export class RefreshTokenEntity {
  @ApiProperty({ example: 'uuid-v4' })
  @Expose()
  id: string;

  @ApiProperty({ example: 'uuid-user' })
  @Expose()
  userId: string;

  /** tokenHash is intentionally NOT exposed */
  tokenHash: string;

  @ApiProperty({ type: Date })
  @Expose()
  expiresAt: Date;

  @ApiPropertyOptional({
    type: Date,
    nullable: true,
  })
  @Expose()
  revokedAt?: Date | null;

  @ApiProperty({ type: Date })
  @Expose()
  createdAt: Date;

  constructor(partial: Partial<RefreshTokenEntity>) {
    Object.assign(this, partial);
  }
}
