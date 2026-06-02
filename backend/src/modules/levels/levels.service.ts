import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { LearningLevelEntity } from './entities/learning-level.entity';
import { LearningPathEntity } from '../learning-paths/entities/learning-path.entity';

@Injectable()
export class LevelsService {
  constructor(private prisma: PrismaService) {}

  async findAllActive(): Promise<LearningLevelEntity[]> {
    const levels = await this.prisma.learningLevel.findMany({
      where: {
        isActive: true,
      },
      include: {
        learningPath: true,
      },
      orderBy: [
        {
          learningPath: {
            displayOrder: 'asc',
          },
        },
        {
          displayOrder: 'asc',
        },
      ],
    });

    return levels.map((level) => {
      return new LearningLevelEntity({
        ...level,
        minScore: level.minScore ? Number(level.minScore) : null,
        maxScore: level.maxScore ? Number(level.maxScore) : null,
        learningPath: level.learningPath ? new LearningPathEntity(level.learningPath) : undefined,
      });
    });
  }
}
