# 项目架构

> 最后更新：2026-04-18（Step 3.7+ 用户管理完成后）

## 技术架构总览

```
┌─────────────────────────────────────────────────┐
│               客户端层 (Frontend)                │
│  ┌──────────────────┐  ┌─────────────────────┐  │
│  │  UniApp 小程序    │  │  Vue3 管理后台       │  │
│  │  (Vue3+uView)    │  │  (Element Plus)      │  │
│  │  用户/回收员/机构  │  │  从零搭建            │  │
│  └────────┬─────────┘  └──────────┬──────────┘  │
└───────────┼────────────────────────┼────────────┘
            │ HTTP/JSON              │ HTTP/JSON
┌───────────┼────────────────────────┼────────────┐
│           ▼                        ▼             │
│              Spring Boot 3.2 后端                │
│  ┌─────────────────────────────────────────────┐ │
│  │  Controller 层（按角色隔离路由前缀）          │ │
│  │  /api/auth/**  /api/user/**                 │ │
│  │  /api/collector/**  /api/institution/**      │ │
│  │  /api/admin/**  /api/common/**              │ │
│  ├─────────────────────────────────────────────┤ │
│  │  Sa-Token 鉴权（2 套体系）                   │ │
│  │  StpUtil（所有用户）| StpLogic("admin")      │ │
│  ├─────────────────────────────────────────────┤ │
│  │  Service 层（业务逻辑）                      │ │
│  ├─────────────────────────────────────────────┤ │
│  │  MyBatis-Plus Mapper 层（数据访问）          │ │
│  └──────────────────┬──────────────────────────┘ │
└─────────────────────┼────────────────────────────┘
                      │ JDBC
              ┌───────▼───────┐
              │  MySQL 8.0    │
              │ clothes_recycle│
              │  utf8mb4      │
              └───────────────┘
```

## 后端项目结构

