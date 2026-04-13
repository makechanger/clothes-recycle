# 项目进度记录

## 当前状态：第 2 周核心业务开发 ✅ 全部完成，第 3 周待开发

---

### Step 1.1 🔴 后端项目初始化（2h）— ✅ 2026-04-02 完成

**完成内容：**
- Spring Boot 3.2.5 项目创建，Maven 构建，JDK 17
- 依赖配置：MyBatis-Plus 3.5.6、Sa-Token 1.38.0、Knife4j 4.4.0、Hutool 5.8.27、ZXing 3.5.3
- `application.yml` 配置完成（数据源、Sa-Token、Knife4j、文件上传 5MB 限制）
- 四个配置类：SaTokenConfig（多账号角色隔离鉴权）、CorsConfig（跨域）、WebConfig（静态资源映射）、Knife4jConfig（API 文档）
- 统一响应封装 `Result<T>`
- 测试接口 `/api/test` 验证通过
- 数据库 `clothes_recycle` 已创建（utf8mb4）

**验证结果：**
- `http://localhost:8080` → 404 ✅
- `http://localhost:8080/api/test` → `{"code":200,"message":"success","data":"hello"}` ✅
- `http://localhost:8080/doc.html` → Knife4j 文档页面 ✅

**用户手动修改：**
- SaTokenConfig：~~改为 `StpLogic` 实现多账号体系（user/collector/institution/admin 四套独立鉴权）~~ → **已被方案 B 重构简化为 2 套：StpUtil + StpLogic("admin")**
- RecycleApplication：添加 `@MapperScan("com.recycle.mapper")`
- application.yml：移除全局 `logic-delete-field`
- WebConfig：添加路径末尾斜杠兼容处理

---

### Step 1.2 🔴 数据库创建与核心建表（3h）— ✅ 2026-04-02 完成

**完成内容：**
- 创建 6 张核心表：`community`、`admin`、`user`、`user_address`、`recycle_order`、`order_status_log`
- 额外创建 `collector`（回收员表）和 `institution`（机构表）
- 所有表均含主键自增、`created_at` 默认 CURRENT_TIMESTAMP
- `user` 表：phone 索引、community_id 索引
- `recycle_order` 表：order_no 唯一索引、user_id / collector_id / status / created_at 索引
- `order_status_log` 表：order_id 索引
- `admin` 表：username 唯一索引
- 插入 5 条测试社区数据、1 条默认管理员（admin/admin123）
- 插入测试数据：用户（13800000001）、回收员（13800000002）、机构（13800000003），密码均为 123456

**验证结果：**
- 所有表全部存在 ✅
- 字段类型、索引、默认值均正确 ✅
- 测试数据插入成功 ✅

**SQL 脚本位置：** `src/main/resources/sql/init_tables.sql`

**注意事项：**
- MySQL 客户端连接需指定 `--default-character-set=utf8mb4`，否则中文数据会因 gbk 编码截断
- `user` 是 MySQL 保留字，Entity 中需用 `@TableName("\`user\`")` 转义

---

### Step 1.3 🔴 后端基础架构搭建（2h）— ✅ 2026-04-02 完成

**完成内容：**
- 全局异常处理器 `GlobalExceptionHandler`：捕获 6 类异常（未登录401、无权限403、文件超限400、业务异常、参数异常、兜底500）
- 自定义业务异常类 `BusinessException`：支持自定义错误码和消息
- （`Result<T>` 封装、文件上传限制、静态资源映射已在 Step 1.1 完成）

**验证结果：**
- 访问需鉴权接口返回 401 JSON（非错误页面）✅
- 编译通过 ✅

---

### Step 1.4 🔴 小程序项目初始化与角色路由骨架（3h）— ✅ 2026-04-02 完成

**完成内容：**
- UniApp + Vue 3 项目创建，安装 uView Plus 和 Pinia
- 封装 `uni.request` 统一请求工具（`utils/request.js`）
- Pinia 用户状态管理（`store/user.js`），存储 token、role、userInfo
- 自定义 TabBar 组件（`components/custom-tabbar/custom-tabbar.vue`），根据角色动态显示不同导航
- 页面骨架：首页、登录页、预约回收页、订单列表页、订单详情页、个人中心页、地址列表/编辑页
- `pages.json` 路由配置完成

**验证结果：**
- 编译到微信小程序成功 ✅
- 各页面骨架可正常访问 ✅

