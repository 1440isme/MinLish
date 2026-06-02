import { ApiProperty } from '@nestjs/swagger';
import { IsString, MinLength } from 'class-validator';

export class RefreshTokenDto {
  @ApiProperty({
    example: 'a1b2c3d4e5f6...',
    description: 'Refresh token',
  })
  @IsString()
  @MinLength(1, { message: 'Refresh token không được để trống' })
  refreshToken: string;
}
