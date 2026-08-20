-- SQLite 初始化脚本
-- 注意：SQLite 不支持 CREATE DATABASE / USE / ENGINE / CHARSET / COLLATE / COMMENT 等 MySQL 语法

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` INTEGER NOT NULL PRIMARY KEY,
    `username` TEXT NOT NULL,
    `password` TEXT NOT NULL,
    `nickname` TEXT DEFAULT NULL,
    `phone` TEXT DEFAULT NULL,
    `email` TEXT DEFAULT NULL,
    `token` TEXT DEFAULT NULL,
    `status` INTEGER NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` INTEGER NOT NULL DEFAULT 0,
    UNIQUE (`username`)
);

-- 创建微信账号表
CREATE TABLE IF NOT EXISTS `wx_account` (
    `id` INTEGER NOT NULL PRIMARY KEY,
    `user_id` INTEGER NOT NULL,
    `app_id` TEXT DEFAULT NULL,
    `uuid` TEXT DEFAULT NULL,
    `wxid` TEXT DEFAULT NULL,
    `alias` TEXT DEFAULT NULL,
    `nick_name` TEXT DEFAULT NULL,
    `head_img_url` TEXT DEFAULT NULL,
    `region_id` TEXT DEFAULT NULL,
    `region_name` TEXT DEFAULT NULL,
    `login_type` TEXT DEFAULT 'mac',
    `status` INTEGER NOT NULL DEFAULT 1,
    `login_info` TEXT DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS `idx_wx_account_user_id` ON `wx_account` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_wx_account_wxid` ON `wx_account` (`wxid`);
CREATE INDEX IF NOT EXISTS `idx_wx_account_app_id` ON `wx_account` (`app_id`);

-- 创建微信联系人表
CREATE TABLE IF NOT EXISTS `wx_contact` (
    `id` INTEGER NOT NULL PRIMARY KEY,
    `user_id` INTEGER NOT NULL,
    `owner_wxid` TEXT NOT NULL,
    `contact_wxid` TEXT NOT NULL,
    `user_name` TEXT DEFAULT NULL,
    `nick_name` TEXT DEFAULT NULL,
    `alias` TEXT DEFAULT NULL,
    `remark` TEXT DEFAULT NULL,
    `sex` INTEGER DEFAULT NULL,
    `signature` TEXT DEFAULT NULL,
    `country` TEXT DEFAULT NULL,
    `province` TEXT DEFAULT NULL,
    `city` TEXT DEFAULT NULL,
    `big_head_img_url` TEXT DEFAULT NULL,
    `small_head_img_url` TEXT DEFAULT NULL,
    `phone_num` TEXT DEFAULT NULL,
    `type` INTEGER NOT NULL DEFAULT 1,
    `last_msg_content` TEXT DEFAULT NULL,
    `last_msg_time` INTEGER DEFAULT NULL,
    `raw_data` TEXT DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (`user_id`, `owner_wxid`, `contact_wxid`)
);
CREATE INDEX IF NOT EXISTS `idx_wx_contact_user_id` ON `wx_contact` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_wx_contact_owner_wxid` ON `wx_contact` (`owner_wxid`);
CREATE INDEX IF NOT EXISTS `idx_wx_contact_contact_wxid` ON `wx_contact` (`contact_wxid`);
CREATE INDEX IF NOT EXISTS `idx_wx_contact_last_msg_time` ON `wx_contact` (`last_msg_time`);
CREATE INDEX IF NOT EXISTS `idx_wx_contact_type` ON `wx_contact` (`type`);

-- 创建微信消息表
CREATE TABLE IF NOT EXISTS `wx_message` (
    `id` INTEGER NOT NULL PRIMARY KEY,
    `user_id` INTEGER NOT NULL,
    `app_id` TEXT DEFAULT NULL,
    `wxid` TEXT DEFAULT NULL,
    `msg_id` INTEGER DEFAULT NULL,
    `new_msg_id` INTEGER DEFAULT NULL,
    `from_wxid` TEXT DEFAULT NULL,
    `to_wxid` TEXT DEFAULT NULL,
    `msg_type` INTEGER DEFAULT NULL,
    `content` TEXT,
    `msg_time` INTEGER DEFAULT NULL,
    `raw_data` TEXT DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS `idx_wx_message_user_app_wxid_time` ON `wx_message` (`user_id`, `app_id`, `wxid`, `msg_time`);
CREATE INDEX IF NOT EXISTS `idx_wx_message_msg_time` ON `wx_message` (`msg_time`);

-- 创建微信消息归档表
CREATE TABLE IF NOT EXISTS `wx_message_history` (
    `id` INTEGER NOT NULL PRIMARY KEY,
    `user_id` INTEGER NOT NULL,
    `app_id` TEXT DEFAULT NULL,
    `wxid` TEXT DEFAULT NULL,
    `msg_id` INTEGER DEFAULT NULL,
    `new_msg_id` INTEGER DEFAULT NULL,
    `from_wxid` TEXT DEFAULT NULL,
    `to_wxid` TEXT DEFAULT NULL,
    `msg_type` INTEGER DEFAULT NULL,
    `content` TEXT,
    `msg_time` INTEGER DEFAULT NULL,
    `raw_data` TEXT DEFAULT NULL,
    `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS `idx_wx_message_history_user_app_wxid_time` ON `wx_message_history` (`user_id`, `app_id`, `wxid`, `msg_time`);
CREATE INDEX IF NOT EXISTS `idx_wx_message_history_msg_time` ON `wx_message_history` (`msg_time`);

-- 创建素材库表（群发任务可选素材）
CREATE TABLE IF NOT EXISTS `material` (
    `id` INTEGER NOT NULL PRIMARY KEY,
    `user_id` INTEGER NOT NULL,
    `name` TEXT NOT NULL,
    `type` INTEGER NOT NULL DEFAULT 1,
    `content` TEXT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS `idx_material_user_id` ON `material` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_material_type` ON `material` (`type`);

-- 创建消息群发任务表
CREATE TABLE IF NOT EXISTS `wx_mass_task` (
    `id` INTEGER NOT NULL PRIMARY KEY,
    `user_id` INTEGER NOT NULL,
    `app_id` TEXT NOT NULL,
    `owner_wxid` TEXT NOT NULL,
    `name` TEXT NOT NULL,
    `target_type` INTEGER NOT NULL DEFAULT 1,
    `msg_type` INTEGER NOT NULL DEFAULT 1,
    `content` TEXT NOT NULL,
    `send_type` INTEGER NOT NULL DEFAULT 1,
    `start_date` TEXT DEFAULT NULL,
    `end_date` TEXT DEFAULT NULL,
    `start_time` TEXT DEFAULT NULL,
    `end_time` TEXT DEFAULT NULL,
    `interval_seconds` INTEGER NOT NULL DEFAULT 0,
    `schedule_time` TEXT DEFAULT NULL,
    `status` INTEGER NOT NULL DEFAULT 0,
    `total_count` INTEGER NOT NULL DEFAULT 0,
    `success_count` INTEGER NOT NULL DEFAULT 0,
    `fail_count` INTEGER NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS `idx_wx_mass_task_user_id` ON `wx_mass_task` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_wx_mass_task_app_id` ON `wx_mass_task` (`app_id`);
CREATE INDEX IF NOT EXISTS `idx_wx_mass_task_status` ON `wx_mass_task` (`status`);
CREATE INDEX IF NOT EXISTS `idx_wx_mass_task_schedule_time` ON `wx_mass_task` (`schedule_time`);

-- 创建消息群发任务记录表
CREATE TABLE IF NOT EXISTS `wx_mass_task_record` (
    `id` INTEGER NOT NULL PRIMARY KEY,
    `task_id` INTEGER NOT NULL,
    `user_id` INTEGER NOT NULL,
    `app_id` TEXT NOT NULL,
    `owner_wxid` TEXT NOT NULL,
    `contact_wxid` TEXT NOT NULL,
    `contact_type` INTEGER NOT NULL DEFAULT 1,
    `nick_name` TEXT DEFAULT NULL,
    `status` INTEGER NOT NULL DEFAULT 0,
    `error_msg` TEXT DEFAULT NULL,
    `send_time` TEXT DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted` INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS `idx_wx_mass_task_record_task_id` ON `wx_mass_task_record` (`task_id`);
CREATE INDEX IF NOT EXISTS `idx_wx_mass_task_record_user_id` ON `wx_mass_task_record` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_wx_mass_task_record_status` ON `wx_mass_task_record` (`status`);
CREATE INDEX IF NOT EXISTS `idx_wx_mass_task_record_send_time` ON `wx_mass_task_record` (`send_time`);