---

### Step 1.5 🔴 后端 - 统一登录与注册接口（3h）— ✅ 2026-04-03 完成

**重大设计变更：** 放弃微信 openid 登录方案，改为**手机号+密码**统一登录。原因：使用微信测试号限制多，手机号+密码方案更简单直接。

**完成内容：**
- 统一登录接口 `POST /api/auth/login`：三种角色（用户/回收员/机构）共用一个登录入口，后端根据手机号自动识别角色（依次查 user → collector → institution 表）
- 用户注册接口 `POST /api/auth/register`：所有角色先注册为普通用户（USER），后续通过资质申请升级角色
- Sa-Token 多账号体系：~~用户用默认 StpUtil，回收员用 StpLogic("collector")，机构用 StpLogic("institution")~~ → **已被方案 B 重构统一为 StpUtil**
- Entity 类：User.java（`@TableName("\`user\`")`）、Collector.java、Institution.java
- Mapper 类：UserMapper、CollectorMapper、InstitutionMapper
- DTO 类：LoginRequest、LoginResponse、RegisterRequest
- Service 类：AuthService（login + register 方法）
- Controller 类：AuthController（`/api/auth/login` + `/api/auth/register`）
- SaTokenConfig 放行：`/api/auth/login`、`/api/auth/register`

**新增文件：**
- `dto/RegisterRequest.java` — 注册请求 DTO（phone, password, name）
- `dto/LoginRequest.java` — 登录请求 DTO（phone, password）
- `dto/LoginResponse.java` — 登录响应 DTO（token, role, userInfo）
- `controller/AuthController.java` — 统一登录/注册控制器
- `service/AuthService.java` — 统一登录/注册服务
- `entity/User.java`、`entity/Collector.java`、`entity/Institution.java`
- `mapper/UserMapper.java`、`mapper/CollectorMapper.java`、`mapper/InstitutionMapper.java`

**验证结果：**
- 用户登录（13800000001/123456）→ 返回 token + role=USER ✅
- 回收员登录（13800000002/123456）→ 返回 token + role=COLLECTOR ✅
- 机构登录（13800000003/123456）→ 返回 token + role=INSTITUTION ✅
- 错误密码 → 400 "密码错误" ✅
- 未注册手机号 → 400 "手机号未注册" ✅
- 用户注册 → 编译通过 ✅

**Bug 修复：**
- JDBC URL `characterEncoding=utf8mb4` → 改为 `characterEncoding=UTF-8`（Java 不支持 utf8mb4 编码名）
- `@TableName("user")` → `@TableName("\`user\`")`（user 是 MySQL 保留字）

---

### Step 1.5+ 🔴 小程序 - 登录页对接（1h）— ✅ 2026-04-03 完成

**完成内容：**
- 登录页（`pages/login/login.vue`）对接后端统一登录接口
- 登录成功后存储 token、role、userInfo 到 Pinia + localStorage
- 根据角色跳转到对应首页

---

### 进行中：注册 + 修改密码 + 资质申请功能

**计划步骤（详见 plan 文件）：**
- Step A ✅：后端用户注册接口（已完成）
- Step B ✅：前端注册页面（已完成）
- Step C ✅：后端修改密码接口（已完成，后被方案 B 重构统一）
- Step D ✅：前端修改密码页面（已完成，后被方案 B 重构统一）
- Step E ✅：后端资质申请 + 管理员审批接口（已完成，后被方案 B 重构优化）
- Step F ✅：前端资质申请页面（已完成）
- Step G ✅：更新进度文档（当前）

---

### Step B 🔴 前端注册页面 — ✅ 2026-04-03 完成

**完成内容：**
- 新增注册页面 `pages/register/register.vue`：手机号+密码+确认密码表单（姓名改为下单时再要求实名）
- `pages.json` 添加注册页路由（自定义导航栏）
- `pages/login/login.vue` 底部添加"没有账号？立即注册"链接
- 注册成功后自动登录并跳转首页
- 前端校验：手机号格式、密码至少6位、两次密码一致

**额外修复：**
- `GlobalExceptionHandler` 添加 `NoResourceFoundException` 处理（浏览器请求 favicon.ico 不再返回 500）
- `request.js` 的 `BASE_URL` 改为局域网 IP（真机调试需要）
- 登录页和注册页 input 添加 focus/blur 控制（修复光标不消失问题，用户自行移除了该方案）

