USE `jooq_db`;

-- ==============================================================
-- Seed DML (minimal)
-- ==============================================================

-- user_preferences
INSERT INTO `user_preferences` (`id`, `theme`, `language`) VALUES
 (1, 'Dark', 'EN'),
 (2, 'Light', 'EN')
ON DUPLICATE KEY UPDATE theme = VALUES(theme), language = VALUES(language);

-- users
INSERT INTO `users` (`user_no`,`id`,`name`,`email`,`password`,`is_active`,`is_super_user`,`created_at`,`updated_at`,`preferences_id`) VALUES
 (1,UUID_TO_BIN('00000000-0000-0000-0000-000000000001'),'Admin','admin@gmail.com','admin',1,1,NOW(),NOW(),1),
 (2,UUID_TO_BIN('00000000-0000-0000-0000-000000000002'),'Siva','siva@gmail.com','siva',1,0,NOW(),NOW(),2)
ON DUPLICATE KEY UPDATE
 name = VALUES(name),
 password = VALUES(password),
 is_active = VALUES(is_active),
 is_super_user = VALUES(is_super_user),
 updated_at = VALUES(updated_at),
 preferences_id = VALUES(preferences_id);

-- company
INSERT INTO `company` (`id`, `display_name`, `is_active`) VALUES
 (UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), 'Acme Corp', 1),
 (UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 'Globex Inc', 1)
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name), is_active = VALUES(is_active);

-- user_company
INSERT INTO `user_company` (`user_id`, `company_id`, `role`, `is_active`) VALUES
 (UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), 'PG_SENIOR_MANAGER', 1),
 (UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), 'USER', 1);

-- documents (two documents in company 1)
INSERT INTO `document` (`id`, `company_id`, `document_type`, `created_date`, `is_active`, `overwritten_by`) VALUES
 (UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), 'shared_file', NOW() - INTERVAL 2 DAY, 1, NULL),
 (UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), 'shared_file', NOW() - INTERVAL 1 DAY, 1, NULL)
ON DUPLICATE KEY UPDATE is_active = VALUES(is_active), overwritten_by = VALUES(overwritten_by);

-- user_document_permission (give user2 access to document 2)
INSERT INTO `user_document_permission` (`document_id`, `type_id`, `is_active`) VALUES
 (UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), 1);

