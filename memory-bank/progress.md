# 项目进度记录

## 当前状态：第 4 周功能补全进行中 — Step 4.2 ✅ 完成，Step 4.3~4.13 待开发

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

### Step 3.1 🔴 数据库 - 补建积分与机构相关表（1.5h）— ✅ 2026-04-13 完成

**完成内容：**
- 创建 `points_transaction` 表（积分流水）
- 创建 `points_rule` 表（积分规则，按衣物分类配置积分/kg）
- 插入默认积分规则数据

**SQL 脚本位置：** `src/main/resources/sql/step3_1_points.sql`

---

### Step 3.2 🔴 后端 - 机构扫码接收与积分策略升级（3h）— ✅ 2026-04-13 完成

**完成内容：**
- `InstitutionController`：`POST /api/institution/order/receive`（扫码接收）、`GET /api/institution/order/list`（已接收订单列表）
- `InstitutionOrderService`：`scanReceive()` 乐观更新 WHERE status=4，计算积分，更新状态为7
- `PointsService`：积分发放服务，根据 `points_rule` 按分类计算积分
- `PointsRuleService`：积分规则查询
- `CollectorOrderService.completeWeighing()`：称重完成后生成溯源二维码（QRCodeUtil）
- `RecycleOrderService.confirmOrder()`：用户确认不再发放积分，改为等待机构接收
- `ScanReceiveRequest` DTO
- `PointsRule`、`PointsTransaction` 实体类
- `PointsRuleMapper`、`PointsTransactionMapper`

**新增文件：**
- `dto/ScanReceiveRequest.java`
- `entity/PointsRule.java`、`entity/PointsTransaction.java`
- `mapper/PointsRuleMapper.java`、`mapper/PointsTransactionMapper.java`
- `service/InstitutionOrderService.java`
- `service/PointsService.java`、`service/PointsRuleService.java`
- `util/QRCodeUtil.java`

---

### Step 3.3 🔴 小程序 - 机构端页面（3h）— ✅ 2026-04-17 完成

**完成内容：**
- `pages/index/index.vue`：机构首页 — 扫码接收按钮（`uni.scanCode`）+ 最近接收订单列表（最多5条）
- `pages/order/list/list.vue`：机构订单列表 — 机构Tab筛选（全部/已接收）、机构API、状态7样式、用户确认文案更新
- `pages/order/detail/detail.vue`：订单详情 — 状态7横幅、机构视角状态描述、用户确认文案更新

**关键改动：**
- 三个页面均添加 `isInstitution` 角色判断
- `statusMap` 增加 `7: '机构已接收'`
- 用户确认订单文案从"确认后获得积分"改为"确认后等待机构接收发放积分"
- 机构订单列表卡片不跳转详情页（后端无机构详情API）
- 底部 TabBar `current` 值适配机构角色

**验证结果：**
- 真机调试测试通过 ✅

---

### Step 3.4 🔴 管理后台项目初始化与登录（3h）— ✅ 2026-04-17 完成

**完成内容：**
- 从零搭建 `clothes-recycle-admin/` 项目（Vue 3 + Vite 5 + Element Plus + Pinia + Axios + Vue Router）
- Vite 开发代理 `/api` → `localhost:8080`，端口 3000
- Axios 封装：token 自动注入 `Authorization` header、401 拦截跳登录页、`Result<T>` 解包
- Pinia 管理员状态：login/logout + localStorage 持久化
- Vue Router 导航守卫：未登录拦截到 /login，已登录 /login 重定向 /dashboard
- 登录页：Element Plus 表单（用户名+密码），居中卡片布局，渐变背景
- 后台骨架：侧边栏菜单（仪表盘/订单管理/回收员管理/积分规则/用户管理）+ 顶栏（用户名+退出）+ 内容区
- 仪表盘占位页

**新建文件（10个）：**
- `clothes-recycle-admin/package.json`、`vite.config.js`、`index.html`
- `src/main.js`、`src/App.vue`
- `src/utils/request.js`、`src/store/admin.js`、`src/router/index.js`
- `src/views/login/LoginView.vue`、`src/views/layout/AdminLayout.vue`、`src/views/layout/DashboardView.vue`

