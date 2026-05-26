INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `app_code`, `sort`, `hidden`)
VALUES (104, 100, '网站管理', '/site', 'SiteManage', 'Globe', 'CONSOLE', 4, 0)
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
WHERE r.role_code = 'SUPER_ADMIN' AND m.id = 104
ON DUPLICATE KEY UPDATE
    `role_id` = VALUES(`role_id`),
    `menu_id` = VALUES(`menu_id`);
