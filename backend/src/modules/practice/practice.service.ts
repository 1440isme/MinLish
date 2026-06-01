import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { PrismaService } from '../../prisma/prisma.service';
import { ErrorCodes } from '../../config/common/errors/error-codes';
import { CreatePracticeSessionDto } from './dto/create-practice-session.dto';
import { SubmitAnswerDto } from './dto/submit-answer.dto';
import { PracticeQuestionDto } from './dto/practice-question.dto';
import { FinishSessionResponseDto } from './dto/finish-session-response.dto';
import {
  PracticeAnswerEntity,
  PracticeSessionEntity,
  PracticeType,
  QuestionType,
} from './entities/practice.entity';

import { AnalyticsService } from '../analytics/analytics.service';

function normalizeText(value: string): string {
  return value.trim().toLowerCase().replace(/\s+/g, ' ');
}

function hideWordInExample(word: string, example: string): string {
  const escapedWord = word.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
  const regex = new RegExp(`\\b${escapedWord}\\b`, 'gi');
  if (regex.test(example)) {
    return example.replace(regex, '_____');
  }
  const regexSub = new RegExp(escapedWord, 'gi');
  if (regexSub.test(example)) {
    return example.replace(regexSub, '_____');
  }
  return example;
}

function getDistractors(allVocabs: any[], currentVocab: any, key: 'meaning' | 'word', count = 3): string[] {
  const others = allVocabs.filter((v) => v.id !== currentVocab.id);
  const uniqueValues = Array.from(
    new Set(others.map((v) => (key === 'meaning' ? v.meaning.trim() : v.word.trim())))
  ).filter((v) => v !== (key === 'meaning' ? currentVocab.meaning.trim() : currentVocab.word.trim()));
  const shuffled = uniqueValues.sort(() => Math.random() - 0.5);
  return shuffled.slice(0, count);
}

@Injectable()
export class PracticeService {
  constructor(private prisma: PrismaService, private readonly analyticsService: AnalyticsService,) {}

