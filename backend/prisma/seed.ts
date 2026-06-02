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
    allowPublicKeyRetrieval: true,
  };
}

const databaseUrl = process.env.DATABASE_URL || 'mysql://root:root@localhost:3306/minlish';
const adapter = new PrismaMariaDb(parseDatabaseUrl(databaseUrl));
const prisma = new PrismaClient({ adapter });

type SeedDifficulty = 'EASY' | 'MEDIUM' | 'HARD';

type SeedVocabularyInput = {
  id?: string;
  word: string;
  pronunciation: string;
  meaning: string;
  partOfSpeech: string;
  difficulty: SeedDifficulty;
  descriptionEn: string;
  example: string;
  collocation: string;
  relatedWords: string;
  note?: string;
};

type SeedDeckSpec = {
  id: string;
  learningLevelId: string;
  name: string;
  description: string;
  tags: string[];
  displayOrder: number;
  vocabularies: SeedVocabularyInput[];
};

function normalizeText(value: string): string {
  return value.trim().toLowerCase().replace(/\s+/g, ' ');
}

function v(
  word: string,
  pronunciation: string,
  meaning: string,
  partOfSpeech: string,
  difficulty: SeedDifficulty,
  descriptionEn: string,
  example: string,
  collocation: string,
  relatedWords: string,
  note?: string,
): SeedVocabularyInput {
  return {
    word,
    pronunciation,
    meaning,
    partOfSpeech,
    difficulty,
    descriptionEn,
    example,
    collocation,
    relatedWords,
    note,
  };
}

function vx(
  id: string,
  word: string,
  pronunciation: string,
  meaning: string,
  partOfSpeech: string,
  difficulty: SeedDifficulty,
  descriptionEn: string,
  example: string,
  collocation: string,
  relatedWords: string,
  note?: string,
): SeedVocabularyInput {
  return {
    id,
    ...v(
      word,
      pronunciation,
      meaning,
      partOfSpeech,
      difficulty,
      descriptionEn,
      example,
      collocation,
      relatedWords,
      note,
    ),
  };
}

