# MinLish App - MySQL Database Design & SQL Script

## 1. Mục tiêu file

File này mô tả database hoàn chỉnh cho MinLish App và cung cấp SQL MySQL có thể chạy trực tiếp để tạo database, tables, constraints, indexes, views, stored procedure và seed data cơ bản.

Hệ thống theo hướng:

```text
Android Jetpack Compose
→ NestJS Backend
→ Prisma ORM hoặc raw SQL
→ MySQL Database
```

Database phục vụ các module:

- Authentication
- User Profile
- Learning Path TOEIC/IELTS
- Level
- System Decks
- User Decks
- Favorites deck mặc định
- Vocabulary
- Flashcard Learning
- SM-2 Spaced Repetition
- Practice: multiple choice, fill in blank, listening optional
- Analytics dashboard
- Notification settings
- CSV import/export tracking

---

## 2. Ghi chú triển khai

### 2.1 MySQL version

Khuyến nghị:

```text
MySQL 8.0+
Engine: InnoDB
Charset: utf8mb4
Collation: utf8mb4_unicode_ci
```

### 2.2 ID strategy

Toàn bộ bảng dùng `CHAR(36)` để lưu UUID.

Backend nên tự tạo UUID bằng `crypto.randomUUID()` hoặc thư viện tương đương.

### 2.3 Soft delete

Các bảng nghiệp vụ chính có `deleted_at`.

Quy ước:

- `deleted_at IS NULL`: còn hoạt động.
- `deleted_at IS NOT NULL`: đã xóa mềm.

Một số bảng có generated column `is_deleted` để hỗ trợ unique constraint với soft delete.

### 2.4 Date time

Dùng `DATETIME(3)` để lưu millisecond.

Backend nên lưu theo UTC.

### 2.5 Quyết định thiết kế quan trọng

#### Favorites

Không có bảng `user_favorite_vocabularies`.

Favorites là một deck cá nhân mặc định được tạo khi user đăng ký:

```text
decks.name = 'Favorites'
decks.deck_type = 'USER'
decks.is_default = 1
decks.owner_user_id = user.id
```

Khi bấm tim ở bất kỳ vocabulary nào, backend copy vocabulary đó vào Favorites deck và set:

```text
vocabularies.source_vocabulary_id = originalVocabularyId
```

#### Vocabulary vẫn thuộc một deck

MVP giữ quan hệ:

```text
decks 1 - n vocabularies
```

Không dùng bảng trung gian many-to-many.

#### Duplicate rule

Trong cùng một deck, duplicate exact được định nghĩa bằng:

```text
deck_id + normalized_word + normalized_meaning
```

Cùng `word` nhưng khác `meaning` được phép tồn tại.

#### Review card

SRS state dựa trên:

```text
user_id + vocabulary_id
```

Không thêm `deck_id` vào `review_cards`.

Muốn ôn theo deck thì filter qua:

```text
review_cards → vocabularies.deck_id
```

---

## 3. ERD logic

```text
users 1 - n decks
learning_paths 1 - n learning_levels
learning_levels 1 - n decks
decks 1 - n vocabularies
vocabularies self-reference through source_vocabulary_id

users 1 - n review_cards
vocabularies 1 - n review_cards

users 1 - n review_logs
review_cards 1 - n review_logs
vocabularies 1 - n review_logs

users 1 - n practice_sessions
decks 1 - n practice_sessions
practice_sessions 1 - n practice_answers
vocabularies 1 - n practice_answers

users 1 - n daily_activities
users 1 - 1 notification_settings
users 1 - n device_tokens
users 1 - n import_jobs
decks 1 - n import_jobs
```

---

## 4. Full SQL Script

> Có thể copy toàn bộ script bên dưới và chạy trong MySQL 8.0+.