```
clothes-recycle-server/
├── pom.xml                          # Maven 依赖（Spring Boot 3.2.5）
└── src/main/
    ├── java/com/recycle/
    │   ├── RecycleApplication.java  # 启动类 + @MapperScan
    │   ├── config/
    │   │   ├── SaTokenConfig.java   # 鉴权拦截（2 套：StpUtil + StpLogic("admin")）
    │   │   ├── CorsConfig.java      # 跨域（开发阶段允许所有）
    │   │   ├── WebConfig.java       # 静态资源映射 /uploads/**
    │   │   ├── Knife4jConfig.java   # API 文档配置
    │   │   └── MyBatisPlusConfig.java # 分页插件注册
    │   ├── common/
    │   │   ├── Result.java          # 统一响应 { code, message, data }
    │   │   ├── GlobalExceptionHandler.java  # 全局异常处理（401/403/400/500）
    │   │   └── BusinessException.java       # 自定义业务异常
    │   ├── controller/
    │   │   ├── CommonController.java        # 公共接口（社区列表、文件上传）
    │   │   ├── TestController.java          # 验证用测试接口
    │   │   ├── admin/
    │   │   │   ├── AuthController.java      # 统一登录/注册（/api/auth/**）
    │   │   │   └── AdminController.java     # 管理员接口（/api/admin/**）
    │   │   ├── user/
    │   │   │   └── UserController.java      # 用户接口（改密码、地址、订单、资质申请）
    │   │   ├── collector/
    │   │   │   └── CollectorController.java # 回收员接口（接单、称重、订单管理）
    │   │   └── institution/
    │   │       └── InstitutionController.java # 机构接口（扫码接收、订单列表）
    │   ├── service/
    │   │   ├── AuthService.java             # 登录/注册/改密码
    │   │   ├── RoleApplicationService.java  # 资质申请/审批
    │   │   ├── UserAddressService.java      # 地址 CRUD
    │   │   ├── RecycleOrderService.java     # 用户订单（创建/列表/取消/确认）
    │   │   ├── CollectorOrderService.java   # 回收员订单（接单/上门/称重）
    │   │   ├── InstitutionOrderService.java # 机构订单（扫码接收）
    │   │   ├── OrderStatusLogService.java   # 订单状态流转日志
    │   │   ├── PointsService.java           # 积分发放
    │   │   ├── PointsRuleService.java       # 积分规则查询
    │   │   ├── AdminOrderService.java       # 管理员订单查询（分页+详情）
    │   │   ├── AdminCollectorService.java   # 管理员回收员管理（创建/审批）
    │   │   └── AdminUserService.java        # 管理员用户管理（列表/状态/密码/角色）
    │   ├── mapper/
    │   │   ├── UserMapper.java
    │   │   ├── CollectorMapper.java
    │   │   ├── InstitutionMapper.java
    │   │   ├── AdminMapper.java
    │   │   ├── RoleApplicationMapper.java
    │   │   ├── UserAddressMapper.java
    │   │   ├── CommunityMapper.java
    │   │   ├── RecycleOrderMapper.java
    │   │   ├── OrderStatusLogMapper.java
    │   │   ├── PointsRuleMapper.java
    │   │   └── PointsTransactionMapper.java
    │   ├── entity/
    │   │   ├── User.java            # 用户（含 role 字段：USER/COLLECTOR/INSTITUTION）
    │   │   ├── Collector.java       # 回收员扩展信息（通过 userId 关联 user）
    │   │   ├── Institution.java     # 机构扩展信息（通过 userId 关联 user）
    │   │   ├── Admin.java           # 管理员
    │   │   ├── RoleApplication.java # 角色升级申请
    │   │   ├── UserAddress.java     # 用户地址
    │   │   ├── Community.java       # 社区
    │   │   ├── RecycleOrder.java    # 回收订单
    │   │   ├── OrderStatusLog.java  # 订单状态日志
    │   │   ├── PointsRule.java      # 积分规则
    │   │   └── PointsTransaction.java # 积分流水
    │   ├── dto/
    │   │   ├── LoginRequest.java        # 登录请求（phone, password）
    │   │   ├── LoginResponse.java       # 登录响应（token, role, userInfo）
    │   │   ├── RegisterRequest.java     # 注册请求（phone, password, name）
    │   │   ├── AdminLoginRequest.java   # 管理员登录请求
    │   │   ├── ChangePasswordRequest.java    # 修改密码请求
    │   │   ├── RoleApplicationRequest.java  # 资质申请请求
    │   │   ├── UserAddressRequest.java  # 地址请求
    │   │   ├── CreateOrderRequest.java  # 创建订单请求
    │   │   ├── CompleteWeighingRequest.java  # 称重完成请求
    │   │   ├── ScanReceiveRequest.java  # 机构扫码接收请求
    │   │   ├── CreateCollectorRequest.java  # 管理员创建回收员请求
    │   │   └── CollectorVO.java         # 回收员视图对象
    │   └── util/
    │       └── QRCodeUtil.java          # 二维码生成工具
    └── resources/
        ├── application.yml          # 全部配置（数据源、Sa-Token、Knife4j、上传限制）
        └── sql/
            ├── init_tables.sql          # 核心建表脚本
            ├── step1_5_auth.sql         # 认证相关补充
            ├── role_application.sql     # 角色申请表
            ├── step2_refactor_role.sql  # 方案 B 重构（统一用户表 + 扩展表）
            └── step3_1_points.sql       # 积分相关表
```

## 管理后台项目结构

```
clothes-recycle-admin/
├── package.json                     # 依赖（Vue 3, Vite 5, Element Plus, Pinia, Axios, Vue Router）
├── vite.config.js                   # Vite 配置（端口 3000，代理 /api 和 /uploads 到 8080）
├── index.html                       # 入口 HTML
└── src/
    ├── main.js                      # 应用入口（Element Plus + Router + Pinia）
    ├── App.vue                      # 根组件
    ├── utils/
    │   └── request.js               # Axios 封装（token 注入、Result<T> 解包、401 跳转）
    ├── store/
    │   └── admin.js                 # Pinia 管理员状态（login/logout + localStorage）
    ├── router/
    │   └── index.js                 # Vue Router（导航守卫 + 路由配置）
    ├── api/
    │   ├── collector.js             # 回收员管理 API
    │   ├── order.js                 # 订单管理 API
    │   ├── pointsRule.js            # 积分规则 API
    │   └── user.js                  # 用户管理 API
    └── views/
        ├── login/
        │   └── LoginView.vue        # 登录页（Element Plus 表单，渐变背景）
        ├── layout/
        │   ├── AdminLayout.vue      # 后台骨架（侧边栏+顶栏+内容区）
        │   └── DashboardView.vue    # 仪表盘占位页
        ├── collector/
        │   └── CollectorView.vue    # 回收员管理（列表 Tab + 待审核 Tab）
        ├── order/
        │   └── OrderView.vue        # 订单管理（筛选+分页+详情弹窗+时间线）
        ├── points/
        │   └── PointsRuleView.vue   # 积分规则（规则表格+编辑弹窗）
        └── user/
            └── UserView.vue         # 用户管理（筛选+分页+状态/密码/角色操作）
```