**验证结果：**
- Vite 开发服务器启动成功（localhost:3000）✅
- 用户测试通过 ✅

---

### Step 3.5 🔴 管理后台 - 回收员账号创建与资质审核（3h）— ✅ 2026-04-17 完成

**完成内容：**
- `CollectorView.vue`：回收员管理页面（回收员列表 Tab + 待审核申请 Tab）
- 回收员列表：展示手机号、姓名、资质状态、身份证照片
- 创建回收员弹窗：手机号+密码+姓名表单
- 待审核申请列表：审批通过/拒绝操作
- `api/collector.js`：回收员管理 API 模块

**验证结果：**
- 用户测试通过 ✅

---

### Step 3.6 🔴 管理后台 - 订单管理与积分规则（4h）— ✅ 2026-04-17 完成

**完成内容：**
- `MyBatisPlusConfig.java`：注册分页插件（PaginationInnerInterceptor）
- `AdminOrderService.java`：管理员订单分页查询 + 详情（含状态流转日志）
- `AdminController.java`：新增 4 个端点（GET /orders, GET /orders/{id}, GET /points-rules, PUT /points-rules/{id}）
- `api/order.js`：订单管理 API 模块
- `api/pointsRule.js`：积分规则 API 模块
- `OrderView.vue`：订单管理页面（筛选+分页表格+详情弹窗+状态时间线+地址JSON解析）
- `PointsRuleView.vue`：积分规则页面（规则表格+编辑弹窗，支持修改积分/kg、最低重量、启用/禁用）
- `router/index.js`：添加 /orders 和 /points-rules 路由
- `AdminLayout.vue`：启用"订单管理"和"积分规则"菜单项

**验证结果：**
- 用户测试通过 ✅

---

### Step 3.7+ 🔴 管理后台 - 用户管理页面（3h）— ✅ 2026-04-18 完成

**完成内容：**
- `AdminUserService.java`：用户管理服务（分页列表、启用/禁用、重置密码、修改角色）
- `AdminController.java`：新增 4 个用户管理端点（GET /users, PUT /users/{id}/status, POST /users/{id}/reset-password, PUT /users/{id}/role）
- `api/user.js`：用户管理 API 模块
- `UserView.vue`：用户管理页面（筛选+分页表格+启用禁用+重置密码+修改角色弹窗）
- `router/index.js`：添加 /users 路由
- `AdminLayout.vue`：启用"用户管理"菜单项（移除 disabled）

**验证结果：**
- 用户测试通过 ✅

---

### Bug 修复记录（2026-04-17 ~ 2026-04-18）

**1. 身份证照片上传修复：**
- 问题：小程序资质申请页 `chooseImage()` 直接存储本地临时路径到数据库，管理后台无法显示
- 修复：`apply-role.vue` 的 `chooseImage()` 改为先调用 `uploadFile()` 上传到服务器，再存储返回的 `/uploads/xxx.jpg` URL
- 影响：已有的旧申请记录中的临时路径无法恢复，需用户重新提交

**2. 管理后台登录过期不跳转修复：**
- 问题：后端 `GlobalExceptionHandler` 对 `NotLoginException` 返回 HTTP 200 + `{ code: 401 }`，但管理后台 `request.js` 只在 HTTP error 拦截器中处理 401 状态码
- 修复：在 `request.js` 响应成功拦截器中增加 `res.code === 401` 判断，清除 localStorage 并跳转 `/login`

**3. 后端启动冲突修复：**
- 问题：`AdminController.java` 从 `controller/` 移动到 `controller/admin/` 后，旧 `.class` 文件残留导致 `ConflictingBeanDefinitionException`
- 修复：执行 `mvn clean compile` 清除 `target/` 目录中的旧编译产物

---

## 第 4 周：补全功能 + 优化 + 答辩准备

