SET @sys_user_phone_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'phone'
);

SET @alter_sys_user_phone_sql := IF(
    @sys_user_phone_exists = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL AFTER `email`',
    'SELECT 1'
);

PREPARE alter_sys_user_phone_stmt FROM @alter_sys_user_phone_sql;
EXECUTE alter_sys_user_phone_stmt;
DEALLOCATE PREPARE alter_sys_user_phone_stmt;
