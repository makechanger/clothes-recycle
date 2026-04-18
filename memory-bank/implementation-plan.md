# 衣物回收平台 - 分步实施计划（已更新版）

> 4 周 / 28 天 | 每步小而具体 | 每步包含验证方式 | 不包含代码
> 
> **最后更新：2026-04-18**
> 
> **本次更新已融入以下设计决策：**
> 1. 仅使用微信小程序测试号，登录采用**手机号+密码**方式（放弃微信 openid 登录）。
> 2. 三端（用户、回收员、机构）整合为**一个小程序，通过登录角色切换**展示不同 TabBar 和路由。
> 3. 积分发放降级策略：优先在”用户确认称重”时发放；第三周机构端完成后，升级为”机构扫码确认”后发放。
> 4. 所有角色共用一个注册页面，注册后均为普通用户（USER），后续通过**资质申请**升级角色。
> 5. **方案 B 重构（已完成）**：统一用户表 + 扩展表架构，user 表永远保留 + role 字段，collector/institution 为扩展信息表。
> 6. 图片上传增加限制：前端压缩至 ≤1MB，后端限制 5MB。
> 7. 商城商品图片由后台上传，物流仅做”管理员填单号 + 用户看单号”。
> 8. 取消转运任务，回收员完成回收后**直接送达机构**进行扫码。

---

## 使用说明
- 每个步骤标注 **预计耗时**，帮助你判断进度是否正常。
- **验证** 栏描述该步骤完成后如何确认"做对了"。
- 步骤之间有依赖关系时会标注 `依赖: Step X.Y`。
- 优先级标记：🔴 P0 必须完成 | 🟡 P1 尽量完成 | 🟢 P2 有时间再做

---

## 第 1 周：基础框架 + 用户核心链路

> 周目标：用户可以微信登录、管理地址、提交回收预约、查看订单列表

### Day 1-2：环境搭建与项目初始化

#### Step 1.1 🔴 后端项目初始化（2h）— ✅ 已完成
- 使用 Spring Initializr 创建 Spring Boot 3.2 项目。
- 勾选依赖：Spring Web、MySQL Driver、Lombok。
- 手动添加：MyBatis-Plus、Sa-Token、Knife4j、Hutool。
- 配置 `application.yml`：数据源、端口（8080）、Sa-Token 基础配置。
- **验证**：启动项目，浏览器访问 `http://localhost:8080` 返回 404；访问 `http://localhost:8080/doc.html` 能看到 Knife4j 文档。

#### Step 1.2 🔴 数据库创建与核心建表（3h）— ✅ 已完成
- 安装 MySQL 8.0，创建数据库 `clothes_recycle`（字符集 utf8mb4）。
- 本周只建以下表：`user`（用户表）、`user_address`（用户地址表）、`recycle_order`（回收订单表）、`order_status_log`（订单状态流转记录表）、`admin`（管理员表）。
- 为每张表添加主键自增、`created_at` 默认值、常用字段索引。
- 在 `admin` 中插入一条默认管理员。
- **验证**：用数据库工具连接，确认所有表存在、字段类型正确。

#### Step 1.3 🔴 后端基础架构搭建（2h）— ✅ 已完成
- 创建统一响应封装类 `Result<T>`。
- 创建全局异常处理器。
- 配置文件上传路径和静态资源映射（限制单文件最大 5MB）。
- **验证**：写一个测试接口返回 `Result.success("hello")`，Apifox 调用确认 JSON 格式正确。

#### Step 1.4 🔴 小程序项目初始化与角色路由骨架（3h）— ✅ 已完成
- HBuilderX 创建 UniApp 项目（Vue 3 版本），安装 uView Plus 和 Pinia。
- 封装 `uni.request` 统一请求工具。
- **核心改动**：在 `pages.json` 中配置一套大路由，但利用 Pinia 存储当前登录用户的 `role`（USER / COLLECTOR / INSTITUTION）。
- 编写动态 TabBar 组件或在页面入口处根据 `role` 进行条件重定向。
- **验证**：HBuilderX 编译到微信开发者工具，能看到登录入口。

### Day 2-3：用户登录与个人信息

