CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_code` VARCHAR(50) NOT NULL UNIQUE,
    `role_name` VARCHAR(50) NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `real_name` VARCHAR(50) DEFAULT NULL,
    `git_name` VARCHAR(50) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `avatar` VARCHAR(500) DEFAULT NULL,
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
);

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `parent_id` BIGINT DEFAULT 0,
    `name` VARCHAR(50) NOT NULL,
    `path` VARCHAR(200) DEFAULT NULL,
    `component` VARCHAR(255) DEFAULT NULL,
    `icon` VARCHAR(50) DEFAULT NULL,
    `app_code` VARCHAR(20) DEFAULT 'CONSOLE',
    `sort` INT DEFAULT 0,
    `hidden` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL,
    `menu_id` BIGINT NOT NULL,
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
);

CREATE TABLE IF NOT EXISTS `site_config` (
    `id` BIGINT PRIMARY KEY,
    `site_name` VARCHAR(100) DEFAULT NULL,
    `site_description` VARCHAR(500) DEFAULT NULL,
    `logo_url` VARCHAR(500) DEFAULT NULL,
    `icon_url` VARCHAR(500) DEFAULT NULL,
    `footer_text` VARCHAR(500) DEFAULT NULL,
    `footer_copyright` VARCHAR(300) DEFAULT NULL,
    `footer_record` VARCHAR(200) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO `sys_role` (`role_code`, `role_name`) VALUES
    ('SUPER_ADMIN', '超级管理员'),
    ('OP_ADMIN', '运营管理员'),
    ('USER', '普通用户')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `status`) VALUES
    ('admin', 'admin123', '管理员', 'admin@example.com', 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `sys_user` u, `sys_role` r
WHERE u.username = 'admin' AND r.role_code = 'SUPER_ADMIN'
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`), `role_id` = VALUES(`role_id`);

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `app_code`, `sort`, `hidden`) VALUES
    (1, 0, '工作台', '/dashboard', 'Dashboard', 'House', 'CONSOLE', 1, 0),
    (2, 0, '个人设置', '/settings', 'Settings', 'Setting', 'CONSOLE', 2, 0),
    (100, 0, '系统管理', '', '', 'Setting', 'CONSOLE', 3, 0),
    (101, 100, '用户管理', '/users', 'Users', 'User', 'CONSOLE', 1, 0),
    (102, 100, '角色管理', '/roles', 'Roles', 'Key', 'CONSOLE', 2, 0),
    (103, 100, '菜单管理', '/menus', 'Menus', 'Menu', 'CONSOLE', 3, 0)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `name` = VALUES(`name`),
    `path` = VALUES(`path`),
    `component` = VALUES(`component`),
    `icon` = VALUES(`icon`),
    `app_code` = VALUES(`app_code`),
    `sort` = VALUES(`sort`),
    `hidden` = VALUES(`hidden`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM `sys_role` r, `sys_menu` m
WHERE r.role_code = 'SUPER_ADMIN'
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `menu_id` = VALUES(`menu_id`);

INSERT INTO `site_config` (`id`, `site_name`, `site_description`, `logo_url`, `icon_url`, `footer_text`, `footer_copyright`, `footer_record`)
VALUES (1, 'AI Studio 模板', '最小化控制台模板', '/ai-studio-logo.svg', '/favicon.png', 'AI Studio 模板', 'Copyright 2026', '')
ON DUPLICATE KEY UPDATE `site_name` = VALUES(`site_name`);
