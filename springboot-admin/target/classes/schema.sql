-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `wx`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `wx`;

-- 创建用户表（表不存在时自动创建）
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT '主键 ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(32) NOT NULL COMMENT 'MD5 加密密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `token` VARCHAR(512) DEFAULT NULL COMMENT '微信 API Token（X-finder-TOKEN）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 为已存在的数据库追加 token 字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `token` VARCHAR(512) DEFAULT NULL COMMENT '微信 API Token（X-finder-TOKEN）';

-- 创建微信账号表（已登录的微信实体）
CREATE TABLE IF NOT EXISTS `wx_account` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '所属系统用户 ID',
    `app_id` VARCHAR(128) DEFAULT NULL COMMENT '微信应用 ID',
    `uuid` VARCHAR(128) DEFAULT NULL COMMENT '登录 UUID',
    `wxid` VARCHAR(128) DEFAULT NULL COMMENT '微信 wxid',
    `alias` VARCHAR(128) DEFAULT NULL COMMENT '微信别名',
    `nick_name` VARCHAR(128) DEFAULT NULL COMMENT '微信昵称',
    `head_img_url` VARCHAR(512) DEFAULT NULL COMMENT '微信头像 URL',
    `region_id` VARCHAR(20) DEFAULT NULL COMMENT '地区编码',
    `region_name` VARCHAR(50) DEFAULT NULL COMMENT '地区名称',
    `login_type` VARCHAR(20) DEFAULT 'mac' COMMENT '登录方式',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-在线，0-离线',
    `login_info` JSON DEFAULT NULL COMMENT '登录返回的原始 loginInfo JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_wxid` (`wxid`) USING BTREE,
    KEY `idx_app_id` (`app_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信账号表';

-- 创建微信联系人表（好友、群聊、公众号）
CREATE TABLE IF NOT EXISTS `wx_contact` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '所属系统用户 ID',
    `owner_wxid` VARCHAR(128) NOT NULL COMMENT '当前登录微信的 wxid',
    `contact_wxid` VARCHAR(128) NOT NULL COMMENT '联系人 wxid',
    `user_name` VARCHAR(128) DEFAULT NULL COMMENT '联系人用户名',
    `nick_name` VARCHAR(128) DEFAULT NULL COMMENT '昵称',
    `alias` VARCHAR(128) DEFAULT NULL COMMENT '别名',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `sex` TINYINT DEFAULT NULL COMMENT '性别：0-未知，1-男，2-女',
    `signature` VARCHAR(512) DEFAULT NULL COMMENT '个性签名',
    `country` VARCHAR(50) DEFAULT NULL COMMENT '国家',
    `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
    `big_head_img_url` VARCHAR(512) DEFAULT NULL COMMENT '大头像 URL',
    `small_head_img_url` VARCHAR(512) DEFAULT NULL COMMENT '小头像 URL',
    `phone_num` VARCHAR(50) DEFAULT NULL COMMENT '手机号（取 phoneNumList 第一个）',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型：1-好友，2-群聊，3-公众号',
    `last_msg_content` VARCHAR(512) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_msg_time` BIGINT DEFAULT NULL COMMENT '最后一条消息时间戳',
    `raw_data` JSON DEFAULT NULL COMMENT '接口返回的原始 JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_owner_wxid` (`owner_wxid`) USING BTREE,
    KEY `idx_contact_wxid` (`contact_wxid`) USING BTREE,
    KEY `idx_last_msg_time` (`last_msg_time`) USING BTREE,
    KEY `idx_type` (`type`) USING BTREE,
    UNIQUE KEY `uk_user_owner_contact` (`user_id`, `owner_wxid`, `contact_wxid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信联系人表';

-- 创建微信消息表
CREATE TABLE IF NOT EXISTS `wx_message` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '所属系统用户 ID',
    `app_id` VARCHAR(128) DEFAULT NULL COMMENT '设备 ID',
    `wxid` VARCHAR(128) DEFAULT NULL COMMENT '所属微信的 wxid',
    `msg_id` BIGINT DEFAULT NULL COMMENT '消息 ID',
    `new_msg_id` BIGINT DEFAULT NULL COMMENT '新消息 ID',
    `from_wxid` VARCHAR(128) DEFAULT NULL COMMENT '发送人 wxid',
    `to_wxid` VARCHAR(128) DEFAULT NULL COMMENT '接收人 wxid',
    `msg_type` INT DEFAULT NULL COMMENT '消息类型：1-文本，3-图片，6-文件，43-视频',
    `content` TEXT COMMENT '消息内容',
    `msg_time` BIGINT DEFAULT NULL COMMENT '消息发送时间戳',
    `raw_data` JSON DEFAULT NULL COMMENT '原始消息 JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '本地创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_user_app_wxid_time` (`user_id`,`app_id`,`wxid`,`msg_time`) USING BTREE,
    KEY `idx_msg_time` (`msg_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信消息表';

-- 创建微信消息归档表（存储 90 天前的历史消息）
CREATE TABLE IF NOT EXISTS `wx_message_history` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '所属系统用户 ID',
    `app_id` VARCHAR(128) DEFAULT NULL COMMENT '设备 ID',
    `wxid` VARCHAR(128) DEFAULT NULL COMMENT '所属微信的 wxid',
    `msg_id` BIGINT DEFAULT NULL COMMENT '消息 ID',
    `new_msg_id` BIGINT DEFAULT NULL COMMENT '新消息 ID',
    `from_wxid` VARCHAR(128) DEFAULT NULL COMMENT '发送人 wxid',
    `to_wxid` VARCHAR(128) DEFAULT NULL COMMENT '接收人 wxid',
    `msg_type` INT DEFAULT NULL COMMENT '消息类型：1-文本，3-图片，6-文件，43-视频',
    `content` TEXT COMMENT '消息内容',
    `msg_time` BIGINT DEFAULT NULL COMMENT '消息发送时间戳',
    `raw_data` JSON DEFAULT NULL COMMENT '原始消息 JSON',
    `archive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    KEY `idx_user_app_wxid_time` (`user_id`,`app_id`,`wxid`,`msg_time`) USING BTREE,
    KEY `idx_msg_time` (`msg_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信消息归档表';
