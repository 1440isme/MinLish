import {
  Body,
  ClassSerializerInterceptor,
  Controller,
  Get,
  Param,
  Post,
  Query,
  UseInterceptors,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import type { User } from '@prisma/client';
import { CurrentUser } from '../../config/common/decorators/current-user.decorator';
import { DailyPlanResponseDto } from './dto/daily-plan-response.dto';
import { DueCardsResponseDto } from './dto/due-cards-response.dto';
import { GetDueCardsQueryDto } from './dto/get-due-cards-query.dto';
import { ReviewCardResponseDto } from './dto/review-card-response.dto';
import { ReviewHistoryQueryDto } from './dto/review-history-query.dto';
import { ReviewHistoryResponseDto } from './dto/review-history-response.dto';
import { StartLearningDeckQueryDto } from './dto/start-learning-deck-query.dto';
import { SubmitReviewDto } from './dto/submit-review.dto';
import { SubmitReviewResponseDto } from './dto/submit-review-response.dto';
import { LearningService } from './learning.service';

@ApiTags('Learning')
@ApiBearerAuth()
@Controller('learning')
@UseInterceptors(ClassSerializerInterceptor)
export class LearningController {
  constructor(private readonly learningService: LearningService) {}

  @Post('decks/:deckId/start')
  @ApiOperation({ summary: 'Start learning a deck in flashcard mode' })
  @ApiResponse({ status: 201, type: DailyPlanResponseDto })
  async startDeck(
    @CurrentUser() user: User,
    @Param('deckId') deckId: string,
    @Query() query: StartLearningDeckQueryDto,
  ): Promise<DailyPlanResponseDto> {
    return this.learningService.startDeck(user, deckId, query);
  }

  @Post('vocabularies/:vocabularyId/start')
  @ApiOperation({ summary: 'Ensure review card exists for a vocabulary' })
  @ApiResponse({ status: 201, type: ReviewCardResponseDto })
  async startVocabulary(
    @CurrentUser() user: User,
    @Param('vocabularyId') vocabularyId: string,
  ): Promise<ReviewCardResponseDto> {
    return this.learningService.ensureReviewCardExists(user, vocabularyId);
  }

  @Get('daily-plan')
  @ApiOperation({ summary: 'Get daily learning plan' })
  @ApiResponse({ status: 200, type: DailyPlanResponseDto })
  async getDailyPlan(
    @CurrentUser() user: User,
    @Query() query: GetDueCardsQueryDto,
  ): Promise<DailyPlanResponseDto> {
    return this.learningService.getDailyPlan(user, query);
  }

  @Get('due')
  @ApiOperation({ summary: 'Get due review cards' })
  @ApiResponse({ status: 200, type: DueCardsResponseDto })
  async getDueCards(
    @CurrentUser() user: User,
    @Query() query: GetDueCardsQueryDto,
  ): Promise<DueCardsResponseDto> {
    return this.learningService.getDueCards(user, query);
  }

  @Post('review')
  @ApiOperation({ summary: 'Submit a flashcard review result' })
  @ApiResponse({ status: 201, type: SubmitReviewResponseDto })
  async submitReview(
    @CurrentUser() user: User,
    @Body() dto: SubmitReviewDto,
  ): Promise<SubmitReviewResponseDto> {
    return this.learningService.submitReview(user, dto);
  }

  @Get('history')
  @ApiOperation({ summary: 'Get review history for analytics/dashboard' })
  @ApiResponse({ status: 200, type: ReviewHistoryResponseDto })
  async getReviewHistory(
    @CurrentUser() user: User,
    @Query() query: ReviewHistoryQueryDto,
  ): Promise<ReviewHistoryResponseDto> {
    return this.learningService.getReviewHistory(user, query);
  }
}
