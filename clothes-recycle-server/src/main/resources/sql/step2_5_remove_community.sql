-- ============================================================
-- 移除社区表及相关字段
-- 原因：用户地址已有省/市/区字段，社区表无实际业务价值
-- ============================================================

USE clothes_recycle;

-- 1. 删除 user 表的 community_id 列
ALTER TABLE `user` DROP COLUMN `community_id`;

-- 2. 删除社区表
DROP TABLE IF EXISTS `community`;
