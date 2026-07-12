-- 微信功能升级脚本（如启动时 schema.sql 未自动执行，可手动运行）
USE `wx`;

-- 为用户表追加微信 API Token 字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `token` VARCHAR(512) DEFAULT NULL COMMENT '微信 API Token（X-finder-TOKEN）';

-- 创建微信账号表
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