```sql
-- =========================================================
-- MinLish App Database
-- MySQL 8.0+
-- =========================================================

DROP DATABASE IF EXISTS minlish_db;
CREATE DATABASE minlish_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE minlish_db;

-- =========================================================
-- 1. USERS
-- =========================================================

CREATE TABLE users (
    id CHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NULL,
    full_name VARCHAR(150) NOT NULL,
    avatar_url TEXT NULL,

    auth_provider ENUM('LOCAL', 'GOOGLE') NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255) NULL,

    learning_goal ENUM('TOEIC', 'IELTS') NULL,
    current_level_id CHAR(36) NULL,
    target_level_id CHAR(36) NULL,

    daily_new_words_goal INT NOT NULL DEFAULT 10,
    timezone VARCHAR(100) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',

    is_active TINYINT(1) NOT NULL DEFAULT 1,
    last_login_at DATETIME(3) NULL,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3) NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    UNIQUE KEY uq_users_provider (auth_provider, provider_id),
    KEY idx_users_learning_goal (learning_goal),
    KEY idx_users_target_level_id (target_level_id),
    KEY idx_users_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- current_level_id and target_level_id foreign keys are added after learning_levels table exists.

-- =========================================================
-- 2. REFRESH TOKENS
-- Optional but recommended for JWT refresh flow.
-- =========================================================

CREATE TABLE refresh_tokens (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME(3) NOT NULL,
    revoked_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),
    KEY idx_refresh_tokens_user_id (user_id),
    KEY idx_refresh_tokens_expires_at (expires_at),
    KEY idx_refresh_tokens_revoked_at (revoked_at),

    CONSTRAINT fk_refresh_tokens_user
      FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 3. LEARNING PATHS
-- Examples: TOEIC, IELTS
-- =========================================================

CREATE TABLE learning_paths (
    id CHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),
    UNIQUE KEY uq_learning_paths_code (code),
    KEY idx_learning_paths_active_order (is_active, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 4. LEARNING LEVELS
-- Examples: TOEIC 450, IELTS 6.5
-- =========================================================

CREATE TABLE learning_levels (
    id CHAR(36) NOT NULL,
    learning_path_id CHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    min_score DECIMAL(5,2) NULL,
    max_score DECIMAL(5,2) NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),
    UNIQUE KEY uq_learning_levels_path_code (learning_path_id, code),
    KEY idx_learning_levels_path_order (learning_path_id, display_order),
    KEY idx_learning_levels_active (is_active),

    CONSTRAINT fk_learning_levels_path
      FOREIGN KEY (learning_path_id) REFERENCES learning_paths(id)
      ON DELETE RESTRICT
      ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE users
  ADD CONSTRAINT fk_users_current_level
    FOREIGN KEY (current_level_id) REFERENCES learning_levels(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

ALTER TABLE users
  ADD CONSTRAINT fk_users_target_level
    FOREIGN KEY (target_level_id) REFERENCES learning_levels(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =========================================================
-- 5. DECKS
-- SYSTEM: built-in decks maintained by app/admin.
-- USER: user-created decks, including Favorites default deck.
-- =========================================================

CREATE TABLE decks (
    id CHAR(36) NOT NULL,
    owner_user_id CHAR(36) NULL,

    learning_level_id CHAR(36) NULL,

    deck_type ENUM('SYSTEM', 'USER') NOT NULL DEFAULT 'USER',
    visibility ENUM('PRIVATE', 'PUBLIC') NOT NULL DEFAULT 'PRIVATE',

    name VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) NOT NULL,
    description TEXT NULL,
    tags JSON NULL,

    thumbnail_url TEXT NULL,
    display_order INT NOT NULL DEFAULT 0,

    total_words INT NOT NULL DEFAULT 0,

    is_default TINYINT(1) NOT NULL DEFAULT 0,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3) NULL,
    is_deleted TINYINT(1) GENERATED ALWAYS AS (IF(deleted_at IS NULL, 0, 1)) STORED,

    PRIMARY KEY (id),

    KEY idx_decks_owner_user_id (owner_user_id),
    KEY idx_decks_learning_level_id (learning_level_id),
    KEY idx_decks_type_visibility (deck_type, visibility),
    KEY idx_decks_deleted_at (deleted_at),
    KEY idx_decks_name (name),
    KEY idx_decks_owner_default (owner_user_id, is_default),

    UNIQUE KEY uq_user_active_deck_name (owner_user_id, normalized_name, is_deleted),

    CONSTRAINT fk_decks_owner_user
      FOREIGN KEY (owner_user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT fk_decks_learning_level
      FOREIGN KEY (learning_level_id) REFERENCES learning_levels(id)
      ON DELETE SET NULL
      ON UPDATE CASCADE,

    CONSTRAINT chk_decks_owner_by_type
      CHECK (
        (deck_type = 'SYSTEM' AND owner_user_id IS NULL)
        OR
        (deck_type = 'USER' AND owner_user_id IS NOT NULL)
      ),

    CONSTRAINT chk_decks_default_user_only
      CHECK (
        (is_default = 0)
        OR
        (is_default = 1 AND deck_type = 'USER' AND owner_user_id IS NOT NULL)
      )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- NOTE:
-- The application must create exactly one default Favorites deck for each user after register.
-- MySQL CHECK cannot enforce "one default deck per user" cleanly without more generated logic.
-- Backend should enforce that only one active is_default deck exists for each user.

-- =========================================================
-- 6. VOCABULARIES
-- A vocabulary entry belongs to exactly one deck.
-- Same word with different meanings is allowed.
-- source_vocabulary_id is used for copied/favorite vocabularies.
-- =========================================================

CREATE TABLE vocabularies (
    id CHAR(36) NOT NULL,
    deck_id CHAR(36) NOT NULL,

    source_vocabulary_id CHAR(36) NULL,

    word VARCHAR(150) NOT NULL,
    normalized_word VARCHAR(150) NOT NULL,

    pronunciation VARCHAR(255) NULL,

    meaning TEXT NOT NULL,
    normalized_meaning VARCHAR(500) NOT NULL,

    description_en TEXT NULL,
    example TEXT NULL,
    collocation TEXT NULL,
    related_words TEXT NULL,
    note TEXT NULL,

    audio_url TEXT NULL,
    image_url TEXT NULL,

    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NULL,
    part_of_speech VARCHAR(50) NULL,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3) NULL,
    is_deleted TINYINT(1) GENERATED ALWAYS AS (IF(deleted_at IS NULL, 0, 1)) STORED,

    PRIMARY KEY (id),

    KEY idx_vocabularies_deck_id (deck_id),
    KEY idx_vocabularies_source_id (source_vocabulary_id),
    KEY idx_vocabularies_word (word),
    KEY idx_vocabularies_normalized_word (normalized_word),
    KEY idx_vocabularies_deleted_at (deleted_at),

    UNIQUE KEY uq_vocabularies_deck_word_meaning_active
      (deck_id, normalized_word, normalized_meaning, is_deleted),

    FULLTEXT KEY ft_vocabularies_search
      (word, meaning, description_en, example, collocation, related_words, note),

    CONSTRAINT fk_vocabularies_deck
      FOREIGN KEY (deck_id) REFERENCES decks(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT fk_vocabularies_source
      FOREIGN KEY (source_vocabulary_id) REFERENCES vocabularies(id)
      ON DELETE SET NULL
      ON UPDATE CASCADE,

    CONSTRAINT chk_vocabularies_not_self_source
      CHECK (source_vocabulary_id IS NULL OR source_vocabulary_id <> id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- IMPORTANT:
-- Manual duplicate exact is checked by deck_id + normalized_word + normalized_meaning.
-- Favorites duplicate is checked by backend using favoritesDeckId + source_vocabulary_id.
-- Do not use user_favorite_vocabularies table.

-- =========================================================
-- 7. REVIEW CARDS
-- One card represents one user's SRS state for one vocabulary.
-- Do not add deck_id here.
-- =========================================================

CREATE TABLE review_cards (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    vocabulary_id CHAR(36) NOT NULL,

    status ENUM('NEW', 'LEARNING', 'REVIEW', 'MASTERED', 'SUSPENDED') NOT NULL DEFAULT 'NEW',

    repetition INT NOT NULL DEFAULT 0,
    interval_days INT NOT NULL DEFAULT 0,
    ease_factor DECIMAL(4,2) NOT NULL DEFAULT 2.50,

    due_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    last_reviewed_at DATETIME(3) NULL,
    first_learned_at DATETIME(3) NULL,

    lapses INT NOT NULL DEFAULT 0,
    total_reviews INT NOT NULL DEFAULT 0,
    correct_reviews INT NOT NULL DEFAULT 0,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    UNIQUE KEY uq_review_cards_user_vocab (user_id, vocabulary_id),
    KEY idx_review_cards_user_due (user_id, due_at),
    KEY idx_review_cards_user_status (user_id, status),
    KEY idx_review_cards_vocabulary_id (vocabulary_id),

    CONSTRAINT fk_review_cards_user
      FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT fk_review_cards_vocabulary
      FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT chk_review_cards_ease_factor
      CHECK (ease_factor >= 1.30),

    CONSTRAINT chk_review_cards_interval
      CHECK (interval_days >= 0),

    CONSTRAINT chk_review_cards_repetition
      CHECK (repetition >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 8. REVIEW LOGS
-- Stores every review event.
-- =========================================================

CREATE TABLE review_logs (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    review_card_id CHAR(36) NOT NULL,
    vocabulary_id CHAR(36) NOT NULL,

    rating ENUM('AGAIN', 'HARD', 'GOOD', 'EASY') NOT NULL,
    quality INT NOT NULL,
    is_correct TINYINT(1) NOT NULL DEFAULT 1,

    old_repetition INT NOT NULL DEFAULT 0,
    new_repetition INT NOT NULL DEFAULT 0,

    old_interval_days INT NOT NULL DEFAULT 0,
    new_interval_days INT NOT NULL DEFAULT 0,

    old_ease_factor DECIMAL(4,2) NOT NULL DEFAULT 2.50,
    new_ease_factor DECIMAL(4,2) NOT NULL DEFAULT 2.50,

    old_due_at DATETIME(3) NULL,
    new_due_at DATETIME(3) NOT NULL,

    reviewed_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    KEY idx_review_logs_user_reviewed_at (user_id, reviewed_at),
    KEY idx_review_logs_card_reviewed_at (review_card_id, reviewed_at),
    KEY idx_review_logs_vocabulary_id (vocabulary_id),
    KEY idx_review_logs_rating (rating),

    CONSTRAINT fk_review_logs_user
      FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT fk_review_logs_card
      FOREIGN KEY (review_card_id) REFERENCES review_cards(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT fk_review_logs_vocabulary
      FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT chk_review_logs_quality
      CHECK (quality BETWEEN 0 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 9. PRACTICE SESSIONS
-- =========================================================

CREATE TABLE practice_sessions (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    deck_id CHAR(36) NULL,

    practice_type ENUM('MULTIPLE_CHOICE', 'FILL_IN_BLANK', 'LISTENING') NOT NULL,

    total_questions INT NOT NULL DEFAULT 0,
    correct_answers INT NOT NULL DEFAULT 0,
    wrong_answers INT NOT NULL DEFAULT 0,
    accuracy DECIMAL(5,2) NOT NULL DEFAULT 0.00,

    status ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',

    started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    finished_at DATETIME(3) NULL,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    KEY idx_practice_sessions_user_started (user_id, started_at),
    KEY idx_practice_sessions_deck_id (deck_id),
    KEY idx_practice_sessions_type (practice_type),
    KEY idx_practice_sessions_status (status),

    CONSTRAINT fk_practice_sessions_user
      FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT fk_practice_sessions_deck
      FOREIGN KEY (deck_id) REFERENCES decks(id)
      ON DELETE SET NULL
      ON UPDATE CASCADE,

    CONSTRAINT chk_practice_sessions_total
      CHECK (total_questions >= 0),

    CONSTRAINT chk_practice_sessions_accuracy
      CHECK (accuracy >= 0.00 AND accuracy <= 100.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 10. PRACTICE ANSWERS
-- =========================================================

CREATE TABLE practice_answers (
    id CHAR(36) NOT NULL,
    session_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    vocabulary_id CHAR(36) NULL,

    question_type ENUM('WORD_TO_MEANING', 'MEANING_TO_WORD', 'FILL_IN_BLANK', 'LISTENING_WORD') NOT NULL,

    question_text TEXT NOT NULL,
    options_json JSON NULL,

    user_answer TEXT NULL,
    correct_answer TEXT NOT NULL,
    is_correct TINYINT(1) NOT NULL DEFAULT 0,

    answered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    KEY idx_practice_answers_session_id (session_id),
    KEY idx_practice_answers_user_answered (user_id, answered_at),
    KEY idx_practice_answers_vocab (vocabulary_id),
    KEY idx_practice_answers_is_correct (is_correct),

    CONSTRAINT fk_practice_answers_session
      FOREIGN KEY (session_id) REFERENCES practice_sessions(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT fk_practice_answers_user
      FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT fk_practice_answers_vocabulary
      FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id)
      ON DELETE SET NULL
      ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 11. DAILY ACTIVITIES
-- Used for streak and dashboard.
-- =========================================================

CREATE TABLE daily_activities (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    activity_date DATE NOT NULL,

    new_words_count INT NOT NULL DEFAULT 0,
    review_words_count INT NOT NULL DEFAULT 0,
    practice_sessions_count INT NOT NULL DEFAULT 0,

    correct_count INT NOT NULL DEFAULT 0,
    wrong_count INT NOT NULL DEFAULT 0,

    total_learning_seconds INT NOT NULL DEFAULT 0,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    UNIQUE KEY uq_daily_activities_user_date (user_id, activity_date),
    KEY idx_daily_activities_user_date (user_id, activity_date),

    CONSTRAINT fk_daily_activities_user
      FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT chk_daily_activities_counts
      CHECK (
        new_words_count >= 0
        AND review_words_count >= 0
        AND practice_sessions_count >= 0
        AND correct_count >= 0
        AND wrong_count >= 0
        AND total_learning_seconds >= 0
      )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 12. NOTIFICATION SETTINGS
-- =========================================================

CREATE TABLE notification_settings (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,

    daily_reminder_enabled TINYINT(1) NOT NULL DEFAULT 1,
    daily_reminder_time TIME NOT NULL DEFAULT '20:00:00',

    due_review_reminder_enabled TINYINT(1) NOT NULL DEFAULT 1,

    push_enabled TINYINT(1) NOT NULL DEFAULT 1,
    email_enabled TINYINT(1) NOT NULL DEFAULT 0,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    UNIQUE KEY uq_notification_settings_user (user_id),

    CONSTRAINT fk_notification_settings_user
      FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 13. DEVICE TOKENS
-- Used for FCM push notification.
-- Optional in MVP.
-- =========================================================

CREATE TABLE device_tokens (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,

    token TEXT NOT NULL,
    platform ENUM('ANDROID', 'IOS', 'WEB') NOT NULL DEFAULT 'ANDROID',
    device_name VARCHAR(255) NULL,

    is_active TINYINT(1) NOT NULL DEFAULT 1,
    last_used_at DATETIME(3) NULL,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    KEY idx_device_tokens_user (user_id),
    KEY idx_device_tokens_platform (platform),
    KEY idx_device_tokens_active (is_active),

    CONSTRAINT fk_device_tokens_user
      FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 14. IMPORT JOBS
-- Tracks CSV import results.
-- =========================================================

CREATE TABLE import_jobs (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    deck_id CHAR(36) NOT NULL,

    file_name VARCHAR(255) NOT NULL,
    file_type ENUM('CSV', 'XLSX') NOT NULL DEFAULT 'CSV',

    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'PARTIAL_SUCCESS') NOT NULL DEFAULT 'PENDING',

    total_rows INT NOT NULL DEFAULT 0,
    success_rows INT NOT NULL DEFAULT 0,
    duplicate_rows INT NOT NULL DEFAULT 0,
    failed_rows INT NOT NULL DEFAULT 0,

    error_report_json JSON NULL,

    started_at DATETIME(3) NULL,
    finished_at DATETIME(3) NULL,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    KEY idx_import_jobs_user_created (user_id, created_at),
    KEY idx_import_jobs_deck (deck_id),
    KEY idx_import_jobs_status (status),

    CONSTRAINT fk_import_jobs_user
      FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT fk_import_jobs_deck
      FOREIGN KEY (deck_id) REFERENCES decks(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,

    CONSTRAINT chk_import_jobs_counts
      CHECK (
        total_rows >= 0
        AND success_rows >= 0
        AND duplicate_rows >= 0
        AND failed_rows >= 0
      )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 15. SEED DATA
-- UUID values are fixed for easier development.
-- =========================================================

-- Learning Paths
INSERT INTO learning_paths (id, code, name, description, display_order, is_active)
VALUES
('11111111-1111-1111-1111-111111111111', 'TOEIC', 'TOEIC', 'Lộ trình học từ vựng TOEIC', 1, 1),
('22222222-2222-2222-2222-222222222222', 'IELTS', 'IELTS', 'Lộ trình học từ vựng IELTS', 2, 1);

-- TOEIC Levels
INSERT INTO learning_levels (id, learning_path_id, code, name, description, min_score, max_score, display_order, is_active)
VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '11111111-1111-1111-1111-111111111111', 'TOEIC_450', 'TOEIC 450+', 'Từ vựng nền tảng cho mục tiêu TOEIC 450+', 0, 450, 1, 1),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '11111111-1111-1111-1111-111111111111', 'TOEIC_600', 'TOEIC 600+', 'Từ vựng trung cấp cho mục tiêu TOEIC 600+', 451, 600, 2, 1),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', '11111111-1111-1111-1111-111111111111', 'TOEIC_750', 'TOEIC 750+', 'Từ vựng nâng cao cho mục tiêu TOEIC 750+', 601, 750, 3, 1),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', '11111111-1111-1111-1111-111111111111', 'TOEIC_900', 'TOEIC 900+', 'Từ vựng nâng cao cho mục tiêu TOEIC 900+', 751, 990, 4, 1);

-- IELTS Levels
INSERT INTO learning_levels (id, learning_path_id, code, name, description, min_score, max_score, display_order, is_active)
VALUES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', '22222222-2222-2222-2222-222222222222', 'IELTS_4_0', 'IELTS 4.0+', 'Từ vựng nền tảng cho IELTS 4.0+', 0, 4.0, 1, 1),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '22222222-2222-2222-2222-222222222222', 'IELTS_5_5', 'IELTS 5.5+', 'Từ vựng trung cấp cho IELTS 5.5+', 4.5, 5.5, 2, 1),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3', '22222222-2222-2222-2222-222222222222', 'IELTS_6_5', 'IELTS 6.5+', 'Từ vựng học thuật cho IELTS 6.5+', 6.0, 6.5, 3, 1),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4', '22222222-2222-2222-2222-222222222222', 'IELTS_7_0', 'IELTS 7.0+', 'Từ vựng nâng cao cho IELTS 7.0+', 7.0, 9.0, 4, 1);

-- System Decks
INSERT INTO decks (
    id, owner_user_id, learning_level_id,
    deck_type, visibility, name, normalized_name, description, tags, display_order, total_words, is_default
)
VALUES
(
    'dddddddd-dddd-dddd-dddd-dddddddddd01',
    NULL,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
    'SYSTEM',
    'PUBLIC',
    'TOEIC 450 - Daily Life',
    'toeic 450 - daily life',
    'Bộ từ vựng TOEIC nền tảng về đời sống hằng ngày.',
    JSON_ARRAY('TOEIC', 'Daily Life', 'Beginner'),
    1,
    0,
    0
),
(
    'dddddddd-dddd-dddd-dddd-dddddddddd02',
    NULL,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
    'SYSTEM',
    'PUBLIC',
    'TOEIC 600 - Office & Business',
    'toeic 600 - office & business',
    'Bộ từ vựng TOEIC về công sở và kinh doanh.',
    JSON_ARRAY('TOEIC', 'Business', 'Office'),
    2,
    0,
    0
),
(
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01',
    NULL,
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2',
    'SYSTEM',
    'PUBLIC',
    'IELTS 5.5 - Common Topics',
    'ielts 5.5 - common topics',
    'Bộ từ vựng IELTS theo các chủ đề phổ biến.',
    JSON_ARRAY('IELTS', 'Common Topics', 'Intermediate'),
    1,
    0,
    0
),
(
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02',
    NULL,
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3',
    'SYSTEM',
    'PUBLIC',
    'IELTS 6.5 - Academic Vocabulary',
    'ielts 6.5 - academic vocabulary',
    'Bộ từ vựng học thuật cho IELTS band 6.5+.',
    JSON_ARRAY('IELTS', 'Academic', 'Advanced'),
    2,
    0,
    0
);

-- Sample Vocabularies
INSERT INTO vocabularies (
    id, deck_id, source_vocabulary_id,
    word, normalized_word, pronunciation,
    meaning, normalized_meaning,
    description_en, example, collocation, related_words, note,
    difficulty, part_of_speech
)
VALUES
(
    'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv001',
    'dddddddd-dddd-dddd-dddd-dddddddddd01',
    NULL,
    'appointment',
    'appointment',
    '/əˈpɔɪnt.mənt/',
    'cuộc hẹn',
    'cuộc hẹn',
    'An arrangement to meet someone at a particular time and place.',
    'I have an appointment with the manager at 9 a.m.',
    'make an appointment; schedule an appointment; doctor appointment',
    'meeting; schedule; arrangement',
    'Common in TOEIC office and daily life contexts.',
    'MEDIUM',
    'noun'
),
(
    'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv002',
    'dddddddd-dddd-dddd-dddd-dddddddddd01',
    NULL,
    'purchase',
    'purchase',
    '/ˈpɝː.tʃəs/',
    'mua hàng; sự mua hàng',
    'mua hàng; sự mua hàng',
    'To buy something.',
    'Customers can purchase tickets online.',
    'make a purchase; purchase order; purchase price',
    'buy; order; payment',
    NULL,
    'EASY',
    'verb/noun'
),
(
    'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv003',
    'dddddddd-dddd-dddd-dddd-dddddddddd01',
    NULL,
    'receipt',
    'receipt',
    '/rɪˈsiːt/',
    'hóa đơn; biên lai',
    'hóa đơn; biên lai',
    'A piece of paper or digital record showing that money has been paid.',
    'Please keep your receipt for future reference.',
    'sales receipt; keep a receipt; receipt number',
    'invoice; bill; payment',
    NULL,
    'EASY',
    'noun'
),
(
    'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv101',
    'dddddddd-dddd-dddd-dddd-dddddddddd02',
    NULL,
    'deadline',
    'deadline',
    '/ˈded.laɪn/',
    'hạn chót',
    'hạn chót',
    'The latest time or date by which something should be completed.',
    'The deadline for the report is Friday.',
    'meet a deadline; miss a deadline; tight deadline',
    'due date; schedule; timeline',
    NULL,
    'MEDIUM',
    'noun'
),
(
    'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv102',
    'dddddddd-dddd-dddd-dddd-dddddddddd02',
    NULL,
    'negotiate',
    'negotiate',
    '/nəˈɡoʊ.ʃi.eɪt/',
    'đàm phán',
    'đàm phán',
    'To discuss something in order to reach an agreement.',
    'The company will negotiate a new contract.',
    'negotiate a contract; negotiate terms; negotiate with clients',
    'discuss; bargain; agree',
    NULL,
    'HARD',
    'verb'
),
(
    'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv201',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01',
    NULL,
    'environment',
    'environment',
    '/ɪnˈvaɪ.rən.mənt/',
    'môi trường',
    'môi trường',
    'The air, water, and land in or on which people, animals, and plants live.',
    'Many governments are taking action to protect the environment.',
    'protect the environment; natural environment; environmental issue',
    'nature; ecosystem; surroundings',
    NULL,
    'EASY',
    'noun'
),
(
    'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv202',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01',
    NULL,
    'significant',
    'significant',
    '/sɪɡˈnɪf.ə.kənt/',
    'đáng kể; quan trọng',
    'đáng kể; quan trọng',
    'Important or large enough to be noticed.',
    'There has been a significant increase in online learning.',
    'significant impact; significant change; statistically significant',
    'important; notable; considerable',
    NULL,
    'MEDIUM',
    'adjective'
),
(
    'vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvv301',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02',
    NULL,
    'sustainable',
    'sustainable',
    '/səˈsteɪ.nə.bəl/',
    'bền vững',
    'bền vững',
    'Able to continue over a period of time without damaging the environment or using too many resources.',
    'Countries need to develop sustainable energy sources.',
    'sustainable development; sustainable growth; sustainable energy',
    'renewable; long-term; eco-friendly',
    'Common in IELTS Writing Task 2.',
    'HARD',
    'adjective'
);

-- Update total_words for seeded decks
UPDATE decks d
SET total_words = (
    SELECT COUNT(*)
    FROM vocabularies v
    WHERE v.deck_id = d.id
      AND v.deleted_at IS NULL
);

-- =========================================================
-- 16. VIEWS
-- =========================================================

CREATE OR REPLACE VIEW v_deck_stats AS
SELECT
    d.id AS deck_id,
    d.name AS deck_name,
    d.deck_type,
    d.owner_user_id,
    d.learning_level_id,
    ll.learning_path_id,
    d.is_default,
    COUNT(v.id) AS total_words
FROM decks d
LEFT JOIN learning_levels ll
    ON ll.id = d.learning_level_id
LEFT JOIN vocabularies v
    ON v.deck_id = d.id
   AND v.deleted_at IS NULL
WHERE d.deleted_at IS NULL
GROUP BY
    d.id,
    d.name,
    d.deck_type,
    d.owner_user_id,
    d.learning_level_id,
    ll.learning_path_id,
    d.is_default;

CREATE OR REPLACE VIEW v_user_review_stats AS
SELECT
    rc.user_id,
    COUNT(rc.id) AS total_review_cards,
    SUM(CASE WHEN rc.status IN ('LEARNING', 'REVIEW', 'MASTERED') THEN 1 ELSE 0 END) AS learned_words,
    SUM(CASE WHEN rc.due_at <= UTC_TIMESTAMP(3) THEN 1 ELSE 0 END) AS due_now,
    SUM(rc.total_reviews) AS total_reviews,
    SUM(rc.correct_reviews) AS correct_reviews
FROM review_cards rc
GROUP BY rc.user_id;

-- =========================================================
-- 17. STORED PROCEDURES
-- =========================================================

DELIMITER //

CREATE PROCEDURE sp_recalculate_deck_total_words(IN p_deck_id CHAR(36))
BEGIN
    UPDATE decks
    SET total_words = (
        SELECT COUNT(*)
        FROM vocabularies
        WHERE deck_id = p_deck_id
          AND deleted_at IS NULL
    )
    WHERE id = p_deck_id;
END //

DELIMITER ;

-- =========================================================
-- END OF SCRIPT
-- =========================================================
```

