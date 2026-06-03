CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_code` VARCHAR(50) NOT NULL UNIQUE,
    `role_name` VARCHAR(50) NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `sys_org` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `parent_id` BIGINT DEFAULT 0,
    `org_name` VARCHAR(100) NOT NULL,
    `org_code` VARCHAR(50) NOT NULL,
    `leader_name` VARCHAR(50) DEFAULT NULL,
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_sys_org_code` (`org_code`)
);

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `real_name` VARCHAR(50) DEFAULT NULL,
    `git_name` VARCHAR(50) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `avatar` VARCHAR(500) DEFAULT NULL,
    `org_id` BIGINT DEFAULT NULL,
    `status` TINYINT DEFAULT 1,
    `failed_login_count` INT NOT NULL DEFAULT 0,
    `locked_until` DATETIME DEFAULT NULL,
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
    `menu_type` VARCHAR(20) NOT NULL DEFAULT 'MENU',
    `permission_code` VARCHAR(100) DEFAULT NULL,
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

CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `parent_id` BIGINT DEFAULT 0,
    `dict_type` VARCHAR(50) NOT NULL,
    `dict_label` VARCHAR(100) NOT NULL,
    `dict_value` VARCHAR(100) NOT NULL,
    `status` TINYINT DEFAULT 1,
    `sort` INT DEFAULT 0,
    `remark` VARCHAR(255) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_sys_dict_type_value` (`dict_type`, `dict_value`)
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

INSERT INTO `sys_role` (`id`, `role_code`, `role_name`) VALUES
    (1, 'SUPER_ADMIN', '超级管理员')
ON DUPLICATE KEY UPDATE
    `role_code` = VALUES(`role_code`),
    `role_name` = VALUES(`role_name`);

INSERT INTO `sys_org` (`id`, `parent_id`, `org_name`, `org_code`, `leader_name`, `status`)
VALUES
    (1, 0, '总部', 'HQ', '管理员', 1),
    (2, 1, '运营中心', 'OPS', '运营负责人', 1)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `org_name` = VALUES(`org_name`),
    `leader_name` = VALUES(`leader_name`),
    `status` = VALUES(`status`);

INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `org_id`, `status`)
VALUES
    (1, 'admin', 'admin123', '管理员', 'admin@example.com', '', 1, 1)
ON DUPLICATE KEY UPDATE
    `real_name` = VALUES(`real_name`),
    `email` = VALUES(`email`),
    `phone` = VALUES(`phone`),
    `org_id` = VALUES(`org_id`),
    `status` = VALUES(`status`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `sys_user` u, `sys_role` r
WHERE u.username = 'admin' AND r.role_code = 'SUPER_ADMIN'
ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `role_id` = VALUES(`role_id`);

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `menu_type`, `permission_code`, `app_code`, `sort`, `hidden`)
VALUES
    (1, 0, '工作台', '/dashboard', 'Dashboard', 'House', 'MENU', 'dashboard:page', 'CONSOLE', 1, 0),
    (100, 0, '系统管理', '', '', 'Setting', 'MENU', 'system:manage', 'CONSOLE', 2, 0),
    (101, 100, '用户管理', '/users', 'Users', 'User', 'MENU', 'user:manage', 'CONSOLE', 1, 0),
    (102, 100, '角色管理', '/roles', 'Roles', 'Key', 'MENU', 'role:manage', 'CONSOLE', 2, 0),
    (103, 100, '菜单管理', '/menus', 'Menus', 'Menu', 'MENU', 'menu:manage', 'CONSOLE', 3, 0),
    (105, 100, '组织管理', '/orgs', 'Orgs', 'OfficeBuilding', 'MENU', 'org:manage', 'CONSOLE', 4, 0),
    (106, 100, '字典管理', '/dicts', 'Dicts', 'CollectionTag', 'MENU', 'dict:manage', 'CONSOLE', 5, 0),
    (104, 100, '网站管理', '/site', 'SiteManage', 'Monitor', 'MENU', 'site:manage', 'CONSOLE', 6, 0),
    (2, 0, '个人中心', '/settings', 'Settings', 'Setting', 'MENU', 'settings:page', 'CONSOLE', 99, 0),
    (107, 103, '新增菜单按钮', '', '', '', 'BUTTON', 'menu:create', 'CONSOLE', 1, 1),
    (108, 103, '编辑菜单按钮', '', '', '', 'BUTTON', 'menu:update', 'CONSOLE', 2, 1),
    (109, 103, '删除菜单按钮', '', '', '', 'BUTTON', 'menu:delete', 'CONSOLE', 3, 1),
    (110, 101, '新增账号按钮', '', '', '', 'BUTTON', 'user:create', 'CONSOLE', 1, 1),
    (111, 101, '编辑账号按钮', '', '', '', 'BUTTON', 'user:update', 'CONSOLE', 2, 1),
    (112, 101, '重置密码按钮', '', '', '', 'BUTTON', 'user:reset-password', 'CONSOLE', 3, 1),
    (113, 101, '删除账号按钮', '', '', '', 'BUTTON', 'user:delete', 'CONSOLE', 4, 1),
    (114, 101, '切换状态按钮', '', '', '', 'BUTTON', 'user:toggle-status', 'CONSOLE', 5, 1),
    (115, 106, '新增字典按钮', '', '', '', 'BUTTON', 'dict:create', 'CONSOLE', 1, 1),
    (116, 106, '编辑字典按钮', '', '', '', 'BUTTON', 'dict:update', 'CONSOLE', 2, 1),
    (117, 106, '删除字典按钮', '', '', '', 'BUTTON', 'dict:delete', 'CONSOLE', 3, 1)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `name` = VALUES(`name`),
    `path` = VALUES(`path`),
    `component` = VALUES(`component`),
    `icon` = VALUES(`icon`),
    `menu_type` = VALUES(`menu_type`),
    `permission_code` = VALUES(`permission_code`),
    `app_code` = VALUES(`app_code`),
    `sort` = VALUES(`sort`),
    `hidden` = VALUES(`hidden`);

INSERT INTO `sys_dict` (`id`, `parent_id`, `dict_type`, `dict_label`, `dict_value`, `status`, `sort`, `remark`)
VALUES
    (1, 0, 'industry', '行业', 'industry', 1, 1, '一级行业目录'),
    (2, 1, 'industry', '船舶', 'ship', 1, 1, '一级行业'),
    (3, 1, 'industry', '新闻', 'news', 1, 2, '一级行业'),
    (4, 2, 'industry', '客船', 'passenger-ship', 1, 1, '二级行业'),
    (5, 2, 'industry', '货船', 'cargo-ship', 1, 2, '二级行业')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `dict_type` = VALUES(`dict_type`),
    `dict_label` = VALUES(`dict_label`),
    `dict_value` = VALUES(`dict_value`),
    `status` = VALUES(`status`),
    `sort` = VALUES(`sort`),
    `remark` = VALUES(`remark`);

INSERT INTO `site_config` (`id`, `site_name`, `site_description`, `logo_url`, `icon_url`, `footer_text`, `footer_copyright`, `footer_record`)
VALUES
    (1, 'AI Studio 模板', '最小化控制台模板', '/ai-studio-logo.svg', '/favicon.png', 'AI Studio 模板', 'Copyright 2026', '')
ON DUPLICATE KEY UPDATE
    `site_name` = VALUES(`site_name`),
    `site_description` = VALUES(`site_description`),
    `logo_url` = VALUES(`logo_url`),
    `icon_url` = VALUES(`icon_url`),
    `footer_text` = VALUES(`footer_text`),
    `footer_copyright` = VALUES(`footer_copyright`),
    `footer_record` = VALUES(`footer_record`);
