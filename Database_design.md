### X.X.3 数据表详细设计

**（1）用户表（user）**

用户表是系统的统一账户表，所有角色（普通用户、回收员、机构）均通过该表进行认证。通过 `role` 字段区分用户角色，实现"一个账户，多种身份"的设计。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 用户ID |
| phone | VARCHAR(20) | 非空，唯一索引 | 手机号（登录凭证） |
| password_hash | VARCHAR(255) | 非空 | BCrypt加密密码 |
| name | VARCHAR(50) | 可空 | 用户姓名 |
| role | VARCHAR(20) | 非空，默认'USER' | 角色：USER/COLLECTOR/INSTITUTION |
| points_balance | INT | 非空，默认0 | 积分余额 |
| status | TINYINT | 非空，默认1 | 状态：1启用/0禁用/2注销 |
| created_at | DATETIME | 非空，默认当前时间 | 注册时间 |
| updated_at | DATETIME | 非空，自动更新 | 更新时间 |

**（2）管理员表（admin）**

管理员表独立于用户体系，用于后台管理系统的登录认证。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 管理员ID |
| username | VARCHAR(50) | 非空，唯一索引 | 登录用户名 |
| password_hash | VARCHAR(255) | 非空 | 加密密码 |
| role | VARCHAR(20) | 非空，默认'admin' | 角色：admin/operator |
| status | TINYINT | 非空，默认1 | 状态：1启用/0禁用 |
| created_at | DATETIME | 非空，默认当前时间 | 创建时间 |

**（3）回收员扩展表（collector）**

回收员扩展表存储回收员的资质认证信息，通过 `user_id` 与用户表建立一对一关联。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 主键 |
| user_id | BIGINT | 非空，唯一索引 | 关联用户ID |
| name | VARCHAR(50) | 可空 | 回收员真实姓名 |
| id_card_photo | VARCHAR(255) | 可空 | 身份证照片URL |
| status | TINYINT | 默认2 | 0待完善/1待审核/2已认证/3已禁用 |
| created_at | DATETIME | 默认当前时间 | 创建时间 |
| updated_at | DATETIME | 自动更新 | 更新时间 |

**（4）机构扩展表（institution）**

机构扩展表存储回收机构的详细信息，通过 `user_id` 与用户表建立一对一关联。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 主键 |
| user_id | BIGINT | 非空，唯一索引 | 关联用户ID |
| name | VARCHAR(100) | 可空 | 机构名称 |
| address | VARCHAR(255) | 可空 | 机构地址 |
| contact_person | VARCHAR(50) | 可空 | 联系人 |
| status | TINYINT | 默认1 | 0禁用/1启用 |
| created_at | DATETIME | 默认当前时间 | 创建时间 |
| updated_at | DATETIME | 自动更新 | 更新时间 |

**（5）用户地址表（user_address）**

用户地址表存储用户的上门回收地址信息，支持设置默认地址。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 地址ID |
| user_id | BIGINT | 非空，索引 | 关联用户ID |
| name | VARCHAR(50) | 非空 | 联系人姓名 |
| phone | VARCHAR(20) | 非空 | 联系电话 |
| province | VARCHAR(50) | 非空 | 省份 |
| city | VARCHAR(50) | 非空 | 城市 |
| district | VARCHAR(50) | 非空 | 区县 |
| detail_address | VARCHAR(255) | 非空 | 详细地址 |
| is_default | TINYINT | 非空，默认0 | 是否默认：1是/0否 |
| created_at | DATETIME | 非空，默认当前时间 | 创建时间 |

**（6）回收订单表（recycle_order）**

回收订单表是系统的核心业务表，记录从用户下单到回收完成的全流程信息。该表关联用户、回收员、机构、地址等多个实体。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 订单ID |
| order_no | VARCHAR(32) | 非空，唯一索引 | 订单编号 |
| user_id | BIGINT | 非空，索引 | 下单用户ID |
| collector_id | BIGINT | 可空，索引 | 接单回收员ID |
| institution_id | BIGINT | 可空 | 送达机构ID |
| address_id | BIGINT | 非空 | 关联地址ID |
| address_snapshot | TEXT | 可空 | 地址JSON快照 |
| appointment_date | DATE | 非空 | 预约上门日期 |
| time_slot_start | TIME | 非空 | 时间段开始 |
| time_slot_end | TIME | 非空 | 时间段结束 |
| estimated_weight | DECIMAL(6,2) | 可空 | 预估重量(kg) |
| actual_weight | DECIMAL(6,2) | 可空 | 实际称重(kg) |
| clothes_categories | VARCHAR(500) | 可空 | 衣物类别JSON数组 |
| photos | TEXT | 可空 | 照片URL的JSON数组 |
| remark | VARCHAR(500) | 可空 | 用户备注 |
| status | TINYINT | 非空，默认0，索引 | 订单状态 |
| qr_code | VARCHAR(255) | 可空 | 溯源二维码URL |
| points_awarded | INT | 可空 | 本单获得积分 |
| cancelled_at | DATETIME | 可空 | 取消时间 |
| accepted_at | DATETIME | 可空 | 接单时间 |
| completed_at | DATETIME | 可空 | 完成时间 |
| created_at | DATETIME | 非空，默认当前时间，索引 | 下单时间 |
| updated_at | DATETIME | 非空，自动更新 | 更新时间 |