---

## 5. Bảng và mục đích

### 5.1 `users`

Lưu tài khoản người dùng.

Quan trọng:

- `password_hash`: dùng bcrypt.
- `auth_provider`: `LOCAL` hoặc `GOOGLE`.
- `learning_goal`: TOEIC/IELTS.
- `target_level_id`: level mục tiêu.
- `daily_new_words_goal`: số từ mới mỗi ngày.

Khi tạo user, backend phải tạo thêm:

- `notification_settings`
- default deck `Favorites`

### 5.2 `refresh_tokens`

Lưu refresh token đã hash.

Nếu MVP chỉ dùng access token ngắn hạn và logout client-side thì có thể chưa dùng.

### 5.3 `learning_paths`

Lưu hướng học:

- TOEIC
- IELTS

### 5.4 `learning_levels`

Lưu level theo learning path:

- TOEIC 450
- TOEIC 600
- IELTS 5.5
- IELTS 6.5

### 5.5 `decks`

Lưu bộ từ vựng.

Có 2 loại:

- `SYSTEM`: bộ từ có sẵn.
- `USER`: bộ từ người dùng tạo, bao gồm Favorites.

Field quan trọng:

- `is_default = 1`: deck mặc định, hiện tại dùng cho Favorites.
- `normalized_name`: dùng check trùng tên deck cá nhân.
- `learning_level_id`: system deck thuộc level nào.

