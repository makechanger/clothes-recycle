-- ============================================================
-- 衣物回收平台 - Step 1.2 核心建表脚本
-- 数据库：clothes_recycle（utf8mb4）
-- 共 6 张表：community, admin, user, user_address,
--           recycle_order, order_status_log
-- ============================================================

USE clothes_recycle;

-- -----------------------------------------------------------
-- 1. 社区表（community）
--    记录平台管理的社区信息，用户注册时选择所属社区
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `community` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '社区ID',
  `name`       VARCHAR(100) NOT NULL                COMMENT '社区名称',
  `province`   VARCHAR(50)  NOT NULL                COMMENT '省份',
  `city`       VARCHAR(50)  NOT NULL                COMMENT '城市',
  `district`   VARCHAR(50)  NOT NULL                COMMENT '区/县',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区表';

-- -----------------------------------------------------------
-- 2. 管理员表（admin）
--    后台管理员账号，用于 Web 管理后台登录
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `admin` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username`      VARCHAR(50)  NOT NULL                COMMENT '登录用户名',
  `password_hash` VARCHAR(255) NOT NULL                COMMENT '密码（加密存储）',
  `role`          VARCHAR(20)  NOT NULL DEFAULT 'admin' COMMENT '角色：admin=超级管理员, operator=运营',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=正常, 0=禁用',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- -----------------------------------------------------------
-- 3. 用户表（user）
--    微信小程序端普通用户，通过 openid 关联微信账号
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid`         VARCHAR(64)  NOT NULL                COMMENT '微信openid（唯一标识）',
  `phone`          VARCHAR(20)  DEFAULT NULL             COMMENT '手机号（手动绑定）',
  `name`           VARCHAR(50)  DEFAULT NULL             COMMENT '用户姓名',
  `community_id`   BIGINT       DEFAULT NULL             COMMENT '所属社区ID',
  `points_balance` INT          NOT NULL DEFAULT 0      COMMENT '积分余额',
  `status`         TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=正常, 0=禁用, 2=已注销',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_phone` (`phone`),
  KEY `idx_community` (`community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------------
-- 4. 用户地址表（user_address）
--    用户的上门地址簿，支持设置默认地址
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_address` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id`        BIGINT       NOT NULL                COMMENT '所属用户ID',
  `name`           VARCHAR(50)  NOT NULL                COMMENT '联系人姓名',
  `phone`          VARCHAR(20)  NOT NULL                COMMENT '联系电话',
  `province`       VARCHAR(50)  NOT NULL                COMMENT '省份',
  `city`           VARCHAR(50)  NOT NULL                COMMENT '城市',
  `district`       VARCHAR(50)  NOT NULL                COMMENT '区/县',
  `detail_address` VARCHAR(255) NOT NULL                COMMENT '详细地址',
  `is_default`     TINYINT      NOT NULL DEFAULT 0      COMMENT '是否默认地址：1=是, 0=否',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- -----------------------------------------------------------
-- 5. 回收订单表（recycle_order）
--    核心业务表，记录从下单到完成的全部信息
--    status 状态值：
--      0=待接单, 1=已接单, 2=上门中, 3=待确认(称重完成),
--      4=已完成, 5=已取消, 6=异常
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `recycle_order` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`           VARCHAR(32)   NOT NULL                COMMENT '订单编号（唯一）',
  `user_id`            BIGINT        NOT NULL                COMMENT '下单用户ID',
  `collector_id`       BIGINT        DEFAULT NULL             COMMENT '接单回收员ID',
  `institution_id`     BIGINT        DEFAULT NULL             COMMENT '接收机构ID',
  `address_id`         BIGINT        NOT NULL                COMMENT '上门地址ID',
  `address_snapshot`   TEXT          DEFAULT NULL             COMMENT '下单时地址快照（JSON），防止地址修改后影响历史订单',
  `appointment_date`   DATE          NOT NULL                COMMENT '预约上门日期',
  `time_slot_start`    TIME          NOT NULL                COMMENT '预约时间段-开始（如 09:00）',
  `time_slot_end`      TIME          NOT NULL                COMMENT '预约时间段-结束（如 11:00）',
  `estimated_weight`   DECIMAL(6,2)  DEFAULT NULL             COMMENT '用户预估重量（kg）',
  `actual_weight`      DECIMAL(6,2)  DEFAULT NULL             COMMENT '实际称重重量（kg）',
  `clothes_categories` VARCHAR(500)  DEFAULT NULL             COMMENT '衣物分类（JSON数组，如 ["外套","裤子"]）',
  `photos`             TEXT          DEFAULT NULL             COMMENT '衣物照片URL列表（JSON数组）',
  `remark`             VARCHAR(500)  DEFAULT NULL             COMMENT '用户备注',
  `status`             TINYINT       NOT NULL DEFAULT 0      COMMENT '订单状态：0=待接单,1=已接单,2=上门中,3=待确认,4=已完成,5=已取消,6=异常',
  `qr_code`            VARCHAR(255)  DEFAULT NULL             COMMENT '溯源二维码图片URL',
  `points_awarded`     INT           DEFAULT NULL             COMMENT '本单发放的积分数',
  `cancel_count_date`  DATE          DEFAULT NULL             COMMENT '取消计数日期（用于每日取消上限判断）',
  `cancelled_at`       DATETIME      DEFAULT NULL             COMMENT '取消时间',
  `completed_at`       DATETIME      DEFAULT NULL             COMMENT '完成时间',
  `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_collector_id` (`collector_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回收订单表';

-- -----------------------------------------------------------
-- 6. 订单状态流转记录表（order_status_log）
--    记录每次订单状态变更，用于溯源和审计
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_status_log` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `order_id`          BIGINT       NOT NULL                COMMENT '订单ID',
  `from_status`       TINYINT      DEFAULT NULL             COMMENT '变更前状态（首次创建时为NULL）',
  `to_status`         TINYINT      NOT NULL                COMMENT '变更后状态',
  `operator_id`       BIGINT       DEFAULT NULL             COMMENT '操作人ID',
  `operator_type`     VARCHAR(20)  DEFAULT NULL             COMMENT '操作人类型：user/collector/institution/admin/system',
  `operator_location` VARCHAR(255) DEFAULT NULL             COMMENT '操作人位置（可选）',
  `remark`            VARCHAR(500) DEFAULT NULL             COMMENT '备注说明',
  `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态流转记录表';


-- ============================================================
-- 测试数据
-- ============================================================

-- 插入 5 条测试社区数据
INSERT INTO `community` (`name`, `province`, `city`, `district`) VALUES
('阳光花园小区', '广东省', '广州市', '天河区'),
('翠苑社区',     '浙江省', '杭州市', '西湖区'),
('幸福里小区',   '北京市', '北京市', '朝阳区'),
('锦绣家园',     '上海市', '上海市', '浦东新区'),
('和平新村',     '江苏省', '南京市', '鼓楼区');

-- 插入默认管理员（用户名: admin, 密码: admin123）
-- 密码使用 BCrypt 加密存储
INSERT INTO `admin` (`username`, `password_hash`, `role`, `status`) VALUES
('admin', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'admin', 1);
