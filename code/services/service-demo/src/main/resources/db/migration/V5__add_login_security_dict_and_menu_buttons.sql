SET @sys_user_failed_login_count_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'failed_login_count'
);
SET @alter_sys_user_failed_login_count_sql := IF(
    @sys_user_failed_login_count_exists = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `failed_login_count` INT NOT NULL DEFAULT 0 AFTER `status`',
    'SELECT 1'
);
PREPARE alter_sys_user_failed_login_count_stmt FROM @alter_sys_user_failed_login_count_sql;
EXECUTE alter_sys_user_failed_login_count_stmt;
DEALLOCATE PREPARE alter_sys_user_failed_login_count_stmt;

SET @sys_user_locked_until_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'locked_until'
);
SET @alter_sys_user_locked_until_sql := IF(
    @sys_user_locked_until_exists = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `locked_until` DATETIME DEFAULT NULL AFTER `failed_login_count`',
    'SELECT 1'
);
PREPARE alter_sys_user_locked_until_stmt FROM @alter_sys_user_locked_until_sql;
EXECUTE alter_sys_user_locked_until_stmt;
DEALLOCATE PREPARE alter_sys_user_locked_until_stmt;

SET @sys_menu_menu_type_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_menu'
      AND COLUMN_NAME = 'menu_type'
);
SET @alter_sys_menu_menu_type_sql := IF(
    @sys_menu_menu_type_exists = 0,
    'ALTER TABLE `sys_menu` ADD COLUMN `menu_type` VARCHAR(20) NOT NULL DEFAULT ''MENU'' AFTER `icon`',
    'SELECT 1'
);
PREPARE alter_sys_menu_menu_type_stmt FROM @alter_sys_menu_menu_type_sql;
EXECUTE alter_sys_menu_menu_type_stmt;
DEALLOCATE PREPARE alter_sys_menu_menu_type_stmt;

SET @sys_menu_permission_code_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_menu'
      AND COLUMN_NAME = 'permission_code'
);
SET @alter_sys_menu_permission_code_sql := IF(
    @sys_menu_permission_code_exists = 0,
    'ALTER TABLE `sys_menu` ADD COLUMN `permission_code` VARCHAR(100) DEFAULT NULL AFTER `menu_type`',
    'SELECT 1'
);
PREPARE alter_sys_menu_permission_code_stmt FROM @alter_sys_menu_permission_code_sql;
EXECUTE alter_sys_menu_permission_code_stmt;
DEALLOCATE PREPARE alter_sys_menu_permission_code_stmt;

UPDATE `sys_menu`
SET `menu_type` = 'MENU'
WHERE `menu_type` IS NULL OR `menu_type` = '';

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

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `menu_type`, `permission_code`, `app_code`, `sort`, `hidden`)
VALUES
    (106, 100, '字典管理', '/dicts', 'Dicts', 'CollectionTag', 'MENU', 'dict:manage', 'CONSOLE', 6, 0),
    (107, 103, '新增按钮', '', '', '', 'BUTTON', 'menu:create', 'CONSOLE', 1, 1),
    (108, 103, '编辑按钮', '', '', '', 'BUTTON', 'menu:update', 'CONSOLE', 2, 1),
    (109, 103, '删除按钮', '', '', '', 'BUTTON', 'menu:delete', 'CONSOLE', 3, 1),
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

UPDATE `sys_menu`
SET `sort` = 7
WHERE `id` = 104;

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
