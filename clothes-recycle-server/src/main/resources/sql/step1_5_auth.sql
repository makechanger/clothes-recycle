-- ============================================================
-- Step 1.5 数据库变更脚本
-- 1. 重建 user 表：去掉 openid，改为手机号+密码登录
-- 2. 新建 collector（回收员）表
-- 3. 新建 institution（机构）表
-- 4. 插入测试数据
-- ============================================================

USE clothes_recycle;

-- -----------------------------------------------------------
-- 1. 重建 user 表：去掉 openid，使用手机号+密码登录
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `user`;

CREATE TABLE IF NOT EXISTS `user` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone`          VARCHAR(20)  NOT NULL                COMMENT '手机号（登录凭证）',
  `password_hash`  VARCHAR(255) NOT NULL                COMMENT '密码（加密存储）',
  `name`           VARCHAR(50)  DEFAULT NULL             COMMENT '用户姓名',
  `community_id`   BIGINT       DEFAULT NULL             COMMENT '所属社区ID',
  `points_balance` INT          NOT NULL DEFAULT 0      COMMENT '积分余额',
  `status`         TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=正常, 0=禁用, 2=已注销',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_community` (`community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 回收员表（collector）
--    由管理员在后台创建账号，回收员登录后上传资质
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `collector` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '回收员ID',
  `phone`           VARCHAR(20)  NOT NULL                COMMENT '手机号（登录凭证）',
  `password_hash`   VARCHAR(255) NOT NULL                COMMENT '密码（加密存储）',
  `name`            VARCHAR(50)  DEFAULT NULL             COMMENT '回收员姓名',
  `id_card_photo`   VARCHAR(255) DEFAULT NULL             COMMENT '身份证照片URL',
  `health_cert_photo` VARCHAR(255) DEFAULT NULL           COMMENT '健康证照片URL',
  `status`          TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0=待完善资质, 1=待审核, 2=已认证, 3=已禁用',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回收员表';

-- -----------------------------------------------------------
-- 3. 机构表（institution）
--    由管理员在后台创建
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `institution` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '机构ID',
  `phone`           VARCHAR(20)  NOT NULL                COMMENT '手机号（登录凭证）',
  `password_hash`   VARCHAR(255) NOT NULL                COMMENT '密码（加密存储）',
  `name`            VARCHAR(100) DEFAULT NULL             COMMENT '机构名称',
  `address`         VARCHAR(255) DEFAULT NULL             COMMENT '机构地址',
  `contact_person`  VARCHAR(50)  DEFAULT NULL             COMMENT '联系人',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=正常, 0=禁用',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构表';


-- ============================================================
-- 测试数据
-- ============================================================

-- 测试用户（手机号: 13800000001, 密码: 123456）
INSERT INTO `user` (`phone`, `password_hash`, `name`, `status`) VALUES
('13800000001', '123456', '测试用户', 1);

-- 测试回收员（手机号: 13800000002, 密码: 123456, 状态: 已认证）
INSERT INTO `collector` (`phone`, `password_hash`, `name`, `status`) VALUES
('13800000002', '123456', '测试回收员', 2);

-- 测试机构（手机号: 13800000003, 密码: 123456）
INSERT INTO `institution` (`phone`, `password_hash`, `name`, `address`, `contact_person`, `status`) VALUES
('13800000003', '123456', '绿色环保回收站', '广州市天河区天河路100号', '张经理', 1);