订单状态（status）取值说明：0-待接单、1-已接单、2-上门中、3-待确认、4-已完成、5-已取消、6-异常、7-机构已接收。

**（7）订单状态日志表（order_status_log）**

订单状态日志表记录订单每次状态变更的详细信息，实现订单全生命周期的可追溯。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 日志ID |
| order_id | BIGINT | 非空，索引 | 关联订单ID |
| from_status | TINYINT | 可空 | 变更前状态 |
| to_status | TINYINT | 非空 | 变更后状态 |
| operator_id | BIGINT | 可空 | 操作人ID |
| operator_type | VARCHAR(20) | 可空 | 操作人类型 |
| operator_location | VARCHAR(255) | 可空 | 操作人位置 |
| remark | VARCHAR(500) | 可空 | 备注 |
| created_at | DATETIME | 非空，默认当前时间 | 操作时间 |

**（8）角色申请表（role_application）**

角色申请表记录用户申请升级为回收员或机构的审批流程。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 申请ID |
| user_id | BIGINT | 非空，索引 | 申请用户ID |
| apply_role | VARCHAR(20) | 非空 | 申请角色：COLLECTOR/INSTITUTION |
| name | VARCHAR(50) | 非空 | 真实姓名/机构名称 |
| id_card_photo | VARCHAR(255) | 可空 | 身份证照片URL |
| health_cert_photo | VARCHAR(255) | 可空 | 健康证照片URL |
| address | VARCHAR(255) | 可空 | 机构地址 |
| contact_person | VARCHAR(50) | 可空 | 联系人 |
| status | TINYINT | 非空，默认0，索引 | 0待审核/1已通过/2已拒绝 |
| reject_reason | VARCHAR(255) | 可空 | 拒绝原因 |
| created_at | DATETIME | 非空，默认当前时间 | 申请时间 |
| updated_at | DATETIME | 非空，自动更新 | 更新时间 |

**（9）积分流水表（points_transaction）**

积分流水表记录用户积分的每一笔变动，实现积分的完整审计追踪。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 主键 |
| user_id | BIGINT | 非空，索引 | 用户ID |
| type | VARCHAR(20) | 非空，索引 | 类型：EARN/EXCHANGE/DEDUCT/ADJUST |
| amount | INT | 非空 | 积分变动量（正为增，负为减） |
| balance_after | INT | 非空 | 变动后余额 |
| related_order_id | BIGINT | 可空 | 关联回收订单ID |
| related_exchange_id | BIGINT | 可空 | 关联兑换订单ID |
| description | VARCHAR(200) | 可空 | 变动描述 |
| created_at | DATETIME | 默认当前时间，索引 | 交易时间 |

**（10）积分规则表（points_rule）**

积分规则表配置不同衣物类别的积分兑换规则，支持管理员动态调整。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 主键 |
| clothes_category | VARCHAR(50) | 非空，唯一索引 | 衣物类别名称 |
| points_per_kg | INT | 非空，默认10 | 每公斤积分数 |
| min_weight | DECIMAL(10,2) | 默认0.50 | 最低重量阈值(kg) |
| status | TINYINT | 默认1 | 0禁用/1启用 |
| created_at | DATETIME | 默认当前时间 | 创建时间 |
| updated_at | DATETIME | 自动更新 | 更新时间 |

**（11）服务评价表（service_review）**

服务评价表记录用户对已完成订单的回收员服务评价，每个订单仅允许一次评价。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 主键 |
| order_id | BIGINT | 非空，唯一索引 | 关联订单ID（一单一评） |
| user_id | BIGINT | 非空，索引 | 评价用户ID |
| collector_id | BIGINT | 非空，索引 | 被评价回收员ID |
| punctuality_score | TINYINT | 非空 | 准时性评分(1-5) |
| attitude_score | TINYINT | 非空 | 服务态度评分(1-5) |
| weighing_score | TINYINT | 非空 | 称重准确性评分(1-5) |
| content | VARCHAR(500) | 可空 | 文字评价 |
| created_at | DATETIME | 默认当前时间 | 评价时间 |

**（12）投诉表（complaint）**

投诉表记录用户对异常订单的投诉信息及管理员的处理结果。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | 主键，自增 | 主键 |
| order_id | BIGINT | 非空，索引 | 关联订单ID |
| user_id | BIGINT | 非空，索引 | 投诉用户ID |
| type | VARCHAR(50) | 非空 | 类型：WEIGHT_DISPUTE/SERVICE_ISSUE/ITEM_LOST/OTHER |
| description | VARCHAR(1000) | 非空 | 投诉描述 |
| status | TINYINT | 默认0，索引 | 0待处理/1已处理 |
| admin_remark | VARCHAR(500) | 可空 | 管理员处理备注 |
| created_at | DATETIME | 默认当前时间 | 投诉时间 |
| handled_at | DATETIME | 可空 | 处理时间 |
| action | VARCHAR(30) | 可空 | 处理动作：ADD_POINTS/DEDUCT_POINTS/MARK_ABNORMAL |
| refund_amount | INT | 可空 | 退还积分数量 |
