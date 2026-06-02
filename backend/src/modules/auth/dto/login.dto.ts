import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsString, MinLength } from 'class-validator';

export class LoginDto {
  @ApiProperty({
    example: 'user@example.com',
    description: 'Email của người dùng',
  })
  @IsEmail({}, { message: 'Email không hợp lệ' })
  email: string;

  @ApiProperty({
    example: 'Password123',
    description: 'Mật khẩu',
  })
  @IsString()
  @MinLength(1, { message: 'Mật khẩu không được để trống' })
  password: string;
}
