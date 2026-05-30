import { Module } from '@nestjs/common';
import { APP_GUARD } from '@nestjs/core';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './modules/auth/auth.module';
import { UsersModule } from './modules/users/users.module';
import { DecksModule } from './modules/decks/decks.module';
import { VocabulariesModule } from './modules/vocabularies/vocabularies.module';
import { FavoritesModule } from './modules/favorites/favorites.module';
import { ImportsModule } from './modules/imports/imports.module';
import { LearningModule } from './modules/learning/learning.module';
import { JwtAuthGuard } from './config/common/guards/jwt-auth.guard';

@Module({
  imports: [
    PrismaModule,
    AuthModule,
    UsersModule,
    DecksModule,
    VocabulariesModule,
    FavoritesModule,
    ImportsModule,
    LearningModule,
  ],
  controllers: [AppController],
  providers: [
    AppService,
    {
      provide: APP_GUARD,
      useClass: JwtAuthGuard,
    },
  ],
})
export class AppModule {}
