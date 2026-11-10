-- ==============================================================================
-- AttendEase: Smart Student Attendance Management System
-- Normalized Relational Database Schema (MySQL 8 / MariaDB 10.x compatible)
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS `attendease` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `attendease`;

-- 1. Users Table (Core Identity & RBAC)
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `role` ENUM('STUDENT', 'FACULTY', 'ADMIN') NOT NULL,
  `active` BIT(1) NOT NULL DEFAULT b'1',
  `created_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Classes Table
CREATE TABLE IF NOT EXISTS `classes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_classes_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Sections Table
CREATE TABLE IF NOT EXISTS `sections` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `class_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sections_name_class` (`name`, `class_id`),
  CONSTRAINT `fk_sections_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Subjects / Courses Table
CREATE TABLE IF NOT EXISTS `subjects` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `code` VARCHAR(255) NOT NULL,
  `credits` INT DEFAULT 3,
  `class_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subjects_code` (`code`),
  CONSTRAINT `fk_subjects_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Students Profile Table
CREATE TABLE IF NOT EXISTS `students` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `roll_no` VARCHAR(255) NOT NULL,
  `class_id` BIGINT NOT NULL,
  `section_id` BIGINT NOT NULL,
  `phone` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_students_user` (`user_id`),
  UNIQUE KEY `uk_students_roll_no` (`roll_no`),
  CONSTRAINT `fk_students_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_students_class` FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`),
  CONSTRAINT `fk_students_section` FOREIGN KEY (`section_id`) REFERENCES `sections` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Faculty Profile Table
CREATE TABLE IF NOT EXISTS `faculty` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `department` VARCHAR(255) DEFAULT NULL,
  `phone` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_faculty_user` (`user_id`),
  CONSTRAINT `fk_faculty_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Faculty Subject Assignment Table
CREATE TABLE IF NOT EXISTS `faculty_subject_assignment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `faculty_id` BIGINT NOT NULL,
  `subject_id` BIGINT NOT NULL,
  `section_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_faculty_subject_section` (`faculty_id`, `subject_id`, `section_id`),
  CONSTRAINT `fk_fsa_faculty` FOREIGN KEY (`faculty_id`) REFERENCES `faculty` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fsa_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fsa_section` FOREIGN KEY (`section_id`) REFERENCES `sections` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Attendance Sessions Table
CREATE TABLE IF NOT EXISTS `attendance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL,
  `subject_id` BIGINT NOT NULL,
  `date` DATE NOT NULL,
  `status` ENUM('PRESENT', 'ABSENT') NOT NULL,
  `marked_by` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_student_subject_date` (`student_id`, `subject_id`, `date`),
  CONSTRAINT `fk_attendance_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `fk_attendance_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `fk_attendance_marked_by` FOREIGN KEY (`marked_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Attendance Correction Requests (Grievance Portal) Table
CREATE TABLE IF NOT EXISTS `attendance_correction_requests` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL,
  `attendance_id` BIGINT DEFAULT NULL,
  `subject_id` BIGINT NOT NULL,
  `date` DATE NOT NULL,
  `current_status` ENUM('PRESENT', 'ABSENT') NOT NULL,
  `requested_status` ENUM('PRESENT', 'ABSENT') NOT NULL,
  `reason` TEXT NOT NULL,
  `document_path` VARCHAR(255) DEFAULT NULL,
  `status` ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
  `faculty_remarks` VARCHAR(255) DEFAULT NULL,
  `reviewed_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_grievance_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `fk_grievance_attendance` FOREIGN KEY (`attendance_id`) REFERENCES `attendance` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_grievance_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `fk_grievance_reviewer` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Audit Logs Table
CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `entity` VARCHAR(255) NOT NULL,
  `entity_id` BIGINT NOT NULL,
  `action` VARCHAR(255) NOT NULL,
  `old_value` TEXT DEFAULT NULL,
  `new_value` TEXT DEFAULT NULL,
  `changed_by` BIGINT NOT NULL,
  `changed_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_audit_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Notifications Table
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `message` VARCHAR(255) NOT NULL,
  `type` ENUM('INFO', 'WARNING', 'SUCCESS', 'ATTENDANCE_UPDATE', 'CORRECTION_UPDATE') NOT NULL,
  `is_read` BIT(1) NOT NULL DEFAULT b'0',
  `created_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Settings Table (Threshold & Configuration)
CREATE TABLE IF NOT EXISTS `settings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `setting_key` VARCHAR(255) NOT NULL,
  `setting_value` VARCHAR(255) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settings_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

