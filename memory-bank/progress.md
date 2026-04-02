# 项目进度记录

## 当前状态：Step 1.3 ✅ 已完成

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
- SaTokenConfig：改为 `StpLogic` 实现多账号体系（user/collector/institution/admin 四套独立鉴权）
- RecycleApplication：添加 `@MapperScan("com.recycle.mapper")`
- application.yml：移除全局 `logic-delete-field`
- WebConfig：添加路径末尾斜杠兼容处理

---

### Step 1.2 🔴 数据库创建与核心建表（3h）— ✅ 2026-04-02 完成

**完成内容：**
- 创建 6 张核心表：`community`、`admin`、`user`、`user_address`、`recycle_order`、`order_status_log`
- 所有表均含主键自增、`created_at` 默认 CURRENT_TIMESTAMP
- `user` 表：openid 唯一索引、phone 索引、community_id 索引
- `recycle_order` 表：order_no 唯一索引、user_id / collector_id / status / created_at 索引
- `order_status_log` 表：order_id 索引
- `admin` 表：username 唯一索引
- 插入 5 条测试社区数据、1 条默认管理员（admin/admin123）

**验证结果：**
- 6 张表全部存在 ✅
- 字段类型、索引、默认值均正确 ✅
- 社区测试数据 5 条、管理员 1 条插入成功 ✅

**SQL 脚本位置：** `src/main/resources/sql/init_tables.sql`

**注意事项：**
- MySQL 客户端连接需指定 `--default-character-set=utf8mb4`，否则中文数据会因 gbk 编码截断

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

### 下一步：Step 1.4 小程序项目初始化与角色路由骨架