Rules:

- `SYSTEM` deck có `owner_user_id = NULL`.
- `USER` deck phải có `owner_user_id`.
- Favorites là `USER` deck với `is_default = 1`.
- Favorites không được xóa.
- Favorites không được import trực tiếp.
- Favorites chỉ nhận từ qua nút tim.

### 5.6 `vocabularies`

Lưu vocabulary entry trong deck.

Field quan trọng:

- `deck_id`: vocabulary thuộc deck nào.
- `source_vocabulary_id`: nếu vocabulary là bản copy/favorite, lưu id vocabulary gốc.
- `word`: dữ liệu hiển thị.
- `normalized_word`: dữ liệu kỹ thuật để search/check trùng.
- `meaning`: nghĩa hiển thị.
- `normalized_meaning`: dữ liệu kỹ thuật để check trùng.
- `audio_url`: future, MVP dùng Android TextToSpeech.

Duplicate exact trong cùng deck:

```text
deck_id + normalized_word + normalized_meaning + is_deleted
```

Cùng word khác meaning được phép tồn tại.

### 5.7 `review_cards`

Lưu trạng thái SRS hiện tại của mỗi user với mỗi vocabulary.

Đây là bảng quan trọng nhất cho SM-2.

Không có `deck_id` vì SRS state phải dựa trên `user_id + vocabulary_id`.