#### Step 1.5 🔴 后端 - 统一登录与注册接口（3h）— ✅ 已完成
- **设计变更**：放弃微信 openid 登录，改为**手机号+密码**统一登录。三种角色共用 `POST /api/auth/login`，后端根据手机号自动识别角色。
- 实现 `POST /api/auth/register`：用户注册接口，所有角色先注册为普通用户（USER），后续通过资质申请升级角色。
- 创建 Entity（User/Collector/Institution）、Mapper、DTO（LoginRequest/LoginResponse/RegisterRequest）、Service（AuthService）、Controller（AuthController）。
- **验证**：三种角色登录均返回正确 token 和 role；注册新用户成功并自动登录。

#### Step 1.6 🔴 小程序 - 登录页对接（3h）— ✅ 已完成
- 登录页对接后端统一登录接口，使用手机号+密码表单。
- 登录成功后存储 token、role、userInfo 到 Pinia + localStorage。
- **验证**：在微信开发者工具中输入手机号密码登录成功，跳转首页。

#### Step 1.5+ 注册 + 修改密码 + 资质申请 + 方案 B 重构 — ✅ 全部完成
- **Step A** ✅：后端用户注册接口
- **Step B** ✅：前端注册页面
- **Step C** ✅：后端修改密码接口（统一到 /api/user/changePassword）
- **Step D** ✅：前端修改密码页面
- **Step E** ✅：后端资质申请接口 + 管理员审批接口
- **Step F** ✅：前端资质申请页面
- **Step G** ✅：更新进度文档
- **方案 B 重构** ✅：统一用户表 + 扩展表架构（详见 progress.md）

### ~~Day 3-6：地址管理 + 回收预约 + 订单列表~~（已合并到第 2 周细化步骤）

---

## 第 2 周：核心业务 — 地址管理 + 订单全流程

> 周目标：用户可管理地址、创建回收订单；回收员可接单、称重；全流程闭环
> 
> **注意**：方案 B 重构后，回收员/机构不再有独立登录接口，统一通过 /api/auth/login 登录（通过 user.role 自动识别角色）。资质申请/审批已完成。

### Day 8-9：地址管理 + 订单基础

