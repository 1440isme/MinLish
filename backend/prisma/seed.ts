// =============================================================
// MinLish - Prisma Seed
// Run: npx prisma db seed
// =============================================================

import 'dotenv/config';
import { PrismaClient } from '@prisma/client';
import { PrismaMariaDb } from '@prisma/adapter-mariadb';

function parseDatabaseUrl(url: string) {
  const parsed = new URL(url);
  return {
    host: parsed.hostname,
    port: parseInt(parsed.port || '3306', 10),
    user: decodeURIComponent(parsed.username),
    password: decodeURIComponent(parsed.password),
    database: parsed.pathname.replace(/^\//, ''),
    connectionLimit: 5,
  };
}

const databaseUrl = process.env.DATABASE_URL || 'mysql://root:root@localhost:3306/minlish';
const adapter = new PrismaMariaDb(parseDatabaseUrl(databaseUrl));
const prisma = new PrismaClient({ adapter });

async function main() {
  console.log('🌱 Seeding MinLish database...');

  // ------------------------------------------------------------------
  // 1. Learning Paths
  // ------------------------------------------------------------------
  const toeic = await prisma.learningPath.upsert({
    where: { code: 'TOEIC' },
    update: {},
    create: {
      id: '11111111-1111-1111-1111-111111111111',
      code: 'TOEIC',
      name: 'TOEIC',
      description: 'Lộ trình học từ vựng TOEIC',
      displayOrder: 1,
      isActive: true,
    },
  });

  const ielts = await prisma.learningPath.upsert({
    where: { code: 'IELTS' },
    update: {},
    create: {
      id: '22222222-2222-2222-2222-222222222222',
      code: 'IELTS',
      name: 'IELTS',
      description: 'Lộ trình học từ vựng IELTS',
      displayOrder: 2,
      isActive: true,
    },
  });

  console.log('✅ Learning paths seeded:', toeic.code, ielts.code);

  // ------------------------------------------------------------------
  // 2. Learning Levels - TOEIC
  // ------------------------------------------------------------------
  const toeicLevels = [
    {
      id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
      code: 'TOEIC_450',
      name: 'TOEIC 450+',
      description: 'Từ vựng nền tảng cho mục tiêu TOEIC 450+',
      minScore: 0,
      maxScore: 450,
      displayOrder: 1,
    },
    {
      id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
      code: 'TOEIC_600',
      name: 'TOEIC 600+',
      description: 'Từ vựng trung cấp cho mục tiêu TOEIC 600+',
      minScore: 451,
      maxScore: 600,
      displayOrder: 2,
    },
    {
      id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3',
      code: 'TOEIC_750',
      name: 'TOEIC 750+',
      description: 'Từ vựng nâng cao cho mục tiêu TOEIC 750+',
      minScore: 601,
      maxScore: 750,
      displayOrder: 3,
    },
    {
      id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4',
      code: 'TOEIC_900',
      name: 'TOEIC 900+',
      description: 'Từ vựng nâng cao cho mục tiêu TOEIC 900+',
      minScore: 751,
      maxScore: 990,
      displayOrder: 4,
    },
  ];

  for (const level of toeicLevels) {
    await prisma.learningLevel.upsert({
      where: {
        learningPathId_code: { learningPathId: toeic.id, code: level.code },
      },
      update: {},
      create: {
        ...level,
        learningPathId: toeic.id,
        isActive: true,
      },
    });
  }

  // ------------------------------------------------------------------
  // 3. Learning Levels - IELTS
  // ------------------------------------------------------------------
  const ieltsLevels = [
    {
      id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1',
      code: 'IELTS_4_0',
      name: 'IELTS 4.0+',
      description: 'Từ vựng nền tảng cho IELTS 4.0+',
      minScore: 0,
      maxScore: 4.0,
      displayOrder: 1,
    },
    {
      id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2',
      code: 'IELTS_5_5',
      name: 'IELTS 5.5+',
      description: 'Từ vựng trung cấp cho IELTS 5.5+',
      minScore: 4.5,
      maxScore: 5.5,
      displayOrder: 2,
    },
    {
      id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3',
      code: 'IELTS_6_5',
      name: 'IELTS 6.5+',
      description: 'Từ vựng học thuật cho IELTS 6.5+',
      minScore: 6.0,
      maxScore: 6.5,
      displayOrder: 3,
    },
    {
      id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4',
      code: 'IELTS_7_0',
      name: 'IELTS 7.0+',
      description: 'Từ vựng nâng cao cho IELTS 7.0+',
      minScore: 7.0,
      maxScore: 9.0,
      displayOrder: 4,
    },
  ];

  for (const level of ieltsLevels) {
    await prisma.learningLevel.upsert({
      where: {
        learningPathId_code: { learningPathId: ielts.id, code: level.code },
      },
      update: {},
      create: {
        ...level,
        learningPathId: ielts.id,
        isActive: true,
      },
    });
  }

  console.log('✅ Learning levels seeded (TOEIC + IELTS)');

  // ------------------------------------------------------------------
  // 4. System Decks
  // ------------------------------------------------------------------
  const systemDecks = [
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd01',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
      name: 'TOEIC 450 - Daily Life',
      normalizedName: 'toeic 450 - daily life',
      description: 'Bộ từ vựng TOEIC nền tảng về đời sống hằng ngày.',
      tags: ['TOEIC', 'Daily Life', 'Beginner'],
      displayOrder: 1,
    },
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd02',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
      name: 'TOEIC 600 - Office & Business',
      normalizedName: 'toeic 600 - office & business',
      description: 'Bộ từ vựng TOEIC về công sở và kinh doanh.',
      tags: ['TOEIC', 'Business', 'Office'],
      displayOrder: 2,
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2',
      name: 'IELTS 5.5 - Common Topics',
      normalizedName: 'ielts 5.5 - common topics',
      description: 'Bộ từ vựng IELTS theo các chủ đề phổ biến.',
      tags: ['IELTS', 'Common Topics', 'Intermediate'],
      displayOrder: 1,
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3',
      name: 'IELTS 6.5 - Academic Vocabulary',
      normalizedName: 'ielts 6.5 - academic vocabulary',
      description: 'Bộ từ vựng học thuật cho IELTS band 6.5+.',
      tags: ['IELTS', 'Academic', 'Advanced'],
      displayOrder: 2,
    },
  ];

  for (const deck of systemDecks) {
    await prisma.deck.upsert({
      where: { id: deck.id },
      update: {},
      create: {
        ...deck,
        ownerUserId: null,
        deckType: 'SYSTEM',
        visibility: 'PUBLIC',
        isDefault: false,
        totalWords: 0,
      },
    });
  }

  console.log('✅ System decks seeded');

  // ------------------------------------------------------------------
  // 5. Sample Vocabularies
  // ------------------------------------------------------------------
  const vocabularies = [
    {
      id: 'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv001',
      deckId: 'dddddddd-dddd-dddd-dddd-dddddddddd01',
      word: 'appointment',
      normalizedWord: 'appointment',
      pronunciation: '/əˈpɔɪnt.mənt/',
      meaning: 'cuộc hẹn',
      normalizedMeaning: 'cuộc hẹn',
      descriptionEn: 'An arrangement to meet someone at a particular time and place.',
      example: 'I have an appointment with the manager at 9 a.m.',
      collocation: 'make an appointment; schedule an appointment; doctor appointment',
      relatedWords: 'meeting; schedule; arrangement',
      note: 'Common in TOEIC office and daily life contexts.',
      difficulty: 'MEDIUM' as const,
      partOfSpeech: 'noun',
    },
    {
      id: 'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv002',
      deckId: 'dddddddd-dddd-dddd-dddd-dddddddddd01',
      word: 'purchase',
      normalizedWord: 'purchase',
      pronunciation: '/ˈpɝː.tʃəs/',
      meaning: 'mua hàng; sự mua hàng',
      normalizedMeaning: 'mua hàng; sự mua hàng',
      descriptionEn: 'To buy something.',
      example: 'Customers can purchase tickets online.',
      collocation: 'make a purchase; purchase order; purchase price',
      relatedWords: 'buy; order; payment',
      difficulty: 'EASY' as const,
      partOfSpeech: 'verb/noun',
    },
    {
      id: 'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv003',
      deckId: 'dddddddd-dddd-dddd-dddd-dddddddddd01',
      word: 'receipt',
      normalizedWord: 'receipt',
      pronunciation: '/rɪˈsiːt/',
      meaning: 'hóa đơn; biên lai',
      normalizedMeaning: 'hóa đơn; biên lai',
      descriptionEn: 'A piece of paper or digital record showing that money has been paid.',
      example: 'Please keep your receipt for future reference.',
      collocation: 'sales receipt; keep a receipt; receipt number',
      relatedWords: 'invoice; bill; payment',
      difficulty: 'EASY' as const,
      partOfSpeech: 'noun',
    },
    {
      id: 'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv101',
      deckId: 'dddddddd-dddd-dddd-dddd-dddddddddd02',
      word: 'deadline',
      normalizedWord: 'deadline',
      pronunciation: '/ˈded.laɪn/',
      meaning: 'hạn chót',
      normalizedMeaning: 'hạn chót',
      descriptionEn: 'The latest time or date by which something should be completed.',
      example: 'The deadline for the report is Friday.',
      collocation: 'meet a deadline; miss a deadline; tight deadline',
      relatedWords: 'due date; schedule; timeline',
      difficulty: 'MEDIUM' as const,
      partOfSpeech: 'noun',
    },
    {
      id: 'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv102',
      deckId: 'dddddddd-dddd-dddd-dddd-dddddddddd02',
      word: 'negotiate',
      normalizedWord: 'negotiate',
      pronunciation: '/nəˈɡoʊ.ʃi.eɪt/',
      meaning: 'đàm phán',
      normalizedMeaning: 'đàm phán',
      descriptionEn: 'To discuss something in order to reach an agreement.',
      example: 'The company will negotiate a new contract.',
      collocation: 'negotiate a contract; negotiate terms; negotiate with clients',
      relatedWords: 'discuss; bargain; agree',
      difficulty: 'HARD' as const,
      partOfSpeech: 'verb',
    },
    {
      id: 'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv201',
      deckId: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01',
      word: 'environment',
      normalizedWord: 'environment',
      pronunciation: '/ɪnˈvaɪ.rən.mənt/',
      meaning: 'môi trường',
      normalizedMeaning: 'môi trường',
      descriptionEn: 'The air, water, and land in or on which people, animals, and plants live.',
      example: 'Many governments are taking action to protect the environment.',
      collocation: 'protect the environment; natural environment; environmental issue',
      relatedWords: 'nature; ecosystem; surroundings',
      difficulty: 'EASY' as const,
      partOfSpeech: 'noun',
    },
    {
      id: 'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv202',
      deckId: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01',
      word: 'significant',
      normalizedWord: 'significant',
      pronunciation: '/sɪɡˈnɪf.ə.kənt/',
      meaning: 'đáng kể; quan trọng',
      normalizedMeaning: 'đáng kể; quan trọng',
      descriptionEn: 'Important or large enough to be noticed.',
      example: 'There has been a significant increase in online learning.',
      collocation: 'significant impact; significant change; statistically significant',
      relatedWords: 'important; notable; considerable',
      difficulty: 'MEDIUM' as const,
      partOfSpeech: 'adjective',
    },
    {
      id: 'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv301',
      deckId: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02',
      word: 'sustainable',
      normalizedWord: 'sustainable',
      pronunciation: '/səˈsteɪ.nə.bəl/',
      meaning: 'bền vững',
      normalizedMeaning: 'bền vững',
      descriptionEn:
        'Able to continue over a period of time without damaging the environment or using too many resources.',
      example: 'Countries need to develop sustainable energy sources.',
      collocation: 'sustainable development; sustainable growth; sustainable energy',
      relatedWords: 'renewable; long-term; eco-friendly',
      note: 'Common in IELTS Writing Task 2.',
      difficulty: 'HARD' as const,
      partOfSpeech: 'adjective',
    },
  ];

  for (const vocab of vocabularies) {
    await prisma.vocabulary.upsert({
      where: { id: vocab.id },
      update: {},
      create: vocab,
    });
  }

  // Update total_words for each seeded deck
  for (const deck of systemDecks) {
    const count = await prisma.vocabulary.count({
      where: { deckId: deck.id, deletedAt: null },
    });
    await prisma.deck.update({
      where: { id: deck.id },
      data: { totalWords: count },
    });
  }

  console.log('✅ Sample vocabularies seeded');
  console.log('🎉 Seed completed successfully!');
}

main()
  .catch((e) => {
    console.error('❌ Seed failed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
