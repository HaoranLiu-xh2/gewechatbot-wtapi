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
