import {
  Body,
  ClassSerializerInterceptor,
  Controller,
  Get,
  HttpCode,
  HttpStatus,
  Param,
  Post,
  Query,
  UseInterceptors,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiProperty, ApiResponse, ApiTags } from '@nestjs/swagger';
import type { User } from '@prisma/client';
import { CurrentUser } from '../../config/common/decorators/current-user.decorator';
import { PracticeService } from './practice.service';
import { CreatePracticeSessionDto } from './dto/create-practice-session.dto';
import { SubmitAnswerDto } from './dto/submit-answer.dto';
import { PracticeQuestionDto } from './dto/practice-question.dto';
import { FinishSessionResponseDto } from './dto/finish-session-response.dto';
import { IsUUID, IsString } from 'class-validator';
import { PracticeAnswerEntity, PracticeSessionEntity } from './entities/practice.entity';

export class CreateSessionResponseDto {
  @ApiProperty({ type: () => PracticeSessionEntity })
  session: PracticeSessionEntity;

  @ApiProperty({ type: [PracticeQuestionDto] })
  questions: PracticeQuestionDto[];
}

export class GetActiveSessionQueryDto {
  @ApiProperty({
    example: 'd3b07384-d113-4ec5-a5e6-ec8d10332f7a',
    description: 'UUID of the deck',
  })
  @IsString()
  @IsUUID()
  deckId: string;
}

@ApiTags('Practice')
@ApiBearerAuth()
@Controller('practice')
@UseInterceptors(ClassSerializerInterceptor)
export class PracticeController {
  constructor(private readonly practiceService: PracticeService) {}

  @Post('sessions')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create a new practice session and generate mixed questions' })
  @ApiResponse({
    status: 201,
    type: CreateSessionResponseDto,
  })
  async createSession(
    @CurrentUser() user: User,
    @Body() dto: CreatePracticeSessionDto
  ): Promise<CreateSessionResponseDto> {
    return this.practiceService.createSession(user.id, dto);
  }

  @Get('sessions/active')
  @ApiOperation({ summary: 'Check and retrieve any active practice session in progress for a deck' })
  @ApiResponse({
    status: 200,
    type: CreateSessionResponseDto,
    description: 'Returns active session details or null if none is in progress',
  })
  async getActiveSession(
    @CurrentUser() user: User,
    @Query() query: GetActiveSessionQueryDto
  ): Promise<CreateSessionResponseDto | null> {
    return this.practiceService.getActiveSession(user.id, query.deckId);
  }

  @Get('sessions/:sessionId/questions')
  @ApiOperation({ summary: 'Retrieve list of questions for a session (hides answers if in progress)' })
  @ApiResponse({
    status: 200,
    type: [PracticeQuestionDto],
  })
  async getQuestions(
    @CurrentUser() user: User,
    @Param('sessionId') sessionId: string
  ): Promise<PracticeQuestionDto[]> {
    return this.practiceService.getQuestions(user.id, sessionId);
  }

  @Post('sessions/:sessionId/answers')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Submit an answer for a specific question index' })
  @ApiResponse({
    status: 200,
    type: PracticeAnswerEntity,
  })
  async submitAnswer(
    @CurrentUser() user: User,
    @Param('sessionId') sessionId: string,
    @Body() dto: SubmitAnswerDto
  ): Promise<PracticeAnswerEntity> {
    return this.practiceService.submitAnswer(user.id, sessionId, dto);
  }

  @Post('sessions/:sessionId/finish')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Finish a session, calculate metrics, and output final score summary' })
  @ApiResponse({
    status: 200,
    type: FinishSessionResponseDto,
  })
  async finishSession(
    @CurrentUser() user: User,
    @Param('sessionId') sessionId: string
  ): Promise<FinishSessionResponseDto> {
    return this.practiceService.finishSession(user.id, sessionId);
  }

  @Get('sessions/:sessionId/results')
  @ApiOperation({ summary: 'Retrieve results of a completed practice session' })
  @ApiResponse({
    status: 200,
    type: FinishSessionResponseDto,
  })
  async getSessionResults(
    @CurrentUser() user: User,
    @Param('sessionId') sessionId: string
  ): Promise<FinishSessionResponseDto> {
    return this.practiceService.getSessionResults(user.id, sessionId);
  }

  @Post('sessions/:sessionId/cancel')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Cancel a practice session in progress' })
  @ApiResponse({
    status: 200,
    type: PracticeSessionEntity,
  })
  async cancelSession(
    @CurrentUser() user: User,
    @Param('sessionId') sessionId: string
  ): Promise<PracticeSessionEntity> {
    return this.practiceService.cancelSession(user.id, sessionId);
  }
}
