-- Step 5.1: 机构类型 + 衣物去向表
-- institution 表新增 type 字段（机构类型）
-- 新建 clothing_destination 表（衣物去向批次）
-- 新建 destination_order 关联表（去向-订单多对多）

-- 1. institution 表新增机构类型
ALTER TABLE institution
    ADD COLUMN `type` VARCHAR(20) DEFAULT NULL COMMENT '机构类型：DONATION=捐赠机构, RECYCLE=再生利用, ENVIRONMENTAL=环保处理'
    AFTER `contact_person`;

-- 2. 衣物去向批次表
CREATE TABLE IF NOT EXISTS `clothing_destination` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `institution_id` BIGINT NOT NULL COMMENT '所属机构ID',
    `destination_type` VARCHAR(20) NOT NULL COMMENT '去向类型：DONATION=捐赠, RECYCLE=再生利用, ENVIRONMENTAL=环保处理',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '去向描述（如：捐赠至云南山区小学）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_institution_id` (`institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='衣物去向批次表';

-- 3. 去向-订单关联表
CREATE TABLE IF NOT EXISTS `destination_order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `destination_id` BIGINT NOT NULL COMMENT '去向批次ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_dest_order` (`destination_id`, `order_id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='去向-订单关联表';