### 5.8 `review_logs`

Lưu lịch sử từng lần ôn.

Dùng cho:

- accuracy
- retention
- daily activity
- audit lịch sử học

### 5.9 `practice_sessions`

Lưu một lần làm bài luyện tập.

Loại practice:

- MULTIPLE_CHOICE
- FILL_IN_BLANK
- LISTENING

### 5.10 `practice_answers`

Lưu từng câu trả lời trong session.

### 5.11 `daily_activities`

Lưu dữ liệu hoạt động theo ngày để tính dashboard và streak.

### 5.12 `notification_settings`

Lưu cấu hình nhắc học.

### 5.13 `device_tokens`

Lưu FCM token, optional.

### 5.14 `import_jobs`

Theo dõi import CSV/XLSX.

Có thêm:

- `duplicate_rows`
- `error_report_json`

MVP dùng CSV trước.

---

## 6. Query mẫu cho backend

### 6.1 Lấy decks theo TOEIC/IELTS và level

```sql
SELECT d.*
FROM decks d
JOIN learning_levels ll ON ll.id = d.learning_level_id
JOIN learning_paths lp ON lp.id = ll.learning_path_id
WHERE lp.code = 'TOEIC'
  AND ll.code = 'TOEIC_600'
  AND d.deck_type = 'SYSTEM'
  AND d.deleted_at IS NULL
ORDER BY d.display_order, d.name;
```