**设计变更：**
- 注册时不再要求填写姓名，实名信息改为用户操作订单时再要求填写

**验证结果：**
- 登录页→注册页跳转正常 ✅
- 注册新用户→自动登录→跳转首页 ✅
- 真机调试网络连接正常 ✅

---

### Step C 🔴 后端修改密码接口 — ✅ 2026-04-03 完成

**完成内容：**
- 新增 `ChangePasswordRequest` DTO（oldPassword, newPassword）
- `AuthService` 添加修改密码方法（校验旧密码、更新新密码）
- `UserController` 添加 `POST /api/user/changePassword` 统一修改密码入口
- `CollectorController` 和 `InstitutionController` 也曾添加各自的修改密码接口（后被方案 B 重构移除）

---

### Step D 🔴 前端修改密码页面 — ✅ 2026-04-03 完成

**完成内容：**
- 新增修改密码页面 `pages/user/change-password/change-password.vue`
- 表单：旧密码、新密码、确认新密码
- 前端校验：旧密码必填、新密码至少6位、两次密码一致、新旧密码不能相同
- 修改成功后自动退出登录，要求重新登录
- 所有角色统一调用 `/api/user/changePassword`

---

### Step E 🔴 后端资质申请 + 管理员审批接口 — ✅ 2026-04-03 完成

**完成内容：**
- 新增 `RoleApplication` 实体类（role_application 表）
- 新增 `RoleApplicationMapper`
- 新增 `RoleApplicationRequest` DTO（applyRole, name, idCardPhoto, address, contactPerson）
- 新增 `RoleApplicationService`：
  - `apply()`：提交资质申请（校验角色合法性、必填字段、防重复提交）
  - `getMyApplications()`：查询用户的申请记录
  - `getPendingApplications()`：查询所有待审核申请（管理员用）
  - `approve()`：管理员审批通过（升级角色 + 插入扩展记录）
  - `reject()`：管理员审批拒绝
- `UserController` 添加 `POST /api/user/applyRole` 和 `GET /api/user/myApplications`
- `AdminController` 添加审批接口

**新增数据库表：**
- `role_application` 表（SQL 在 init_tables.sql 中）

---

### Step F 🔴 前端资质申请页面 — ✅ 2026-04-03 完成

**完成内容：**
- 新增资质申请页面，支持选择申请回收员或机构
- 回收员申请：填写姓名、上传身份证照片
- 机构申请：填写机构名称、地址、联系人
- 申请提交后可查看申请记录和审核状态

---

### 🔧 方案 B 重大重构：统一用户表 + 扩展表架构 — ✅ 2026-04-03 完成

**重构原因：**
原架构中角色升级（审批通过）时会**删除 user 表记录**，将数据迁移到 collector/institution 表。这存在严重风险：
- `recycle_order.user_id` 外键引用会断裂
- `user_address.user_id` 外键引用会断裂
- 角色无法回退（降级）
- 三张表各自存储 phone/passwordHash，登录需要级联查三张表

**新架构（方案 B）：**
- **user 表永远保留**，添加 `role` 字段（USER/COLLECTOR/INSTITUTION）标识角色
- **collector/institution 改为扩展信息表**，通过 `user_id` 关联，去掉 phone/passwordHash
- **登录只查 user 表**，通过 user.role 判断角色
- **角色升级 = UPDATE user.role + INSERT 扩展记录**（不删除任何数据）
- **Sa-Token 简化为 2 套**：StpUtil（所有用户）+ StpLogic("admin")（管理员）

**数据库变更：**
- `user` 表添加 `role` 字段（VARCHAR(20)，默认 'USER'）
- `collector` 表重建为扩展表（去掉 phone/passwordHash，添加 user_id）
- `institution` 表重建为扩展表（去掉 phone/passwordHash，添加 user_id）
- SQL 脚本：`src/main/resources/sql/step2_refactor_role.sql`

