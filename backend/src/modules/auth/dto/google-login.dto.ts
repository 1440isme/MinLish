import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString } from 'class-validator';

export class GoogleLoginDto {
  @ApiProperty({
    example: 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjEifQ...',
    description: 'Google OAuth ID Token nhận từ SDK client',
  })
  @IsString()
  @IsNotEmpty({ message: 'idToken không được để trống' })
  idToken: string;
}