  async createSession(
    userId: string,
    dto: CreatePracticeSessionDto
  ): Promise<{ session: PracticeSessionEntity; questions: PracticeQuestionDto[] }> {
    // 1. Validate deck
    const deck = await this.prisma.deck.findUnique({
      where: { id: dto.deckId },
    });
    if (!deck || deck.deletedAt) {
      throw new NotFoundException(ErrorCodes.PRACTICE_DECK_NOT_FOUND);
    }
    if (deck.deckType === 'USER' && deck.ownerUserId !== userId) {
      throw new ForbiddenException(ErrorCodes.PRACTICE_SESSION_FORBIDDEN);
    }

    // 2. Fetch vocabulary
    const scope = dto.scope ?? 'LEARNED_ONLY';
    const vocabularies = await this.prisma.vocabulary.findMany({
      where: {
        deckId: dto.deckId,
        deletedAt: null,
        ...(scope === 'LEARNED_ONLY' ? {
          reviewCards: {
            some: {
              userId,
            },
          },
        } : {}),
      },
    });

    console.log('CREATE PRACTICE SESSION DEBUG:', {
      userId,
      deckId: dto.deckId,
      deckName: deck.name,
      deckType: deck.deckType,
      scope,
      vocabCount: vocabularies.length
    });

    if (vocabularies.length === 0) {
      if (scope === 'LEARNED_ONLY') {
        throw new BadRequestException(ErrorCodes.PRACTICE_NOT_ENOUGH_LEARNED_VOCABULARY);
      }
      throw new BadRequestException(ErrorCodes.PRACTICE_NOT_ENOUGH_VOCABULARY);
    }

    // Resolve practiceTypes
    const practiceTypes = dto.practiceTypes && dto.practiceTypes.length > 0
      ? dto.practiceTypes
      : [PracticeType.MULTIPLE_CHOICE, PracticeType.FILL_IN_BLANK, PracticeType.LISTENING];

    // Validate vocab length for multiple choice/listening
    const hasMultipleChoiceOrListening = practiceTypes.some(
      (type) => type === PracticeType.MULTIPLE_CHOICE || type === PracticeType.LISTENING
    );
    if (hasMultipleChoiceOrListening && vocabularies.length < 4) {
      if (scope === 'LEARNED_ONLY') {
        throw new BadRequestException(ErrorCodes.PRACTICE_NOT_ENOUGH_LEARNED_VOCABULARY);
      }
      throw new BadRequestException(ErrorCodes.PRACTICE_NOT_ENOUGH_VOCABULARY);
    }

    // Validate totalQuestions
    let totalQuestions = dto.totalQuestions ?? 10;
    if (totalQuestions > vocabularies.length) {
      totalQuestions = vocabularies.length;
    }

    // Shuffle and pick vocabularies
    const shuffledVocabs = [...vocabularies].sort(() => Math.random() - 0.5);
    const selectedVocabs = shuffledVocabs.slice(0, totalQuestions);

    // Assign question types sequentially / mixed
    const questionsToGenerate = selectedVocabs.map((vocab, index) => {
      // Resolve which practice type
      const pType = practiceTypes[index % practiceTypes.length];
      let qType: QuestionType;
      let questionText = '';
      let options: string[] | undefined;
      let correctAnswer = '';

      if (pType === PracticeType.MULTIPLE_CHOICE) {
        // Randomly select WORD_TO_MEANING or MEANING_TO_WORD
        const isWordToMeaning = Math.random() < 0.5;
        if (isWordToMeaning) {
          qType = QuestionType.WORD_TO_MEANING;
          questionText = `What does "${vocab.word}" mean?`;
          correctAnswer = vocab.meaning.trim();
          const distractors = getDistractors(vocabularies, vocab, 'meaning', 3);
          options = [correctAnswer, ...distractors].sort(() => Math.random() - 0.5);
        } else {
          qType = QuestionType.MEANING_TO_WORD;
          questionText = `Từ nào có nghĩa là: "${vocab.meaning.trim()}"?`;
          correctAnswer = vocab.word.trim();
          const distractors = getDistractors(vocabularies, vocab, 'word', 3);
          options = [correctAnswer, ...distractors].sort(() => Math.random() - 0.5);
        }
      } else if (pType === PracticeType.FILL_IN_BLANK) {
        qType = QuestionType.FILL_IN_BLANK;
        correctAnswer = vocab.word.trim();
        if (vocab.example) {
          const hidden = hideWordInExample(vocab.word, vocab.example);
          if (hidden !== vocab.example) {
            questionText = hidden;
          } else {
            questionText = `Điền từ có nghĩa là: "${vocab.meaning.trim()}"`;
          }
        } else {
          questionText = `Điền từ có nghĩa là: "${vocab.meaning.trim()}"`;
        }
      } else {
        // LISTENING
        qType = QuestionType.LISTENING_WORD;
        questionText = 'Nghe và chọn nghĩa đúng';
        correctAnswer = vocab.meaning.trim();
        const distractors = getDistractors(vocabularies, vocab, 'meaning', 3);
        options = [correctAnswer, ...distractors].sort(() => Math.random() - 0.5);
      }

      return {
        vocabularyId: vocab.id,
        questionType: qType,
        questionText,
        options,
        correctAnswer,
      };
    });

    // Create session & answers in a transaction
    const session = await this.prisma.$transaction(async (tx) => {
      const newSession = await tx.practiceSession.create({
        data: {
          userId,
          deckId: dto.deckId,
          //practiceType: practiceTypes[0],
          practiceType: practiceTypes.length > 1 ? ('MIXED' as any) : practiceTypes[0],
          totalQuestions,
          status: 'IN_PROGRESS',
        },
      });

      const answersData = questionsToGenerate.map((q) => ({
        sessionId: newSession.id,
        userId,
        vocabularyId: q.vocabularyId,
        questionType: q.questionType,
        questionText: q.questionText,
        optionsJson: q.options ? (q.options as any) : Prisma.DbNull,
        userAnswer: null,
        correctAnswer: q.correctAnswer,
        isCorrect: false,
      }));

      await tx.practiceAnswer.createMany({
        data: answersData,
      });

      return newSession;
    });

    const questions = await this.getQuestions(userId, session.id);

    return {
      session: new PracticeSessionEntity(session as any),
      questions,
    };
  }

