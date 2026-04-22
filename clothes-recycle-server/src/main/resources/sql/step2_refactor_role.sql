-- ============================================
-- Step 2: 重构为统一用户表 + 扩展表架构
--
-- 变更说明：
--   1. user 表添加 role 字段，标识用户角色（USER/COLLECTOR/INSTITUTION）
--   2. collector 表改为扩展信息表，通过 user_id 关联 user 表，去掉 phone/password_hash
--   3. institution 表改为扩展信息表，通过 user_id 关联 user 表，去掉 phone/password_hash
--   4. 迁移已有测试数据
--
-- 执行前请确保已执行过 step1_5_auth.sql
-- ============================================

-- 1. user 表添加 role 字段（默认值 USER，表示普通用户）
ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER'
  COMMENT '角色：USER-普通用户 / COLLECTOR-回收员 / INSTITUTION-机构' AFTER `name`;

-- 2. 重建 collector 表为扩展信息表（用 user_id 关联，不再存储 phone/password_hash）
DROP TABLE IF EXISTS `collector`;
CREATE TABLE `collector` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '关联 user 表的 id',
  `name` VARCHAR(50) COMMENT '回收员真实姓名',
  `id_card_photo` VARCHAR(255) COMMENT '身份证照片 URL',
  `status` TINYINT DEFAULT 2 COMMENT '状态：0-待提交资料 1-待审核 2-已认证 3-已禁用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '每个用户只能有一条回收员记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回收员扩展信息表';

-- 3. 重建 institution 表为扩展信息表
DROP TABLE IF EXISTS `institution`;
CREATE TABLE `institution` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '关联 user 表的 id',
  `name` VARCHAR(100) COMMENT '机构名称',
  `address` VARCHAR(255) COMMENT '机构地址',
  `contact_person` VARCHAR(50) COMMENT '联系人姓名',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '每个用户只能有一条机构记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构扩展信息表';

-- 4. 迁移测试数据：将原 collector/institution 表的测试用户插入 user 表
-- 原测试数据：user 表有 13800000001，collector 表有 13800000002，institution 表有 13800000003
-- 重构后：所有用户都在 user 表，通过 role 字段区分角色

-- 插入回收员测试用户到 user 表
INSERT INTO `user` (phone, password_hash, name, `role`, status) VALUES
('13800000002', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '测试回收员', 'COLLECTOR', 1);

-- 插入机构测试用户到 user 表
INSERT INTO `user` (phone, password_hash, name, `role`, status) VALUES
('13800000003', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '测试机构', 'INSTITUTION', 1);

-- 为回收员创建扩展信息记录
INSERT INTO `collector` (user_id, name, status)
SELECT id, name, 2 FROM `user` WHERE phone = '13800000002';

-- 为机构创建扩展信息记录
INSERT INTO `institution` (user_id, name, address, contact_person, status)
SELECT id, '测试机构', '测试地址', '测试联系人', 1 FROM `user` WHERE phone = '13800000003';
