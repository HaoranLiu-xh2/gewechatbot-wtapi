-- 初始化管理员账号（账号：admin，密码：123456 的 MD5 值）
-- 当数据已存在时，保持 admin 密码为 123456
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `status`, `deleted`)
VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 1, 0)
ON DUPLICATE KEY UPDATE `password` = 'e10adc3949ba59abbe56e057f20f883e';