**后端修改文件：**
| 文件 | 修改内容 |
|---|---|
| `entity/User.java` | 添加 `role` 字段 |
| `entity/Collector.java` | 改为扩展表结构（userId，去掉 phone/passwordHash/healthCertPhoto） |
| `entity/Institution.java` | 改为扩展表结构（userId，去掉 phone/passwordHash） |
| `service/AuthService.java` | login 只查 user 表，统一 StpUtil，合并 changePassword，新增 buildUserInfoByRole |
| `config/SaTokenConfig.java` | 去掉多套 StpLogic，所有用户路由统一用 StpUtil.checkLogin() |
| `service/RoleApplicationService.java` | approve() 改为 UPDATE role + INSERT 扩展记录（不删除 user） |
| `controller/UserController.java` | 统一修改密码入口 |
| `controller/CollectorController.java` | 简化为空壳（删除 changePassword） |
| `controller/InstitutionController.java` | 简化为空壳（删除 changePassword） |

**前端修改文件：**
| 文件 | 修改内容 |
|---|---|
| `change-password.vue` | 所有角色统一调用 `/api/user/changePassword` |

**验证结果：**
- 用户登录（13800000001/123456）→ role=USER ✅
- 回收员登录（13800000002/123456）→ role=COLLECTOR，返回扩展信息 ✅
- 机构登录（13800000003/123456）→ role=INSTITUTION，返回扩展信息 ✅
- 修改密码（所有角色统一接口）→ 成功 ✅
- user 表记录在角色升级后保留 → 外键引用安全 ✅
- 编译通过 ✅

### 下一步：Step 2.3 订单实体 + 状态日志基础设施

---

### Step 2.1 🔴 后端 - 社区列表 + 地址 CRUD 接口 — ✅ 2026-04-04 完成

**完成内容：**
- Community 实体 + CommunityMapper
- UserAddress 实体 + UserAddressMapper
- UserAddressRequest DTO（新增/编辑地址请求参数）
- UserAddressService（列表、详情、新增、编辑、删除、设默认，含地址归属校验）
- CommonController：`GET /api/common/community/list`（无需登录，已在 SaTokenConfig 放行）
- UserController 新增 6 个地址接口：
  - `GET /api/user/address/list` — 我的地址列表
  - `GET /api/user/address/{id}` — 地址详情
  - `POST /api/user/address/create` — 新增地址
  - `POST /api/user/address/{id}/update` — 编辑地址
  - `POST /api/user/address/{id}/delete` — 删除地址
  - `POST /api/user/address/{id}/setDefault` — 设置默认地址

**新增文件：**
- `entity/Community.java`
- `entity/UserAddress.java`
- `mapper/CommunityMapper.java`
- `mapper/UserAddressMapper.java`
- `dto/UserAddressRequest.java`
- `service/UserAddressService.java`
- `controller/CommonController.java`

**验证结果：**
- 编译通过 ✅
- 用户测试通过 ✅

---

### Step 2.2 🔴 小程序 - 地址管理页面 — ✅ 2026-04-04 完成

**完成内容：**
- 地址列表页 `address/list/list.vue`：展示所有地址，默认地址排前，支持新增/编辑/删除/设默认操作
- 地址编辑页 `address/edit/edit.vue`：新增/编辑模式自动切换（通过 URL 参数 `?id=xxx`），省市区 `<picker mode="region">` 选择器，表单校验（姓名、手机号格式、地区、详细地址）
- `onShow` 每次返回自动刷新列表
- 删除操作有二次确认弹窗
- 保存成功后延迟 800ms 自动返回列表页

**修改文件：**
- `pages/address/list/list.vue`（从骨架填充为完整页面）
- `pages/address/edit/edit.vue`（从骨架填充为完整页面）

**验证结果：**
- 用户测试通过 ✅

---

### Step 2.3 🔴 后端 - 订单实体 + 状态日志基础设施 — ✅ 2026-04-04 完成

**完成内容：**
- RecycleOrder 实体（含 orderNo、userId、collectorId、addressSnapshot、appointmentDate、timeSlotStart/End、clothesCategories、estimatedWeight、actualWeight、pointsAwarded、photos、remark、status、各时间戳字段）
- OrderStatusLog 实体（orderId、fromStatus、toStatus、operatorId、operatorRole、remark）
- RecycleOrderMapper、OrderStatusLogMapper
- OrderStatusLogService：记录每次状态变更日志

**验证结果：**
- 编译通过 ✅

---

### Step 2.4 🔴 后端 - 文件上传接口 — ✅ 2026-04-04 完成

**完成内容：**
- CommonController 添加 `POST /api/common/upload`
- 保存到 `./uploads/` 目录，文件名使用 UUID 防重复
- 后端限制单文件最大 5MB（application.yml 配置）
- 返回相对路径 `/uploads/xxx.jpg`

