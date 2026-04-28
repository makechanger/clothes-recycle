-- 衣物去向重构：从多对多批次模型改为一对一订单模型
-- 1. recycle_order 新增 destination_type 和 destination_desc 字段
-- 2. 删除不再需要的中间表

-- 新增字段
ALTER TABLE recycle_order
    ADD COLUMN destination_type VARCHAR(20) DEFAULT NULL COMMENT '去向类型：DONATION/RECYCLE/ENVIRONMENTAL',
    ADD COLUMN destination_desc VARCHAR(500) DEFAULT NULL COMMENT '去向描述';

-- 删除中间表（先删关联表，再删主表）
DROP TABLE IF EXISTS destination_order;
DROP TABLE IF EXISTS clothing_destination;