## 数据库表结构

```
clothes_recycle (MySQL 8.0, utf8mb4)

已实现（有 Entity + Mapper + Service）：
├── user               # 用户表（统一所有角色，含 role 字段：USER/COLLECTOR/INSTITUTION）
├── collector          # 回收员扩展信息表（通过 user_id 关联 user）
├── institution        # 机构扩展信息表（通过 user_id 关联 user）
├── admin              # 管理员表（独立鉴权体系）
├── role_application   # 角色升级申请表
├── user_address       # 用户地址表
├── community          # 社区表
├── recycle_order      # 回收订单表（核心业务表）
├── order_status_log   # 订单状态流转记录
├── points_rule        # 积分规则表
└── points_transaction # 积分流水表

后续分步补建：
├── points_product     # 积分商品表
├── points_exchange_order # 积分兑换订单表
├── service_review     # 服务评价表
└── complaint          # 异常申诉表
```

## 小程序项目结构

```
clothes-recycle-mini/
├── package.json                     # 依赖（Vue 3, Pinia, uView Plus）
└── src/
    ├── App.vue                      # 应用入口
    ├── main.js                      # 初始化 Pinia
    ├── manifest.json                # 小程序配置
    ├── pages.json                   # 路由配置（11 个页面）
    ├── components/
    │   └── custom-tabbar/           # 自定义底部导航栏（根据角色动态显示）
    ├── pages/
    │   ├── index/index.vue          # 首页（角色分视图：USER/COLLECTOR/INSTITUTION）
    │   ├── login/login.vue          # 登录页（手机号+密码）✅
    │   ├── register/register.vue    # 注册页 ✅
    │   ├── order/
    │   │   ├── create/create.vue    # 预约回收页 ✅
    │   │   ├── list/list.vue        # 订单列表页（用户/回收员/机构三视图）✅
    │   │   └── detail/detail.vue    # 订单详情页（用户/回收员/机构三视图）✅
    │   ├── user/
    │   │   ├── user.vue             # 个人中心页 ✅
    │   │   ├── change-password/     # 修改密码 ✅
    │   │   └── apply-role/          # 资质申请 ✅
    │   └── address/
    │       ├── list/list.vue        # 地址列表页 ✅
    │       └── edit/edit.vue        # 地址编辑页 ✅
    ├── store/
    │   └── user.js                  # Pinia 用户状态（token, role, userInfo）
    └── utils/
        └── request.js               # uni.request 封装（自动携带 token + uploadFile）
```

## 关键设计决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 登录方案 | 手机号+密码统一登录 | 放弃微信 openid，三种角色共用 /api/auth/login，后端通过 user.role 判断角色 |
| 注册方案 | 统一注册为 USER | 所有角色先注册为普通用户，后续通过资质申请升级角色 |
| 用户表架构 | **统一用户表 + 扩展表**（方案 B） | user 表永远保留，role 字段标识角色；collector/institution 为扩展信息表通过 user_id 关联。角色升级 = UPDATE user.role + INSERT 扩展记录，不删除 user 数据 |
| 鉴权方案 | Sa-Token **2 套体系** | StpUtil（所有用户 USER/COLLECTOR/INSTITUTION 共用）+ StpLogic("admin")（管理员独立） |
| ORM | MyBatis-Plus | 单表零 SQL，复杂查询手写 XML |
| 文件存储 | 本地 ./uploads/ | 无需云 OSS，静态资源映射直接访问 |
| 逻辑删除 | 按需 @TableLogic | 不全局启用，避免干扰不需要的表 |
| API 文档 | Knife4j (doc.html) | 开发阶段自测 + 答辩展示 |
| 底部导航 | 自定义 TabBar 组件 | 非原生 tabBar，支持角色动态切换 |

## 端口与路径约定

| 服务 | 地址 |
|------|------|
| 后端 API | `http://localhost:8080/api/**` |
| API 文档 | `http://localhost:8080/doc.html` |
| 上传文件访问 | `http://localhost:8080/uploads/**` |
| 管理后台 | `http://localhost:3000`（Vite dev），代理 /api 和 /uploads 到 8080 |
