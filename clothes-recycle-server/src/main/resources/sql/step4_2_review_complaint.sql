-- Step 4.2: 评价与申诉表
-- 执行时间：2026-04-18

-- 服务评价表
CREATE TABLE service_review (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL COMMENT '关联订单ID',
  user_id BIGINT NOT NULL COMMENT '评价用户ID',
  collector_id BIGINT NOT NULL COMMENT '被评价回收员的user_id',
  punctuality_score TINYINT NOT NULL COMMENT '准时度评分(1-5)',
  attitude_score TINYINT NOT NULL COMMENT '态度评分(1-5)',
  weighing_score TINYINT NOT NULL COMMENT '称重规范度评分(1-5)',
  content VARCHAR(500) DEFAULT NULL COMMENT '文字评价（可选）',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_id (order_id),
  KEY idx_user_id (user_id),
  KEY idx_collector_id (collector_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务评价表';

-- 异常申诉表
CREATE TABLE complaint (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL COMMENT '关联订单ID',
  user_id BIGINT NOT NULL COMMENT '申诉用户ID',
  type VARCHAR(50) NOT NULL COMMENT '申诉类型：WEIGHT_DISPUTE/SERVICE_ISSUE/ITEM_LOST/OTHER',
  description VARCHAR(1000) NOT NULL COMMENT '申诉描述',
  status TINYINT DEFAULT 0 COMMENT '0-待处理 1-已处理',
  admin_remark VARCHAR(500) DEFAULT NULL COMMENT '管理员处理备注',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  handled_at DATETIME DEFAULT NULL COMMENT '处理时间',
  KEY idx_order_id (order_id),
  KEY idx_user_id (user_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常申诉表';
