-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `db_admin`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `db_admin`;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT '主键 ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(32) NOT NULL COMMENT 'MD5 加密密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 初始化管理员账号（账号：admin，密码：123456 的 MD5 值）
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `status`, `deleted`)
VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 1, 0)
ON DUPLICATE KEY UPDATE `password` = 'e10adc3949ba59abbe56e057f20f883e';