#### Step 2.1 ✅ 后端 - 地址 CRUD 接口（3h）— 2026-04-04 完成
- ~~Community 实体 + Mapper，GET /api/common/community/list~~（已移除，见下方说明）
- UserAddress 实体 + Mapper + Service，地址增删改查 + 设默认
- 接口挂在 UserController 下：/api/user/address/**
- 地址归属校验：只能操作自己的地址（StpUtil.getLoginIdAsLong()）
- **验证**：地址 CRUD 全流程 + 越权拦截
- **变更记录**：移除社区表（community）及 User.communityId 字段。用户地址已有省/市/区字段，社区表无实际业务价值。

#### Step 2.2 ✅ 小程序 - 地址管理页面（2h）— 2026-04-04 完成
#### Step 2.2 ✅ 小程序 - 地址管理页面（2h）— 2026-04-04 完成
- 依赖: Step 2.1
- 填充 address/list/list.vue 和 address/edit/edit.vue 骨架
- 省市区选择器使用 `<picker mode=”region”>`
- **验证**：新增→列表→编辑→删除→设默认 全流程

#### Step 2.3 ✅ 后端 - 订单实体 + 状态日志基础设施（1.5h）— 2026-04-04 完成
- RecycleOrder / OrderStatusLog 实体 + Mapper
- OrderStatusLogService（每次状态变更记录日志）
- **验证**：编译通过

#### Step 2.4 ✅ 后端 - 文件上传接口（1h）— 2026-04-04 完成
- CommonController: POST /api/common/upload，保存到 ./uploads/，**限制文件大小不得超过 5MB**
- **验证**：Knife4j 上传图片，确认可通过 /uploads/xxx.jpg 访问；传入大于 5MB 的文件被拦截

#### Step 2.5 ✅ 后端 - 用户订单接口（创建 + 查看 + 取消 + 确认）（3h）— 2026-04-04 完成
- 依赖: Step 2.3, 2.4
- RecycleOrderService + CreateOrderRequest DTO
- POST /api/user/order/create, GET /api/user/order/list, GET /api/user/order/{id}
- POST /api/user/order/{id}/cancel, POST /api/user/order/{id}/confirm
- 订单号生成：RC + 时间戳 + 4位随机数
- 地址快照序列化为 JSON
- 取消规则：仅 status 0/1 可取消
- 确认完成：status 3→4，发放积分（actualWeight × 10）
- **验证**：创建订单→查看列表→查看详情→取消/确认

#### Step 2.6 ✅ 后端 - 回收员订单接口（2h）— 2026-04-04 完成
- 依赖: Step 2.3
- CollectorController：pending / list / accept / pickup / complete
- CollectorOrderService：角色校验、接单防并发、状态流转
- CompleteWeighingRequest DTO
- 数据库补充 accepted_at 列
- **验证**：回收员接单→上门→称重→用户确认→全流程

### Day 9-12：前端开发

#### Step 2.7 ✅ 小程序 - 预约回收页面（创建订单）（4h）— 2026-04-04 完成
- 依赖: Step 2.2, 2.5
- 填充 order/create/create.vue
- 地址选择（uni.$once 事件通道） + 日期 + 时间段多列选择器 + 衣物分类多选标签 + 预估重量 + 照片上传（canvas 压缩） + 备注
- address/list/list.vue 增加选择模式（mode=select）
- **验证**：完整下单流程 ✅

#### Step 2.8 ✅ 小程序 - 订单列表 + 详情页（3h）— 2026-04-04 完成
- 依赖: Step 2.5
- 填充 order/list/list.vue：状态 Tab 筛选（全部/待接单/进行中/待确认/已完成/已取消）、订单卡片、操作按钮
- 填充 order/detail/detail.vue：状态横幅、地址解析、预约信息、照片预览、订单时间线、操作按钮
- "进行中" Tab 合并状态 1+2（Promise.all 并行请求）
- **验证**：列表查看 + 详情查看 + 取消/确认操作 ✅

#### Step 2.9 ✅ 小程序 - 回收员前端（3h）— 2026-04-10 完成
- 依赖: Step 2.6
- 后端补充：CollectorController 新增 GET /api/collector/order/{id} 详情接口，CollectorOrderService 新增 getOrderDetail() 方法（待接单订单所有回收员可查看，其他状态仅接单本人可查看）
- index.vue 回收员区块：展示待接单列表 + 接单按钮（调用 GET /api/collector/order/pending）
- order/list/list.vue 根据角色切换 API：回收员调用 GET /api/collector/order/list，Tab 改为（全部/已接单/上门中/待确认/已完成），回收员操作按钮（开始上门/完成称重），TabBar current 根据角色动态切换
- order/detail/detail.vue 回收员视角：根据角色调用不同 API，回收员专属状态描述文字，操作按钮（接单/开始上门/完成称重，称重需输入实际重量）
- **验证**：端到端全流程（用户下单→回收员接单→上门→称重→用户确认）✅

#### Step 2.10 ✅ 更新进度文档 — 2026-04-10 完成

---

## 第 3 周：积分规则 + 机构端 + 管理后台

> 周目标：积分发放策略升级（机构扫码后发放）、管理后台实现全方位管控

### Day 15：数据库补全

#### Step 3.1 ✅ 数据库 - 补建积分与机构相关表（1.5h）— 2026-04-13 完成
- 建表：`points_transaction`（流水）、`points_rule`（规则）、`institution`（机构表）。

### Day 15-16：机构端开发（升级积分策略）

#### Step 3.2 ✅ 后端 - 机构登录与扫码接收接口（3h）— 2026-04-13 完成
- 实现 `POST /api/institution/login`：机构账号登录（Sa-Token 独立 token）。
- 实现 `POST /api/institution/receive`：
  - 参数：`qr_code`。
  - **核心改动（积分策略升级）**：校验溯源码 -> 订单状态变更为“机构已接收” -> **此时再触发积分计算并给用户发放积分**。同时将 Step 2.6 处的积分发放逻辑注释或移除。
- **验证**：调用此接口，订单状态更新，用户积分增加。

#### Step 3.3 ✅ 小程序 - 机构端页面（3h）— 2026-04-17 完成
- 小程序登录页增加“机构登录”入口。
- 提供扫码接收页面，调用 `uni.scanCode` 获取二维码中的溯源码，传给后端。
- **验证**：机构登录 -> 扫描回收员出示的二维码 -> 提示接收成功。

### Day 16-17：管理后台搭建与审核

#### Step 3.4 ✅ 管理后台项目初始化与登录（3h）— 2026-04-17 完成
- 从零搭建 Vue 3 + Element Plus + Vite 5 管理后台项目（clothes-recycle-admin/）。
- 对接后端 `POST /api/admin/login`。
- **验证**：后台能成功登录，进入骨架屏页面。

#### Step 3.5 ✅ 管理后台 - 回收员账号创建与资质审核（3h）— 2026-04-17 完成
- **核心改动**：实现回收员管理页面。管理员可以在此**新增回收员账号**，也可以看到状态为”待审核”的回收员，查看其资质照片，点击”通过”或”驳回”。
- **验证**：在后台新建回收员 -> 用该账号登录小程序上传资质 -> 后台刷新能看到 -> 点击通过 -> 小程序端回收员状态变为”已认证”。

### Day 17-20：管理后台核心管控

#### Step 3.6 ✅ 管理后台 - 订单管理与积分规则（4h）— 2026-04-17 完成
- 列表展示所有订单，多条件筛选。
- 提供积分规则配置表单（如：外套多少积分/kg）。
- **验证**：修改积分规则，新完成的订单按新规则计算积分。

### Day 21：第 3 周收尾

#### Step 3.7 ✅ 管理后台 - 用户管理（3h）— 2026-04-18 完成
- **实际内容**：实现用户管理页面（用户列表分页筛选 + 启用/禁用 + 重置密码 + 修改角色），替代原计划的全链路集成自测。
- **验证**：用户列表分页筛选正常，启用/禁用/重置密码/修改角色操作均正常。

---

## 第 4 周：补全功能 + 优化 + 答辩准备

> 周目标：补充 P1 功能（评价、申诉、数据统计）、修 Bug、准备演示

### ~~Day 22-23：积分商城~~（已取消）

#### ~~Step 4.1 积分商城接口与页面~~ ❌ 已取消
- 原因：工作量大但对核心回收链路贡献不高，用数据统计看板替代。

### Day 22-23：服务评价与异常申诉

#### Step 4.2 ✅ 评价与申诉（4h）— 2026-04-18 完成
- 已完成内容：
  - 后端：ServiceReview 实体 + ReviewService + ReviewRequest DTO
  - 后端：Complaint 实体 + ComplaintService + ComplaintRequest DTO
  - 后端：UserController 评价提交/查询 + 申诉提交/列表接口
  - 后端：AdminController 评价列表 + 申诉列表/处理接口
  - 小程序：订单详情页评价弹窗（三维度星级评分 + 文字）+ 申诉弹窗（类型选择 + 描述）
  - 管理后台：ReviewView.vue 评价管理页面（星级展示 + 分页）
  - 管理后台：ComplaintView.vue 申诉管理页面（状态筛选 + 处理弹窗）
  - 申诉处理增强：3 种处理动作（增加积分 / 减少积分 / 标记订单异常）
  - SQL 迁移脚本：step4_2_review_complaint.sql + step4_2_enhance_complaint.sql

### Day 24-25：管理后台数据统计看板（替代积分商城）

#### Step 4.3 🔴 后端 - 数据统计聚合接口（2h）
- 新建 `AdminStatisticsService`，提供 `GET /api/admin/statistics` 接口
- **概览卡片数据**：
  - 用户总数、回收员总数、机构总数
  - 订单总数、已完成订单数
  - 积分总发放量
  - 回收总重量（kg）
- **图表数据**：
  - 近 7 天订单趋势（按日统计订单量，折线图，固定 7 天）
  - 订单状态分布（各状态数量，饼图）
  - 衣物品类分布（各品类回收次数，柱状图）
  - 回收员接单排行 Top5（横向柱状图）
- **验证**：Knife4j 调用接口，返回完整 JSON 数据

#### Step 4.4 🔴 前端 - 数据统计看板页面（2h）
- 依赖: Step 4.3
- 安装 ECharts：`npm install echarts`
- 改造现有 `DashboardView.vue`（当前为空白仪表盘页面）
- 顶部：4 个数据概览卡片（用户/订单/积分/重量）
- 下方：2×2 图表网格（订单趋势折线图、状态分布饼图、品类分布柱状图、回收员排行柱状图）
- **验证**：登录管理后台，首页展示完整数据看板，图表可交互

### Day 25-26：小程序功能补全

#### Step 4.5 🟡 小程序 - 我的申诉列表页（1h）
- 后端接口已有：`GET /api/user/complaint/list`
- 新建页面 `pages/complaint/list/list.vue`，展示申诉列表（类型、描述、状态、处理结果）
- 个人中心新增"我的申诉"入口
- **验证**：提交申诉后，在我的申诉列表中可查看，管理员处理后状态更新

#### Step 4.6 🟡 小程序 - 我的评价记录页（1h）
- 后端新增接口：`GET /api/user/review/list`（查询当前用户所有评价）
- 新建页面 `pages/review/list/list.vue`，展示评价列表（订单号、三维度星级、评价内容、时间）
- 个人中心新增"我的评价"入口
- **验证**：评价订单后，在我的评价列表中可查看

#### Step 4.7 🟡 小程序 - 积分流水明细页（1.5h）
- 后端接口已有：`GET /api/user/points/history`（PointsService.getPointsHistory）
- 新建页面 `pages/points/history/history.vue`，展示积分收支明细（类型、金额、余额、时间、关联订单）
- 个人中心新增"积分明细"菜单入口（独立菜单项）
- **验证**：回收完成获得积分后，积分明细页显示对应流水记录

#### Step 4.8 🟡 小程序 - 订单详情状态时间线（1h）
- 后端接口已有：订单详情接口返回 `statusLogs`（order_status_log 表数据）
- 在订单详情页 `order/detail/detail.vue` 底部新增时间线组件
- 展示全链路流转：下单 → 接单 → 上门 → 称重确认 → 机构接收，每步显示操作人、操作时间
- **验证**：查看已完成订单详情，底部展示完整状态流转时间线

#### Step 4.9 🟡 小程序 - 接单后展示回收员实名信息（0.5h）
- 订单详情页 `order/detail/detail.vue`：当订单 status ≥ 1（已接单）时，展示回收员姓名、手机号
- 后端订单详情接口补充回收员信息字段（如未返回）
- **验证**：用户查看已接单订单，能看到回收员姓名和联系方式

### Day 26-27：机构端 + 管理后台增强

#### Step 4.10 🟡 机构端 - 接收数据统计页（1.5h）
- 后端新增接口：`GET /api/institution/statistics`
  - 接收订单总数、接收总重量
  - 近 7 天接收趋势（按日统计）
- 小程序机构首页新增统计区块，展示核心数据
- **验证**：机构登录后，首页展示接收总量和趋势数据

#### Step 4.11 🟡 管理后台 - 回收员管理页展示平均评分（0.5h）
- 依赖: Step 4.2 已完成的评价数据
- 回收员列表页 `CollectorView.vue` 新增"平均评分"列
- 后端 `AdminCollectorService.listCollectors()` 补充评分聚合查询
- **验证**：回收员列表展示每个回收员的平均服务评分

#### Step 4.12 🟡 管理后台 - 订单列表 + 用户列表导出 Excel（1.5h）
- 安装前端 xlsx 库：`npm install xlsx file-saver`
- 订单管理页 `OrderView.vue` 新增"导出 Excel"按钮，导出当前筛选条件下的订单数据
- 用户管理页 `UserView.vue` 新增"导出 Excel"按钮，导出当前筛选条件下的用户数据
- 纯前端导出（基于当前已加载的分页数据），无需后端改动
- **验证**：点击导出按钮，下载 .xlsx 文件，内容与页面表格一致

### Day 28：最终收尾

#### Step 4.13 🔴 体验优化与演示准备（6h）
- 清理脏数据，手动录入几套覆盖全链路状态的演示数据。
- 录制或演练 8-10 分钟的答辩演示脚本。
- **验证**：连续跑 2 遍演示脚本，不出现任何报错。