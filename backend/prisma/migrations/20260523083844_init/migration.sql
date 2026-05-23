-- CreateTable
CREATE TABLE `users` (
    `id` CHAR(36) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password_hash` VARCHAR(255) NULL,
    `full_name` VARCHAR(150) NOT NULL,
    `avatar_url` TEXT NULL,
    `auth_provider` ENUM('LOCAL', 'GOOGLE') NOT NULL DEFAULT 'LOCAL',
    `provider_id` VARCHAR(255) NULL,
    `learning_goal` ENUM('TOEIC', 'IELTS') NULL,
    `current_level_id` CHAR(36) NULL,
    `target_level_id` CHAR(36) NULL,
    `daily_new_words_goal` INTEGER NOT NULL DEFAULT 10,
    `timezone` VARCHAR(100) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
    `is_active` BOOLEAN NOT NULL DEFAULT true,
    `last_login_at` DATETIME(3) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,
    `deleted_at` DATETIME(3) NULL,

    UNIQUE INDEX `users_email_key`(`email`),
    INDEX `users_learning_goal_idx`(`learning_goal`),
    INDEX `users_target_level_id_idx`(`target_level_id`),
    INDEX `users_deleted_at_idx`(`deleted_at`),
    UNIQUE INDEX `users_auth_provider_provider_id_key`(`auth_provider`, `provider_id`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `refresh_tokens` (
    `id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `token_hash` VARCHAR(255) NOT NULL,
    `expires_at` DATETIME(3) NOT NULL,
    `revoked_at` DATETIME(3) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    INDEX `refresh_tokens_user_id_idx`(`user_id`),
    INDEX `refresh_tokens_expires_at_idx`(`expires_at`),
    INDEX `refresh_tokens_revoked_at_idx`(`revoked_at`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `learning_paths` (
    `id` CHAR(36) NOT NULL,
    `code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `display_order` INTEGER NOT NULL DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT true,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    UNIQUE INDEX `learning_paths_code_key`(`code`),
    INDEX `learning_paths_is_active_display_order_idx`(`is_active`, `display_order`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `learning_levels` (
    `id` CHAR(36) NOT NULL,
    `learning_path_id` CHAR(36) NOT NULL,
    `code` VARCHAR(50) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `min_score` DECIMAL(5, 2) NULL,
    `max_score` DECIMAL(5, 2) NULL,
    `display_order` INTEGER NOT NULL DEFAULT 0,
    `is_active` BOOLEAN NOT NULL DEFAULT true,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    INDEX `learning_levels_learning_path_id_display_order_idx`(`learning_path_id`, `display_order`),
    INDEX `learning_levels_is_active_idx`(`is_active`),
    UNIQUE INDEX `learning_levels_learning_path_id_code_key`(`learning_path_id`, `code`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `decks` (
    `id` CHAR(36) NOT NULL,
    `owner_user_id` CHAR(36) NULL,
    `learning_level_id` CHAR(36) NULL,
    `deck_type` ENUM('SYSTEM', 'USER') NOT NULL DEFAULT 'USER',
    `visibility` ENUM('PRIVATE', 'PUBLIC') NOT NULL DEFAULT 'PRIVATE',
    `name` VARCHAR(200) NOT NULL,
    `normalized_name` VARCHAR(200) NOT NULL,
    `description` TEXT NULL,
    `tags` JSON NULL,
    `thumbnail_url` TEXT NULL,
    `display_order` INTEGER NOT NULL DEFAULT 0,
    `total_words` INTEGER NOT NULL DEFAULT 0,
    `is_default` BOOLEAN NOT NULL DEFAULT false,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,
    `deleted_at` DATETIME(3) NULL,

    INDEX `decks_owner_user_id_idx`(`owner_user_id`),
    INDEX `decks_learning_level_id_idx`(`learning_level_id`),
    INDEX `decks_deck_type_visibility_idx`(`deck_type`, `visibility`),
    INDEX `decks_deleted_at_idx`(`deleted_at`),
    INDEX `decks_name_idx`(`name`),
    INDEX `decks_owner_user_id_is_default_idx`(`owner_user_id`, `is_default`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `vocabularies` (
    `id` CHAR(36) NOT NULL,
    `deck_id` CHAR(36) NOT NULL,
    `source_vocabulary_id` CHAR(36) NULL,
    `word` VARCHAR(150) NOT NULL,
    `normalized_word` VARCHAR(150) NOT NULL,
    `pronunciation` VARCHAR(255) NULL,
    `meaning` TEXT NOT NULL,
    `normalized_meaning` VARCHAR(500) NOT NULL,
    `description_en` TEXT NULL,
    `example` TEXT NULL,
    `collocation` TEXT NULL,
    `related_words` TEXT NULL,
    `note` TEXT NULL,
    `audio_url` TEXT NULL,
    `image_url` TEXT NULL,
    `difficulty` ENUM('EASY', 'MEDIUM', 'HARD') NULL,
    `part_of_speech` VARCHAR(50) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,
    `deleted_at` DATETIME(3) NULL,

    INDEX `vocabularies_deck_id_idx`(`deck_id`),
    INDEX `vocabularies_source_vocabulary_id_idx`(`source_vocabulary_id`),
    INDEX `vocabularies_word_idx`(`word`),
    INDEX `vocabularies_normalized_word_idx`(`normalized_word`),
    INDEX `vocabularies_deleted_at_idx`(`deleted_at`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `review_cards` (
    `id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `vocabulary_id` CHAR(36) NOT NULL,
    `status` ENUM('NEW', 'LEARNING', 'REVIEW', 'MASTERED', 'SUSPENDED') NOT NULL DEFAULT 'NEW',
    `repetition` INTEGER NOT NULL DEFAULT 0,
    `interval_days` INTEGER NOT NULL DEFAULT 0,
    `ease_factor` DECIMAL(4, 2) NOT NULL DEFAULT 2.50,
    `due_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `last_reviewed_at` DATETIME(3) NULL,
    `first_learned_at` DATETIME(3) NULL,
    `lapses` INTEGER NOT NULL DEFAULT 0,
    `total_reviews` INTEGER NOT NULL DEFAULT 0,
    `correct_reviews` INTEGER NOT NULL DEFAULT 0,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    INDEX `review_cards_user_id_due_at_idx`(`user_id`, `due_at`),
    INDEX `review_cards_user_id_status_idx`(`user_id`, `status`),
    INDEX `review_cards_vocabulary_id_idx`(`vocabulary_id`),
    UNIQUE INDEX `review_cards_user_id_vocabulary_id_key`(`user_id`, `vocabulary_id`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `review_logs` (
    `id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `review_card_id` CHAR(36) NOT NULL,
    `vocabulary_id` CHAR(36) NOT NULL,
    `rating` ENUM('AGAIN', 'HARD', 'GOOD', 'EASY') NOT NULL,
    `quality` INTEGER NOT NULL,
    `is_correct` BOOLEAN NOT NULL DEFAULT true,
    `old_repetition` INTEGER NOT NULL DEFAULT 0,
    `new_repetition` INTEGER NOT NULL DEFAULT 0,
    `old_interval_days` INTEGER NOT NULL DEFAULT 0,
    `new_interval_days` INTEGER NOT NULL DEFAULT 0,
    `old_ease_factor` DECIMAL(4, 2) NOT NULL DEFAULT 2.50,
    `new_ease_factor` DECIMAL(4, 2) NOT NULL DEFAULT 2.50,
    `old_due_at` DATETIME(3) NULL,
    `new_due_at` DATETIME(3) NOT NULL,
    `reviewed_at` DATETIME(3) NOT NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    INDEX `review_logs_user_id_reviewed_at_idx`(`user_id`, `reviewed_at`),
    INDEX `review_logs_review_card_id_reviewed_at_idx`(`review_card_id`, `reviewed_at`),
    INDEX `review_logs_vocabulary_id_idx`(`vocabulary_id`),
    INDEX `review_logs_rating_idx`(`rating`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `practice_sessions` (
    `id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `deck_id` CHAR(36) NULL,
    `practice_type` ENUM('MULTIPLE_CHOICE', 'FILL_IN_BLANK', 'LISTENING') NOT NULL,
    `total_questions` INTEGER NOT NULL DEFAULT 0,
    `correct_answers` INTEGER NOT NULL DEFAULT 0,
    `wrong_answers` INTEGER NOT NULL DEFAULT 0,
    `accuracy` DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    `status` ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',
    `started_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `finished_at` DATETIME(3) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    INDEX `practice_sessions_user_id_started_at_idx`(`user_id`, `started_at`),
    INDEX `practice_sessions_deck_id_idx`(`deck_id`),
    INDEX `practice_sessions_practice_type_idx`(`practice_type`),
    INDEX `practice_sessions_status_idx`(`status`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `practice_answers` (
    `id` CHAR(36) NOT NULL,
    `session_id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `vocabulary_id` CHAR(36) NULL,
    `question_type` ENUM('WORD_TO_MEANING', 'MEANING_TO_WORD', 'FILL_IN_BLANK', 'LISTENING_WORD') NOT NULL,
    `question_text` TEXT NOT NULL,
    `options_json` JSON NULL,
    `user_answer` TEXT NULL,
    `correct_answer` TEXT NOT NULL,
    `is_correct` BOOLEAN NOT NULL DEFAULT false,
    `answered_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    INDEX `practice_answers_session_id_idx`(`session_id`),
    INDEX `practice_answers_user_id_answered_at_idx`(`user_id`, `answered_at`),
    INDEX `practice_answers_vocabulary_id_idx`(`vocabulary_id`),
    INDEX `practice_answers_is_correct_idx`(`is_correct`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `daily_activities` (
    `id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `activity_date` DATE NOT NULL,
    `new_words_count` INTEGER NOT NULL DEFAULT 0,
    `review_words_count` INTEGER NOT NULL DEFAULT 0,
    `practice_sessions_count` INTEGER NOT NULL DEFAULT 0,
    `correct_count` INTEGER NOT NULL DEFAULT 0,
    `wrong_count` INTEGER NOT NULL DEFAULT 0,
    `total_learning_seconds` INTEGER NOT NULL DEFAULT 0,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    INDEX `daily_activities_user_id_activity_date_idx`(`user_id`, `activity_date`),
    UNIQUE INDEX `daily_activities_user_id_activity_date_key`(`user_id`, `activity_date`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `notification_settings` (
    `id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `daily_reminder_enabled` BOOLEAN NOT NULL DEFAULT true,
    `daily_reminder_time` TIME(0) NOT NULL DEFAULT '1970-01-01 20:00:00.000',
    `due_review_reminder_enabled` BOOLEAN NOT NULL DEFAULT true,
    `push_enabled` BOOLEAN NOT NULL DEFAULT true,
    `email_enabled` BOOLEAN NOT NULL DEFAULT false,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    UNIQUE INDEX `notification_settings_user_id_key`(`user_id`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `device_tokens` (
    `id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `token` TEXT NOT NULL,
    `platform` ENUM('ANDROID', 'IOS', 'WEB') NOT NULL DEFAULT 'ANDROID',
    `device_name` VARCHAR(255) NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT true,
    `last_used_at` DATETIME(3) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    INDEX `device_tokens_user_id_idx`(`user_id`),
    INDEX `device_tokens_platform_idx`(`platform`),
    INDEX `device_tokens_is_active_idx`(`is_active`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `import_jobs` (
    `id` CHAR(36) NOT NULL,
    `user_id` CHAR(36) NOT NULL,
    `deck_id` CHAR(36) NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_type` ENUM('CSV', 'XLSX') NOT NULL DEFAULT 'CSV',
    `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'PARTIAL_SUCCESS') NOT NULL DEFAULT 'PENDING',
    `total_rows` INTEGER NOT NULL DEFAULT 0,
    `success_rows` INTEGER NOT NULL DEFAULT 0,
    `duplicate_rows` INTEGER NOT NULL DEFAULT 0,
    `failed_rows` INTEGER NOT NULL DEFAULT 0,
    `error_report_json` JSON NULL,
    `started_at` DATETIME(3) NULL,
    `finished_at` DATETIME(3) NULL,
    `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` DATETIME(3) NOT NULL,

    INDEX `import_jobs_user_id_created_at_idx`(`user_id`, `created_at`),
    INDEX `import_jobs_deck_id_idx`(`deck_id`),
    INDEX `import_jobs_status_idx`(`status`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `users` ADD CONSTRAINT `users_current_level_id_fkey` FOREIGN KEY (`current_level_id`) REFERENCES `learning_levels`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `users` ADD CONSTRAINT `users_target_level_id_fkey` FOREIGN KEY (`target_level_id`) REFERENCES `learning_levels`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `refresh_tokens` ADD CONSTRAINT `refresh_tokens_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `learning_levels` ADD CONSTRAINT `learning_levels_learning_path_id_fkey` FOREIGN KEY (`learning_path_id`) REFERENCES `learning_paths`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `decks` ADD CONSTRAINT `decks_owner_user_id_fkey` FOREIGN KEY (`owner_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `decks` ADD CONSTRAINT `decks_learning_level_id_fkey` FOREIGN KEY (`learning_level_id`) REFERENCES `learning_levels`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `vocabularies` ADD CONSTRAINT `vocabularies_deck_id_fkey` FOREIGN KEY (`deck_id`) REFERENCES `decks`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `vocabularies` ADD CONSTRAINT `vocabularies_source_vocabulary_id_fkey` FOREIGN KEY (`source_vocabulary_id`) REFERENCES `vocabularies`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `review_cards` ADD CONSTRAINT `review_cards_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `review_cards` ADD CONSTRAINT `review_cards_vocabulary_id_fkey` FOREIGN KEY (`vocabulary_id`) REFERENCES `vocabularies`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `review_logs` ADD CONSTRAINT `review_logs_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `review_logs` ADD CONSTRAINT `review_logs_review_card_id_fkey` FOREIGN KEY (`review_card_id`) REFERENCES `review_cards`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `review_logs` ADD CONSTRAINT `review_logs_vocabulary_id_fkey` FOREIGN KEY (`vocabulary_id`) REFERENCES `vocabularies`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `practice_sessions` ADD CONSTRAINT `practice_sessions_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `practice_sessions` ADD CONSTRAINT `practice_sessions_deck_id_fkey` FOREIGN KEY (`deck_id`) REFERENCES `decks`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `practice_answers` ADD CONSTRAINT `practice_answers_session_id_fkey` FOREIGN KEY (`session_id`) REFERENCES `practice_sessions`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `practice_answers` ADD CONSTRAINT `practice_answers_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `practice_answers` ADD CONSTRAINT `practice_answers_vocabulary_id_fkey` FOREIGN KEY (`vocabulary_id`) REFERENCES `vocabularies`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `daily_activities` ADD CONSTRAINT `daily_activities_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `notification_settings` ADD CONSTRAINT `notification_settings_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `device_tokens` ADD CONSTRAINT `device_tokens_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `import_jobs` ADD CONSTRAINT `import_jobs_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `import_jobs` ADD CONSTRAINT `import_jobs_deck_id_fkey` FOREIGN KEY (`deck_id`) REFERENCES `decks`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
