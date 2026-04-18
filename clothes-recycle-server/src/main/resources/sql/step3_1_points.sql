-- ============================================
-- Step 3.1：积分相关表（积分流水 + 积分规则）
-- 执行时间：第 3 周 Day 15
-- ============================================

-- -------------------------------------------
-- 1. 积分流水表：记录每一笔积分变动（获取/兑换/扣除/调整）
-- -------------------------------------------
CREATE TABLE `points_transaction` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID，关联 user 表',
  `type` VARCHAR(20) NOT NULL COMMENT '变动类型：EARN-获取 EXCHANGE-兑换 DEDUCT-扣除 ADJUST-调整',
  `amount` INT NOT NULL COMMENT '积分变动数量（正数表示增加，负数表示减少）',
  `balance_after` INT NOT NULL COMMENT '变动后的积分余额',
  `related_order_id` BIGINT DEFAULT NULL COMMENT '关联的回收订单ID（获取积分时记录）',
  `related_exchange_id` BIGINT DEFAULT NULL COMMENT '关联的兑换订单ID（兑换积分时记录）',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '变动描述（如：回收订单RC20260413xxxx获得积分）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_user_id` (`user_id`) COMMENT '按用户查询流水',
  KEY `idx_type` (`type`) COMMENT '按类型筛选',
  KEY `idx_created_at` (`created_at`) COMMENT '按时间排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- -------------------------------------------
-- 2. 积分规则表：配置不同衣物分类的积分兑换比例
-- -------------------------------------------
CREATE TABLE `points_rule` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `clothes_category` VARCHAR(50) NOT NULL COMMENT '衣物分类（如：外套、裤子、鞋子、床品、其他）',
  `points_per_kg` INT NOT NULL DEFAULT 10 COMMENT '每公斤可获得的积分数',
  `min_weight` DECIMAL(10,2) DEFAULT 0.50 COMMENT '最低回收重量(kg)，低于此重量不计积分',
  `status` TINYINT DEFAULT 1 COMMENT '规则状态：0-禁用 1-启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_category` (`clothes_category`) COMMENT '每个分类只能有一条规则'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';

-- -------------------------------------------
-- 3. 插入默认积分规则（与当前硬编码的 actualWeight * 10 保持一致）
-- -------------------------------------------
INSERT INTO `points_rule` (`clothes_category`, `points_per_kg`, `min_weight`, `status`) VALUES
('外套', 10, 0.50, 1),
('裤子', 10, 0.50, 1),
('鞋子', 10, 0.50, 1),
('床品', 10, 0.50, 1),
('其他', 10, 0.50, 1);