**验证结果：**
- 编译通过 ✅

---

### Step 2.5 🔴 后端 - 用户订单接口 — ✅ 2026-04-04 完成

**完成内容：**
- CreateOrderRequest DTO（addressId、appointmentDate、timeSlotStart/End、estimatedWeight、clothesCategories、photos、remark）
- RecycleOrderService：
  - `createOrder()`：生成订单号（RC + yyyyMMddHHmmss + 4位随机数）、地址快照序列化为 JSON、衣物分类/照片序列化为 JSON
  - `listByUser()`：按状态筛选用户订单
  - `getDetail()`：获取订单详情（含归属校验）
  - `cancelOrder()`：仅 status 0/1 可取消
  - `confirmOrder()`：status 3→4，发放积分（actualWeight × 10，取整）
- UserController 新增 5 个订单接口：
  - `POST /api/user/order/create` — 创建订单
  - `GET /api/user/order/list` — 订单列表（支持 ?status= 筛选）
  - `GET /api/user/order/{id}` — 订单详情
  - `POST /api/user/order/{id}/cancel` — 取消订单
  - `POST /api/user/order/{id}/confirm` — 确认完成

**新增文件：**
- `dto/CreateOrderRequest.java`
- `service/RecycleOrderService.java`

**验证结果：**
- 编译通过 ✅

---

### Step 2.6 🔴 后端 - 回收员订单接口 — ✅ 2026-04-04 完成

**完成内容：**
- CompleteWeighingRequest DTO（actualWeight）
- CollectorOrderService：
  - `checkCollectorRole(userId)`：校验 user.role == "COLLECTOR"
  - `pendingList(userId)`：查询所有 status=0 的待接单订单
  - `myOrders(userId, status)`：查询回收员已接的订单（collectorId=userId）
  - `acceptOrder(orderId, userId)`：接单，乐观更新 WHERE status=0 防并发
  - `startPickup(orderId, userId)`：开始上门，status 1→2
  - `completeWeighing(orderId, userId, actualWeight)`：完成称重，status 2→3
  - `getMyOrder(orderId, userId)`：订单归属校验
- CollectorController 5 个接口：
  - `GET /api/collector/order/pending` — 待接单列表
  - `GET /api/collector/order/list` — 我的订单列表
  - `POST /api/collector/order/{id}/accept` — 接单
  - `POST /api/collector/order/{id}/pickup` — 开始上门
  - `POST /api/collector/order/{id}/complete` — 完成称重

**新增文件：**
- `dto/CompleteWeighingRequest.java`
- `service/CollectorOrderService.java`

**数据库变更：**
- `ALTER TABLE recycle_order ADD COLUMN accepted_at DATETIME DEFAULT NULL`（接单时间字段）

**验证结果：**
- 编译通过 ✅
- 回收员接单→上门→称重 全流程 ✅

---

### Step 2.7 🔴 小程序 - 预约回收页面 — ✅ 2026-04-04 完成

**完成内容：**
- 完整重写 `order/create/create.vue`：
  - 地址选择：通过 `uni.$once('selectAddress')` 事件通道从地址列表页接收选中地址
  - 日期选择：`<picker mode="date">`，范围今天到30天后
  - 时间段选择：`<picker mode="multiSelector">`，开始时间 08:00-18:00，结束时间 09:00-20:00，联动校验
  - 衣物分类：10 个标签多选（外套、裤子、衬衫、T恤、裙子、羽绒服、毛衣、鞋子、包包、其他）
  - 预估重量：digit 输入框
  - 照片上传：最多 6 张，canvas 压缩（maxWidth 1200px，quality 0.7，确保 ≤1MB）
  - 备注：textarea，最多 200 字
  - 表单校验 + 提交到 `POST /api/user/order/create`
  - 成功后 `uni.reLaunch` 跳转订单列表
- 修改 `address/list/list.vue`：
  - 添加选择模式（URL 参数 `?mode=select`）
  - 选择模式下点击地址 → `uni.$emit('selectAddress', address)` + `uni.navigateBack()`

**修改文件：**
- `pages/order/create/create.vue`（完整重写）
- `pages/address/list/list.vue`（添加选择模式）

**验证结果：**
- 完整下单流程测试通过 ✅

---

### Step 2.8 🔴 小程序 - 订单列表 + 详情页 — ✅ 2026-04-04 完成