### 6.2 Lấy user decks, bao gồm Favorites

```sql
SELECT *
FROM decks
WHERE owner_user_id = ?
  AND deck_type = 'USER'
  AND deleted_at IS NULL
ORDER BY is_default DESC, updated_at DESC;
```

### 6.3 Lấy Favorites deck của user

```sql
SELECT *
FROM decks
WHERE owner_user_id = ?
  AND deck_type = 'USER'
  AND is_default = 1
  AND normalized_name = 'favorites'
  AND deleted_at IS NULL
LIMIT 1;
```

### 6.4 Lấy vocabulary trong deck

```sql
SELECT *
FROM vocabularies
WHERE deck_id = ?
  AND deleted_at IS NULL
ORDER BY word ASC, meaning ASC;
```

### 6.5 Check duplicate exact khi thêm thủ công

```sql
SELECT *
FROM vocabularies
WHERE deck_id = ?
  AND normalized_word = ?
  AND normalized_meaning = ?
  AND deleted_at IS NULL
LIMIT 1;
```

### 6.6 Check same word different meaning khi thêm thủ công

```sql
SELECT *
FROM vocabularies
WHERE deck_id = ?
  AND normalized_word = ?
  AND normalized_meaning <> ?
  AND deleted_at IS NULL
ORDER BY created_at DESC;
```