### Step 4.1 ❌ 积分商城 — 已取消（2026-04-18）

**取消原因：** 工作量大但对核心回收链路贡献不高，用数据统计看板替代。

---

### Step 4.2 ✅ 评价与申诉（4h）— 2026-04-18 完成

**完成内容：**

**后端新增文件：**
- `entity/ServiceReview.java`：服务评价实体
- `entity/Complaint.java`：申诉实体（含 action、refundAmount 字段）
- `mapper/ServiceReviewMapper.java`
- `mapper/ComplaintMapper.java`
- `dto/ReviewRequest.java`：评价请求 DTO（orderId, punctualityScore, attitudeScore, weighingScore, content）
- `dto/ComplaintRequest.java`：申诉请求 DTO（orderId, type, description）
- `service/ReviewService.java`：评价服务（提交评价、查询评价列表）
- `service/ComplaintService.java`：申诉服务（提交申诉、查询列表、管理员处理）

**后端修改文件：**
- `controller/user/UserController.java`：新增评价提交/查询 + 申诉提交/列表接口
- `controller/admin/AdminController.java`：新增评价列表 + 申诉列表/处理接口
- `service/PointsService.java`：新增 `addPoints()`、`adminDeductPoints()` 方法

**小程序修改文件：**
- `pages/order/detail/detail.vue`：新增评价弹窗（三维度星级评分 + 文字）+ 申诉弹窗（类型选择 + 描述）

**管理后台新增文件：**
- `api/review.js`：评价管理 API
- `api/complaint.js`：申诉管理 API
- `views/review/ReviewView.vue`：评价管理页面（星级展示 + 分页）
- `views/complaint/ComplaintView.vue`：申诉管理页面（状态筛选 + 处理弹窗）
- `components/OrderDetailDialog.vue`：订单详情弹窗组件（从 OrderView 抽取复用）

**管理后台修改文件：**
- `router/index.js`：添加 /reviews 和 /complaints 路由
- `views/layout/AdminLayout.vue`：启用"评价管理"和"申诉管理"菜单项
- `views/order/OrderView.vue`：详情弹窗改用 OrderDetailDialog 组件

**申诉处理增强：**
- 3 种处理动作：增加积分（ADD_POINTS）/ 减少积分（DEDUCT_POINTS）/ 标记订单异常（MARK_ABNORMAL）
- 管理员处理弹窗：选择动作 + 输入积分数量 + 填写备注

**SQL 迁移脚本：**
- `step4_2_review_complaint.sql`：创建 service_review 和 complaint 表
- `step4_2_enhance_complaint.sql`：complaint 表新增 action 和 refund_amount 字段

---

### 下一步计划（Step 4.3 ~ 4.13）

| 步骤 | 优先级 | 内容 | 预计耗时 | 状态 |
|------|--------|------|----------|------|
| 4.3 | 🔴 P0 | 后端数据统计聚合接口（AdminStatisticsService） | 2h | 未开始 |
| 4.4 | 🔴 P0 | 前端数据统计看板页面（ECharts） | 2h | 未开始 |
| 4.5 | 🟡 P1 | 小程序我的申诉列表页 | 1h | 未开始 |
| 4.6 | 🟡 P1 | 小程序我的评价记录页 | 1h | 未开始 |
| 4.7 | 🟡 P1 | 小程序积分流水明细页 | 1.5h | 未开始 |
| 4.8 | 🟡 P1 | 小程序订单详情状态时间线 | 1h | 未开始 |
| 4.9 | 🟡 P1 | 小程序接单后展示回收员实名信息 | 0.5h | 未开始 |
| 4.10 | 🟡 P1 | 机构端接收数据统计页 | 1.5h | 未开始 |
| 4.11 | 🟡 P1 | 管理后台回收员平均评分展示 | 0.5h | 未开始 |
| 4.12 | 🟡 P1 | 管理后台订单+用户列表导出 Excel | 1.5h | 未开始 |
| 4.13 | 🔴 P0 | 体验优化与演示准备 | 6h | 未开始 |
