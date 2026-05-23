import {
  Injectable,
  OnModuleInit,
  OnModuleDestroy,
  Logger,
} from '@nestjs/common';
import { PrismaClient } from '@prisma/client';
import { PrismaMariaDb } from '@prisma/adapter-mariadb';

/**
 * Parse a MySQL connection URL into mariadb connection options.
 * Format: mysql://USER:PASSWORD@HOST:PORT/DATABASE
 */
function parseDatabaseUrl(url: string) {
  const parsed = new URL(url);
  return {
    host: parsed.hostname,
    port: parseInt(parsed.port || '3306', 10),
    user: decodeURIComponent(parsed.username),
    password: decodeURIComponent(parsed.password),
    database: parsed.pathname.replace(/^\//, ''),
    connectionLimit: 10,
    allowPublicKeyRetrieval: true,
  };
}

@Injectable()
export class PrismaService
  extends PrismaClient
  implements OnModuleInit, OnModuleDestroy
{
  private readonly logger = new Logger(PrismaService.name);

  constructor() {
    const databaseUrl =
      process.env.DATABASE_URL ||
      'mysql://root:root@localhost:3306/minlish';

    const connectionOpts = parseDatabaseUrl(databaseUrl);
    const adapter = new PrismaMariaDb(connectionOpts);

    super({ adapter });
  }

  async onModuleInit(): Promise<void> {
    await this.$connect();
    this.logger.log('✅ Database connected (minlish @ MySQL)');
  }

  async onModuleDestroy(): Promise<void> {
    await this.$disconnect();
    this.logger.log('Database disconnected');
  }

  /**
   * Generic soft-delete helper.
   * Usage: await this.prisma.softDelete(this.prisma.user, id)
   */
  softDelete<T extends { update: (args: any) => Promise<any> }>(
    delegate: T,
    id: string,
  ): Promise<any> {
    return (delegate.update as any)({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }
}
