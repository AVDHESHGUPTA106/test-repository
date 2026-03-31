CREATE DATABASE IF NOT EXISTS `jooq_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `jooq_db`;

-- ==============================================================
-- Schema DDL (consolidated)
-- ==============================================================
-- This file contains only table definitions needed by the helper
-- functions and SP_GetDocumentsForFileProcessing_genAi_multi_company.
-- It is safe to run multiple times.

-- ---------- user_preferences ----------
DROP TABLE IF EXISTS `user_preferences`;
CREATE TABLE `user_preferences` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `theme` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------- users (UUID-based id for functions/SP) ----------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_no` bigint NOT NULL AUTO_INCREMENT,
  `id` binary(16) NOT NULL,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `is_super_user` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `preferences_id` bigint DEFAULT NULL,
  PRIMARY KEY (`user_no`),
  UNIQUE KEY `users_id_unique` (`id`),
  UNIQUE KEY `user_email_unique` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------- company ----------
DROP TABLE IF EXISTS `company`;
CREATE TABLE `company` (
  `id` binary(16) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_company_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------- user_company ----------
DROP TABLE IF EXISTS `user_company`;
CREATE TABLE `user_company` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` binary(16) NOT NULL,
  `company_id` binary(16) NOT NULL,
  `role` varchar(64) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_company_user` (`user_id`),
  KEY `idx_user_company_company` (`company_id`),
  KEY `idx_user_company_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------- document ----------
DROP TABLE IF EXISTS `document`;
CREATE TABLE `document` (
  `id` binary(16) NOT NULL,
  `company_id` binary(16) NOT NULL,
  `document_type` varchar(64) NOT NULL,
  `created_date` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `overwritten_by` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_document_company` (`company_id`),
  KEY `idx_document_active` (`is_active`),
  KEY `idx_document_type` (`document_type`),
  KEY `idx_document_created_date` (`created_date`),
  KEY `idx_document_overwritten_by` (`overwritten_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------- user_document_permission ----------
DROP TABLE IF EXISTS `user_document_permission`;
CREATE TABLE `user_document_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `document_id` binary(16) NOT NULL,
  `type_id` binary(16) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_udp_document` (`document_id`),
  KEY `idx_udp_type` (`type_id`),
  KEY `idx_udp_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

