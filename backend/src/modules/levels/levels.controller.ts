import {
  Controller,
  Get,
  UseInterceptors,
  ClassSerializerInterceptor,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';
import { LevelsService } from './levels.service';
import { LearningLevelEntity } from './entities/learning-level.entity';

@ApiTags('Levels')
@Controller('levels')
export class LevelsController {
  constructor(private levelsService: LevelsService) {}

  @Get()
  @ApiBearerAuth()
  @UseInterceptors(ClassSerializerInterceptor)
  @ApiOperation({ summary: 'Lấy danh sách các levels học tập khả dụng' })
  @ApiResponse({
    status: 200,
    description: 'Danh sách các levels active',
    type: [LearningLevelEntity],
  })
  async getLevels(): Promise<LearningLevelEntity[]> {
    return this.levelsService.findAllActive();
  }
}