### 6.7 Check Favorites theo source

```sql
SELECT *
FROM vocabularies
WHERE deck_id = ?
  AND source_vocabulary_id = ?
  AND deleted_at IS NULL
LIMIT 1;
```

### 6.8 Copy vocabulary vào Favorites

```sql
INSERT INTO vocabularies (
    id,
    deck_id,
    source_vocabulary_id,
    word,
    normalized_word,
    pronunciation,
    meaning,
    normalized_meaning,
    description_en,
    example,
    collocation,
    related_words,
    note,
    audio_url,
    image_url,
    difficulty,
    part_of_speech
)
SELECT
    ? AS id,
    ? AS favorites_deck_id,
    v.id AS source_vocabulary_id,
    v.word,
    v.normalized_word,
    v.pronunciation,
    v.meaning,
    v.normalized_meaning,
    v.description_en,
    v.example,
    v.collocation,
    v.related_words,
    v.note,
    v.audio_url,
    v.image_url,
    v.difficulty,
    v.part_of_speech
FROM vocabularies v
WHERE v.id = ?
  AND v.deleted_at IS NULL;
```

### 6.9 Lấy due review cards global

```sql
SELECT
    rc.*,
    v.word,
    v.pronunciation,
    v.meaning,
    v.example,
    v.collocation,
    v.deck_id
FROM review_cards rc
JOIN vocabularies v ON v.id = rc.vocabulary_id
WHERE rc.user_id = ?
  AND rc.due_at <= UTC_TIMESTAMP(3)
  AND rc.status <> 'SUSPENDED'
  AND v.deleted_at IS NULL
ORDER BY rc.due_at ASC
LIMIT 50;
```

### 6.10 Lấy due review cards theo deck

```sql
SELECT
    rc.*,
    v.word,
    v.pronunciation,
    v.meaning,
    v.example,
    v.collocation,
    v.deck_id
FROM review_cards rc
JOIN vocabularies v ON v.id = rc.vocabulary_id
WHERE rc.user_id = ?
  AND v.deck_id = ?
  AND rc.due_at <= UTC_TIMESTAMP(3)
  AND rc.status <> 'SUSPENDED'
  AND v.deleted_at IS NULL
ORDER BY rc.due_at ASC
LIMIT 50;
```

### 6.11 Dashboard cơ bản

```sql
SELECT
    COUNT(DISTINCT rc.id) AS total_review_cards,
    SUM(CASE WHEN rc.status IN ('LEARNING', 'REVIEW', 'MASTERED') THEN 1 ELSE 0 END) AS learned_words,
    SUM(CASE WHEN rc.due_at <= UTC_TIMESTAMP(3) THEN 1 ELSE 0 END) AS due_today,
    SUM(rc.total_reviews) AS total_reviews,
    SUM(rc.correct_reviews) AS correct_reviews,
    CASE
      WHEN SUM(rc.total_reviews) = 0 THEN 0
      ELSE ROUND(SUM(rc.correct_reviews) * 100 / SUM(rc.total_reviews), 2)
    END AS accuracy
FROM review_cards rc
WHERE rc.user_id = ?;
```

### 6.12 Deck progress

```sql
SELECT
    d.id AS deck_id,
    d.name AS deck_name,
    COUNT(v.id) AS total_words,
    SUM(CASE WHEN rc.status IN ('LEARNING', 'REVIEW', 'MASTERED') THEN 1 ELSE 0 END) AS learned_words,
    SUM(CASE WHEN rc.due_at <= UTC_TIMESTAMP(3) THEN 1 ELSE 0 END) AS due_words
FROM decks d
LEFT JOIN vocabularies v
    ON v.deck_id = d.id
   AND v.deleted_at IS NULL
LEFT JOIN review_cards rc
    ON rc.vocabulary_id = v.id
   AND rc.user_id = ?
WHERE d.id = ?
  AND d.deleted_at IS NULL
GROUP BY d.id, d.name;
```

### 6.13 Activity 7 ngày gần nhất

```sql
SELECT *
FROM daily_activities
WHERE user_id = ?
  AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
ORDER BY activity_date ASC;
```

### 6.14 Search vocabulary

```sql
SELECT *
FROM vocabularies
WHERE deck_id = ?
  AND deleted_at IS NULL
  AND (
      word LIKE CONCAT('%', ?, '%')
      OR meaning LIKE CONCAT('%', ?, '%')
      OR example LIKE CONCAT('%', ?, '%')
  )
ORDER BY word ASC;
```

---

## 7. Backend implementation notes

### 7.1 Normalize function

Backend cần normalize word và meaning trước khi lưu/check trùng.

Gợi ý:

```text
trim
lowercase
replace multiple spaces by single space
```

Ví dụ:

```ts
function normalizeText(value: string): string {
  return value.trim().toLowerCase().replace(/\s+/g, ' ');
}
```

### 7.2 Khi tạo user

Backend phải:

1. Hash password bằng bcrypt.
2. Insert user.
3. Insert notification_settings mặc định.
4. Tạo Favorites deck:

```text
name = Favorites
normalized_name = favorites
deck_type = USER
owner_user_id = user.id
is_default = 1
visibility = PRIVATE
```

5. Trả JWT.

