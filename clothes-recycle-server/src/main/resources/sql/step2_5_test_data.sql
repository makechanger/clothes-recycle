-- ============================================================
-- Step 2.5 测试数据：用户地址 + 回收订单 + 状态日志
-- 前置条件：已执行 step2_refactor_role.sql，user 表中存在测试用户
-- ============================================================

USE clothes_recycle;

-- ==================== 1. 用户地址测试数据 ====================
-- 为测试用户(13800000001) 添加 3 个地址，其中一个为默认地址

INSERT INTO `user_address` (user_id, name, phone, province, city, district, detail_address, is_default) VALUES
-- 默认地址
(1, '张三', '13800000001', '广东省', '广州市', '天河区', '天河路385号太古汇北塔12楼', 1),
-- 普通地址
(1, '张三', '13800000001', '广东省', '广州市', '海珠区', '新港中路397号TIT创意园B3栋', 0),
-- 公司地址
(1, '张三（公司）', '13800000001', '广东省', '广州市', '番禺区', '万博CBD汉溪大道东362号', 0);

-- 为测试回收员(13800000002) 添加 1 个地址（回收员也可以作为普通用户下单）
INSERT INTO `user_address` (user_id, name, phone, province, city, district, detail_address, is_default) VALUES
(2, '李四', '13800000002', '浙江省', '杭州市', '西湖区', '文三路478号华星科技大厦5楼', 1);

-- ==================== 2. 回收订单测试数据 ====================
-- 为测试用户创建不同状态的订单，方便测试各个接口

-- 订单1：待接单状态（status=0），可测试「取消订单」
INSERT INTO `recycle_order` (order_no, user_id, address_id, address_snapshot, appointment_date, time_slot_start, time_slot_end, estimated_weight, clothes_categories, photos, remark, status, created_at) VALUES
('RC20260404100001', 1, 1,
 '{"name":"张三","phone":"13800000001","province":"广东省","city":"广州市","district":"天河区","detailAddress":"天河路385号太古汇北塔12楼"}',
 '2026-04-06', '09:00:00', '11:00:00', 5.0,
 '["外套","裤子","T恤"]', '[]',
 '请准时上门，家里有狗', 0, '2026-04-04 10:00:00');

-- 订单2：已接单状态（status=1），可测试「取消订单」
INSERT INTO `recycle_order` (order_no, user_id, address_id, address_snapshot, appointment_date, time_slot_start, time_slot_end, estimated_weight, clothes_categories, remark, status, collector_id, accepted_at, created_at) VALUES
('RC20260403090002', 1, 2,
 '{"name":"张三","phone":"13800000001","province":"广东省","city":"广州市","district":"海珠区","detailAddress":"新港中路397号TIT创意园B3栋"}',
 '2026-04-05', '14:00:00', '16:00:00', 3.5,
 '["衬衫","裙子"]',
 '放在门口即可', 1, 2, '2026-04-03 10:30:00', '2026-04-03 09:00:00');

-- 订单3：上门中状态（status=2），不可取消也不可确认
INSERT INTO `recycle_order` (order_no, user_id, address_id, address_snapshot, appointment_date, time_slot_start, time_slot_end, estimated_weight, clothes_categories, status, collector_id, accepted_at, created_at) VALUES
('RC20260402140003', 1, 1,
 '{"name":"张三","phone":"13800000001","province":"广东省","city":"广州市","district":"天河区","detailAddress":"天河路385号太古汇北塔12楼"}',
 '2026-04-04', '10:00:00', '12:00:00', 8.0,
 '["棉被","冬装外套","毛衣"]',
 2, 2, '2026-04-02 15:00:00', '2026-04-02 14:00:00');

-- 订单4：待确认状态（status=3），可测试「确认完成」+ 积分发放
INSERT INTO `recycle_order` (order_no, user_id, address_id, address_snapshot, appointment_date, time_slot_start, time_slot_end, estimated_weight, actual_weight, clothes_categories, status, collector_id, accepted_at, created_at) VALUES
('RC20260401080004', 1, 3,
 '{"name":"张三（公司）","phone":"13800000001","province":"广东省","city":"广州市","district":"番禺区","detailAddress":"万博CBD汉溪大道东362号"}',
 '2026-04-02', '09:00:00', '11:00:00', 10.0, 8.5,
 '["外套","裤子","鞋子","包包"]',
 3, 2, '2026-04-01 09:00:00', '2026-04-01 08:00:00');