function generateVocabularyId(deckIndex: number, vocabIndex: number): string {
  return `ffff0000-0000-${String(deckIndex + 1).padStart(4, '0')}-0000-${String(
    vocabIndex + 1,
  ).padStart(12, '0')}`;
}

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
  const systemDeckSpecs: SeedDeckSpec[] = [
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd01',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
      name: 'TOEIC 450 - Daily Life',
      description: 'Bộ từ vựng TOEIC nền tảng về đời sống hằng ngày.',
      tags: ['TOEIC', 'Daily Life', 'Beginner'],
      displayOrder: 1,
      vocabularies: [
        vx('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv001', 'appointment', '/əˈpɔɪnt.mənt/', 'cuộc hẹn', 'noun', 'MEDIUM', 'An arrangement to meet someone at a particular time and place.', 'I have an appointment with the manager at 9 a.m.', 'make an appointment; schedule an appointment; doctor appointment', 'meeting; schedule; arrangement', 'Common in TOEIC office and daily life contexts.'),
        vx('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv002', 'purchase', '/ˈpɝː.tʃəs/', 'mua hàng; sự mua hàng', 'verb/noun', 'EASY', 'To buy something or the act of buying something.', 'Customers can purchase tickets online.', 'make a purchase; purchase order; purchase price', 'buy; order; payment'),
        vx('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv003', 'receipt', '/rɪˈsiːt/', 'hóa đơn; biên lai', 'noun', 'EASY', 'A piece of paper or digital record showing that money has been paid.', 'Please keep your receipt for future reference.', 'sales receipt; keep a receipt; receipt number', 'invoice; bill; payment'),
        v('customer', '/ˈkʌs.tə.mər/', 'khách hàng', 'noun', 'EASY', 'A person who buys goods or services.', 'Every customer received a welcome email.', 'customer service; loyal customer', 'client; buyer'),
        v('reservation', '/ˌrez.əˈveɪ.ʃən/', 'đặt chỗ', 'noun', 'MEDIUM', 'An arrangement to keep something such as a seat or room for someone.', 'She made a hotel reservation for the weekend.', 'make a reservation; reservation desk', 'booking; schedule'),
        v('schedule', '/ˈskedʒ.uːl/', 'lịch trình', 'noun', 'EASY', 'A plan that lists times and activities.', 'The tour schedule changed because of the weather.', 'daily schedule; on schedule', 'plan; timetable'),
        v('luggage', '/ˈlʌɡ.ɪdʒ/', 'hành lý', 'noun', 'EASY', 'Bags and suitcases carried during travel.', 'Please keep your luggage with you at all times.', 'carry-on luggage; lost luggage', 'bags; suitcase'),
        v('visitor', '/ˈvɪz.ɪ.tər/', 'khách tham quan', 'noun', 'EASY', 'A person who goes to a place for a short time.', 'The museum welcomed over 500 visitors today.', 'visitor center; business visitor', 'guest; traveler'),
        v('available', '/əˈveɪ.lə.bəl/', 'có sẵn', 'adjective', 'EASY', 'Able to be used or obtained.', 'A larger room is available after noon.', 'available now; readily available', 'accessible; open'),
        v('discount', '/ˈdɪs.kaʊnt/', 'giảm giá', 'noun', 'EASY', 'A reduction in the usual price.', 'Members receive a discount on all tickets.', 'special discount; discount rate', 'sale; reduction'),
        v('cashier', '/kæˈʃɪr/', 'thu ngân', 'noun', 'EASY', 'A person who receives payments in a store.', 'The cashier gave me the wrong change.', 'store cashier; head cashier', 'clerk; teller'),
        v('grocery', '/ˈɡroʊ.sər.i/', 'thực phẩm tạp hóa', 'noun', 'EASY', 'Food and household items sold in a store.', 'He bought groceries on his way home.', 'grocery store; grocery bill', 'food; supplies'),
        v('package', '/ˈpæk.ɪdʒ/', 'bưu kiện', 'noun', 'EASY', 'An object wrapped for transport or delivery.', 'Your package will arrive tomorrow morning.', 'package delivery; holiday package', 'parcel; bundle'),
        v('complaint', '/kəmˈpleɪnt/', 'lời phàn nàn', 'noun', 'MEDIUM', 'A statement that something is unsatisfactory.', 'The manager handled the complaint politely.', 'file a complaint; customer complaint', 'protest; criticism'),
        v('neighborhood', '/ˈneɪ.bɚ.hʊd/', 'khu dân cư', 'noun', 'EASY', 'The area around where people live.', 'There is a new cafe in our neighborhood.', 'quiet neighborhood; neighborhood store', 'district; area'),
        v('utility', '/juːˈtɪl.ə.t̬i/', 'tiện ích', 'noun', 'MEDIUM', 'A useful service such as water, gas, or electricity.', 'Utility costs increased during the summer.', 'utility bill; public utility', 'service; facility'),
        v('commute', '/kəˈmjuːt/', 'đi làm hàng ngày', 'verb', 'MEDIUM', 'To travel regularly between home and work.', 'Many employees commute by train.', 'commute daily; long commute', 'travel; journey'),
        v('repair', '/rɪˈper/', 'sửa chữa', 'verb', 'EASY', 'To fix something that is broken.', 'The technician repaired the washing machine.', 'repair a device; repair service', 'fix; restore'),
        v('brochure', '/broʊˈʃʊr/', 'tờ rơi', 'noun', 'MEDIUM', 'A small book or leaflet giving information.', 'The travel agent handed me a brochure.', 'travel brochure; company brochure', 'leaflet; pamphlet'),
        v('payment', '/ˈpeɪ.mənt/', 'thanh toán', 'noun', 'EASY', 'The act of paying money for something.', 'Online payment is available for all orders.', 'payment method; late payment', 'fee; transaction'),
      ],
    },
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd02',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
      name: 'TOEIC 600 - Office & Business',
      description: 'Bộ từ vựng TOEIC về công sở và kinh doanh.',
      tags: ['TOEIC', 'Business', 'Office'],
      displayOrder: 2,
      vocabularies: [
        vx('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv101', 'deadline', '/ˈded.laɪn/', 'hạn chót', 'noun', 'MEDIUM', 'The latest time or date by which something should be completed.', 'The deadline for the report is Friday.', 'meet a deadline; miss a deadline; tight deadline', 'due date; schedule; timeline'),
        vx('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv102', 'negotiate', '/nəˈɡoʊ.ʃi.eɪt/', 'đàm phán', 'verb', 'HARD', 'To discuss something in order to reach an agreement.', 'The company will negotiate a new contract.', 'negotiate a contract; negotiate terms; negotiate with clients', 'discuss; bargain; agree'),
        v('agenda', '/əˈdʒen.də/', 'chương trình nghị sự', 'noun', 'MEDIUM', 'A list of matters to be discussed at a meeting.', 'The meeting agenda was shared yesterday.', 'meeting agenda; agenda item', 'schedule; outline'),
        v('colleague', '/ˈkɑː.liːɡ/', 'đồng nghiệp', 'noun', 'EASY', 'A person you work with.', 'My colleague prepared the presentation slides.', 'close colleague; former colleague', 'coworker; associate'),
        v('supervisor', '/ˈsuː.pɚ.vaɪ.zɚ/', 'giám sát viên', 'noun', 'MEDIUM', 'A person who oversees the work of others.', 'Please ask your supervisor to approve the request.', 'shift supervisor; direct supervisor', 'manager; leader'),
        v('department', '/dɪˈpɑːrt.mənt/', 'phòng ban', 'noun', 'EASY', 'A division of a company or organization.', 'She works in the sales department.', 'HR department; finance department', 'division; unit'),
        v('approve', '/əˈpruːv/', 'phê duyệt', 'verb', 'MEDIUM', 'To officially agree to something.', 'The director approved the travel budget.', 'approve a plan; approve a request', 'authorize; accept'),
        v('budget', '/ˈbʌdʒ.ɪt/', 'ngân sách', 'noun', 'MEDIUM', 'An amount of money available for a purpose.', 'The project stayed within budget this quarter.', 'annual budget; budget plan', 'finance; cost limit'),
        v('contract', '/ˈkɑːn.trækt/', 'hợp đồng', 'noun', 'MEDIUM', 'A legal agreement between two parties.', 'The supplier signed a new contract.', 'service contract; contract terms', 'agreement; deal'),
        v('proposal', '/prəˈpoʊ.zəl/', 'đề xuất', 'noun', 'MEDIUM', 'A formal suggestion or plan.', 'The client accepted our proposal.', 'business proposal; project proposal', 'plan; suggestion'),
        v('report', '/rɪˈpɔːrt/', 'báo cáo', 'noun', 'EASY', 'A written or spoken account of facts or results.', 'Please submit the sales report by noon.', 'annual report; status report', 'summary; record'),
        v('invoice', '/ˈɪn.vɔɪs/', 'hóa đơn', 'noun', 'MEDIUM', 'A document showing what must be paid.', 'The invoice was sent to the accounting team.', 'invoice number; issue an invoice', 'bill; statement'),
        v('branch', '/bræntʃ/', 'chi nhánh', 'noun', 'EASY', 'A local office or shop of a larger organization.', 'Our branch opens at eight o’clock.', 'branch office; local branch', 'office; division'),
        v('revenue', '/ˈrev.ə.nuː/', 'doanh thu', 'noun', 'HARD', 'Income generated by business activities.', 'The company reported higher revenue this year.', 'annual revenue; revenue growth', 'income; earnings'),
        v('client', '/ˈklaɪ.ənt/', 'khách hàng', 'noun', 'EASY', 'A person or company receiving professional services.', 'The lawyer met a new client this morning.', 'major client; client request', 'customer; account'),
        v('merger', '/ˈmɝː.dʒɚ/', 'sáp nhập', 'noun', 'HARD', 'The joining of two companies into one.', 'The merger created a larger retail group.', 'company merger; merger deal', 'combination; acquisition'),
        v('conference', '/ˈkɑːn.fɚ.əns/', 'hội nghị', 'noun', 'MEDIUM', 'A formal meeting for discussion.', 'She spoke at the annual conference.', 'business conference; video conference', 'meeting; convention'),
        v('policy', '/ˈpɑː.lə.si/', 'chính sách', 'noun', 'MEDIUM', 'An officially agreed set of rules or guidelines.', 'The company updated its leave policy.', 'company policy; privacy policy', 'rule; guideline'),
        v('quarterly', '/ˈkwɔːr.t̬ɚ.li/', 'hàng quý', 'adjective', 'MEDIUM', 'Happening once every three months.', 'The team reviews performance in quarterly meetings.', 'quarterly report; quarterly results', 'seasonal; periodic'),
        v('recruit', '/rɪˈkruːt/', 'tuyển dụng', 'verb', 'MEDIUM', 'To find and hire new employees.', 'The company plans to recruit more engineers.', 'recruit staff; recruit candidates', 'hire; employ'),
      ],
    },
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd03',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
      name: 'TOEIC 450 - Shopping & Services',
      description: 'Bộ từ vựng TOEIC nền tảng về mua sắm và dịch vụ.',
      tags: ['TOEIC', 'Shopping', 'Services'],
      displayOrder: 3,
      vocabularies: [
        v('refund', '/ˈriː.fʌnd/', 'hoàn tiền', 'noun', 'EASY', 'Money returned after a purchase.', 'The store offered a full refund.', 'request a refund; full refund', 'repayment; return'),
        v('exchange', '/ɪksˈtʃeɪndʒ/', 'đổi hàng', 'verb', 'EASY', 'To give something and receive another thing instead.', 'You can exchange the shirt for a different size.', 'exchange an item; exchange policy', 'swap; replace'),
        v('warranty', '/ˈwɔːr.ən.t̬i/', 'bảo hành', 'noun', 'MEDIUM', 'A written promise to repair or replace a product.', 'The laptop includes a two-year warranty.', 'extended warranty; warranty period', 'guarantee; assurance'),
        v('aisle', '/aɪl/', 'lối đi giữa các kệ', 'noun', 'EASY', 'A passage between rows of shelves or seats.', 'The cereal is in aisle five.', 'store aisle; center aisle', 'passage; row'),
        v('coupon', '/ˈkuː.pɑːn/', 'phiếu giảm giá', 'noun', 'EASY', 'A voucher giving a discount.', 'She used a coupon to save ten dollars.', 'discount coupon; coupon code', 'voucher; deal'),
        v('product', '/ˈprɑː.dʌkt/', 'sản phẩm', 'noun', 'EASY', 'Something made to be sold.', 'This product is popular with students.', 'new product; product line', 'item; merchandise'),
        v('clerk', '/klɝːk/', 'nhân viên bán hàng', 'noun', 'EASY', 'A person who works in a store or office.', 'A clerk helped me find the right size.', 'store clerk; ticket clerk', 'assistant; cashier'),
        v('shelf', '/ʃelf/', 'kệ', 'noun', 'EASY', 'A flat board for holding goods or books.', 'The boxes were stacked on the top shelf.', 'store shelf; shelf space', 'rack; stand'),
        v('bargain', '/ˈbɑːr.ɡɪn/', 'món hời', 'noun', 'EASY', 'Something bought at a low price.', 'This jacket is a real bargain.', 'good bargain; bargain price', 'deal; value'),
        v('consumer', '/kənˈsuː.mɚ/', 'người tiêu dùng', 'noun', 'MEDIUM', 'A person who buys goods or services.', 'Consumers expect fast delivery.', 'consumer demand; consumer behavior', 'buyer; customer'),
        v('return', '/rɪˈtɝːn/', 'trả lại hàng', 'verb', 'EASY', 'To take something back to the seller.', 'You can return the shoes within seven days.', 'return a product; return policy', 'give back; refund'),
        v('stock', '/stɑːk/', 'hàng tồn', 'noun', 'MEDIUM', 'Goods kept for sale.', 'The item is currently out of stock.', 'in stock; stock level', 'inventory; supply'),
        v('estimate', '/ˈes.tə.meɪt/', 'ước tính', 'noun', 'MEDIUM', 'A rough calculation of cost or size.', 'The mechanic gave us an estimate for the repair.', 'cost estimate; rough estimate', 'calculation; forecast'),
        v('inquiry', '/ɪnˈkwaɪr.i/', 'sự hỏi thông tin', 'noun', 'MEDIUM', 'A request for information.', 'We received an inquiry about room rates.', 'customer inquiry; inquiry form', 'question; request'),
        v('appliance', '/əˈplaɪ.əns/', 'thiết bị gia dụng', 'noun', 'MEDIUM', 'A machine used in the home.', 'The appliance section is on the second floor.', 'home appliance; kitchen appliance', 'device; machine'),
        v('maintenance', '/ˈmeɪn.tən.əns/', 'bảo trì', 'noun', 'MEDIUM', 'Work done to keep something in good condition.', 'Regular maintenance extends the life of the machine.', 'building maintenance; maintenance fee', 'upkeep; repair'),
        v('guarantee', '/ˌɡer.ənˈtiː/', 'bảo đảm', 'verb', 'MEDIUM', 'To promise that something will happen or work well.', 'The brand guarantees product quality.', 'guarantee quality; money-back guarantee', 'assure; warrant'),
        v('counter', '/ˈkaʊn.t̬ɚ/', 'quầy', 'noun', 'EASY', 'A long flat surface where goods are sold or served.', 'Please pay at the front counter.', 'service counter; checkout counter', 'desk; booth'),
        v('delivery', '/dɪˈlɪv.ər.i/', 'giao hàng', 'noun', 'EASY', 'The act of bringing goods to someone.', 'Free delivery is available for online orders.', 'express delivery; next-day delivery', 'shipment; dispatch'),
        v('service', '/ˈsɝː.vɪs/', 'dịch vụ', 'noun', 'EASY', 'Help or work done for others.', 'The restaurant is known for friendly service.', 'customer service; quality service', 'assistance; support'),
      ],
    },
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd04',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
      name: 'TOEIC 600 - Meetings & Email',
      description: 'Bộ từ vựng TOEIC về họp hành và email công việc.',
      tags: ['TOEIC', 'Meetings', 'Email'],
      displayOrder: 4,
      vocabularies: [
        v('minutes', '/ˈmɪn.ɪts/', 'biên bản cuộc họp', 'noun', 'MEDIUM', 'Notes of what was discussed during a meeting.', 'Please send the minutes after the meeting.', 'meeting minutes; approve minutes', 'notes; record'),
        v('confirm', '/kənˈfɝːm/', 'xác nhận', 'verb', 'EASY', 'To state that something is true or definite.', 'Please confirm your attendance by noon.', 'confirm by email; confirm attendance', 'verify; approve'),
        v('attach', '/əˈtætʃ/', 'đính kèm', 'verb', 'EASY', 'To add a file or document to something.', 'I attached the invoice to the email.', 'attach a file; attach a document', 'enclose; append'),
        v('postpone', '/poʊstˈpoʊn/', 'hoãn lại', 'verb', 'MEDIUM', 'To delay something until a later time.', 'We had to postpone the client meeting.', 'postpone a meeting; postpone until', 'delay; reschedule'),
        v('participant', '/pɑːrˈtɪs.ə.pənt/', 'người tham gia', 'noun', 'MEDIUM', 'Someone who takes part in an activity.', 'All participants received the agenda.', 'meeting participant; active participant', 'attendee; member'),
        v('reminder', '/rɪˈmaɪn.dɚ/', 'lời nhắc', 'noun', 'EASY', 'Something that helps you remember something.', 'This is a reminder about tomorrow’s workshop.', 'friendly reminder; reminder email', 'notice; prompt'),
        v('reschedule', '/riːˈskedʒ.uːl/', 'sắp xếp lại lịch', 'verb', 'MEDIUM', 'To arrange a new time for something.', 'The call was rescheduled for Friday.', 'reschedule a call; reschedule a meeting', 'rearrange; postpone'),
        v('brief', '/briːf/', 'ngắn gọn', 'adjective', 'EASY', 'Lasting only a short time or using few words.', 'She gave a brief update to the team.', 'brief report; brief discussion', 'short; concise'),
        v('update', '/ʌpˈdeɪt/', 'cập nhật', 'noun', 'EASY', 'New information about a situation.', 'The manager shared an update on the project.', 'status update; project update', 'news; progress'),
        v('recipient', '/rɪˈsɪp.i.ənt/', 'người nhận', 'noun', 'MEDIUM', 'A person who receives something.', 'Please check the recipient list before sending.', 'email recipient; award recipient', 'receiver; addressee'),
        v('subject', '/ˈsʌb.dʒekt/', 'chủ đề', 'noun', 'EASY', 'The main topic of a message or discussion.', 'Write a clear subject line in every email.', 'subject line; discussion subject', 'topic; theme'),
        v('summary', '/ˈsʌm.ər.i/', 'bản tóm tắt', 'noun', 'MEDIUM', 'A short statement of the main points.', 'The email included a summary of the meeting.', 'brief summary; executive summary', 'overview; outline'),
        v('presentation', '/ˌprez.ənˈteɪ.ʃən/', 'bài thuyết trình', 'noun', 'MEDIUM', 'A talk in which information is given to an audience.', 'Her presentation impressed the clients.', 'sales presentation; slide presentation', 'talk; report'),
        v('seminar', '/ˈsem.ə.nɑːr/', 'hội thảo', 'noun', 'MEDIUM', 'A meeting for teaching or discussion.', 'The company hosted a seminar on leadership.', 'training seminar; online seminar', 'workshop; session'),
        v('workshop', '/ˈwɝːk.ʃɑːp/', 'buổi hội thảo', 'noun', 'MEDIUM', 'A meeting where people learn through discussion and activity.', 'I signed up for the writing workshop.', 'skills workshop; team workshop', 'seminar; class'),
        v('announce', '/əˈnaʊns/', 'thông báo', 'verb', 'EASY', 'To make something known publicly.', 'The CEO announced the new policy.', 'announce a change; announce results', 'declare; inform'),
        v('reply', '/rɪˈplaɪ/', 'trả lời', 'verb', 'EASY', 'To answer someone in writing or speech.', 'Please reply to the email before noon.', 'reply all; quick reply', 'respond; answer'),
        v('clarify', '/ˈkler.ə.faɪ/', 'làm rõ', 'verb', 'MEDIUM', 'To make something easier to understand.', 'Could you clarify the next steps for us?', 'clarify a point; clarify requirements', 'explain; specify'),
        v('follow-up', '/ˈfɑː.loʊ ʌp/', 'theo dõi tiếp', 'noun', 'MEDIUM', 'An action taken later to continue something.', 'We scheduled a follow-up call with the vendor.', 'follow-up email; follow-up meeting', 'continuation; response'),
        v('circulate', '/ˈsɝː.kjə.leɪt/', 'luân chuyển', 'verb', 'MEDIUM', 'To distribute something to many people.', 'The assistant circulated the draft report.', 'circulate a memo; circulate among staff', 'distribute; share'),
      ],
    },
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd05',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3',
      name: 'TOEIC 750 - Sales & Marketing',
      description: 'Bộ từ vựng TOEIC nâng cao về bán hàng và marketing.',
      tags: ['TOEIC', 'Sales', 'Marketing'],
      displayOrder: 5,
      vocabularies: [
        v('campaign', '/kæmˈpeɪn/', 'chiến dịch', 'noun', 'MEDIUM', 'A planned set of activities to achieve a result.', 'The campaign boosted online sales.', 'marketing campaign; ad campaign', 'promotion; effort'),
        v('advertise', '/ˈæd.vɚ.taɪz/', 'quảng cáo', 'verb', 'MEDIUM', 'To tell people about a product or service to persuade them to buy.', 'The company advertises on social media.', 'advertise a product; advertise widely', 'promote; market'),
        v('brand', '/brænd/', 'thương hiệu', 'noun', 'EASY', 'The name and image of a product or company.', 'The brand is popular with young consumers.', 'brand identity; global brand', 'label; trademark'),
        v('consumer', '/kənˈsuː.mɚ/', 'người tiêu dùng', 'noun', 'MEDIUM', 'A person who uses or buys products.', 'Consumers want better delivery options.', 'consumer demand; consumer trend', 'buyer; customer'),
        v('launch', '/lɔːntʃ/', 'ra mắt', 'verb', 'MEDIUM', 'To introduce a new product or campaign.', 'They will launch the app next month.', 'launch a product; launch event', 'introduce; release'),
        v('target', '/ˈtɑːr.ɡɪt/', 'đối tượng mục tiêu', 'noun', 'MEDIUM', 'A person or group a product is aimed at.', 'Teenagers are the main target for this ad.', 'target audience; target market', 'goal; audience'),
        v('survey', '/ˈsɝː.veɪ/', 'khảo sát', 'noun', 'MEDIUM', 'A set of questions used to collect opinions.', 'The team conducted a customer survey.', 'online survey; survey result', 'questionnaire; poll'),
        v('promotion', '/prəˈmoʊ.ʃən/', 'khuyến mãi', 'noun', 'MEDIUM', 'An activity to increase sales or awareness.', 'The summer promotion attracted many buyers.', 'sales promotion; special promotion', 'campaign; discount'),
        v('sponsor', '/ˈspɑːn.sɚ/', 'tài trợ', 'verb', 'MEDIUM', 'To support an event or activity with money.', 'A local bank will sponsor the event.', 'sponsor a program; major sponsor', 'support; fund'),
        v('strategy', '/ˈstræt̬.ə.dʒi/', 'chiến lược', 'noun', 'HARD', 'A plan for achieving a goal.', 'The brand changed its digital strategy.', 'marketing strategy; growth strategy', 'plan; approach'),
        v('competitor', '/kəmˈpet̬.ə.t̬ɚ/', 'đối thủ cạnh tranh', 'noun', 'MEDIUM', 'A business offering similar products.', 'Our competitor reduced prices last week.', 'major competitor; direct competitor', 'rival; opponent'),
        v('demand', '/dɪˈmænd/', 'nhu cầu', 'noun', 'MEDIUM', 'A strong need for a product or service.', 'Demand for electric cars is rising.', 'customer demand; market demand', 'need; request'),
        v('segment', '/ˈseɡ.mənt/', 'phân khúc', 'noun', 'HARD', 'A part of a market with similar needs.', 'The company targets the luxury segment.', 'market segment; customer segment', 'category; division'),
        v('feedback', '/ˈfiːd.bæk/', 'phản hồi', 'noun', 'EASY', 'Opinions about how good something is.', 'Customer feedback improved the website.', 'positive feedback; user feedback', 'comment; response'),
        v('market share', '/ˈmɑːr.kɪt ʃer/', 'thị phần', 'noun', 'HARD', 'The percentage of total sales a company has in a market.', 'The brand gained market share this year.', 'gain market share; market share growth', 'position; portion'),
        v('distribute', '/dɪˈstrɪb.juːt/', 'phân phối', 'verb', 'MEDIUM', 'To deliver goods to many places or people.', 'The firm distributes products nationwide.', 'distribute goods; distribute samples', 'deliver; supply'),
        v('persuade', '/pɚˈsweɪd/', 'thuyết phục', 'verb', 'MEDIUM', 'To make someone agree to do something.', 'The ad persuaded many users to subscribe.', 'persuade customers; persuade someone', 'convince; influence'),
        v('loyal', '/ˈlɔɪ.əl/', 'trung thành', 'adjective', 'MEDIUM', 'Continuing to support or buy from one brand.', 'Loyal customers receive extra benefits.', 'loyal customer; brand loyal', 'faithful; dedicated'),
        v('exposure', '/ɪkˈspoʊ.ʒɚ/', 'sự tiếp cận rộng rãi', 'noun', 'HARD', 'Attention from many people.', 'The event gave the startup more exposure.', 'media exposure; brand exposure', 'visibility; attention'),
        v('retail', '/ˈriː.teɪl/', 'bán lẻ', 'noun', 'MEDIUM', 'The business of selling directly to consumers.', 'She has ten years of retail experience.', 'retail store; retail market', 'sales; commerce'),
      ],
    },
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd06',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3',
      name: 'TOEIC 750 - Logistics & Procurement',
      description: 'Bộ từ vựng TOEIC nâng cao về logistics và mua hàng.',
      tags: ['TOEIC', 'Logistics', 'Procurement'],
      displayOrder: 6,
      vocabularies: [
        v('shipment', '/ˈʃɪp.mənt/', 'lô hàng', 'noun', 'MEDIUM', 'Goods sent from one place to another.', 'The shipment arrived at the port yesterday.', 'international shipment; shipment date', 'cargo; delivery'),
        v('warehouse', '/ˈwer.haʊs/', 'nhà kho', 'noun', 'MEDIUM', 'A large building where goods are stored.', 'The goods are stored in a secure warehouse.', 'warehouse staff; warehouse space', 'storage; depot'),
        v('inventory', '/ˈɪn.vən.tɔːr.i/', 'hàng tồn kho', 'noun', 'MEDIUM', 'A complete list of goods in stock.', 'The system updates inventory automatically.', 'inventory control; inventory level', 'stock; supplies'),
        v('supplier', '/səˈplaɪ.ɚ/', 'nhà cung cấp', 'noun', 'MEDIUM', 'A company that provides goods or services.', 'We switched to a more reliable supplier.', 'preferred supplier; supplier agreement', 'vendor; provider'),
        v('procure', '/prəˈkjʊr/', 'mua sắm', 'verb', 'HARD', 'To obtain goods or services for business use.', 'The team will procure new office furniture.', 'procure materials; procure equipment', 'purchase; obtain'),
        v('customs', '/ˈkʌs.təmz/', 'hải quan', 'noun', 'MEDIUM', 'The government service that checks goods entering a country.', 'The goods are waiting at customs.', 'clear customs; customs office', 'border control; import office'),
        v('freight', '/freɪt/', 'hàng vận chuyển', 'noun', 'HARD', 'Goods carried by ship, aircraft, or truck.', 'Air freight is more expensive than sea freight.', 'freight cost; freight service', 'cargo; shipment'),
        v('tender', '/ˈten.dɚ/', 'hồ sơ thầu', 'noun', 'HARD', 'A formal offer to supply goods or services.', 'The company submitted a tender for the project.', 'submit a tender; tender process', 'bid; offer'),
        v('dispatch', '/dɪˈspætʃ/', 'gửi đi', 'verb', 'MEDIUM', 'To send goods to a destination.', 'The warehouse will dispatch the order today.', 'dispatch goods; dispatch center', 'send; ship'),
        v('container', '/kənˈteɪ.nɚ/', 'container', 'noun', 'MEDIUM', 'A large box used for transporting goods.', 'The container was loaded onto the ship.', 'shipping container; storage container', 'box; unit'),
        v('shortage', '/ˈʃɔːr.t̬ɪdʒ/', 'sự thiếu hụt', 'noun', 'HARD', 'A situation in which there is not enough of something.', 'A fuel shortage delayed production.', 'staff shortage; supply shortage', 'lack; deficit'),
        v('surplus', '/ˈsɝː.plʌs/', 'dư thừa', 'noun', 'HARD', 'An amount that is more than needed.', 'The warehouse had a surplus of chairs.', 'budget surplus; food surplus', 'excess; extra'),
        v('transit', '/ˈtræn.zɪt/', 'vận chuyển', 'noun', 'MEDIUM', 'The movement of goods from one place to another.', 'The package is currently in transit.', 'in transit; transit time', 'transport; movement'),
        v('route', '/ruːt/', 'tuyến đường', 'noun', 'EASY', 'A way from one place to another.', 'The truck followed the fastest route.', 'delivery route; route map', 'path; direction'),
        v('carrier', '/ˈker.i.ɚ/', 'hãng vận chuyển', 'noun', 'MEDIUM', 'A company that transports goods or people.', 'The carrier updated the delivery schedule.', 'air carrier; delivery carrier', 'shipper; transporter'),
        v('loading', '/ˈloʊ.dɪŋ/', 'bốc xếp', 'noun', 'MEDIUM', 'The act of putting goods onto a vehicle.', 'Loading will begin at 6 a.m.', 'loading dock; loading process', 'packing; transfer'),
        v('inspection', '/ɪnˈspek.ʃən/', 'kiểm tra', 'noun', 'MEDIUM', 'A careful examination of something.', 'The goods passed the final inspection.', 'safety inspection; quality inspection', 'check; review'),
        v('batch', '/bætʃ/', 'lô sản xuất', 'noun', 'MEDIUM', 'A group of items produced together.', 'The damaged batch was returned to the supplier.', 'batch number; batch production', 'group; lot'),
        v('allocate', '/ˈæl.ə.keɪt/', 'phân bổ', 'verb', 'HARD', 'To give resources for a particular purpose.', 'The manager will allocate more storage space.', 'allocate funds; allocate resources', 'assign; distribute'),
        v('reorder', '/ˌriːˈɔːr.dɚ/', 'đặt lại hàng', 'verb', 'MEDIUM', 'To order something again.', 'We need to reorder toner this afternoon.', 'reorder supplies; reorder stock', 'restock; buy again'),
      ],
    },
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd07',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4',
      name: 'TOEIC 900 - Contracts & Finance',
      description: 'Bộ từ vựng TOEIC nâng cao về hợp đồng và tài chính doanh nghiệp.',
      tags: ['TOEIC', 'Contracts', 'Finance'],
      displayOrder: 7,
      vocabularies: [
        v('audit', '/ˈɔː.dɪt/', 'kiểm toán', 'noun', 'HARD', 'An official examination of financial records.', 'The company passed the annual audit.', 'internal audit; financial audit', 'review; inspection'),
        v('clause', '/klɔːz/', 'điều khoản', 'noun', 'HARD', 'A particular section of a legal document.', 'The clause protects both parties.', 'contract clause; legal clause', 'provision; term'),
        v('compliance', '/kəmˈplaɪ.əns/', 'sự tuân thủ', 'noun', 'HARD', 'The act of obeying a rule or law.', 'The company monitors compliance carefully.', 'regulatory compliance; legal compliance', 'obedience; conformity'),
        v('deficit', '/ˈdef.ə.sɪt/', 'thâm hụt', 'noun', 'HARD', 'The amount by which something is too small.', 'The budget deficit worried investors.', 'budget deficit; trade deficit', 'shortfall; loss'),
        v('equity', '/ˈek.wə.t̬i/', 'vốn chủ sở hữu', 'noun', 'HARD', 'The value of ownership in a company or property.', 'The startup raised equity from investors.', 'equity fund; shareholder equity', 'ownership; capital'),
        v('liability', '/ˌlaɪ.əˈbɪl.ə.t̬i/', 'khoản nợ phải trả', 'noun', 'HARD', 'Money that a company owes.', 'The report listed all current liabilities.', 'legal liability; total liability', 'debt; obligation'),
        v('forecast', '/ˈfɔːr.kæst/', 'dự báo', 'noun', 'MEDIUM', 'A statement about what is expected to happen.', 'The forecast predicts steady growth.', 'sales forecast; market forecast', 'prediction; projection'),
        v('expenditure', '/ɪkˈspen.dɪ.tʃɚ/', 'chi tiêu', 'noun', 'HARD', 'The amount of money spent.', 'Travel expenditure increased this quarter.', 'public expenditure; total expenditure', 'spending; outlay'),
        v('reconcile', '/ˈrek.ən.saɪl/', 'đối chiếu', 'verb', 'HARD', 'To make two sets of figures agree.', 'The accountant reconciled the bank statements.', 'reconcile accounts; reconcile records', 'balance; compare'),
        v('asset', '/ˈæs.et/', 'tài sản', 'noun', 'MEDIUM', 'Something valuable owned by a person or company.', 'The building is the company’s biggest asset.', 'fixed asset; asset value', 'property; resource'),
        v('premium', '/ˈpriː.mi.əm/', 'phí bảo hiểm', 'noun', 'MEDIUM', 'An amount paid for insurance or a higher quality service.', 'The insurance premium is due next week.', 'pay a premium; premium service', 'fee; charge'),
        v('authorize', '/ˈɔː.θɚ.aɪz/', 'ủy quyền', 'verb', 'MEDIUM', 'To give official permission.', 'Only managers can authorize refunds.', 'authorize payment; authorize access', 'approve; permit'),
        v('portfolio', '/pɔːrtˈfoʊ.li.oʊ/', 'danh mục đầu tư', 'noun', 'HARD', 'A collection of investments or work.', 'She manages an international portfolio.', 'investment portfolio; client portfolio', 'collection; assets'),
        v('subsidiary', '/səbˈsɪd.i.er.i/', 'công ty con', 'noun', 'HARD', 'A company controlled by another company.', 'The subsidiary operates in three countries.', 'foreign subsidiary; wholly owned subsidiary', 'affiliate; branch'),
        v('taxation', '/tækˈseɪ.ʃən/', 'thuế', 'noun', 'HARD', 'The system of collecting taxes.', 'The report discussed corporate taxation.', 'taxation policy; taxation system', 'tax; levy'),
        v('fiscal', '/ˈfɪs.kəl/', 'thuộc tài chính', 'adjective', 'HARD', 'Relating to government or company finances.', 'The fiscal year ends in December.', 'fiscal policy; fiscal year', 'financial; budgetary'),
        v('collateral', '/kəˈlæt̬.ɚ.əl/', 'tài sản thế chấp', 'noun', 'HARD', 'Property used to secure a loan.', 'The bank requested collateral for the loan.', 'loan collateral; provide collateral', 'security; guarantee'),
        v('bankruptcy', '/ˈbæŋ.krʌpt.si/', 'phá sản', 'noun', 'HARD', 'The state of being unable to pay debts.', 'The retailer filed for bankruptcy.', 'declare bankruptcy; bankruptcy court', 'insolvency; collapse'),
        v('dividend', '/ˈdɪv.ə.dend/', 'cổ tức', 'noun', 'HARD', 'A payment made to shareholders.', 'Investors were pleased with the dividend increase.', 'annual dividend; dividend payment', 'share profit; payout'),
        v('amendment', '/əˈmend.mənt/', 'sự sửa đổi', 'noun', 'HARD', 'A change made to a contract or law.', 'The parties signed an amendment yesterday.', 'contract amendment; proposed amendment', 'revision; modification'),
      ],
    },
    {
      id: 'dddddddd-dddd-dddd-dddd-dddddddddd08',
      learningLevelId: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4',
      name: 'TOEIC 900 - Management & Strategy',
      description: 'Bộ từ vựng TOEIC nâng cao về quản trị và chiến lược.',
      tags: ['TOEIC', 'Management', 'Strategy'],
      displayOrder: 8,
      vocabularies: [
        v('implement', '/ˈɪm.plə.ment/', 'triển khai', 'verb', 'MEDIUM', 'To put a plan into action.', 'The company will implement the new policy in June.', 'implement a plan; implement changes', 'apply; carry out'),
        v('evaluate', '/ɪˈvæl.ju.eɪt/', 'đánh giá', 'verb', 'MEDIUM', 'To judge the value or quality of something.', 'Managers evaluate performance every quarter.', 'evaluate results; evaluate options', 'assess; review'),
        v('objective', '/əbˈdʒek.tɪv/', 'mục tiêu', 'noun', 'MEDIUM', 'Something that you are trying to achieve.', 'Our main objective is customer retention.', 'business objective; key objective', 'goal; aim'),
        v('stakeholder', '/ˈsteɪkˌhoʊl.dɚ/', 'bên liên quan', 'noun', 'HARD', 'A person or group with an interest in a business.', 'The report was shared with all stakeholders.', 'key stakeholder; stakeholder meeting', 'investor; partner'),
        v('benchmark', '/ˈbentʃ.mɑːrk/', 'chuẩn so sánh', 'noun', 'HARD', 'A standard used for comparison.', 'The firm set a benchmark for service quality.', 'industry benchmark; benchmark data', 'standard; reference'),
        v('innovation', '/ˌɪn.əˈveɪ.ʃən/', 'đổi mới', 'noun', 'MEDIUM', 'A new idea or method.', 'Innovation drives long-term growth.', 'product innovation; business innovation', 'creativity; development'),
        v('delegate', '/ˈdel.ɪ.ɡeɪt/', 'giao phó', 'verb', 'MEDIUM', 'To give work to another person.', 'Leaders should delegate tasks effectively.', 'delegate authority; delegate work', 'assign; transfer'),
        v('leadership', '/ˈliː.dɚ.ʃɪp/', 'khả năng lãnh đạo', 'noun', 'MEDIUM', 'The ability to lead a group or organization.', 'Leadership training is required for new managers.', 'strong leadership; leadership role', 'guidance; management'),
        v('prioritize', '/praɪˈɔːr.ə.taɪz/', 'ưu tiên', 'verb', 'MEDIUM', 'To decide which things are most important.', 'We must prioritize urgent tasks first.', 'prioritize tasks; prioritize goals', 'rank; focus on'),
        v('optimize', '/ˈɑːp.tə.maɪz/', 'tối ưu hóa', 'verb', 'HARD', 'To make something as effective as possible.', 'The team optimized the delivery process.', 'optimize costs; optimize workflow', 'improve; streamline'),
        v('restructure', '/ˌriːˈstrʌk.tʃɚ/', 'tái cấu trúc', 'verb', 'HARD', 'To organize something in a new way.', 'The company plans to restructure its sales team.', 'restructure operations; restructure debt', 'reorganize; reform'),
        v('acquire', '/əˈkwaɪr/', 'mua lại', 'verb', 'HARD', 'To buy or gain something.', 'The group acquired a smaller competitor.', 'acquire a company; acquire skills', 'obtain; purchase'),
        v('initiative', '/ɪˈnɪʃ.ə.t̬ɪv/', 'sáng kiến', 'noun', 'MEDIUM', 'A new plan or action to solve a problem.', 'The green initiative reduced waste.', 'strategic initiative; company initiative', 'program; effort'),
        v('alignment', '/əˈlaɪn.mənt/', 'sự đồng bộ', 'noun', 'HARD', 'Agreement between ideas, plans, or goals.', 'The merger improved team alignment.', 'goal alignment; strategic alignment', 'consistency; agreement'),
        v('streamline', '/ˈstriːm.laɪn/', 'tinh gọn', 'verb', 'HARD', 'To make a process simpler and more efficient.', 'The new software streamlined reporting.', 'streamline operations; streamline workflow', 'simplify; optimize'),
        v('expansion', '/ɪkˈspæn.ʃən/', 'mở rộng', 'noun', 'MEDIUM', 'The act of becoming larger.', 'The firm announced regional expansion.', 'business expansion; market expansion', 'growth; development'),
        v('profitability', '/ˌprɑː.fɪ.t̬əˈbɪl.ə.t̬i/', 'khả năng sinh lợi', 'noun', 'HARD', 'The ability to make a profit.', 'The board reviewed overall profitability.', 'profitability target; improve profitability', 'return; earnings'),
        v('contingency', '/kənˈtɪn.dʒən.si/', 'kế hoạch dự phòng', 'noun', 'HARD', 'A possible future event that must be planned for.', 'The manager prepared a contingency plan.', 'contingency budget; contingency plan', 'backup; precaution'),
        v('enterprise', '/ˈen.t̬ɚ.praɪz/', 'doanh nghiệp', 'noun', 'HARD', 'A business or large project.', 'The enterprise expanded into new markets.', 'private enterprise; business enterprise', 'company; venture'),
        v('coordinate', '/koʊˈɔːr.dən.eɪt/', 'phối hợp', 'verb', 'MEDIUM', 'To organize activities so they work well together.', 'She coordinated the launch across teams.', 'coordinate efforts; coordinate teams', 'organize; manage'),
      ],
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2',
      name: 'IELTS 5.5 - Common Topics',
      description: 'Bộ từ vựng IELTS theo các chủ đề phổ biến.',
      tags: ['IELTS', 'Common Topics', 'Intermediate'],
      displayOrder: 1,
      vocabularies: [
        vx('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv201', 'environment', '/ɪnˈvaɪ.rən.mənt/', 'môi trường', 'noun', 'EASY', 'The air, water, and land in or on which people, animals, and plants live.', 'Many governments are taking action to protect the environment.', 'protect the environment; natural environment; environmental issue', 'nature; ecosystem; surroundings'),
        vx('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv202', 'significant', '/sɪɡˈnɪf.ə.kənt/', 'đáng kể; quan trọng', 'adjective', 'MEDIUM', 'Important or large enough to be noticed.', 'There has been a significant increase in online learning.', 'significant impact; significant change; statistically significant', 'important; notable; considerable'),
        v('technology', '/tekˈnɑː.lə.dʒi/', 'công nghệ', 'noun', 'EASY', 'Knowledge and tools used to solve practical problems.', 'Technology changes the way people communicate.', 'modern technology; digital technology', 'innovation; equipment'),
        v('culture', '/ˈkʌl.tʃɚ/', 'văn hóa', 'noun', 'EASY', 'The customs and beliefs of a society.', 'Travel helps people understand another culture.', 'local culture; cultural difference', 'tradition; heritage'),
        v('transport', '/ˈtræn.spɔːrt/', 'giao thông', 'noun', 'EASY', 'A system for carrying people or goods.', 'Public transport is affordable in this city.', 'public transport; transport system', 'travel; transit'),
        v('tradition', '/trəˈdɪʃ.ən/', 'truyền thống', 'noun', 'EASY', 'A belief or activity passed down through time.', 'The festival is an important local tradition.', 'cultural tradition; family tradition', 'custom; heritage'),
        v('advantage', '/ədˈvæn.tɪdʒ/', 'lợi ích', 'noun', 'EASY', 'Something that helps you be better or more successful.', 'Living downtown has many advantages.', 'major advantage; clear advantage', 'benefit; strength'),
        v('challenge', '/ˈtʃæl.ɪndʒ/', 'thách thức', 'noun', 'EASY', 'A difficult problem or task.', 'Finding affordable housing is a major challenge.', 'face a challenge; global challenge', 'difficulty; obstacle'),
        v('community', '/kəˈmjuː.nə.t̬i/', 'cộng đồng', 'noun', 'EASY', 'A group of people living in the same place or sharing interests.', 'The project helped the local community.', 'local community; online community', 'society; neighborhood'),
        v('communicate', '/kəˈmjuː.nə.keɪt/', 'giao tiếp', 'verb', 'EASY', 'To share information or ideas with others.', 'Children learn to communicate from an early age.', 'communicate clearly; communicate with others', 'express; interact'),
        v('recommend', '/ˌrek.əˈmend/', 'đề xuất', 'verb', 'EASY', 'To suggest something as good or useful.', 'Teachers often recommend reading every day.', 'highly recommend; recommend doing', 'suggest; advise'),
        v('solution', '/səˈluː.ʃən/', 'giải pháp', 'noun', 'EASY', 'An answer to a problem.', 'Better planning could be a simple solution.', 'practical solution; long-term solution', 'answer; remedy'),
        v('pollution', '/pəˈluː.ʃən/', 'ô nhiễm', 'noun', 'MEDIUM', 'Damage to the environment caused by harmful substances.', 'Air pollution affects public health.', 'air pollution; water pollution', 'contamination; waste'),
        v('participate', '/pɑːrˈtɪs.ə.peɪt/', 'tham gia', 'verb', 'MEDIUM', 'To take part in an activity.', 'Students should participate in group discussions.', 'participate actively; participate in', 'join; engage'),
        v('education', '/ˌedʒ.əˈkeɪ.ʃən/', 'giáo dục', 'noun', 'EASY', 'The process of teaching and learning.', 'Education plays a key role in development.', 'higher education; quality education', 'schooling; learning'),
        v('experience', '/ɪkˈspɪr.i.əns/', 'trải nghiệm', 'noun', 'EASY', 'Knowledge or skill gained through doing something.', 'Working abroad was a valuable experience.', 'practical experience; work experience', 'practice; background'),
        v('visitor', '/ˈvɪz.ɪ.t̬ɚ/', 'du khách', 'noun', 'EASY', 'A person visiting a place.', 'The city attracts many visitors every year.', 'international visitor; regular visitor', 'tourist; guest'),
        v('leisure', '/ˈliː.ʒɚ/', 'thời gian rảnh', 'noun', 'MEDIUM', 'Time free from work or duties.', 'People need leisure time to relax.', 'leisure activity; leisure time', 'free time; recreation'),
        v('improvement', '/ɪmˈpruːv.mənt/', 'sự cải thiện', 'noun', 'MEDIUM', 'The process of becoming better.', 'There has been a clear improvement in traffic flow.', 'major improvement; steady improvement', 'progress; development'),
        v('convenience', '/kənˈviː.ni.əns/', 'sự tiện lợi', 'noun', 'MEDIUM', 'The state of being easy and useful.', 'Online shopping offers great convenience.', 'for convenience; added convenience', 'comfort; ease'),
      ],
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3',
      name: 'IELTS 6.5 - Academic Vocabulary',
      description: 'Bộ từ vựng học thuật cho IELTS band 6.5+.',
      tags: ['IELTS', 'Academic', 'Advanced'],
      displayOrder: 2,
      vocabularies: [
        vx('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv301', 'sustainable', '/səˈsteɪ.nə.bəl/', 'bền vững', 'adjective', 'HARD', 'Able to continue over a period of time without damaging the environment or using too many resources.', 'Countries need to develop sustainable energy sources.', 'sustainable development; sustainable growth; sustainable energy', 'renewable; long-term; eco-friendly', 'Common in IELTS Writing Task 2.'),
        v('analyze', '/ˈæn.əl.aɪz/', 'phân tích', 'verb', 'MEDIUM', 'To examine something in detail.', 'Researchers analyze survey data carefully.', 'analyze data; analyze results', 'examine; assess'),
        v('concept', '/ˈkɑːn.sept/', 'khái niệm', 'noun', 'MEDIUM', 'An abstract idea or general notion.', 'The concept is explained in the introduction.', 'key concept; complex concept', 'idea; notion'),
        v('data', '/ˈdeɪ.t̬ə/', 'dữ liệu', 'noun', 'MEDIUM', 'Facts or information used for analysis.', 'The chart presents data from three schools.', 'collect data; data analysis', 'information; figures'),
        v('derive', '/dɪˈraɪv/', 'suy ra', 'verb', 'HARD', 'To obtain or come from a source.', 'Many conclusions derive from limited evidence.', 'derive from; derive meaning', 'obtain; originate'),
        v('factor', '/ˈfæk.tɚ/', 'yếu tố', 'noun', 'MEDIUM', 'One of the things that influences a result.', 'Cost is a major factor in the decision.', 'key factor; contributing factor', 'element; cause'),
        v('methodology', '/ˌmeθ.əˈdɑː.lə.dʒi/', 'phương pháp luận', 'noun', 'HARD', 'A system of methods used in study or work.', 'The paper explains its methodology clearly.', 'research methodology; teaching methodology', 'approach; method'),
        v('interpret', '/ɪnˈtɝː.prət/', 'diễn giải', 'verb', 'MEDIUM', 'To explain the meaning of something.', 'Students must interpret the graph accurately.', 'interpret results; interpret data', 'explain; understand'),
        v('establish', '/ɪˈstæb.lɪʃ/', 'thiết lập', 'verb', 'MEDIUM', 'To set up or prove something firmly.', 'The study establishes a link between diet and health.', 'establish a theory; establish a rule', 'create; prove'),
        v('evidence', '/ˈev.ə.dəns/', 'bằng chứng', 'noun', 'MEDIUM', 'Facts that show something is true.', 'There is strong evidence to support the claim.', 'clear evidence; supporting evidence', 'proof; indication'),
        v('justify', '/ˈdʒʌs.tə.faɪ/', 'chứng minh tính hợp lý', 'verb', 'HARD', 'To show that a decision or idea is reasonable.', 'The benefits justify the high cost.', 'justify a decision; justify spending', 'defend; explain'),
        v('theory', '/ˈθɪr.i/', 'lý thuyết', 'noun', 'MEDIUM', 'A formal explanation of how something works.', 'The theory remains popular among linguists.', 'economic theory; learning theory', 'principle; explanation'),
        v('academic', '/ˌæk.əˈdem.ɪk/', 'học thuật', 'adjective', 'MEDIUM', 'Related to education or study.', 'She has strong academic writing skills.', 'academic article; academic success', 'educational; scholarly'),
        v('impact', '/ˈɪm.pækt/', 'tác động', 'noun', 'MEDIUM', 'A strong effect on someone or something.', 'Technology has a major impact on education.', 'have an impact; environmental impact', 'effect; influence'),
        v('assess', '/əˈses/', 'đánh giá', 'verb', 'MEDIUM', 'To judge the quality or importance of something.', 'Teachers assess progress every month.', 'assess risk; assess performance', 'evaluate; judge'),
        v('framework', '/ˈfreɪm.wɝːk/', 'khung', 'noun', 'HARD', 'A basic structure that supports something.', 'The report offers a useful framework for analysis.', 'legal framework; theoretical framework', 'structure; model'),
        v('infer', '/ɪnˈfɝː/', 'suy luận', 'verb', 'HARD', 'To form an opinion from evidence.', 'Readers can infer the writer’s opinion from the tone.', 'infer from evidence; infer meaning', 'deduce; conclude'),
        v('hypothesis', '/haɪˈpɑː.θə.sɪs/', 'giả thuyết', 'noun', 'HARD', 'An idea suggested for testing.', 'The experiment tested the original hypothesis.', 'test a hypothesis; research hypothesis', 'assumption; theory'),
        v('variable', '/ˈver.i.ə.bəl/', 'biến số', 'noun', 'HARD', 'Something that can change and affect results.', 'Age was treated as an important variable.', 'control variable; variable factor', 'element; factor'),
        v('criterion', '/kraɪˈtɪr.i.ən/', 'tiêu chí', 'noun', 'HARD', 'A standard for judging something.', 'Clarity is one criterion for assessment.', 'selection criterion; evaluation criterion', 'standard; measure'),
      ],
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee03',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1',
      name: 'IELTS 4.0 - Everyday Basics',
      description: 'Bộ từ vựng IELTS nền tảng cho giao tiếp hằng ngày.',
      tags: ['IELTS', 'Basics', 'Everyday'],
      displayOrder: 3,
      vocabularies: [
        v('family', '/ˈfæm.əl.i/', 'gia đình', 'noun', 'EASY', 'People related to one another, especially parents and children.', 'My family meets every weekend.', 'family member; family life', 'relatives; household'),
        v('hobby', '/ˈhɑː.bi/', 'sở thích', 'noun', 'EASY', 'An activity done for enjoyment.', 'Reading is my favorite hobby.', 'new hobby; hobby group', 'interest; pastime'),
        v('weather', '/ˈweð.ɚ/', 'thời tiết', 'noun', 'EASY', 'The conditions of the atmosphere.', 'The weather is cooler in the evening.', 'bad weather; weather forecast', 'climate; temperature'),
        v('travel', '/ˈtræv.əl/', 'du lịch', 'verb', 'EASY', 'To go from one place to another.', 'Many students travel by bus to school.', 'travel abroad; travel by train', 'journey; trip'),
        v('healthy', '/ˈhel.θi/', 'khỏe mạnh', 'adjective', 'EASY', 'In good physical condition.', 'Walking every day helps people stay healthy.', 'healthy diet; healthy lifestyle', 'fit; well'),
        v('routine', '/ruːˈtiːn/', 'thói quen hằng ngày', 'noun', 'EASY', 'The usual order of activities.', 'She follows the same morning routine.', 'daily routine; work routine', 'habit; schedule'),
        v('neighbor', '/ˈneɪ.bɚ/', 'hàng xóm', 'noun', 'EASY', 'A person living nearby.', 'Our neighbors are very friendly.', 'next-door neighbor; good neighbor', 'resident; local'),
        v('festival', '/ˈfes.tə.vəl/', 'lễ hội', 'noun', 'EASY', 'A public celebration.', 'The city hosts a music festival each year.', 'food festival; cultural festival', 'celebration; event'),
        v('popular', '/ˈpɑː.pjə.lɚ/', 'phổ biến', 'adjective', 'EASY', 'Liked by many people.', 'This park is popular with families.', 'popular choice; highly popular', 'well-liked; common'),
        v('public', '/ˈpʌb.lɪk/', 'công cộng', 'adjective', 'EASY', 'Open or available to everyone.', 'The library is a public place.', 'public space; public service', 'shared; communal'),
        v('message', '/ˈmes.ɪdʒ/', 'tin nhắn', 'noun', 'EASY', 'A piece of information sent to someone.', 'I left a message for my teacher.', 'text message; short message', 'note; communication'),
        v('borrow', '/ˈbɑːr.oʊ/', 'mượn', 'verb', 'EASY', 'To take and use something that belongs to someone else.', 'Can I borrow your dictionary?', 'borrow money; borrow a book', 'take; use'),
        v('enjoy', '/ɪnˈdʒɔɪ/', 'thích thú', 'verb', 'EASY', 'To like doing something.', 'Children enjoy playing outside.', 'enjoy doing; really enjoy', 'like; appreciate'),
        v('simple', '/ˈsɪm.pəl/', 'đơn giản', 'adjective', 'EASY', 'Easy to understand or do.', 'The instructions are simple to follow.', 'simple idea; simple plan', 'easy; basic'),
        v('local', '/ˈloʊ.kəl/', 'địa phương', 'adjective', 'EASY', 'Belonging to a particular area.', 'I prefer buying food from local shops.', 'local people; local market', 'regional; nearby'),
        v('crowded', '/ˈkraʊ.dɪd/', 'đông đúc', 'adjective', 'EASY', 'Full of people.', 'The bus is always crowded in the morning.', 'crowded street; crowded station', 'busy; packed'),
        v('quiet', '/ˈkwaɪ.ət/', 'yên tĩnh', 'adjective', 'EASY', 'Making little noise.', 'We found a quiet cafe to study.', 'quiet area; quiet evening', 'calm; silent'),
        v('useful', '/ˈjuːs.fəl/', 'hữu ích', 'adjective', 'EASY', 'Helping you to do something.', 'This app is useful for learning words.', 'useful advice; useful tool', 'helpful; practical'),
        v('friendly', '/ˈfrend.li/', 'thân thiện', 'adjective', 'EASY', 'Kind and pleasant.', 'The staff were friendly to visitors.', 'friendly service; friendly advice', 'kind; warm'),
        v('activity', '/ækˈtɪv.ə.t̬i/', 'hoạt động', 'noun', 'EASY', 'Something that people do.', 'Sports are a popular school activity.', 'daily activity; outdoor activity', 'task; event'),
      ],
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee04',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1',
      name: 'IELTS 4.0 - School & Work',
      description: 'Bộ từ vựng IELTS nền tảng về trường học và công việc.',
      tags: ['IELTS', 'School', 'Work'],
      displayOrder: 4,
      vocabularies: [
        v('classroom', '/ˈklæs.ruːm/', 'lớp học', 'noun', 'EASY', 'A room where students are taught.', 'The classroom was full of posters.', 'modern classroom; classroom activity', 'room; lesson space'),
        v('homework', '/ˈhoʊm.wɝːk/', 'bài tập về nhà', 'noun', 'EASY', 'Work that students do at home.', 'I finished my homework before dinner.', 'do homework; homework task', 'assignment; exercise'),
        v('lesson', '/ˈles.ən/', 'bài học', 'noun', 'EASY', 'A period of learning or teaching.', 'The first lesson starts at eight.', 'English lesson; driving lesson', 'class; session'),
        v('teacher', '/ˈtiː.tʃɚ/', 'giáo viên', 'noun', 'EASY', 'A person who teaches.', 'The teacher explained the task clearly.', 'class teacher; science teacher', 'educator; instructor'),
        v('student', '/ˈstuː.dənt/', 'học sinh; sinh viên', 'noun', 'EASY', 'A person who is studying at a school or college.', 'Every student needs a notebook.', 'international student; full-time student', 'learner; pupil'),
        v('library', '/ˈlaɪ.brer.i/', 'thư viện', 'noun', 'EASY', 'A place where books are kept for reading or borrowing.', 'The library closes at six.', 'school library; library card', 'reading room; archive'),
        v('uniform', '/ˈjuː.nə.fɔːrm/', 'đồng phục', 'noun', 'EASY', 'A special set of clothes worn at work or school.', 'All students wear the same uniform.', 'school uniform; work uniform', 'outfit; dress code'),
        v('salary', '/ˈsæl.ər.i/', 'lương', 'noun', 'EASY', 'Money paid regularly for work.', 'Her salary increased after the promotion.', 'monthly salary; salary range', 'pay; wage'),
        v('office', '/ˈɔː.fɪs/', 'văn phòng', 'noun', 'EASY', 'A room or building where people work.', 'The office opens at 8:30 a.m.', 'office worker; office building', 'workplace; department'),
        v('interview', '/ˈɪn.t̬ɚ.vjuː/', 'phỏng vấn', 'noun', 'EASY', 'A meeting in which questions are asked.', 'He prepared carefully for the job interview.', 'job interview; phone interview', 'discussion; screening'),
        v('skill', '/skɪl/', 'kỹ năng', 'noun', 'EASY', 'The ability to do something well.', 'Communication is an important skill.', 'soft skill; technical skill', 'ability; talent'),
        v('training', '/ˈtreɪ.nɪŋ/', 'đào tạo', 'noun', 'EASY', 'The process of learning the skills needed for a job.', 'New staff receive training in customer service.', 'job training; skills training', 'instruction; practice'),
        v('project', '/ˈprɑː.dʒekt/', 'dự án', 'noun', 'EASY', 'A planned piece of work.', 'The group project is due next week.', 'research project; team project', 'assignment; task'),
        v('manager', '/ˈmæn.ə.dʒɚ/', 'quản lý', 'noun', 'EASY', 'A person who controls an organization or part of it.', 'The manager approved the new schedule.', 'store manager; project manager', 'supervisor; leader'),
        v('goal', '/ɡoʊl/', 'mục tiêu', 'noun', 'EASY', 'Something you hope to achieve.', 'Her goal is to improve her speaking score.', 'career goal; long-term goal', 'aim; objective'),
        v('practice', '/ˈpræk.tɪs/', 'luyện tập', 'verb', 'EASY', 'To do something again and again to get better.', 'Students should practice speaking every day.', 'practice regularly; practice a skill', 'train; repeat'),
        v('improve', '/ɪmˈpruːv/', 'cải thiện', 'verb', 'EASY', 'To become better or make something better.', 'Reading daily can improve vocabulary.', 'improve performance; improve skills', 'develop; enhance'),
        v('certificate', '/sɚˈtɪf.ə.kət/', 'chứng chỉ', 'noun', 'MEDIUM', 'An official document showing achievement.', 'She earned a language certificate.', 'training certificate; course certificate', 'award; document'),
        v('deadline', '/ˈded.laɪn/', 'hạn chót', 'noun', 'MEDIUM', 'The latest time by which something must be done.', 'The assignment deadline is Monday.', 'assignment deadline; meet a deadline', 'due date; time limit'),
        v('schedule', '/ˈskedʒ.uːl/', 'lịch trình', 'noun', 'EASY', 'A plan that shows times and activities.', 'My work schedule changes every week.', 'class schedule; work schedule', 'timetable; plan'),
      ],
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee05',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2',
      name: 'IELTS 5.5 - Travel & Culture',
      description: 'Bộ từ vựng IELTS về du lịch và văn hóa.',
      tags: ['IELTS', 'Travel', 'Culture'],
      displayOrder: 5,
      vocabularies: [
        v('destination', '/ˌdes.təˈneɪ.ʃən/', 'điểm đến', 'noun', 'MEDIUM', 'The place to which someone is going.', 'Da Nang is a popular tourist destination.', 'holiday destination; final destination', 'place; target'),
        v('accommodation', '/əˌkɑː.məˈdeɪ.ʃən/', 'chỗ ở', 'noun', 'MEDIUM', 'A place to stay while traveling.', 'Affordable accommodation is hard to find in summer.', 'hotel accommodation; student accommodation', 'lodging; housing'),
        v('itinerary', '/aɪˈtɪn.ə.rer.i/', 'lịch trình du lịch', 'noun', 'HARD', 'A plan of a journey.', 'The guide shared the full itinerary.', 'travel itinerary; detailed itinerary', 'schedule; plan'),
        v('sightseeing', '/ˈsaɪtˌsiː.ɪŋ/', 'tham quan', 'noun', 'MEDIUM', 'Visiting places of interest.', 'We spent the afternoon sightseeing in the old town.', 'go sightseeing; sightseeing tour', 'touring; visiting'),
        v('museum', '/mjuːˈziː.əm/', 'bảo tàng', 'noun', 'EASY', 'A building where important objects are displayed.', 'The museum is closed on Mondays.', 'art museum; museum ticket', 'gallery; exhibition'),
        v('heritage', '/ˈher.ɪ.t̬ɪdʒ/', 'di sản', 'noun', 'MEDIUM', 'Traditions, buildings, and culture passed from the past.', 'The temple is part of the city’s heritage.', 'cultural heritage; world heritage', 'tradition; legacy'),
        v('customs', '/ˈkʌs.təmz/', 'phong tục', 'noun', 'MEDIUM', 'The habits and traditions of a group.', 'Visitors should respect local customs.', 'local customs; social customs', 'traditions; practices'),
        v('cuisine', '/kwɪˈziːn/', 'ẩm thực', 'noun', 'MEDIUM', 'A style of cooking.', 'Thai cuisine is famous around the world.', 'local cuisine; traditional cuisine', 'food; cooking'),
        v('memorable', '/ˈmem.ər.ə.bəl/', 'đáng nhớ', 'adjective', 'MEDIUM', 'Worth remembering.', 'The concert was a memorable experience.', 'memorable trip; memorable event', 'unforgettable; remarkable'),
        v('adventure', '/ədˈven.tʃɚ/', 'phiêu lưu', 'noun', 'MEDIUM', 'An exciting or unusual experience.', 'Backpacking can be a great adventure.', 'travel adventure; outdoor adventure', 'journey; excitement'),
        v('journey', '/ˈdʒɝː.ni/', 'hành trình', 'noun', 'EASY', 'The act of traveling from one place to another.', 'The journey took six hours by bus.', 'long journey; return journey', 'trip; voyage'),
        v('resident', '/ˈrez.ə.dənt/', 'cư dân', 'noun', 'MEDIUM', 'A person who lives in a place.', 'Residents want more green spaces.', 'local resident; city resident', 'inhabitant; local'),
        v('foreign', '/ˈfɔːr.ən/', 'nước ngoài', 'adjective', 'EASY', 'Belonging to another country.', 'Foreign visitors need a passport.', 'foreign language; foreign visitor', 'international; overseas'),
        v('souvenir', '/ˌsuː.vəˈnɪr/', 'quà lưu niệm', 'noun', 'MEDIUM', 'Something kept to remind you of a place.', 'She bought a souvenir for her parents.', 'holiday souvenir; souvenir shop', 'gift; keepsake'),
        v('explore', '/ɪkˈsplɔːr/', 'khám phá', 'verb', 'EASY', 'To travel through a place to learn more about it.', 'Tourists love to explore the old quarter.', 'explore a city; explore nature', 'discover; investigate'),
        v('guide', '/ɡaɪd/', 'hướng dẫn viên', 'noun', 'EASY', 'A person who shows visitors around.', 'The guide explained the history of the castle.', 'tour guide; guidebook', 'leader; instructor'),
        v('landscape', '/ˈlænd.skeɪp/', 'phong cảnh', 'noun', 'MEDIUM', 'The visible features of an area of land.', 'The mountain landscape was breathtaking.', 'natural landscape; urban landscape', 'scenery; view'),
        v('ceremony', '/ˈser.ə.moʊ.ni/', 'nghi lễ', 'noun', 'MEDIUM', 'A formal public event.', 'The opening ceremony attracted many guests.', 'wedding ceremony; closing ceremony', 'ritual; event'),
        v('diversity', '/daɪˈvɝː.sə.t̬i/', 'sự đa dạng', 'noun', 'MEDIUM', 'The state of having many different types.', 'Cultural diversity enriches society.', 'cultural diversity; biological diversity', 'variety; difference'),
        v('landmark', '/ˈlænd.mɑːrk/', 'địa danh nổi tiếng', 'noun', 'MEDIUM', 'A famous building or feature.', 'The tower is the city’s most famous landmark.', 'historic landmark; local landmark', 'site; monument'),
      ],
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee06',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3',
      name: 'IELTS 6.5 - Society & Environment',
      description: 'Bộ từ vựng IELTS về xã hội và môi trường.',
      tags: ['IELTS', 'Society', 'Environment'],
      displayOrder: 6,
      vocabularies: [
        v('urbanization', '/ˌɝː.bən.əˈzeɪ.ʃən/', 'đô thị hóa', 'noun', 'HARD', 'The growth of cities and urban life.', 'Rapid urbanization changes local lifestyles.', 'rapid urbanization; urbanization rate', 'city growth; development'),
        v('biodiversity', '/ˌbaɪ.oʊ.daɪˈvɝː.sə.t̬i/', 'đa dạng sinh học', 'noun', 'HARD', 'The variety of plant and animal life.', 'Protecting biodiversity is a global priority.', 'marine biodiversity; biodiversity loss', 'ecosystem; wildlife variety'),
        v('conservation', '/ˌkɑːn.sɚˈveɪ.ʃən/', 'bảo tồn', 'noun', 'HARD', 'The protection of natural resources.', 'Forest conservation needs long-term planning.', 'wildlife conservation; conservation program', 'protection; preservation'),
        v('inequality', '/ˌɪn.ɪˈkwɑː.lə.t̬i/', 'bất bình đẳng', 'noun', 'HARD', 'A situation in which some people have more than others.', 'Income inequality is a serious concern.', 'social inequality; economic inequality', 'imbalance; unfairness'),
        v('infrastructure', '/ˈɪn.frəˌstrʌk.tʃɚ/', 'cơ sở hạ tầng', 'noun', 'HARD', 'Basic systems such as transport and power.', 'The city invested in public infrastructure.', 'transport infrastructure; digital infrastructure', 'facilities; foundation'),
        v('legislation', '/ˌledʒ.əˈsleɪ.ʃən/', 'pháp luật', 'noun', 'HARD', 'A law or set of laws.', 'New legislation aims to reduce waste.', 'environmental legislation; national legislation', 'law; regulation'),
        v('renewable', '/rɪˈnuː.ə.bəl/', 'tái tạo', 'adjective', 'HARD', 'Able to be replaced naturally.', 'Solar power is a renewable energy source.', 'renewable energy; renewable source', 'sustainable; green'),
        v('demographic', '/ˌdem.əˈɡræf.ɪk/', 'thuộc dân số', 'adjective', 'HARD', 'Related to population groups.', 'Demographic changes affect housing demand.', 'demographic trend; demographic data', 'population-based; statistical'),
        v('consumption', '/kənˈsʌmp.ʃən/', 'sự tiêu thụ', 'noun', 'MEDIUM', 'The use of goods or resources.', 'Water consumption rises in summer.', 'energy consumption; food consumption', 'use; intake'),
        v('citizen', '/ˈsɪt̬.ə.zən/', 'công dân', 'noun', 'EASY', 'A person who legally belongs to a country.', 'Every citizen should vote responsibly.', 'global citizen; local citizen', 'resident; national'),
        v('emission', '/ɪˈmɪʃ.ən/', 'khí thải', 'noun', 'HARD', 'A gas or substance released into the air.', 'Carbon emissions must be reduced.', 'vehicle emission; carbon emission', 'output; discharge'),
        v('habitat', '/ˈhæb.ə.tæt/', 'môi trường sống', 'noun', 'HARD', 'The natural home of an animal or plant.', 'The project damaged the birds’ habitat.', 'natural habitat; marine habitat', 'environment; ecosystem'),
        v('poverty', '/ˈpɑː.vɚ.t̬i/', 'nghèo đói', 'noun', 'MEDIUM', 'The state of being very poor.', 'Education can help reduce poverty.', 'extreme poverty; poverty rate', 'hardship; deprivation'),
        v('migration', '/maɪˈɡreɪ.ʃən/', 'di cư', 'noun', 'HARD', 'The movement of people from one place to another.', 'Migration affects urban planning.', 'rural migration; mass migration', 'movement; relocation'),
        v('sanitation', '/ˌsæn.əˈteɪ.ʃən/', 'vệ sinh công cộng', 'noun', 'HARD', 'Systems for keeping places clean and healthy.', 'Poor sanitation can cause disease.', 'public sanitation; sanitation facility', 'hygiene; cleanliness'),
        v('welfare', '/ˈwel.fer/', 'phúc lợi', 'noun', 'MEDIUM', 'The health and happiness of people.', 'Child welfare should be protected.', 'social welfare; animal welfare', 'well-being; support'),
        v('climate', '/ˈklaɪ.mət/', 'khí hậu', 'noun', 'EASY', 'The usual weather conditions of a place.', 'Climate change affects agriculture worldwide.', 'warm climate; climate policy', 'weather pattern; environment'),
        v('resource', '/ˈriː.sɔːrs/', 'tài nguyên', 'noun', 'MEDIUM', 'A useful supply of something.', 'Fresh water is a limited resource.', 'natural resource; human resource', 'supply; asset'),
        v('deforestation', '/diːˌfɔːr.əˈsteɪ.ʃən/', 'phá rừng', 'noun', 'HARD', 'The cutting down of forests.', 'Deforestation threatens wildlife habitats.', 'illegal deforestation; forest deforestation', 'forest loss; land clearing'),
        v('regulation', '/ˌreɡ.jəˈleɪ.ʃən/', 'quy định', 'noun', 'MEDIUM', 'An official rule.', 'Strict regulation is needed to reduce pollution.', 'government regulation; safety regulation', 'rule; control'),
      ],
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee07',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4',
      name: 'IELTS 7.0 - Research & Argumentation',
      description: 'Bộ từ vựng IELTS nâng cao cho lập luận và nghiên cứu.',
      tags: ['IELTS', 'Research', 'Argumentation'],
      displayOrder: 7,
      vocabularies: [
        v('articulate', '/ɑːrˈtɪk.jə.lət/', 'diễn đạt rõ ràng', 'verb', 'HARD', 'To express an idea clearly.', 'Good writers articulate their opinions precisely.', 'articulate clearly; articulate an idea', 'express; explain'),
        v('coherent', '/koʊˈhɪr.ənt/', 'mạch lạc', 'adjective', 'HARD', 'Logical and easy to understand.', 'The essay is coherent from start to finish.', 'coherent argument; coherent response', 'logical; consistent'),
        v('contradict', '/ˌkɑːn.trəˈdɪkt/', 'mâu thuẫn', 'verb', 'HARD', 'To say the opposite of something.', 'The new evidence contradicts the earlier claim.', 'contradict a statement; directly contradict', 'oppose; dispute'),
        v('discourse', '/ˈdɪs.kɔːrs/', 'bài diễn ngôn', 'noun', 'HARD', 'Written or spoken communication on a topic.', 'Public discourse shapes political opinion.', 'academic discourse; public discourse', 'discussion; communication'),
        v('empirical', '/ɪmˈpɪr.ɪ.kəl/', 'thực nghiệm', 'adjective', 'HARD', 'Based on observation or experiment.', 'The article provides empirical evidence.', 'empirical study; empirical data', 'practical; observed'),
        v('evaluate', '/ɪˈvæl.ju.eɪt/', 'đánh giá', 'verb', 'MEDIUM', 'To judge carefully.', 'Students must evaluate both sides of the issue.', 'evaluate evidence; evaluate options', 'assess; judge'),
        v('formulate', '/ˈfɔːr.mjə.leɪt/', 'hình thành', 'verb', 'HARD', 'To develop or create something carefully.', 'Researchers formulated a new theory.', 'formulate a policy; formulate a response', 'develop; create'),
        v('infer', '/ɪnˈfɝː/', 'suy luận', 'verb', 'HARD', 'To reach a conclusion from evidence.', 'Readers can infer the author’s attitude.', 'infer meaning; infer from context', 'deduce; conclude'),
        v('notion', '/ˈnoʊ.ʃən/', 'khái niệm', 'noun', 'HARD', 'An idea or belief.', 'The notion of fairness varies across cultures.', 'common notion; abstract notion', 'idea; concept'),
        v('perspective', '/pɚˈspek.tɪv/', 'quan điểm', 'noun', 'MEDIUM', 'A particular way of viewing something.', 'The article offers a new perspective on education.', 'global perspective; personal perspective', 'viewpoint; outlook'),
        v('premise', '/ˈprem.ɪs/', 'tiền đề', 'noun', 'HARD', 'An idea used as the basis for an argument.', 'The argument rests on a false premise.', 'basic premise; flawed premise', 'assumption; basis'),
        v('rational', '/ˈræʃ.ən.əl/', 'hợp lý', 'adjective', 'HARD', 'Based on reason and logic.', 'A rational decision requires careful thought.', 'rational choice; rational explanation', 'logical; sensible'),
        v('rigorous', '/ˈrɪɡ.ɚ.əs/', 'nghiêm ngặt', 'adjective', 'HARD', 'Very careful and thorough.', 'The study followed a rigorous method.', 'rigorous analysis; rigorous standard', 'strict; thorough'),
        v('substantiate', '/səbˈstæn.ʃi.eɪt/', 'chứng minh', 'verb', 'HARD', 'To show that something is true with evidence.', 'You must substantiate your main claim.', 'substantiate an argument; substantiate findings', 'support; verify'),
        v('synthesize', '/ˈsɪn.θə.saɪz/', 'tổng hợp', 'verb', 'HARD', 'To combine ideas into a whole.', 'The conclusion synthesizes several viewpoints.', 'synthesize information; synthesize sources', 'combine; integrate'),
        v('valid', '/ˈvæl.ɪd/', 'hợp lệ', 'adjective', 'MEDIUM', 'Based on sound reasoning or evidence.', 'The criticism is valid in this context.', 'valid argument; valid reason', 'sound; acceptable'),
        v('bias', '/baɪ.əs/', 'thiên kiến', 'noun', 'HARD', 'A tendency to prefer one side unfairly.', 'The report may reflect political bias.', 'media bias; personal bias', 'prejudice; inclination'),
        v('counterargument', '/ˌkaʊn.t̬ɚˈɑːr.ɡjə.mənt/', 'luận điểm phản biện', 'noun', 'HARD', 'An argument against another argument.', 'A strong essay addresses the main counterargument.', 'present a counterargument; major counterargument', 'rebuttal; opposition'),
        v('inference', '/ˈɪn.fɚ.əns/', 'sự suy luận', 'noun', 'HARD', 'A conclusion reached from evidence.', 'The reader must make an inference from the graph.', 'logical inference; correct inference', 'deduction; conclusion'),
        v('criterion', '/kraɪˈtɪr.i.ən/', 'tiêu chí', 'noun', 'HARD', 'A standard for judging something.', 'Clarity is one criterion for a high score.', 'assessment criterion; selection criterion', 'standard; measure'),
      ],
    },
    {
      id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee08',
      learningLevelId: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4',
      name: 'IELTS 7.0 - Advanced Issues',
      description: 'Bộ từ vựng IELTS nâng cao cho các chủ đề xã hội phức tạp.',
      tags: ['IELTS', 'Advanced', 'Issues'],
      displayOrder: 8,
      vocabularies: [
        v('alleviate', '/əˈliː.vi.eɪt/', 'làm giảm bớt', 'verb', 'HARD', 'To make a problem less severe.', 'Better transport could alleviate traffic congestion.', 'alleviate poverty; alleviate pressure', 'reduce; ease'),
        v('controversial', '/ˌkɑːn.trəˈvɝː.ʃəl/', 'gây tranh cãi', 'adjective', 'HARD', 'Causing disagreement.', 'The policy remains highly controversial.', 'controversial issue; controversial decision', 'debatable; disputed'),
        v('dilemma', '/daɪˈlem.ə/', 'tình thế tiến thoái lưỡng nan', 'noun', 'HARD', 'A difficult choice between alternatives.', 'Governments face a dilemma over energy use.', 'ethical dilemma; policy dilemma', 'problem; quandary'),
        v('exacerbate', '/ɪɡˈzæs.ɚ.beɪt/', 'làm trầm trọng hơn', 'verb', 'HARD', 'To make a problem worse.', 'Social media can exacerbate misinformation.', 'exacerbate inequality; exacerbate tension', 'worsen; intensify'),
        v('feasible', '/ˈfiː.zə.bəl/', 'khả thi', 'adjective', 'HARD', 'Able to be done successfully.', 'The plan is feasible with enough funding.', 'financially feasible; technically feasible', 'practical; possible'),
        v('inherent', '/ɪnˈher.ənt/', 'vốn có', 'adjective', 'HARD', 'Existing as a natural part of something.', 'Risk is inherent in every investment.', 'inherent risk; inherent problem', 'built-in; natural'),
        v('marginalized', '/ˈmɑːr.dʒə.nəl.aɪzd/', 'bị gạt ra bên lề', 'adjective', 'HARD', 'Excluded from full participation in society.', 'Marginalized groups need better support.', 'marginalized community; marginalized voice', 'excluded; disadvantaged'),
        v('nuanced', '/ˈnuː.ɑːnst/', 'tinh tế', 'adjective', 'HARD', 'Showing subtle differences.', 'The article offers a nuanced analysis.', 'nuanced argument; nuanced view', 'subtle; refined'),
        v('paradigm', '/ˈper.ə.daɪm/', 'mô hình', 'noun', 'HARD', 'A typical example or model.', 'The internet created a new communication paradigm.', 'paradigm shift; dominant paradigm', 'model; framework'),
        v('plausible', '/ˈplɔː.zə.bəl/', 'hợp lý, có vẻ đúng', 'adjective', 'HARD', 'Seeming likely to be true.', 'She gave a plausible explanation for the delay.', 'plausible reason; plausible theory', 'credible; believable'),
        v('prevalent', '/ˈprev.ə.lənt/', 'phổ biến', 'adjective', 'HARD', 'Common or widespread.', 'Stress is prevalent among urban workers.', 'prevalent problem; prevalent attitude', 'common; widespread'),
        v('resilient', '/rɪˈzɪl.jənt/', 'kiên cường', 'adjective', 'HARD', 'Able to recover quickly from difficulty.', 'Children can be remarkably resilient.', 'resilient community; resilient system', 'strong; adaptable'),
        v('scarcity', '/ˈsker.sə.t̬i/', 'sự khan hiếm', 'noun', 'HARD', 'A situation in which something is difficult to find.', 'Water scarcity affects many regions.', 'food scarcity; resource scarcity', 'shortage; lack'),
        v('transparency', '/trænsˈper.ən.si/', 'tính minh bạch', 'noun', 'HARD', 'The quality of being open and easy to understand.', 'Citizens expect transparency from leaders.', 'financial transparency; policy transparency', 'openness; clarity'),
        v('unprecedented', '/ʌnˈpres.ə.den.t̬ɪd/', 'chưa từng có', 'adjective', 'HARD', 'Never having happened before.', 'The city faced unprecedented flooding.', 'unprecedented growth; unprecedented event', 'extraordinary; unmatched'),
        v('vulnerability', '/ˌvʌl.nɚ.əˈbɪl.ə.t̬i/', 'tính dễ tổn thương', 'noun', 'HARD', 'The state of being at risk of harm.', 'The report highlights social vulnerability.', 'economic vulnerability; vulnerability to', 'risk; weakness'),
        v('welfare', '/ˈwel.fer/', 'phúc lợi', 'noun', 'MEDIUM', 'The health and happiness of people.', 'Public welfare should guide policy decisions.', 'social welfare; public welfare', 'well-being; support'),
        v('holistic', '/hoʊˈlɪs.tɪk/', 'toàn diện', 'adjective', 'HARD', 'Considering the whole rather than parts only.', 'A holistic approach is often more effective.', 'holistic approach; holistic view', 'comprehensive; overall'),
        v('mitigation', '/ˌmɪt̬.əˈɡeɪ.ʃən/', 'sự giảm nhẹ', 'noun', 'HARD', 'The act of making something less serious.', 'Flood mitigation should start immediately.', 'risk mitigation; mitigation strategy', 'reduction; control'),
        v('ethics', '/ˈeθ.ɪks/', 'đạo đức', 'noun', 'HARD', 'Moral principles that guide behavior.', 'Business ethics should be taught at university.', 'professional ethics; ethical issue', 'morality; principles'),
      ],
    },
  ];

  const systemDecks = systemDeckSpecs.map((deck) => ({
    id: deck.id,
    learningLevelId: deck.learningLevelId,
    name: deck.name,
    normalizedName: normalizeText(deck.name),
    description: deck.description,
    tags: deck.tags,
    displayOrder: deck.displayOrder,
  }));

  for (const deck of systemDecks) {
    await prisma.deck.upsert({
      where: { id: deck.id },
      update: {
        ownerUserId: null,
        learningLevelId: deck.learningLevelId,
        deckType: 'SYSTEM',
        visibility: 'PUBLIC',
        name: deck.name,
        normalizedName: deck.normalizedName,
        description: deck.description,
        tags: deck.tags,
        displayOrder: deck.displayOrder,
        isDefault: false,
      },
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

  console.log(`✅ System decks seeded (${systemDecks.length} decks)`);

  // ------------------------------------------------------------------
  // 5. Sample Vocabularies
  // ------------------------------------------------------------------
  const vocabularies = systemDeckSpecs.flatMap((deck, deckIndex) =>
    deck.vocabularies.map((vocab, vocabIndex) => ({
      id: vocab.id ?? generateVocabularyId(deckIndex, vocabIndex),
      deckId: deck.id,
      word: vocab.word,
      normalizedWord: normalizeText(vocab.word),
      pronunciation: vocab.pronunciation,
      meaning: vocab.meaning,
      normalizedMeaning: normalizeText(vocab.meaning),
      descriptionEn: vocab.descriptionEn,
      example: vocab.example,
      collocation: vocab.collocation,
      relatedWords: vocab.relatedWords,
      note: vocab.note ?? `Useful vocabulary from ${deck.name}.`,
      difficulty: vocab.difficulty,
      partOfSpeech: vocab.partOfSpeech,
    })),
  );

  for (const vocab of vocabularies) {
    await prisma.vocabulary.upsert({
      where: { id: vocab.id },
      update: {
        deckId: vocab.deckId,
        word: vocab.word,
        normalizedWord: vocab.normalizedWord,
        pronunciation: vocab.pronunciation,
        meaning: vocab.meaning,
        normalizedMeaning: vocab.normalizedMeaning,
        descriptionEn: vocab.descriptionEn,
        example: vocab.example,
        collocation: vocab.collocation,
        relatedWords: vocab.relatedWords,
        note: vocab.note,
        difficulty: vocab.difficulty,
        partOfSpeech: vocab.partOfSpeech,
      },
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