  async getActiveSession(userId: string, deckId: string): Promise<any | null> {
    const session = await this.prisma.practiceSession.findFirst({
      where: {
        userId,
        deckId,
        status: 'IN_PROGRESS',
      },
      orderBy: {
        startedAt: 'desc',
      },
    });

    if (!session) {
      return null;
    }

    const questions = await this.getQuestions(userId, session.id);

    return {
      session: new PracticeSessionEntity(session as any),
      questions,
    };
  }

  async getQuestions(userId: string, sessionId: string): Promise<PracticeQuestionDto[]> {
    const session = await this.prisma.practiceSession.findUnique({
      where: { id: sessionId },
    });
    if (!session) {
      throw new NotFoundException(ErrorCodes.PRACTICE_SESSION_NOT_FOUND);
    }
    if (session.userId !== userId) {
      throw new ForbiddenException(ErrorCodes.PRACTICE_SESSION_FORBIDDEN);
    }

    const answers = await this.prisma.practiceAnswer.findMany({
      where: { sessionId },
      orderBy: { createdAt: 'asc' },
    });

    return answers.map((answer, index) => ({
      index,
      questionType: answer.questionType as QuestionType,
      questionText: answer.questionText,
      options: answer.optionsJson ? (answer.optionsJson as string[]) : undefined,
      vocabularyId: answer.vocabularyId,
      answered: answer.userAnswer !== null,
    }));
  }

  async submitAnswer(
    userId: string,
    sessionId: string,
    dto: SubmitAnswerDto
  ): Promise<PracticeAnswerEntity> {
    const session = await this.prisma.practiceSession.findUnique({
      where: { id: sessionId },
    });
    if (!session) {
      throw new NotFoundException(ErrorCodes.PRACTICE_SESSION_NOT_FOUND);
    }
    if (session.userId !== userId) {
      throw new ForbiddenException(ErrorCodes.PRACTICE_SESSION_FORBIDDEN);
    }
    if (session.status !== 'IN_PROGRESS') {
      throw new BadRequestException(ErrorCodes.PRACTICE_SESSION_ALREADY_COMPLETED);
    }

    const answers = await this.prisma.practiceAnswer.findMany({
      where: { sessionId },
      orderBy: { createdAt: 'asc' },
    });

    if (dto.questionIndex < 0 || dto.questionIndex >= answers.length) {
      throw new NotFoundException(ErrorCodes.PRACTICE_QUESTION_NOT_FOUND);
    }

    const answer = answers[dto.questionIndex];
    if (answer.userAnswer !== null) {
      throw new BadRequestException(ErrorCodes.PRACTICE_ANSWER_ALREADY_SUBMITTED);
    }

    // Grade answer
    let isCorrect = false;
    const rawUserAnswer = dto.userAnswer;
    let normalizedUserAnswer: string | null = null;

    if (rawUserAnswer !== undefined && rawUserAnswer !== null && rawUserAnswer.trim() !== '') {
      normalizedUserAnswer = rawUserAnswer.trim();
      const normUser = normalizeText(normalizedUserAnswer);
      const normCorrect = normalizeText(answer.correctAnswer);
      isCorrect = normUser === normCorrect;
    } else {
      normalizedUserAnswer = null;
      isCorrect = false;
    }

    // Update PracticeAnswer
    const updatedAnswer = await this.prisma.practiceAnswer.update({
      where: { id: answer.id },
      data: {
        userAnswer: normalizedUserAnswer,
        isCorrect,
        answeredAt: new Date(),
      },
    });

    return new PracticeAnswerEntity(updatedAnswer as any);
  }