**完成内容：**
- 完整重写 `order/list/list.vue`：
  - 状态 Tab 筛选栏：全部(null)、待接单(0)、进行中('ongoing')、待确认(3)、已完成(4)、已取消(5)
  - "进行中" Tab 合并状态 1+2（Promise.all 并行请求后合并排序）
  - 订单卡片：订单号、彩色状态标签、地址（从 JSON 快照解析）、预约时间、衣物分类、重量
  - 操作按钮：取消订单（status 0/1）、确认完成（status 3，显示积分预览）
  - 点击卡片跳转详情页
  - TabBar current=2（用户角色"我的订单"）
- 完整重写 `order/detail/detail.vue`：
  - 状态横幅：不同状态不同渐变背景色 + 状态描述文字
  - 上门地址：从 addressSnapshot JSON 解析
  - 预约信息：日期、时段、衣物分类、预估重量、实际重量、获得积分、备注
  - 衣物照片：展示 + 点击预览（uni.previewImage）
  - 订单信息：订单编号、下单时间、接单时间、完成时间、取消时间
  - 底部操作按钮：取消订单（status 0/1）、确认完成（status 3）

**修改文件：**
- `pages/order/list/list.vue`（完整重写）
- `pages/order/detail/detail.vue`（完整重写）

**验证结果：**
- 用户端订单列表 + 详情 + 取消/确认操作 全流程测试通过 ✅
- 回收员端暂未适配（order/list 和 detail 仅调用用户 API）→ 待 Step 2.9 实现

### 下一步：Step 2.9 小程序 - 回收员前端

---

### Step 2.9 🔴 小程序 - 回收员前端 — ✅ 2026-04-10 完成

**完成内容：**

**后端补充（CollectorController 缺少详情接口）：**
- CollectorController 新增 `GET /api/collector/order/{id}` 订单详情接口
- CollectorOrderService 新增 `getOrderDetail()` 方法：待接单(status=0)订单所有回收员可查看，其他状态仅接单本人可查看

**前端修改（3 个文件）：**

1. **index.vue（回收员首页）：**
   - 回收员区块展示待接单列表（调用 `GET /api/collector/order/pending`）
   - 待接单卡片：订单号、状态标签、上门地址、预约时间、衣物分类、预估重量
   - 接单按钮（二次确认弹窗）→ `POST /api/collector/order/{id}/accept`
   - 点击卡片跳转订单详情页
   - `onShow` 每次显示时刷新待接单列表

2. **order/list/list.vue（订单列表页，用户+回收员共用）：**
   - 引入 `useUserStore`，通过 `isUser`/`isCollector` 计算属性判断角色
   - 用户 Tab：全部/待接单/进行中/待确认/已完成/已取消
   - 回收员 Tab：全部/已接单/上门中/待确认/已完成
   - `loadOrders()` 根据角色分发到 `loadUserOrders()` 或 `loadCollectorOrders()`
   - 回收员操作按钮：开始上门（status=1，蓝色）、完成称重（status=2，绿色，弹出输入框填写重量）
   - TabBar current 根据角色动态切换（回收员=1，用户=2）

3. **order/detail/detail.vue（订单详情页，用户+回收员共用）：**
   - 根据角色调用不同 API：回收员 `/api/collector/order/{id}`，用户 `/api/user/order/{id}`
   - 回收员专属状态描述文字（如"您已接单，请尽快出发上门"）
   - 回收员操作按钮：接单（status=0）、开始上门（status=1，蓝色）、完成称重（status=2，绿色，弹出输入框）
   - 用户操作保持不变：取消订单（status 0/1）、确认完成（status 3）

**新增/修改文件：**
| 文件 | 操作 |
|---|---|
| `CollectorController.java` | 新增 `GET /api/collector/order/{id}` |
| `CollectorOrderService.java` | 新增 `getOrderDetail()` 方法 |
| `index.vue` | 回收员首页待接单列表 |
| `order/list/list.vue` | 用户+回收员共用订单列表 |
| `order/detail/detail.vue` | 用户+回收员共用订单详情 |

**验证结果：**
- 后端编译通过 ✅
- 前端编译通过 ✅
- 端到端全流程（用户下单→回收员接单→上门→称重→用户确认）✅

### 第 2 周全部完成，下一步：第 3 周 — 积分规则 + 机构端 + 管理后台
