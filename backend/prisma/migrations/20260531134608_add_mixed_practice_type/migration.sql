-- AlterTable
ALTER TABLE `notification_settings` MODIFY `daily_reminder_time` TIME(0) NOT NULL DEFAULT '1970-01-01 20:00:00.000';

-- AlterTable
ALTER TABLE `practice_sessions` MODIFY `practice_type` ENUM('MULTIPLE_CHOICE', 'FILL_IN_BLANK', 'LISTENING', 'MIXED') NOT NULL;