### 7.3 Khi tạo deck cá nhân

Backend cần set:

```text
deck_type = USER
owner_user_id = currentUser.id
visibility = PRIVATE
is_default = 0
```

Không cho user tự set `deck_type = SYSTEM` hoặc `is_default = 1`.

### 7.4 Khi thêm vocabulary thủ công

Backend process:

1. Kiểm tra deck tồn tại.
2. Kiểm tra deck là USER deck của current user.
3. Không cho thêm thủ công vào Favorites deck trong MVP.
4. Normalize word và meaning.
5. Check duplicate exact theo `deck_id + normalized_word + normalized_meaning`.
6. Nếu duplicate exact: trả `DUPLICATE_VOCABULARY`.
7. Check same word different meaning.
8. Nếu same word different meaning và request chưa có `allowSameWordDifferentMeaning = true`: trả `WORD_EXISTS_WITH_DIFFERENT_MEANING`.
9. Nếu hợp lệ: insert vocabulary.
10. Cập nhật `decks.total_words`.

### 7.5 Khi import CSV

Backend process:

1. Kiểm tra deck tồn tại.
2. Kiểm tra deck là USER deck của current user.
3. Không cho import vào Favorites deck.
4. Parse CSV.
5. Validate `word` và `meaning`.
6. Normalize word và meaning.
7. Check duplicate trong chính file bằng `normalized_word + normalized_meaning`.
8. Query DB theo batch để lấy các vocabulary đang có trong deck.
9. Skip duplicate.
10. Insert batch các row hợp lệ.
11. Ghi `import_jobs`.
12. Cập nhật `success_rows`, `duplicate_rows`, `failed_rows`, `error_report_json`.
13. Cập nhật `decks.total_words`.

Import không được query DB từng row.

### 7.6 Khi bấm tim / favorite vocabulary

Backend process:

1. Tìm vocabulary gốc.
2. Kiểm tra vocabulary gốc còn active.
3. Tìm Favorites deck của current user.
4. Check trong Favorites có active vocabulary nào có `source_vocabulary_id = originalVocabularyId` chưa.
5. Nếu đã có: trả `already_favorited`.
6. Nếu chưa có: copy vocabulary gốc sang Favorites deck.
7. Set `source_vocabulary_id = originalVocabularyId`.
8. Cập nhật `Favorites.total_words`.

Khi bỏ tim:

1. Tìm Favorites deck.
2. Tìm active vocabulary trong Favorites theo `source_vocabulary_id = originalVocabularyId`.
3. Soft delete vocabulary đó.
4. Cập nhật `Favorites.total_words`.

### 7.7 Khi bắt đầu học từ mới

Backend cần tạo `review_cards` nếu chưa có.

Default:

```text
status = NEW
repetition = 0
interval_days = 0
ease_factor = 2.50
due_at = now
```

### 7.8 Khi review flashcard

Backend process:

1. Nhận `vocabularyId`, `rating`, `reviewedAt`.
2. Tìm hoặc tạo review_card.
3. Mapping rating sang quality.
4. Tính SM-2.
5. Update review_card.
6. Insert review_log.
7. Update daily_activities.
8. Trả review_card mới.

### 7.9 Khi review theo deck

Không cần `deck_id` trong `review_cards`.

Backend chỉ filter qua `vocabularies.deck_id`.

### 7.10 Khi làm practice

Backend process:

1. Tạo practice_session.
2. Generate questions từ vocabulary trong deck.
3. Khi user trả lời, insert practice_answer.
4. Khi finish, update total/correct/wrong/accuracy.
5. Update daily_activities.

---

## 8. Prisma schema mapping notes

Nếu dùng Prisma, các MySQL enum nên map thành Prisma enum:

```prisma
enum AuthProvider {
  LOCAL
  GOOGLE
}

enum LearningGoal {
  TOEIC
  IELTS
}

enum DeckType {
  SYSTEM
  USER
}

enum DeckVisibility {
  PRIVATE
  PUBLIC
}

enum ReviewRating {
  AGAIN
  HARD
  GOOD
  EASY
}
```

Với UUID string:

```prisma
id String @id @db.Char(36)
```

Với JSON:

```prisma
tags Json?
optionsJson Json? @map("options_json")
```

Generated columns như `is_deleted` có thể cần dùng raw SQL migration nếu Prisma không quản lý tốt.

---

## 9. MVP database checklist

Database đủ cho MVP khi có:

- users
- refresh_tokens, optional
- learning_paths
- learning_levels
- decks
- vocabularies
- review_cards
- review_logs
- practice_sessions
- practice_answers
- daily_activities
- notification_settings
- device_tokens, optional
- import_jobs

Không có:

- user_favorite_vocabularies
- deck_vocabularies many-to-many

---

## 10. Important rules for agent

1. Không tạo bảng `user_favorite_vocabularies`.
2. Favorites là default USER deck.
3. Khi tạo user, luôn tạo Favorites deck.
4. Favorites deck có `is_default = 1`.
5. Favorites deck không được xóa.
6. Favorites deck không cho import trực tiếp.
7. Favorites deck không cho thêm từ thủ công trong MVP.
8. Khi bấm tim, copy vocabulary vào Favorites.
9. Bản copy trong Favorites phải có `source_vocabulary_id`.
10. Check trùng trong Favorites bằng `source_vocabulary_id`.
11. Manual add check duplicate exact bằng `deck_id + normalized_word + normalized_meaning`.
12. Manual add cùng word khác meaning phải hỗ trợ popup flow.
13. Import CSV skip duplicate và trả report.
14. Import không fail toàn bộ file nếu một số row trùng/lỗi.
15. Không query DB từng row khi import.
16. Same word different meaning được phép tồn tại.
17. ReviewCard dựa trên `user_id + vocabulary_id`.
18. Không thêm `deck_id` vào review_cards.
19. Deck review filter qua `vocabularies.deck_id`.
20. Backend tính SM-2 trong MVP.
21. Android dùng TextToSpeech cho audio trong MVP.
