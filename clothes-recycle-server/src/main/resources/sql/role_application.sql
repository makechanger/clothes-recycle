-- ============================================================
-- 角色申请表（role_application）
-- 用户申请成为回收员或机构，管理员审批通过后角色升级
-- status: 0=待审核, 1=已通过, 2=已拒绝
-- apply_role: COLLECTOR=申请回收员, INSTITUTION=申请机构
-- ============================================================

USE clothes_recycle;

CREATE TABLE IF NOT EXISTS `role_application` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `user_id`           BIGINT       NOT NULL                COMMENT '申请用户ID',
  `apply_role`        VARCHAR(20)  NOT NULL                COMMENT '申请角色：COLLECTOR=回收员, INSTITUTION=机构',
  `name`              VARCHAR(50)  NOT NULL                COMMENT '真实姓名/机构名称',
  `id_card_photo`     VARCHAR(255) DEFAULT NULL             COMMENT '身份证照片URL（回收员必填）',
  `health_cert_photo` VARCHAR(255) DEFAULT NULL             COMMENT '健康证照片URL（回收员选填）',
  `address`           VARCHAR(255) DEFAULT NULL             COMMENT '机构地址（机构必填）',
  `contact_person`    VARCHAR(50)  DEFAULT NULL             COMMENT '联系人（机构必填）',
  `status`            TINYINT      NOT NULL DEFAULT 0      COMMENT '审核状态：0=待审核, 1=已通过, 2=已拒绝',
  `reject_reason`     VARCHAR(255) DEFAULT NULL             COMMENT '拒绝原因',
  `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色申请表';
