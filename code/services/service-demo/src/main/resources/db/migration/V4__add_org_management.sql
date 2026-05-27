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

SET @org_id_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'org_id'
);
SET @alter_user_org_sql := IF(
    @org_id_exists = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `org_id` BIGINT DEFAULT NULL AFTER `avatar`',
    'SELECT 1'
);
PREPARE alter_user_org_stmt FROM @alter_user_org_sql;
EXECUTE alter_user_org_stmt;
DEALLOCATE PREPARE alter_user_org_stmt;

INSERT INTO `sys_org` (`id`, `parent_id`, `org_name`, `org_code`, `leader_name`, `status`)
VALUES
    (1, 0, '总部', 'HQ', '管理员', 1),
    (2, 1, '运营中心', 'OPS', '运营负责人', 1)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `org_name` = VALUES(`org_name`),
    `leader_name` = VALUES(`leader_name`),
    `status` = VALUES(`status`);

UPDATE `sys_user`
SET `org_id` = 1
WHERE `username` = 'admin' AND (`org_id` IS NULL OR `org_id` = 0);

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `app_code`, `sort`, `hidden`)
VALUES (105, 100, '组织管理', '/orgs', 'Orgs', 'OfficeBuilding', 'CONSOLE', 4, 0)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `name` = VALUES(`name`),
    `path` = VALUES(`path`),
    `component` = VALUES(`component`),
    `icon` = VALUES(`icon`),
    `app_code` = VALUES(`app_code`),
    `sort` = VALUES(`sort`),
    `hidden` = VALUES(`hidden`);

UPDATE `sys_menu`
SET `sort` = 5
WHERE `id` = 104;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM `sys_role` r, `sys_menu` m
WHERE r.role_code = 'SUPER_ADMIN' AND m.id = 105
ON DUPLICATE KEY UPDATE
    `role_id` = VALUES(`role_id`),
    `menu_id` = VALUES(`menu_id`);
