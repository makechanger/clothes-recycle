# 项目架构

## 技术架构总览

```
┌─────────────────────────────────────────────────┐
│               客户端层 (Frontend)                │
│  ┌──────────────────┐  ┌─────────────────────┐  │
│  │  UniApp 小程序    │  │  Vue3 管理后台       │  │
│  │  (Vue3+uView)    │  │  (Element Plus)      │  │
│  │  用户/回收员/机构  │  │  vue-pure-admin      │  │
│  └────────┬─────────┘  └──────────┬──────────┘  │
└───────────┼────────────────────────┼────────────┘
            │ HTTP/JSON              │ HTTP/JSON
┌───────────┼────────────────────────┼────────────┐
│           ▼                        ▼             │
│              Spring Boot 3.2 后端                │
│  ┌─────────────────────────────────────────────┐ │
│  │  Controller 层（按角色隔离路由前缀）          │ │
│  │  /api/user/**  /api/collector/**             │ │
│  │  /api/institution/**  /api/admin/**          │ │
│  ├─────────────────────────────────────────────┤ │
│  │  Sa-Token 鉴权（多账号体系 StpLogic）        │ │
│  │  user=默认体系 | collector | institution | admin│
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
    │   │   ├── SaTokenConfig.java   # 鉴权拦截（4 套 StpLogic 角色隔离）
    │   │   ├── CorsConfig.java      # 跨域（开发阶段允许所有）
    │   │   ├── WebConfig.java       # 静态资源映射 /uploads/**
    │   │   └── Knife4jConfig.java   # API 文档配置
    │   ├── common/
    │   │   ├── Result.java          # 统一响应 { code, message, data }
    │   │   ├── GlobalExceptionHandler.java  # 全局异常处理（401/403/400/500）
    │   │   └── BusinessException.java       # 自定义业务异常
    │   ├── controller/
    │   │   ├── TestController.java  # 验证用测试接口
    │   │   ├── user/                # 用户端接口（待开发）
    │   │   ├── collector/           # 回收员端接口（待开发）
    │   │   ├── institution/         # 机构端接口（待开发）
    │   │   └── admin/               # 管理后台接口（待开发）
    │   ├── service/impl/            # 业务逻辑层（待开发）
    │   ├── mapper/                  # MyBatis-Plus Mapper（待开发）
    │   ├── entity/                  # 数据库实体类（待开发）
    │   └── dto/                     # 请求/响应 DTO（待开发）
    └── resources/
        ├── application.yml          # 全部配置（数据源、Sa-Token、Knife4j、上传限制）
        └── sql/
            └── init_tables.sql      # Step 1.2 核心建表脚本（6 张表）
```

## 数据库表结构（Step 1.2 已建）

```
clothes_recycle (MySQL 8.0, utf8mb4)
├── community          # 社区表（5 条测试数据）
├── admin              # 管理员表（1 条默认 admin）
├── user               # 用户表（openid 唯一索引）
├── user_address       # 用户地址表（user_id 索引）
├── recycle_order      # 回收订单表（order_no 唯一，多字段索引）
└── order_status_log   # 订单状态流转记录（order_id 索引）

后续分步补建：
├── collector          # 回收员表（Step 2.1）
├── institution        # 第三方机构表（Step 3.1）
├── points_transaction # 积分流水表（Step 3.1）
├── points_rule        # 积分规则表（Step 3.1）
├── points_product     # 积分商品表（Step 4.1）
├── points_exchange_order # 积分兑换订单表（Step 4.1）
├── service_review     # 服务评价表（Step 4.2）
└── complaint          # 异常申诉表（Step 4.2）
```

## 关键设计决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 鉴权方案 | Sa-Token 多账号体系 | 4 种角色用独立 StpLogic，token 互不通用 |
| ORM | MyBatis-Plus | 单表零 SQL，复杂查询手写 XML |
| 文件存储 | 本地 ./uploads/ | 无需云 OSS，静态资源映射直接访问 |
| 逻辑删除 | 按需 @TableLogic | 不全局启用，避免干扰不需要的表 |
| API 文档 | Knife4j (doc.html) | 开发阶段自测 + 答辩展示 |

## 端口与路径约定

| 服务 | 地址 |
|------|------|
| 后端 API | `http://localhost:8080/api/**` |
| API 文档 | `http://localhost:8080/doc.html` |
| 上传文件访问 | `http://localhost:8080/uploads/**` |
| 管理后台（待搭建） | `http://localhost:5173` |