-- 订单5：已完成状态（status=4），历史订单
INSERT INTO `recycle_order` (order_no, user_id, address_id, address_snapshot, appointment_date, time_slot_start, time_slot_end, estimated_weight, actual_weight, clothes_categories, points_awarded, status, collector_id, accepted_at, completed_at, created_at) VALUES
('RC20260330100005', 1, 1,
 '{"name":"张三","phone":"13800000001","province":"广东省","city":"广州市","district":"天河区","detailAddress":"天河路385号太古汇北塔12楼"}',
 '2026-03-31', '10:00:00', '12:00:00', 6.0, 5.5,
 '["T恤","短裤"]',
 55, 4, 2, '2026-03-30 11:00:00', '2026-03-31 11:30:00', '2026-03-30 10:00:00');

-- 订单6：已取消状态（status=5），历史订单
INSERT INTO `recycle_order` (order_no, user_id, address_id, address_snapshot, appointment_date, time_slot_start, time_slot_end, estimated_weight, clothes_categories, remark, status, cancelled_at, created_at) VALUES
('RC20260329150006', 1, 2,
 '{"name":"张三","phone":"13800000001","province":"广东省","city":"广州市","district":"海珠区","detailAddress":"新港中路397号TIT创意园B3栋"}',
 '2026-03-30', '15:00:00', '17:00:00', 2.0,
 '["围巾","帽子"]',
 '临时有事取消', 5, '2026-03-29 16:00:00', '2026-03-29 15:00:00');

-- ==================== 3. 订单状态流转日志 ====================
-- 为每个订单记录状态变更历史

-- 订单1：创建
INSERT INTO `order_status_log` (order_id, from_status, to_status, operator_id, operator_type, remark) VALUES
(1, NULL, 0, 1, 'user', '用户创建订单');

-- 订单2：创建 → 接单
INSERT INTO `order_status_log` (order_id, from_status, to_status, operator_id, operator_type, remark) VALUES
(2, NULL, 0, 1, 'user', '用户创建订单'),
(2, 0, 1, 2, 'collector', '回收员接单');

-- 订单3：创建 → 接单 → 上门中
INSERT INTO `order_status_log` (order_id, from_status, to_status, operator_id, operator_type, remark) VALUES
(3, NULL, 0, 1, 'user', '用户创建订单'),
(3, 0, 1, 2, 'collector', '回收员接单'),
(3, 1, 2, 2, 'collector', '回收员出发上门');

-- 订单4：创建 → 接单 → 上门中 → 待确认（称重完成）
INSERT INTO `order_status_log` (order_id, from_status, to_status, operator_id, operator_type, remark) VALUES
(4, NULL, 0, 1, 'user', '用户创建订单'),
(4, 0, 1, 2, 'collector', '回收员接单'),
(4, 1, 2, 2, 'collector', '回收员出发上门'),
(4, 2, 3, 2, 'collector', '称重完成，实际重量8.5kg，等待用户确认');

-- 订单5：完整流程（创建 → 接单 → 上门 → 称重 → 确认完成）
INSERT INTO `order_status_log` (order_id, from_status, to_status, operator_id, operator_type, remark) VALUES
(5, NULL, 0, 1, 'user', '用户创建订单'),
(5, 0, 1, 2, 'collector', '回收员接单'),
(5, 1, 2, 2, 'collector', '回收员出发上门'),
(5, 2, 3, 2, 'collector', '称重完成，实际重量5.5kg，等待用户确认'),
(5, 3, 4, 1, 'user', '用户确认完成，发放积分55');

-- 订单6：创建 → 取消
INSERT INTO `order_status_log` (order_id, from_status, to_status, operator_id, operator_type, remark) VALUES
(6, NULL, 0, 1, 'user', '用户创建订单'),
(6, 0, 5, 1, 'user', '用户取消订单');

-- ==================== 4. 更新用户积分余额 ====================
-- 测试用户已完成订单5获得55积分
UPDATE `user` SET points_balance = 55 WHERE phone = '13800000001';

-- ==================== 完成提示 ====================
-- 测试数据概览：
-- 用户地址：4条（用户1有3个地址，用户2有1个地址）
-- 回收订单：6条（覆盖全部6种状态：待接单/已接单/上门中/待确认/已完成/已取消）
-- 状态日志：15条（每个订单的完整流转记录）
--
-- 可测试场景：
-- 1. GET  /api/user/order/list          → 返回6条订单
-- 2. GET  /api/user/order/list?status=0  → 返回1条待接单订单
-- 3. GET  /api/user/order/1              → 返回订单详情
-- 4. POST /api/user/order/1/cancel       → 取消待接单订单（成功）
-- 5. POST /api/user/order/2/cancel       → 取消已接单订单（成功）
-- 6. POST /api/user/order/3/cancel       → 取消上门中订单（失败，状态不允许）
-- 7. POST /api/user/order/4/confirm      → 确认完成（成功，获得85积分）
-- 8. POST /api/user/order/5/confirm      → 确认已完成订单（失败，状态不允许）