  async finishSession(userId: string, sessionId: string): Promise<FinishSessionResponseDto> {
    const session = await this.prisma.practiceSession.findUnique({
      where: { id: sessionId },
    });
    if (!session) {
      throw new NotFoundException(ErrorCodes.PRACTICE_SESSION_NOT_FOUND);
    }
    if (session.userId !== userId) {
      throw new ForbiddenException(ErrorCodes.PRACTICE_SESSION_FORBIDDEN);
    }
    if (session.status !== 'IN_PROGRESS') {
      throw new BadRequestException(ErrorCodes.PRACTICE_SESSION_ALREADY_COMPLETED);
    }

    const answers = await this.prisma.practiceAnswer.findMany({
      where: { sessionId },
      orderBy: { createdAt: 'asc' },
    });

    const totalQuestions = session.totalQuestions;
    const correctAnswers = answers.filter((a) => a.isCorrect).length;
    const unanswered = answers.filter((a) => a.userAnswer === null).length;
    const wrongAnswers = totalQuestions - correctAnswers - unanswered;
    const accuracy = totalQuestions > 0 ? (correctAnswers / totalQuestions) * 100 : 0;

    const finishedAt = new Date();
    const timeTakenSeconds = Math.max(0, Math.floor((finishedAt.getTime() - session.startedAt.getTime()) / 1000));

    const updatedSession = await this.prisma.practiceSession.update({
      where: { id: sessionId },
      data: {
        status: 'COMPLETED',
        finishedAt,
        correctAnswers,
        wrongAnswers,
        accuracy,
      },
    });

    //--------------
    // Nhúng code tự động chuyển số liệu sang bảng daily_activity  ( của dev E)
    await this.analyticsService.trackPracticeActivity(userId, {
      practiceSessionsCount: 1,
      correctCount: correctAnswers, // Lấy biến correctAnswers  tính sẵn ở trên
      wrongCount: wrongAnswers,     // Lấy biến wrongAnswers tính sẵn ở trên
      totalLearningSeconds: timeTakenSeconds, // Lấy số giây làm bài tính sẵn ở trên
    });


    return {
      session: new PracticeSessionEntity(updatedSession as any),
      answers: answers.map((a) => new PracticeAnswerEntity(a as any)),
      summary: {
        totalQuestions,
        correctAnswers,
        wrongAnswers,
        unanswered,
        accuracy,
        timeTakenSeconds,
      },
    };
  }

  async getSessionResults(userId: string, sessionId: string): Promise<FinishSessionResponseDto> {
    const session = await this.prisma.practiceSession.findUnique({
      where: { id: sessionId },
    });
    if (!session) {
      throw new NotFoundException(ErrorCodes.PRACTICE_SESSION_NOT_FOUND);
    }
    if (session.userId !== userId) {
      throw new ForbiddenException(ErrorCodes.PRACTICE_SESSION_FORBIDDEN);
    }

    const answers = await this.prisma.practiceAnswer.findMany({
      where: { sessionId },
      orderBy: { createdAt: 'asc' },
    });

    const totalQuestions = session.totalQuestions;
    const correctAnswers = answers.filter((a) => a.isCorrect).length;
    const unanswered = answers.filter((a) => a.userAnswer === null).length;
    const wrongAnswers = totalQuestions - correctAnswers - unanswered;
    const accuracy = totalQuestions > 0 ? (correctAnswers / totalQuestions) * 100 : 0;

    const finishedAt = session.finishedAt || new Date();
    const timeTakenSeconds = Math.max(0, Math.floor((finishedAt.getTime() - session.startedAt.getTime()) / 1000));

    return {
      session: new PracticeSessionEntity(session as any),
      answers: answers.map((a) => new PracticeAnswerEntity(a as any)),
      summary: {
        totalQuestions,
        correctAnswers,
        wrongAnswers,
        unanswered,
        accuracy,
        timeTakenSeconds,
      },
    };
  }

  async cancelSession(userId: string, sessionId: string): Promise<PracticeSessionEntity> {
    const session = await this.prisma.practiceSession.findUnique({
      where: { id: sessionId },
    });
    if (!session) {
      throw new NotFoundException(ErrorCodes.PRACTICE_SESSION_NOT_FOUND);
    }
    if (session.userId !== userId) {
      throw new ForbiddenException(ErrorCodes.PRACTICE_SESSION_FORBIDDEN);
    }
    if (session.status !== 'IN_PROGRESS') {
      throw new BadRequestException(ErrorCodes.PRACTICE_SESSION_ALREADY_COMPLETED);
    }

    const updatedSession = await this.prisma.practiceSession.update({
      where: { id: sessionId },
      data: {
        status: 'CANCELLED',
        finishedAt: new Date(),
      },
    });

    return new PracticeSessionEntity(updatedSession as any);
  }
}
