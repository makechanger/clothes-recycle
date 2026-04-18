-- Step 4.2 增强：申诉处理动作
-- complaint 表新增 action 和 refund_amount 字段

ALTER TABLE complaint
  ADD COLUMN action VARCHAR(30) DEFAULT NULL COMMENT '处理动作：ADD_POINTS/DEDUCT_POINTS/MARK_ABNORMAL',
  ADD COLUMN refund_amount INT DEFAULT NULL COMMENT '退还积分数量（仅 REFUND_POINTS 时有值）';
