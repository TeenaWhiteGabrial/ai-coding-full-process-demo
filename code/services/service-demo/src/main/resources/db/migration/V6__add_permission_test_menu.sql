INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `menu_type`, `permission_code`, `app_code`, `sort`, `hidden`)
VALUES
    (118, 100, '权限测试', '/permission-test', 'PermissionTest', 'Lock', 'MENU', 'permission:test:page', 'CONSOLE', 8, 0),
    (119, 118, '查看数据按钮', '', '', '', 'BUTTON', 'permission:test:view', 'CONSOLE', 1, 1),
    (120, 118, '危险操作按钮', '', '', '', 'BUTTON', 'permission:test:danger', 'CONSOLE', 2, 1)
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
