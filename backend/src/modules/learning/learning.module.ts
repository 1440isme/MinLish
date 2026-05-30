import { Module } from '@nestjs/common';
import { LearningController } from './learning.controller';
import { LearningService } from './learning.service';
import { Sm2Service } from './sm2.service';

@Module({
  controllers: [LearningController],
  providers: [LearningService, Sm2Service],
  exports: [LearningService],
})
export class LearningModule {}
