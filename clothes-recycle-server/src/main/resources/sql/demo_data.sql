/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : clothes_recycle

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 14/05/2026 20:13:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'admin' COMMENT '角色：admin=超级管理员, operator=运营',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=正常, 0=禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'admin', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (2, 'operator', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (3, 'op_zhang', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (4, 'op_liu', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (5, 'op_chen', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (6, 'op_yang', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (7, 'op_huang', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (8, 'op_zhou', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (9, 'op_wu', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (10, 'op_xu', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (11, 'op_sun', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (12, 'op_ma', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 0, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (13, 'op_zhu', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 0, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (14, 'admin2', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'admin', 1, '2026-05-02 17:03:14');
INSERT INTO `admin` VALUES (15, 'op_he', '$2a$10$K0rG.TncSNaJzHi8zA3JF.Xpgs/MwM6DppD2Ml8oI6Q6R8mQ7yvgy', 'operator', 1, '2026-05-02 17:03:14');

-- ----------------------------
-- Table structure for collector
-- ----------------------------
DROP TABLE IF EXISTS `collector`;
CREATE TABLE `collector`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '关联 user 表的 id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回收员真实姓名',
  `id_card_photo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证照片 URL',
  `status` tinyint NULL DEFAULT 2 COMMENT '状态：0-待提交资料 1-待审核 2-已认证 3-已禁用',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE COMMENT '每个用户只能有一条回收员记录'
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '回收员扩展信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collector
-- ----------------------------
INSERT INTO `collector` VALUES (1, 3, '王磊', '/uploads/id_card_wang.jpg', 2, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `collector` VALUES (2, 4, '赵敏', '/uploads/id_card_zhao.jpg', 2, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `collector` VALUES (3, 6, '刘贵', '/uploads/id_card_liu.jpg', 2, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `collector` VALUES (4, 7, '孙思通', '/uploads/id_card_sun.jpg', 2, '2026-05-02 17:03:14', '2026-05-02 17:03:14');

-- ----------------------------
-- Table structure for complaint
-- ----------------------------
DROP TABLE IF EXISTS `complaint`;
CREATE TABLE `complaint`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '鍏宠仈璁㈠崟ID',
  `user_id` bigint NOT NULL COMMENT '鐢宠瘔鐢ㄦ埛ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐢宠瘔绫诲瀷锛歐EIGHT_DISPUTE/SERVICE_ISSUE/ITEM_LOST/OTHER',
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐢宠瘔鎻忚堪',
  `status` tinyint NULL DEFAULT 0 COMMENT '0-寰呭?鐞?1-宸插?鐞',
  `admin_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '绠＄悊鍛樺?鐞嗗?娉',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `handled_at` datetime NULL DEFAULT NULL COMMENT '澶勭悊鏃堕棿',
  `action` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MARK_HANDLED',
  `refund_amount` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '寮傚父鐢宠瘔琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of complaint
-- ----------------------------
INSERT INTO `complaint` VALUES (1, 7, 1, 'WEIGHT_DISPUTE', '感觉实际重量应该不止7kg，我称过大概有8kg左右', 1, '经核实，补偿用户5积分', '2026-04-22 10:00:00', '2026-04-22 14:00:00', 'ADD_POINTS', 5);
INSERT INTO `complaint` VALUES (2, 11, 10, 'WEIGHT_DISPUTE', '称重结果和我自己称的差了0.5kg', 1, '已核实，差异在合理范围内', '2026-04-10 16:00:00', '2026-04-11 10:00:00', NULL, NULL);
INSERT INTO `complaint` VALUES (3, 13, 12, 'WEIGHT_DISPUTE', '实际重量应该有6kg，但只称了5kg', 1, '经核实确有偏差，补偿用户10积分', '2026-04-11 16:00:00', '2026-04-12 09:00:00', 'ADD_POINTS', 10);
INSERT INTO `complaint` VALUES (4, 14, 13, 'SERVICE_ISSUE', '回收员迟到了半小时没有提前通知', 1, '已与回收员沟通，下次会提前通知', '2026-04-12 09:00:00', '2026-04-12 14:00:00', NULL, NULL);
INSERT INTO `complaint` VALUES (5, 15, 14, 'WEIGHT_DISPUTE', '称重时没有去掉包装袋的重量', 0, NULL, '2026-04-12 16:00:00', NULL, NULL, NULL);
INSERT INTO `complaint` VALUES (6, 17, 16, 'SERVICE_ISSUE', '回收员态度不好，催促我赶紧把衣服拿出来', 1, '已对回收员进行警告处理', '2026-04-13 16:00:00', '2026-04-14 09:00:00', NULL, NULL);
INSERT INTO `complaint` VALUES (7, 18, 17, 'ITEM_LOST', '有一件羽绒服不见了，回收员说没看到', 1, '经调查确认遗失，补偿用户20积分', '2026-04-14 09:00:00', '2026-04-14 14:00:00', 'ADD_POINTS', 20);
INSERT INTO `complaint` VALUES (8, 19, 18, 'OTHER', '回收员把我家门口弄脏了，希望注意', 1, '已提醒回收员注意卫生', '2026-04-14 16:00:00', '2026-04-15 09:00:00', NULL, NULL);
INSERT INTO `complaint` VALUES (9, 22, 10, 'WEIGHT_DISPUTE', '鞋子称重3.5kg感觉偏轻了', 0, NULL, '2026-04-16 09:00:00', NULL, NULL, NULL);
INSERT INTO `complaint` VALUES (10, 23, 11, 'SERVICE_ISSUE', '预约的上午时段，回收员下午才来', 1, '已核实并对回收员进行处罚，补偿用户5积分', '2026-04-16 16:00:00', '2026-04-17 09:00:00', 'ADD_POINTS', 5);
INSERT INTO `complaint` VALUES (11, 24, 12, 'WEIGHT_DISPUTE', '7kg的衣服只称出6kg，差距太大', 0, NULL, '2026-04-17 09:00:00', NULL, NULL, NULL);
INSERT INTO `complaint` VALUES (12, 25, 13, 'SERVICE_ISSUE', '回收员没有穿工作服，无法确认身份', 1, '已要求所有回收员必须穿工作服上门', '2026-04-17 16:00:00', '2026-04-18 09:00:00', NULL, NULL);
INSERT INTO `complaint` VALUES (13, 26, 14, 'ITEM_LOST', '少了两双鞋子，回收员说可能掉在路上了', 0, NULL, '2026-04-18 09:00:00', NULL, NULL, NULL);
INSERT INTO `complaint` VALUES (14, 27, 15, 'OTHER', '回收员来的时候在打电话，全程没有交流', 1, '已提醒回收员注意服务态度', '2026-04-19 09:00:00', '2026-04-19 14:00:00', NULL, NULL);
INSERT INTO `complaint` VALUES (15, 28, 16, 'WEIGHT_DISPUTE', '裤子称重2.5kg，我觉得至少有3kg', 0, NULL, '2026-04-20 09:00:00', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for institution
-- ----------------------------
DROP TABLE IF EXISTS `institution`;
CREATE TABLE `institution`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '关联 user 表的 id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构名称',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构地址',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构类型：DONATION=捐赠机构, RECYCLE=再生利用, ENVIRONMENTAL=环保处理',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE COMMENT '每个用户只能有一条机构记录'
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '机构扩展信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of institution
-- ----------------------------
INSERT INTO `institution` VALUES (1, 5, '绿色地球环保机构', '北京市海淀区中关村大街1号', '陈德明', '环保机构', 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `institution` VALUES (2, 8, '爱心衣橱慈善中心', '上海市浦东新区陆家嘴环路958号', '周静怡', '慈善机构', 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `institution` VALUES (3, 9, '循环再生科技公司', '深圳市南山区科技园南区1栋', '吴遥辉', '回收企业', 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');

-- ----------------------------
-- Table structure for order_status_log
-- ----------------------------
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `from_status` tinyint NULL DEFAULT NULL COMMENT '变更前状态（首次创建时为NULL）',
  `to_status` tinyint NOT NULL COMMENT '变更后状态',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人类型：user/collector/institution/admin/system',
  `operator_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人位置（可选）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 138 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单状态流转记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_status_log
-- ----------------------------
INSERT INTO `order_status_log` VALUES (1, 1, NULL, 0, 1, 'user', NULL, '用户创建订单', '2026-04-28 09:00:00');
INSERT INTO `order_status_log` VALUES (2, 2, NULL, 0, 1, 'user', NULL, '用户创建订单', '2026-04-27 10:00:00');
INSERT INTO `order_status_log` VALUES (3, 2, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-27 11:00:00');
INSERT INTO `order_status_log` VALUES (4, 3, NULL, 0, 1, 'user', NULL, '用户创建订单', '2026-04-26 14:00:00');
INSERT INTO `order_status_log` VALUES (5, 3, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-26 15:00:00');
INSERT INTO `order_status_log` VALUES (6, 3, 1, 2, 3, 'collector', NULL, '回收员出发上门', '2026-04-26 16:00:00');
INSERT INTO `order_status_log` VALUES (7, 4, NULL, 0, 1, 'user', NULL, '用户创建订单', '2026-04-25 08:00:00');
INSERT INTO `order_status_log` VALUES (8, 4, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-25 09:00:00');
INSERT INTO `order_status_log` VALUES (9, 4, 1, 2, 3, 'collector', NULL, '回收员出发上门', '2026-04-25 09:30:00');
INSERT INTO `order_status_log` VALUES (10, 4, 2, 3, 3, 'collector', NULL, '称重完成，实际重量8.5kg，等待用户确认', '2026-04-25 10:30:00');
INSERT INTO `order_status_log` VALUES (11, 5, NULL, 0, 1, 'user', NULL, '用户创建订单', '2026-04-24 10:00:00');
INSERT INTO `order_status_log` VALUES (12, 5, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-24 11:00:00');
INSERT INTO `order_status_log` VALUES (13, 5, 1, 2, 3, 'collector', NULL, '回收员出发上门', '2026-04-24 11:30:00');
INSERT INTO `order_status_log` VALUES (14, 5, 2, 3, 3, 'collector', NULL, '称重完成，实际重量5.5kg，等待用户确认', '2026-04-24 12:30:00');
INSERT INTO `order_status_log` VALUES (15, 5, 3, 4, 1, 'user', NULL, '用户确认完成，发放积分69', '2026-04-24 14:00:00');
INSERT INTO `order_status_log` VALUES (16, 6, NULL, 0, 1, 'user', NULL, '用户创建订单', '2026-04-23 15:00:00');
INSERT INTO `order_status_log` VALUES (17, 6, 0, 5, 1, 'user', NULL, '用户取消订单', '2026-04-23 16:00:00');
INSERT INTO `order_status_log` VALUES (18, 7, NULL, 0, 1, 'user', NULL, '用户创建订单', '2026-04-20 10:00:00');
INSERT INTO `order_status_log` VALUES (19, 7, 0, 1, 4, 'collector', NULL, '回收员接单', '2026-04-20 11:00:00');
INSERT INTO `order_status_log` VALUES (20, 7, 1, 2, 4, 'collector', NULL, '回收员出发上门', '2026-04-20 11:30:00');
INSERT INTO `order_status_log` VALUES (21, 7, 2, 3, 4, 'collector', NULL, '称重完成，实际重量7.0kg，等待用户确认', '2026-04-20 13:00:00');
INSERT INTO `order_status_log` VALUES (22, 7, 3, 4, 1, 'user', NULL, '用户确认完成，发放积分86', '2026-04-20 15:00:00');
INSERT INTO `order_status_log` VALUES (23, 7, 4, 7, 5, 'institution', NULL, '机构扫码接收', '2026-04-22 09:00:00');
INSERT INTO `order_status_log` VALUES (24, 8, NULL, 0, 1, 'user', NULL, '用户创建订单', '2026-04-18 09:00:00');
INSERT INTO `order_status_log` VALUES (25, 8, 0, 1, 4, 'collector', NULL, '回收员接单', '2026-04-18 10:00:00');
INSERT INTO `order_status_log` VALUES (26, 8, 1, 2, 4, 'collector', NULL, '回收员出发上门', '2026-04-18 10:30:00');
INSERT INTO `order_status_log` VALUES (27, 8, 2, 3, 4, 'collector', NULL, '称重完成，实际重量6.0kg，等待用户确认', '2026-04-18 12:00:00');
INSERT INTO `order_status_log` VALUES (28, 8, 3, 4, 1, 'user', NULL, '用户确认完成，发放积分69', '2026-04-18 14:00:00');
INSERT INTO `order_status_log` VALUES (29, 8, 4, 7, 5, 'institution', NULL, '机构扫码接收', '2026-04-20 10:00:00');
INSERT INTO `order_status_log` VALUES (30, 8, 7, 8, 5, 'institution', NULL, '分配去向：捐赠，捐赠至云南山区希望小学', '2026-04-21 10:00:00');
INSERT INTO `order_status_log` VALUES (31, 9, NULL, 0, 2, 'user', NULL, '用户创建订单', '2026-04-19 08:00:00');
INSERT INTO `order_status_log` VALUES (32, 9, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-19 09:00:00');
INSERT INTO `order_status_log` VALUES (33, 9, 1, 2, 3, 'collector', NULL, '回收员出发上门', '2026-04-19 09:30:00');
INSERT INTO `order_status_log` VALUES (34, 9, 2, 3, 3, 'collector', NULL, '称重完成，实际重量4.0kg，等待用户确认', '2026-04-19 11:00:00');
INSERT INTO `order_status_log` VALUES (35, 9, 3, 4, 2, 'user', NULL, '用户确认完成，发放积分40', '2026-04-19 13:00:00');
INSERT INTO `order_status_log` VALUES (36, 9, 4, 7, 5, 'institution', NULL, '机构扫码接收', '2026-04-21 11:00:00');
INSERT INTO `order_status_log` VALUES (37, 10, NULL, 0, 2, 'user', NULL, '用户创建订单', '2026-04-29 10:00:00');
INSERT INTO `order_status_log` VALUES (38, 11, NULL, 0, 10, 'user', NULL, '用户创建订单', '2026-04-10 09:00:00');
INSERT INTO `order_status_log` VALUES (39, 11, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-10 10:00:00');
INSERT INTO `order_status_log` VALUES (40, 11, 1, 2, 3, 'collector', NULL, '回收员出发上门', '2026-04-10 10:30:00');
INSERT INTO `order_status_log` VALUES (41, 11, 2, 3, 3, 'collector', NULL, '称重完成，实际重量3.5kg', '2026-04-10 12:00:00');
INSERT INTO `order_status_log` VALUES (42, 11, 3, 4, 10, 'user', NULL, '用户确认完成', '2026-04-10 14:00:00');
INSERT INTO `order_status_log` VALUES (43, 12, NULL, 0, 11, 'user', NULL, '用户创建订单', '2026-04-10 14:00:00');
INSERT INTO `order_status_log` VALUES (44, 12, 0, 1, 4, 'collector', NULL, '回收员接单', '2026-04-10 15:00:00');
INSERT INTO `order_status_log` VALUES (45, 12, 1, 2, 4, 'collector', NULL, '回收员出发上门', '2026-04-10 15:30:00');
INSERT INTO `order_status_log` VALUES (46, 12, 2, 3, 4, 'collector', NULL, '称重完成，实际重量4.5kg', '2026-04-10 16:30:00');
INSERT INTO `order_status_log` VALUES (47, 12, 3, 4, 11, 'user', NULL, '用户确认完成', '2026-04-10 18:00:00');
INSERT INTO `order_status_log` VALUES (48, 13, NULL, 0, 12, 'user', NULL, '用户创建订单', '2026-04-11 09:00:00');
INSERT INTO `order_status_log` VALUES (49, 13, 0, 1, 6, 'collector', NULL, '回收员接单', '2026-04-11 10:00:00');
INSERT INTO `order_status_log` VALUES (50, 13, 1, 2, 6, 'collector', NULL, '回收员出发上门', '2026-04-11 10:30:00');
INSERT INTO `order_status_log` VALUES (51, 13, 2, 3, 6, 'collector', NULL, '称重完成，实际重量5.0kg', '2026-04-11 12:00:00');
INSERT INTO `order_status_log` VALUES (52, 13, 3, 4, 12, 'user', NULL, '用户确认完成', '2026-04-11 14:00:00');
INSERT INTO `order_status_log` VALUES (53, 14, NULL, 0, 13, 'user', NULL, '用户创建订单', '2026-04-11 14:00:00');
INSERT INTO `order_status_log` VALUES (54, 14, 0, 1, 7, 'collector', NULL, '回收员接单', '2026-04-11 15:00:00');
INSERT INTO `order_status_log` VALUES (55, 14, 1, 2, 7, 'collector', NULL, '回收员出发上门', '2026-04-11 15:30:00');
INSERT INTO `order_status_log` VALUES (56, 14, 2, 3, 7, 'collector', NULL, '称重完成，实际重量2.5kg', '2026-04-11 16:30:00');
INSERT INTO `order_status_log` VALUES (57, 14, 3, 4, 13, 'user', NULL, '用户确认完成', '2026-04-11 18:00:00');
INSERT INTO `order_status_log` VALUES (58, 15, NULL, 0, 14, 'user', NULL, '用户创建订单', '2026-04-12 09:00:00');
INSERT INTO `order_status_log` VALUES (59, 15, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-12 10:00:00');
INSERT INTO `order_status_log` VALUES (60, 15, 1, 2, 3, 'collector', NULL, '回收员出发上门', '2026-04-12 10:30:00');
INSERT INTO `order_status_log` VALUES (61, 15, 2, 3, 3, 'collector', NULL, '称重完成，实际重量6.5kg', '2026-04-12 12:00:00');
INSERT INTO `order_status_log` VALUES (62, 15, 3, 4, 14, 'user', NULL, '用户确认完成', '2026-04-12 14:00:00');
INSERT INTO `order_status_log` VALUES (63, 16, NULL, 0, 15, 'user', NULL, '用户创建订单', '2026-04-12 14:00:00');
INSERT INTO `order_status_log` VALUES (64, 16, 0, 1, 4, 'collector', NULL, '回收员接单', '2026-04-12 15:00:00');
INSERT INTO `order_status_log` VALUES (65, 16, 1, 2, 4, 'collector', NULL, '回收员出发上门', '2026-04-12 15:30:00');
INSERT INTO `order_status_log` VALUES (66, 16, 2, 3, 4, 'collector', NULL, '称重完成，实际重量7.5kg', '2026-04-12 16:30:00');
INSERT INTO `order_status_log` VALUES (67, 16, 3, 4, 15, 'user', NULL, '用户确认完成', '2026-04-12 18:00:00');
INSERT INTO `order_status_log` VALUES (68, 17, NULL, 0, 16, 'user', NULL, '用户创建订单', '2026-04-13 09:00:00');
INSERT INTO `order_status_log` VALUES (69, 17, 0, 1, 6, 'collector', NULL, '回收员接单', '2026-04-13 10:00:00');
INSERT INTO `order_status_log` VALUES (70, 17, 1, 2, 6, 'collector', NULL, '回收员出发上门', '2026-04-13 10:30:00');
INSERT INTO `order_status_log` VALUES (71, 17, 2, 3, 6, 'collector', NULL, '称重完成，实际重量3.0kg', '2026-04-13 12:00:00');
INSERT INTO `order_status_log` VALUES (72, 17, 3, 4, 16, 'user', NULL, '用户确认完成', '2026-04-13 14:00:00');
INSERT INTO `order_status_log` VALUES (73, 18, NULL, 0, 17, 'user', NULL, '用户创建订单', '2026-04-13 14:00:00');
INSERT INTO `order_status_log` VALUES (74, 18, 0, 1, 7, 'collector', NULL, '回收员接单', '2026-04-13 15:00:00');
INSERT INTO `order_status_log` VALUES (75, 18, 1, 2, 7, 'collector', NULL, '回收员出发上门', '2026-04-13 15:30:00');
INSERT INTO `order_status_log` VALUES (76, 18, 2, 3, 7, 'collector', NULL, '称重完成，实际重量4.0kg', '2026-04-13 16:30:00');
INSERT INTO `order_status_log` VALUES (77, 18, 3, 4, 17, 'user', NULL, '用户确认完成', '2026-04-13 18:00:00');
INSERT INTO `order_status_log` VALUES (78, 19, NULL, 0, 18, 'user', NULL, '用户创建订单', '2026-04-14 09:00:00');
INSERT INTO `order_status_log` VALUES (79, 19, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-14 10:00:00');
INSERT INTO `order_status_log` VALUES (80, 19, 1, 2, 3, 'collector', NULL, '回收员出发上门', '2026-04-14 10:30:00');
INSERT INTO `order_status_log` VALUES (81, 19, 2, 3, 3, 'collector', NULL, '称重完成，实际重量5.5kg', '2026-04-14 12:00:00');
INSERT INTO `order_status_log` VALUES (82, 19, 3, 4, 18, 'user', NULL, '用户确认完成', '2026-04-14 14:00:00');
INSERT INTO `order_status_log` VALUES (83, 20, NULL, 0, 19, 'user', NULL, '用户创建订单', '2026-04-14 14:00:00');
INSERT INTO `order_status_log` VALUES (84, 20, 0, 1, 4, 'collector', NULL, '回收员接单', '2026-04-14 15:00:00');
INSERT INTO `order_status_log` VALUES (85, 20, 1, 2, 4, 'collector', NULL, '回收员出发上门', '2026-04-14 15:30:00');
INSERT INTO `order_status_log` VALUES (86, 20, 2, 3, 4, 'collector', NULL, '称重完成，实际重量2.5kg', '2026-04-14 16:30:00');
INSERT INTO `order_status_log` VALUES (87, 20, 3, 4, 19, 'user', NULL, '用户确认完成', '2026-04-14 18:00:00');
INSERT INTO `order_status_log` VALUES (88, 21, NULL, 0, 20, 'user', NULL, '用户创建订单', '2026-04-15 09:00:00');
INSERT INTO `order_status_log` VALUES (89, 21, 0, 1, 6, 'collector', NULL, '回收员接单', '2026-04-15 10:00:00');
INSERT INTO `order_status_log` VALUES (90, 21, 1, 2, 6, 'collector', NULL, '回收员出发上门', '2026-04-15 10:30:00');
INSERT INTO `order_status_log` VALUES (91, 21, 2, 3, 6, 'collector', NULL, '称重完成，实际重量4.5kg', '2026-04-15 12:00:00');
INSERT INTO `order_status_log` VALUES (92, 21, 3, 4, 20, 'user', NULL, '用户确认完成', '2026-04-15 14:00:00');
INSERT INTO `order_status_log` VALUES (93, 22, NULL, 0, 10, 'user', NULL, '用户创建订单', '2026-04-15 14:00:00');
INSERT INTO `order_status_log` VALUES (94, 22, 0, 1, 7, 'collector', NULL, '回收员接单', '2026-04-15 15:00:00');
INSERT INTO `order_status_log` VALUES (95, 22, 1, 2, 7, 'collector', NULL, '回收员出发上门', '2026-04-15 15:30:00');
INSERT INTO `order_status_log` VALUES (96, 22, 2, 3, 7, 'collector', NULL, '称重完成，实际重量3.5kg', '2026-04-15 16:30:00');
INSERT INTO `order_status_log` VALUES (97, 22, 3, 4, 10, 'user', NULL, '用户确认完成', '2026-04-15 18:00:00');
INSERT INTO `order_status_log` VALUES (98, 23, NULL, 0, 11, 'user', NULL, '用户创建订单', '2026-04-16 09:00:00');
INSERT INTO `order_status_log` VALUES (99, 23, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-16 10:00:00');
INSERT INTO `order_status_log` VALUES (100, 23, 1, 2, 3, 'collector', NULL, '回收员出发上门', '2026-04-16 10:30:00');
INSERT INTO `order_status_log` VALUES (101, 23, 2, 3, 3, 'collector', NULL, '称重完成，实际重量5.0kg', '2026-04-16 12:00:00');
INSERT INTO `order_status_log` VALUES (102, 23, 3, 4, 11, 'user', NULL, '用户确认完成', '2026-04-16 14:00:00');
INSERT INTO `order_status_log` VALUES (103, 24, NULL, 0, 12, 'user', NULL, '用户创建订单', '2026-04-16 14:00:00');
INSERT INTO `order_status_log` VALUES (104, 24, 0, 1, 4, 'collector', NULL, '回收员接单', '2026-04-16 15:00:00');
INSERT INTO `order_status_log` VALUES (105, 24, 1, 2, 4, 'collector', NULL, '回收员出发上门', '2026-04-16 15:30:00');
INSERT INTO `order_status_log` VALUES (106, 24, 2, 3, 4, 'collector', NULL, '称重完成，实际重量6.0kg', '2026-04-16 16:30:00');
INSERT INTO `order_status_log` VALUES (107, 24, 3, 4, 12, 'user', NULL, '用户确认完成', '2026-04-16 18:00:00');
INSERT INTO `order_status_log` VALUES (108, 25, NULL, 0, 13, 'user', NULL, '用户创建订单', '2026-04-17 09:00:00');
INSERT INTO `order_status_log` VALUES (109, 25, 0, 1, 6, 'collector', NULL, '回收员接单', '2026-04-17 10:00:00');
INSERT INTO `order_status_log` VALUES (110, 25, 1, 2, 6, 'collector', NULL, '回收员出发上门', '2026-04-17 10:30:00');
INSERT INTO `order_status_log` VALUES (111, 25, 2, 3, 6, 'collector', NULL, '称重完成，实际重量4.5kg', '2026-04-17 12:00:00');
INSERT INTO `order_status_log` VALUES (112, 25, 3, 4, 13, 'user', NULL, '用户确认完成', '2026-04-17 14:00:00');
INSERT INTO `order_status_log` VALUES (113, 26, NULL, 0, 14, 'user', NULL, '用户创建订单', '2026-04-17 14:00:00');
INSERT INTO `order_status_log` VALUES (114, 26, 0, 1, 7, 'collector', NULL, '回收员接单', '2026-04-17 15:00:00');
INSERT INTO `order_status_log` VALUES (115, 26, 1, 2, 7, 'collector', NULL, '回收员出发上门', '2026-04-17 15:30:00');
INSERT INTO `order_status_log` VALUES (116, 26, 2, 3, 7, 'collector', NULL, '称重完成，实际重量3.0kg', '2026-04-17 16:30:00');
INSERT INTO `order_status_log` VALUES (117, 26, 3, 4, 14, 'user', NULL, '用户确认完成', '2026-04-17 18:00:00');
INSERT INTO `order_status_log` VALUES (118, 27, NULL, 0, 15, 'user', NULL, '用户创建订单', '2026-04-18 14:00:00');
INSERT INTO `order_status_log` VALUES (119, 27, 0, 1, 3, 'collector', NULL, '回收员接单', '2026-04-18 15:00:00');
INSERT INTO `order_status_log` VALUES (120, 27, 1, 2, 3, 'collector', NULL, '回收员出发上门', '2026-04-18 15:30:00');
INSERT INTO `order_status_log` VALUES (121, 27, 2, 3, 3, 'collector', NULL, '称重完成，实际重量7.0kg', '2026-04-18 16:30:00');
INSERT INTO `order_status_log` VALUES (122, 27, 3, 4, 15, 'user', NULL, '用户确认完成', '2026-04-18 18:00:00');
INSERT INTO `order_status_log` VALUES (123, 28, NULL, 0, 16, 'user', NULL, '用户创建订单', '2026-04-19 14:00:00');
INSERT INTO `order_status_log` VALUES (124, 28, 0, 1, 4, 'collector', NULL, '回收员接单', '2026-04-19 15:00:00');
INSERT INTO `order_status_log` VALUES (125, 28, 1, 2, 4, 'collector', NULL, '回收员出发上门', '2026-04-19 15:30:00');
INSERT INTO `order_status_log` VALUES (126, 28, 2, 3, 4, 'collector', NULL, '称重完成，实际重量2.5kg', '2026-04-19 16:30:00');
INSERT INTO `order_status_log` VALUES (127, 28, 3, 4, 16, 'user', NULL, '用户确认完成', '2026-04-19 18:00:00');
INSERT INTO `order_status_log` VALUES (128, 29, NULL, 0, 17, 'user', NULL, '用户创建订单', '2026-04-20 14:00:00');
INSERT INTO `order_status_log` VALUES (129, 29, 0, 1, 6, 'collector', NULL, '回收员接单', '2026-04-20 15:00:00');
INSERT INTO `order_status_log` VALUES (130, 29, 1, 2, 6, 'collector', NULL, '回收员出发上门', '2026-04-20 15:30:00');
INSERT INTO `order_status_log` VALUES (131, 29, 2, 3, 6, 'collector', NULL, '称重完成，实际重量4.0kg', '2026-04-20 16:30:00');
INSERT INTO `order_status_log` VALUES (132, 29, 3, 4, 17, 'user', NULL, '用户确认完成', '2026-04-20 18:00:00');
INSERT INTO `order_status_log` VALUES (133, 30, NULL, 0, 18, 'user', NULL, '用户创建订单', '2026-04-21 09:00:00');
INSERT INTO `order_status_log` VALUES (134, 30, 0, 1, 7, 'collector', NULL, '回收员接单', '2026-04-21 10:00:00');
INSERT INTO `order_status_log` VALUES (135, 30, 1, 2, 7, 'collector', NULL, '回收员出发上门', '2026-04-21 10:30:00');
INSERT INTO `order_status_log` VALUES (136, 30, 2, 3, 7, 'collector', NULL, '称重完成，实际重量5.5kg', '2026-04-21 12:00:00');
INSERT INTO `order_status_log` VALUES (137, 30, 3, 4, 18, 'user', NULL, '用户确认完成', '2026-04-21 14:00:00');

-- ----------------------------
-- Table structure for points_rule
-- ----------------------------
DROP TABLE IF EXISTS `points_rule`;
CREATE TABLE `points_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `clothes_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '衣物分类（如：外套、裤子、鞋子、床品、其他）',
  `points_per_kg` int NOT NULL DEFAULT 10 COMMENT '每公斤可获得的积分数',
  `min_weight` decimal(10, 2) NULL DEFAULT 0.50 COMMENT '最低回收重量(kg)，低于此重量不计积分',
  `status` tinyint NULL DEFAULT 1 COMMENT '规则状态：0-禁用 1-启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category`(`clothes_category` ASC) USING BTREE COMMENT '每个分类只能有一条规则'
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of points_rule
-- ----------------------------
INSERT INTO `points_rule` VALUES (1, '外套', 15, 0.50, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `points_rule` VALUES (2, '裤子', 10, 0.50, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `points_rule` VALUES (3, '鞋子', 12, 0.50, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `points_rule` VALUES (4, '床品', 8, 0.50, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `points_rule` VALUES (5, '其他', 10, 0.50, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');

-- ----------------------------
-- Table structure for points_transaction
-- ----------------------------
DROP TABLE IF EXISTS `points_transaction`;
CREATE TABLE `points_transaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联 user 表',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变动类型：EARN-获取 EXCHANGE-兑换 DEDUCT-扣除 ADJUST-调整',
  `amount` int NOT NULL COMMENT '积分变动数量（正数表示增加，负数表示减少）',
  `balance_after` int NOT NULL COMMENT '变动后的积分余额',
  `related_order_id` bigint NULL DEFAULT NULL COMMENT '关联的回收订单ID（获取积分时记录）',
  `related_exchange_id` bigint NULL DEFAULT NULL COMMENT '关联的兑换订单ID（兑换积分时记录）',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变动描述（如：回收订单RC20260413xxxx获得积分）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '按用户查询流水',
  INDEX `idx_type`(`type` ASC) USING BTREE COMMENT '按类型筛选',
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE COMMENT '按时间排序'
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of points_transaction
-- ----------------------------
INSERT INTO `points_transaction` VALUES (1, 1, 'EARN', 69, 69, 8, NULL, '回收订单RC20260418090008获得积分', '2026-04-18 14:00:00');
INSERT INTO `points_transaction` VALUES (2, 1, 'EARN', 2, 71, 8, NULL, '评价订单RC20260418090008获得奖励积分', '2026-04-19 10:00:00');
INSERT INTO `points_transaction` VALUES (3, 1, 'EARN', 86, 157, 7, NULL, '回收订单RC20260420100007获得积分', '2026-04-20 15:00:00');
INSERT INTO `points_transaction` VALUES (4, 1, 'EARN', 69, 226, 5, NULL, '回收订单RC20260424100005获得积分', '2026-04-24 14:00:00');
INSERT INTO `points_transaction` VALUES (5, 2, 'EARN', 40, 40, 9, NULL, '回收订单RC20260419080009获得积分', '2026-04-19 13:00:00');
INSERT INTO `points_transaction` VALUES (6, 10, 'EARN', 53, 53, 11, NULL, '回收订单RC20260410090011获得积分', '2026-04-10 14:00:00');
INSERT INTO `points_transaction` VALUES (7, 11, 'EARN', 50, 50, 12, NULL, '回收订单RC20260410140012获得积分', '2026-04-10 18:00:00');
INSERT INTO `points_transaction` VALUES (8, 12, 'EARN', 45, 45, 13, NULL, '回收订单RC20260411090013获得积分', '2026-04-11 14:00:00');
INSERT INTO `points_transaction` VALUES (9, 13, 'EARN', 31, 31, 14, NULL, '回收订单RC20260411140014获得积分', '2026-04-11 18:00:00');
INSERT INTO `points_transaction` VALUES (10, 14, 'EARN', 72, 72, 15, NULL, '回收订单RC20260412090015获得积分', '2026-04-12 14:00:00');
INSERT INTO `points_transaction` VALUES (11, 15, 'EARN', 86, 86, 16, NULL, '回收订单RC20260412140016获得积分', '2026-04-12 18:00:00');
INSERT INTO `points_transaction` VALUES (12, 16, 'EARN', 30, 30, 17, NULL, '回收订单RC20260413090017获得积分', '2026-04-13 14:00:00');
INSERT INTO `points_transaction` VALUES (13, 17, 'EARN', 54, 54, 18, NULL, '回收订单RC20260413140018获得积分', '2026-04-13 18:00:00');
INSERT INTO `points_transaction` VALUES (14, 18, 'EARN', 44, 44, 19, NULL, '回收订单RC20260414090019获得积分', '2026-04-14 14:00:00');
INSERT INTO `points_transaction` VALUES (15, 19, 'EARN', 25, 25, 20, NULL, '回收订单RC20260414140020获得积分', '2026-04-14 18:00:00');
INSERT INTO `points_transaction` VALUES (16, 20, 'EARN', 56, 56, 21, NULL, '回收订单RC20260415090021获得积分', '2026-04-15 14:00:00');
INSERT INTO `points_transaction` VALUES (17, 10, 'EARN', 42, 95, 22, NULL, '回收订单RC20260415140022获得积分', '2026-04-15 18:00:00');
INSERT INTO `points_transaction` VALUES (18, 11, 'EARN', 63, 113, 23, NULL, '回收订单RC20260416090023获得积分', '2026-04-16 14:00:00');
INSERT INTO `points_transaction` VALUES (19, 12, 'EARN', 54, 99, 24, NULL, '回收订单RC20260416140024获得积分', '2026-04-16 18:00:00');
INSERT INTO `points_transaction` VALUES (20, 13, 'EARN', 68, 99, 25, NULL, '回收订单RC20260417090025获得积分', '2026-04-17 14:00:00');
INSERT INTO `points_transaction` VALUES (21, 14, 'EARN', 33, 105, 26, NULL, '回收订单RC20260417140026获得积分', '2026-04-17 18:00:00');
INSERT INTO `points_transaction` VALUES (22, 15, 'EARN', 77, 163, 27, NULL, '回收订单RC20260418140027获得积分', '2026-04-18 18:00:00');
INSERT INTO `points_transaction` VALUES (23, 16, 'EARN', 25, 55, 28, NULL, '回收订单RC20260419140028获得积分', '2026-04-19 18:00:00');
INSERT INTO `points_transaction` VALUES (24, 17, 'EARN', 50, 104, 29, NULL, '回收订单RC20260420140029获得积分', '2026-04-20 18:00:00');
INSERT INTO `points_transaction` VALUES (25, 18, 'EARN', 61, 105, 30, NULL, '回收订单RC20260421090030获得积分', '2026-04-21 14:00:00');

-- ----------------------------
-- Table structure for recycle_order
-- ----------------------------
DROP TABLE IF EXISTS `recycle_order`;
CREATE TABLE `recycle_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号（唯一）',
  `user_id` bigint NOT NULL COMMENT '下单用户ID',
  `collector_id` bigint NULL DEFAULT NULL COMMENT '接单回收员ID',
  `institution_id` bigint NULL DEFAULT NULL COMMENT '接收机构ID',
  `address_id` bigint NOT NULL COMMENT '上门地址ID',
  `address_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '下单时地址快照（JSON），防止地址修改后影响历史订单',
  `appointment_date` date NOT NULL COMMENT '预约上门日期',
  `time_slot_start` time NOT NULL COMMENT '预约时间段-开始（如 09:00）',
  `time_slot_end` time NOT NULL COMMENT '预约时间段-结束（如 11:00）',
  `estimated_weight` decimal(6, 2) NULL DEFAULT NULL COMMENT '用户预估重量（kg）',
  `actual_weight` decimal(6, 2) NULL DEFAULT NULL COMMENT '实际称重重量（kg）',
  `clothes_categories` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '衣物分类（JSON数组，如 [\"外套\",\"裤子\"]）',
  `photos` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '衣物照片URL列表（JSON数组）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户备注',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0=待接单,1=已接单,2=上门中,3=待确认,4=已完成,5=已取消,6=异常',
  `qr_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '溯源二维码图片URL',
  `points_awarded` int NULL DEFAULT NULL COMMENT '本单发放的积分数',
  `cancel_count_date` date NULL DEFAULT NULL COMMENT '取消计数日期（用于每日取消上限判断）',
  `cancelled_at` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `accepted_at` datetime NULL DEFAULT NULL COMMENT '接单时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `destination_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '去向类型：DONATION/RECYCLE/ENVIRONMENTAL',
  `destination_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '去向描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_collector_id`(`collector_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '回收订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recycle_order
-- ----------------------------
INSERT INTO `recycle_order` VALUES (1, 'RC20260428090001', 1, NULL, NULL, 1, '{\"name\":\"张三\",\"phone\":\"13800000001\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"天河区\",\"detailAddress\":\"天河路385号太古汇北塔12楼\"}', '2026-04-30', '09:00:00', '11:00:00', 5.00, NULL, '[\"外套\",\"裤子\"]', NULL, '请准时上门', 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 09:00:00', '2026-04-28 09:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (2, 'RC20260427100002', 1, 3, NULL, 1, '{\"name\":\"张三\",\"phone\":\"13800000001\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"天河区\",\"detailAddress\":\"天河路385号太古汇北塔12楼\"}', '2026-04-29', '10:00:00', '12:00:00', 3.00, NULL, '[\"衬衫\",\"裙子\"]', NULL, NULL, 1, NULL, NULL, NULL, NULL, '2026-04-27 11:00:00', NULL, '2026-04-27 10:00:00', '2026-04-27 11:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (3, 'RC20260426140003', 1, 3, NULL, 2, '{\"name\":\"张三\",\"phone\":\"13800000001\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"海珠区\",\"detailAddress\":\"新港中路397号TIT创意园B3栋\"}', '2026-04-28', '14:00:00', '16:00:00', 8.00, NULL, '[\"棉被\",\"冬装外套\",\"毛衣\"]', NULL, NULL, 2, NULL, NULL, NULL, NULL, '2026-04-26 15:00:00', NULL, '2026-04-26 14:00:00', '2026-04-26 16:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (4, 'RC20260425080004', 1, 3, NULL, 1, '{\"name\":\"张三\",\"phone\":\"13800000001\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"天河区\",\"detailAddress\":\"天河路385号太古汇北塔12楼\"}', '2026-04-27', '09:00:00', '11:00:00', 10.00, 8.50, '[\"外套\",\"裤子\",\"鞋子\"]', NULL, NULL, 3, '/uploads/qr_RC20260425080004.png', NULL, NULL, NULL, '2026-04-25 09:00:00', NULL, '2026-04-25 08:00:00', '2026-04-25 10:30:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (5, 'RC20260424100005', 1, 3, NULL, 2, '{\"name\":\"张三\",\"phone\":\"13800000001\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"海珠区\",\"detailAddress\":\"新港中路397号TIT创意园B3栋\"}', '2026-04-26', '10:00:00', '12:00:00', 6.00, 5.50, '[\"外套\",\"裤子\"]', NULL, NULL, 4, '/uploads/qr_RC20260424100005.png', 69, NULL, NULL, '2026-04-24 11:00:00', '2026-04-24 14:00:00', '2026-04-24 10:00:00', '2026-04-24 14:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (6, 'RC20260423150006', 1, NULL, NULL, 1, '{\"name\":\"张三\",\"phone\":\"13800000001\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"天河区\",\"detailAddress\":\"天河路385号太古汇北塔12楼\"}', '2026-04-25', '15:00:00', '17:00:00', 2.00, NULL, '[\"围巾\",\"帽子\"]', NULL, '临时有事取消', 5, NULL, NULL, NULL, '2026-04-23 16:00:00', NULL, NULL, '2026-04-23 15:00:00', '2026-04-23 16:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (7, 'RC20260420100007', 1, 4, 5, 1, '{\"name\":\"张三\",\"phone\":\"13800000001\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"天河区\",\"detailAddress\":\"天河路385号太古汇北塔12楼\"}', '2026-04-22', '10:00:00', '12:00:00', 8.00, 7.00, '[\"外套\",\"裤子\",\"鞋子\"]', NULL, NULL, 7, '/uploads/qr_RC20260420100007.png', 86, NULL, NULL, '2026-04-20 11:00:00', '2026-04-20 15:00:00', '2026-04-20 10:00:00', '2026-04-22 09:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (8, 'RC20260418090008', 1, 4, 5, 2, '{\"name\":\"张三\",\"phone\":\"13800000001\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"海珠区\",\"detailAddress\":\"新港中路397号TIT创意园B3栋\"}', '2026-04-20', '09:00:00', '11:00:00', 7.00, 6.00, '[\"外套\",\"床品\"]', NULL, NULL, 8, '/uploads/qr_RC20260418090008.png', 69, NULL, NULL, '2026-04-18 10:00:00', '2026-04-18 14:00:00', '2026-04-18 09:00:00', '2026-04-21 10:00:00', 'DONATION', '捐赠至云南山区希望小学');
INSERT INTO `recycle_order` VALUES (9, 'RC20260419080009', 2, 3, 5, 3, '{\"name\":\"李四\",\"phone\":\"13800000002\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"番禺区\",\"detailAddress\":\"万博CBD汉溪大道东362号\"}', '2026-04-21', '08:00:00', '10:00:00', 5.00, 4.00, '[\"裤子\",\"其他\"]', NULL, NULL, 7, '/uploads/qr_RC20260419080009.png', 40, NULL, NULL, '2026-04-19 09:00:00', '2026-04-19 13:00:00', '2026-04-19 08:00:00', '2026-04-21 11:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (10, 'RC20260429100010', 2, NULL, NULL, 3, '{\"name\":\"李四\",\"phone\":\"13800000002\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"番禺区\",\"detailAddress\":\"万博CBD汉溪大道东362号\"}', '2026-05-01', '10:00:00', '12:00:00', 3.00, NULL, '[\"T恤\",\"短裤\"]', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-29 10:00:00', '2026-04-29 10:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (11, 'RC20260410090011', 10, 3, NULL, 4, '{\"name\":\"郑伟\",\"phone\":\"13800000010\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"白云区\",\"detailAddress\":\"白云大道北100号\"}', '2026-04-12', '09:00:00', '11:00:00', 4.00, 3.50, '[\"外套\"]', NULL, NULL, 4, '/uploads/qr_RC20260410090011.png', 53, NULL, NULL, '2026-04-10 10:00:00', '2026-04-10 14:00:00', '2026-04-10 09:00:00', '2026-04-10 14:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (12, 'RC20260410140012', 11, 4, NULL, 5, '{\"name\":\"冯丽\",\"phone\":\"13800000011\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"越秀区\",\"detailAddress\":\"中山五路33号\"}', '2026-04-12', '14:00:00', '16:00:00', 5.00, 4.50, '[\"裤子\",\"鞋子\"]', NULL, NULL, 4, '/uploads/qr_RC20260410140012.png', 50, NULL, NULL, '2026-04-10 15:00:00', '2026-04-10 18:00:00', '2026-04-10 14:00:00', '2026-04-10 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (13, 'RC20260411090013', 12, 6, NULL, 6, '{\"name\":\"褚明\",\"phone\":\"13800000012\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"荔湾区\",\"detailAddress\":\"康王中路486号\"}', '2026-04-13', '09:00:00', '11:00:00', 6.00, 5.00, '[\"床品\",\"其他\"]', NULL, NULL, 4, '/uploads/qr_RC20260411090013.png', 45, NULL, NULL, '2026-04-11 10:00:00', '2026-04-11 14:00:00', '2026-04-11 09:00:00', '2026-04-11 14:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (14, 'RC20260411140014', 13, 7, NULL, 7, '{\"name\":\"卫华\",\"phone\":\"13800000013\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"天河区\",\"detailAddress\":\"体育西路103号\"}', '2026-04-13', '14:00:00', '16:00:00', 3.00, 2.50, '[\"外套\",\"裤子\"]', NULL, NULL, 4, '/uploads/qr_RC20260411140014.png', 31, NULL, NULL, '2026-04-11 15:00:00', '2026-04-11 18:00:00', '2026-04-11 14:00:00', '2026-04-11 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (15, 'RC20260412090015', 14, 3, NULL, 8, '{\"name\":\"蒋芳\",\"phone\":\"13800000014\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"海珠区\",\"detailAddress\":\"江南大道中180号\"}', '2026-04-14', '09:00:00', '11:00:00', 7.00, 6.50, '[\"鞋子\",\"其他\"]', NULL, NULL, 4, '/uploads/qr_RC20260412090015.png', 72, NULL, NULL, '2026-04-12 10:00:00', '2026-04-12 14:00:00', '2026-04-12 09:00:00', '2026-04-12 14:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (16, 'RC20260412140016', 15, 4, NULL, 9, '{\"name\":\"沈强\",\"phone\":\"13800000015\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"番禺区\",\"detailAddress\":\"市桥街大北路88号\"}', '2026-04-14', '14:00:00', '16:00:00', 8.00, 7.50, '[\"外套\",\"床品\"]', NULL, NULL, 4, '/uploads/qr_RC20260412140016.png', 86, NULL, NULL, '2026-04-12 15:00:00', '2026-04-12 18:00:00', '2026-04-12 14:00:00', '2026-04-12 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (17, 'RC20260413090017', 16, 6, NULL, 10, '{\"name\":\"韩梅\",\"phone\":\"13800000016\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"黄埔区\",\"detailAddress\":\"开发大道388号\"}', '2026-04-15', '09:00:00', '11:00:00', 4.00, 3.00, '[\"裤子\"]', NULL, NULL, 4, '/uploads/qr_RC20260413090017.png', 30, NULL, NULL, '2026-04-13 10:00:00', '2026-04-13 14:00:00', '2026-04-13 09:00:00', '2026-04-13 14:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (18, 'RC20260413140018', 17, 7, NULL, 11, '{\"name\":\"杨磊\",\"phone\":\"13800000017\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"花都区\",\"detailAddress\":\"新华街建设路66号\"}', '2026-04-15', '14:00:00', '16:00:00', 5.00, 4.00, '[\"外套\",\"鞋子\"]', NULL, NULL, 4, '/uploads/qr_RC20260413140018.png', 54, NULL, NULL, '2026-04-13 15:00:00', '2026-04-13 18:00:00', '2026-04-13 14:00:00', '2026-04-13 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (19, 'RC20260414090019', 18, 3, NULL, 12, '{\"name\":\"朱婷\",\"phone\":\"13800000018\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"增城区\",\"detailAddress\":\"荔城街府佑路2号\"}', '2026-04-16', '09:00:00', '11:00:00', 6.00, 5.50, '[\"床品\"]', NULL, NULL, 4, '/uploads/qr_RC20260414090019.png', 44, NULL, NULL, '2026-04-14 10:00:00', '2026-04-14 14:00:00', '2026-04-14 09:00:00', '2026-04-14 14:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (20, 'RC20260414140020', 19, 4, NULL, 13, '{\"name\":\"秦刚\",\"phone\":\"13800000019\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"从化区\",\"detailAddress\":\"街口街新城路99号\"}', '2026-04-16', '14:00:00', '16:00:00', 3.00, 2.50, '[\"其他\"]', NULL, NULL, 4, '/uploads/qr_RC20260414140020.png', 25, NULL, NULL, '2026-04-14 15:00:00', '2026-04-14 18:00:00', '2026-04-14 14:00:00', '2026-04-14 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (21, 'RC20260415090021', 20, 6, NULL, 14, '{\"name\":\"许静\",\"phone\":\"13800000020\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"南沙区\",\"detailAddress\":\"南沙街进港大道12号\"}', '2026-04-17', '09:00:00', '11:00:00', 5.00, 4.50, '[\"外套\",\"裤子\"]', NULL, NULL, 4, '/uploads/qr_RC20260415090021.png', 56, NULL, NULL, '2026-04-15 10:00:00', '2026-04-15 14:00:00', '2026-04-15 09:00:00', '2026-04-15 14:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (22, 'RC20260415140022', 10, 7, NULL, 4, '{\"name\":\"郑伟\",\"phone\":\"13800000010\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"白云区\",\"detailAddress\":\"白云大道北100号\"}', '2026-04-17', '14:00:00', '16:00:00', 4.00, 3.50, '[\"鞋子\"]', NULL, NULL, 4, '/uploads/qr_RC20260415140022.png', 42, NULL, NULL, '2026-04-15 15:00:00', '2026-04-15 18:00:00', '2026-04-15 14:00:00', '2026-04-15 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (23, 'RC20260416090023', 11, 3, NULL, 5, '{\"name\":\"冯丽\",\"phone\":\"13800000011\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"越秀区\",\"detailAddress\":\"中山五路33号\"}', '2026-04-18', '09:00:00', '11:00:00', 6.00, 5.00, '[\"外套\",\"其他\"]', NULL, NULL, 4, '/uploads/qr_RC20260416090023.png', 63, NULL, NULL, '2026-04-16 10:00:00', '2026-04-16 14:00:00', '2026-04-16 09:00:00', '2026-04-16 14:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (24, 'RC20260416140024', 12, 4, NULL, 6, '{\"name\":\"褚明\",\"phone\":\"13800000012\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"荔湾区\",\"detailAddress\":\"康王中路486号\"}', '2026-04-18', '14:00:00', '16:00:00', 7.00, 6.00, '[\"裤子\",\"床品\"]', NULL, NULL, 4, '/uploads/qr_RC20260416140024.png', 54, NULL, NULL, '2026-04-16 15:00:00', '2026-04-16 18:00:00', '2026-04-16 14:00:00', '2026-04-16 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (25, 'RC20260417090025', 13, 6, NULL, 7, '{\"name\":\"卫华\",\"phone\":\"13800000013\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"天河区\",\"detailAddress\":\"体育西路103号\"}', '2026-04-19', '09:00:00', '11:00:00', 5.00, 4.50, '[\"外套\"]', NULL, NULL, 4, '/uploads/qr_RC20260417090025.png', 68, NULL, NULL, '2026-04-17 10:00:00', '2026-04-17 14:00:00', '2026-04-17 09:00:00', '2026-04-17 14:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (26, 'RC20260417140026', 14, 7, NULL, 8, '{\"name\":\"蒋芳\",\"phone\":\"13800000014\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"海珠区\",\"detailAddress\":\"江南大道中180号\"}', '2026-04-19', '14:00:00', '16:00:00', 4.00, 3.00, '[\"裤子\",\"鞋子\"]', NULL, NULL, 4, '/uploads/qr_RC20260417140026.png', 33, NULL, NULL, '2026-04-17 15:00:00', '2026-04-17 18:00:00', '2026-04-17 14:00:00', '2026-04-17 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (27, 'RC20260418140027', 15, 3, NULL, 9, '{\"name\":\"沈强\",\"phone\":\"13800000015\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"番禺区\",\"detailAddress\":\"市桥街大北路88号\"}', '2026-04-20', '14:00:00', '16:00:00', 8.00, 7.00, '[\"外套\",\"床品\",\"其他\"]', NULL, NULL, 4, '/uploads/qr_RC20260418140027.png', 77, NULL, NULL, '2026-04-18 15:00:00', '2026-04-18 18:00:00', '2026-04-18 14:00:00', '2026-04-18 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (28, 'RC20260419140028', 16, 4, NULL, 10, '{\"name\":\"韩梅\",\"phone\":\"13800000016\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"黄埔区\",\"detailAddress\":\"开发大道388号\"}', '2026-04-21', '14:00:00', '16:00:00', 3.00, 2.50, '[\"裤子\"]', NULL, NULL, 4, '/uploads/qr_RC20260419140028.png', 25, NULL, NULL, '2026-04-19 15:00:00', '2026-04-19 18:00:00', '2026-04-19 14:00:00', '2026-04-19 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (29, 'RC20260420140029', 17, 6, NULL, 11, '{\"name\":\"杨磊\",\"phone\":\"13800000017\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"花都区\",\"detailAddress\":\"新华街建设路66号\"}', '2026-04-22', '14:00:00', '16:00:00', 5.00, 4.00, '[\"外套\",\"裤子\"]', NULL, NULL, 4, '/uploads/qr_RC20260420140029.png', 50, NULL, NULL, '2026-04-20 15:00:00', '2026-04-20 18:00:00', '2026-04-20 14:00:00', '2026-04-20 18:00:00', NULL, NULL);
INSERT INTO `recycle_order` VALUES (30, 'RC20260421090030', 18, 7, NULL, 12, '{\"name\":\"朱婷\",\"phone\":\"13800000018\",\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"增城区\",\"detailAddress\":\"荔城街府佑路2号\"}', '2026-04-23', '09:00:00', '11:00:00', 6.00, 5.50, '[\"鞋子\",\"其他\"]', NULL, NULL, 4, '/uploads/qr_RC20260421090030.png', 61, NULL, NULL, '2026-04-21 10:00:00', '2026-04-21 14:00:00', '2026-04-21 09:00:00', '2026-04-21 14:00:00', NULL, NULL);

-- ----------------------------
-- Table structure for role_application
-- ----------------------------
DROP TABLE IF EXISTS `role_application`;
CREATE TABLE `role_application`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `user_id` bigint NOT NULL COMMENT '申请用户ID',
  `apply_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请角色：COLLECTOR=回收员, INSTITUTION=机构',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名/机构名称',
  `id_card_photo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证照片URL（回收员必填）',
  `health_cert_photo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '健康证照片URL（回收员选填）',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构地址（机构必填）',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人（机构必填）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0=待审核, 1=已通过, 2=已拒绝',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_application
-- ----------------------------
INSERT INTO `role_application` VALUES (1, 3, 'COLLECTOR', '王回收', '/uploads/id_card_wang.jpg', NULL, NULL, NULL, 1, NULL, '2026-04-01 10:00:00', '2026-04-01 14:00:00');
INSERT INTO `role_application` VALUES (2, 4, 'COLLECTOR', '赵回收', '/uploads/id_card_zhao.jpg', NULL, NULL, NULL, 1, NULL, '2026-04-02 10:00:00', '2026-04-02 14:00:00');
INSERT INTO `role_application` VALUES (3, 6, 'COLLECTOR', '刘回收', '/uploads/id_card_liu.jpg', NULL, NULL, NULL, 1, NULL, '2026-04-03 10:00:00', '2026-04-03 14:00:00');
INSERT INTO `role_application` VALUES (4, 7, 'COLLECTOR', '孙回收', '/uploads/id_card_sun.jpg', NULL, NULL, NULL, 1, NULL, '2026-04-04 10:00:00', '2026-04-04 14:00:00');
INSERT INTO `role_application` VALUES (5, 5, 'INSTITUTION', '绿色地球环保机构', NULL, NULL, '北京市海淀区中关村大街1号', '陈机构', 1, NULL, '2026-04-05 10:00:00', '2026-04-05 14:00:00');
INSERT INTO `role_application` VALUES (6, 8, 'INSTITUTION', '爱心衣橱慈善中心', NULL, NULL, '上海市浦东新区陆家嘴环路958号', '周机构', 1, NULL, '2026-04-06 10:00:00', '2026-04-06 14:00:00');
INSERT INTO `role_application` VALUES (7, 9, 'INSTITUTION', '循环再生科技公司', NULL, NULL, '深圳市南山区科技园南区1栋', '吴机构', 1, NULL, '2026-04-07 10:00:00', '2026-04-07 14:00:00');
INSERT INTO `role_application` VALUES (8, 10, 'COLLECTOR', '郑伟', '/uploads/id_card_zheng.jpg', NULL, NULL, NULL, 2, '身份证照片模糊，请重新上传', '2026-04-08 10:00:00', '2026-04-08 14:00:00');
INSERT INTO `role_application` VALUES (9, 11, 'COLLECTOR', '冯丽', '/uploads/id_card_feng.jpg', NULL, NULL, NULL, 2, '年龄不符合要求', '2026-04-09 10:00:00', '2026-04-09 14:00:00');
INSERT INTO `role_application` VALUES (10, 12, 'COLLECTOR', '褚明', '/uploads/id_card_chu.jpg', NULL, NULL, NULL, 2, '身份信息与注册信息不一致', '2026-04-10 10:00:00', '2026-04-10 14:00:00');
INSERT INTO `role_application` VALUES (11, 13, 'COLLECTOR', '卫华', '/uploads/id_card_wei.jpg', NULL, NULL, NULL, 2, '所在区域回收员已满', '2026-04-11 10:00:00', '2026-04-11 14:00:00');
INSERT INTO `role_application` VALUES (12, 14, 'COLLECTOR', '蒋芳', '/uploads/id_card_jiang.jpg', NULL, NULL, NULL, 0, NULL, '2026-04-28 10:00:00', '2026-04-28 10:00:00');
INSERT INTO `role_application` VALUES (13, 15, 'COLLECTOR', '沈强', '/uploads/id_card_shen.jpg', NULL, NULL, NULL, 0, NULL, '2026-04-28 11:00:00', '2026-04-28 11:00:00');
INSERT INTO `role_application` VALUES (14, 16, 'COLLECTOR', '韩梅', '/uploads/id_card_han.jpg', NULL, NULL, NULL, 0, NULL, '2026-04-29 09:00:00', '2026-04-29 09:00:00');
INSERT INTO `role_application` VALUES (15, 17, 'INSTITUTION', '杨磊环保工作室', NULL, NULL, '广州市花都区新华街建设路66号', '杨磊', 0, NULL, '2026-04-29 10:00:00', '2026-04-29 10:00:00');

-- ----------------------------
-- Table structure for service_review
-- ----------------------------
DROP TABLE IF EXISTS `service_review`;
CREATE TABLE `service_review`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '鍏宠仈璁㈠崟ID',
  `user_id` bigint NOT NULL COMMENT '璇勪环鐢ㄦ埛ID',
  `collector_id` bigint NOT NULL COMMENT '琚?瘎浠峰洖鏀跺憳鐨剈ser_id',
  `punctuality_score` tinyint NOT NULL COMMENT '鍑嗘椂搴﹁瘎鍒?1-5)',
  `attitude_score` tinyint NOT NULL COMMENT '鎬佸害璇勫垎(1-5)',
  `weighing_score` tinyint NOT NULL COMMENT '绉伴噸瑙勮寖搴﹁瘎鍒?1-5)',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏂囧瓧璇勪环锛堝彲閫夛級',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_collector_id`(`collector_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鏈嶅姟璇勪环琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_review
-- ----------------------------
INSERT INTO `service_review` VALUES (1, 8, 1, 4, 5, 4, 5, '回收员很准时，态度也不错，称重很规范，整体体验很好！', '2026-04-19 10:00:00');
INSERT INTO `service_review` VALUES (2, 11, 10, 3, 5, 5, 5, '非常满意，回收员服务态度很好，准时到达。', '2026-04-10 15:00:00');
INSERT INTO `service_review` VALUES (3, 12, 11, 4, 4, 4, 4, '整体不错，回收员比较专业。', '2026-04-10 19:00:00');
INSERT INTO `service_review` VALUES (4, 13, 12, 6, 3, 4, 3, '称重感觉有点偏差，但态度还行。', '2026-04-11 15:00:00');
INSERT INTO `service_review` VALUES (5, 14, 13, 7, 5, 5, 4, '回收员很热情，服务很周到。', '2026-04-11 19:00:00');
INSERT INTO `service_review` VALUES (6, 15, 14, 3, 4, 3, 4, '准时到达，但态度一般。', '2026-04-12 15:00:00');
INSERT INTO `service_review` VALUES (7, 16, 15, 4, 5, 5, 5, '五星好评！回收员非常专业负责。', '2026-04-12 19:00:00');
INSERT INTO `service_review` VALUES (8, 17, 16, 6, 4, 4, 3, '还可以，称重速度有点慢。', '2026-04-13 15:00:00');
INSERT INTO `service_review` VALUES (9, 18, 17, 7, 3, 3, 4, '迟到了十分钟，其他还好。', '2026-04-13 19:00:00');
INSERT INTO `service_review` VALUES (10, 19, 18, 3, 5, 4, 5, '很好的服务体验，推荐！', '2026-04-14 15:00:00');
INSERT INTO `service_review` VALUES (11, 20, 19, 4, 4, 5, 4, '回收员态度很好，很有耐心。', '2026-04-14 19:00:00');
INSERT INTO `service_review` VALUES (12, 21, 20, 6, 5, 5, 5, '完美的回收体验，全五星！', '2026-04-15 15:00:00');
INSERT INTO `service_review` VALUES (13, 22, 10, 7, 3, 4, 3, '一般般，称重不太准确。', '2026-04-15 19:00:00');
INSERT INTO `service_review` VALUES (14, 23, 11, 3, 4, 4, 5, '称重很规范，值得信赖。', '2026-04-16 15:00:00');
INSERT INTO `service_review` VALUES (15, 24, 12, 4, 5, 3, 4, '准时但态度可以再好一点。', '2026-04-16 19:00:00');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号（登录凭证）',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户 / COLLECTOR-回收员 / INSTITUTION-机构',
  `points_balance` int NOT NULL DEFAULT 0 COMMENT '积分余额',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=正常, 0=禁用, 2=已注销',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '13800000001', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '张三', 'USER', 226, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `user` VALUES (2, '13800000002', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '李四', 'USER', 40, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (3, '13800000003', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '王磊', 'COLLECTOR', 0, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `user` VALUES (4, '13800000004', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '赵敏', 'COLLECTOR', 0, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `user` VALUES (5, '13800000005', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '陈德明', 'INSTITUTION', 0, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `user` VALUES (6, '13800000006', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '刘贵', 'COLLECTOR', 0, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `user` VALUES (7, '13800000007', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '孙思通', 'COLLECTOR', 0, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `user` VALUES (8, '13800000008', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '周静怡', 'INSTITUTION', 0, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `user` VALUES (9, '13800000009', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '吴遥辉', 'INSTITUTION', 0, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:14');
INSERT INTO `user` VALUES (10, '13800000010', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '郑伟', 'USER', 95, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (11, '13800000011', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '冯丽', 'USER', 113, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (12, '13800000012', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '褚明', 'USER', 99, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (13, '13800000013', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '卫华', 'USER', 99, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (14, '13800000014', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '蒋芳', 'USER', 105, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (15, '13800000015', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '沈强', 'USER', 163, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (16, '13800000016', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '韩梅', 'USER', 55, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (17, '13800000017', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '杨磊', 'USER', 104, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (18, '13800000018', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '朱婷', 'USER', 105, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (19, '13800000019', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '秦刚', 'USER', 25, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');
INSERT INTO `user` VALUES (20, '13800000020', '$2a$10$GxV/zz1Bx1Amn.LUGa2usu2FbFMTDni7EO6O4NKhGOJP8wpNIjOB.', '许静', 'USER', 56, 1, '2026-05-02 17:03:14', '2026-05-02 17:03:15');

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认地址：1=是, 0=否',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_address
-- ----------------------------
INSERT INTO `user_address` VALUES (1, 1, '张三', '13800000001', '广东省', '广州市', '天河区', '天河路385号太古汇北塔12楼', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (2, 1, '张三', '13800000001', '广东省', '广州市', '海珠区', '新港中路397号TIT创意园B3栋', 0, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (3, 2, '李四', '13800000002', '广东省', '广州市', '番禺区', '万博CBD汉溪大道东362号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (4, 10, '郑伟', '13800000010', '广东省', '广州市', '白云区', '白云大道北100号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (5, 11, '冯丽', '13800000011', '广东省', '广州市', '越秀区', '中山五路33号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (6, 12, '褚明', '13800000012', '广东省', '广州市', '荔湾区', '康王中路486号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (7, 13, '卫华', '13800000013', '广东省', '广州市', '天河区', '体育西路103号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (8, 14, '蒋芳', '13800000014', '广东省', '广州市', '海珠区', '江南大道中180号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (9, 15, '沈强', '13800000015', '广东省', '广州市', '番禺区', '市桥街大北路88号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (10, 16, '韩梅', '13800000016', '广东省', '广州市', '黄埔区', '开发大道388号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (11, 17, '杨磊', '13800000017', '广东省', '广州市', '花都区', '新华街建设路66号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (12, 18, '朱婷', '13800000018', '广东省', '广州市', '增城区', '荔城街府佑路2号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (13, 19, '秦刚', '13800000019', '广东省', '广州市', '从化区', '街口街新城路99号', 1, '2026-05-02 17:03:14');
INSERT INTO `user_address` VALUES (14, 20, '许静', '13800000020', '广东省', '广州市', '南沙区', '南沙街进港大道12号', 1, '2026-05-02 17:03:14');

SET FOREIGN_KEY_CHECKS = 1;
