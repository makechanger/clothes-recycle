# 衣物回收平台 - 技术栈选型

> 选型原则：一个人 4 周能交付 > 生态成熟好搜答案 > 技术先进性

---

## 总览

```
┌──────────────────────────────────────────────────────┐
│                    微信小程序 × 3                      │
│          用户端 / 回收员端 / 机构端                      │
│              UniApp + Vue 3 + uView Plus              │
└───────────────────────┬──────────────────────────────┘
                        │ HTTPS / JSON
┌───────────────────────▼──────────────────────────────┐
│                   后端统一服务                          │
│         Spring Boot 3 + MyBatis-Plus + MySQL          │
└───────────────────────┬──────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────┐
│                   Web 管理后台                          │
│           Vue 3 + Element Plus + Vite                 │
└──────────────────────────────────────────────────────┘
```

---

## 一、Web 管理后台：Vue 3 + Element Plus

### 选型

| 项目 | 选择 | 理由 |
|:---|:---|:---|
| 框架 | Vue 3 | 与小程序端统一技术栈，学一套用两处 |
| UI 库 | **Element Plus** | **后台管理 UI 库事实标准**，表格、表单、弹窗、菜单组件完善，中文文档一流 |
| 构建 | Vite 5 | 秒级热更新，开发体验好 |
| 路由 | Vue Router 4 | 标配 |
| 状态 | Pinia | Vue 3 官方推荐，比 Vuex 简单 |
| HTTP | Axios | 拦截器成熟，错误处理方便 |
| 图表 | ECharts 5 (按需) | 数据看板用，P1 功能才需要 |
| 后台模板 | **vue-pure-admin** | 基于 Element Plus，内置权限/布局/菜单，省 3-4 天搭框架时间 |

### 为什么管理后台首选 Element Plus

1. **组件覆盖全**：`el-table` 分页表格、`el-form` 表单校验、`el-dialog` 弹窗、`el-menu` 侧边栏——后台管理需要的组件全都有
2. **中文文档**：官方文档就是中文，不需要翻译
3. **生态最大**：GitHub 25k+ star，问题百度/CSDN 一搜一大把
4. **模板丰富**：vue-pure-admin、vue-vben-admin 等成熟后台模板都基于 Element Plus

### 推荐模板：vue-pure-admin

4 周时间，从零搭管理后台（布局、侧边栏、面包屑、权限路由、登录页……）至少吃掉 3-4 天。直接用成熟模板，只写业务页面。

- 基于 Vue 3 + **Element Plus** + Vite + Pinia
- 内置权限管理、动态路由、多标签页
- 中文文档完善，GitHub 15k+ star
- 删掉示例页面，只保留框架骨架即可

---

## 二、前端小程序：UniApp + Vue 3

### 选型

| 项目 | 选择 | 理由 |
|:---|:---|:---|
| 框架 | UniApp (Vue 3) | **一套代码编译三个小程序**（用户/回收员/机构），省 2/3 前端工作量 |
| UI 库 | uView Plus 3.x | 专为 UniApp + 微信小程序设计，60+ 组件开箱即用 |
| 状态管理 | Pinia | 与管理后台一致 |
| 网络请求 | uni.request 封装 | 统一拦截器处理 token、错误提示、loading |
| 开发工具 | HBuilderX | UniApp 官方 IDE，一键编译到微信小程序 |

### 为什么小程序端不用 Element Plus

Element Plus 是为浏览器 DOM 环境设计的，**微信小程序不是浏览器，无法运行 Element Plus**。小程序有自己的渲染引擎和组件规范，必须使用专为小程序适配的 UI 库（如 uView Plus、Vant Weapp、uni-ui）。

### 为什么不选其他小程序方案

| 方案 | 否决理由 |
|:---|:---|
| 原生微信小程序 × 3 | 三端各写一遍，4 周不可能完成 |
| Taro (React) | 可以，但管理后台是 Vue + Element Plus，统一 Vue 技术栈学习成本更低 |
| Flutter | 微信小程序支持不成熟，社区资源少 |

### 三端差异化策略

一个 UniApp 项目 + 条件编译，不需要三个独立项目：

```
src/
├── pages/
│   ├── user/          # 用户端页面
│   ├── collector/     # 回收员端页面
│   └── institution/   # 机构端页面
├── pages-common/      # 共享页面（登录、订单详情等）
├── components/        # 共享组件
├── api/               # 接口定义（共享）
├── store/             # Pinia 状态（共享）
└── utils/             # 工具函数（共享）
```

编译时通过 `manifest.json` 配置不同的 appid，生成三个独立小程序。

---

## 三、后端：Spring Boot 3

### 选型

| 项目 | 选择 | 理由 |
|:---|:---|:---|
| 框架 | Spring Boot 3.2+ | 毕设答辩老师最认可，Java 生态最成熟 |
| ORM | MyBatis-Plus 3.5+ | 单表 CRUD 零 SQL，省 60% 代码量 |
| 数据库 | MySQL 8.0 | 国内毕设标配，免费，资料最多 |
| 鉴权 | Sa-Token 1.38+ | 比 Spring Security 简单 10 倍，5 行代码搞定登录 |
| API 文档 | Knife4j 4.x (Swagger) | 自动生成接口文档，前后端联调效率翻倍 |
| 工具库 | Hutool 5.8+ | 日期、加密、HTTP、二维码等一站式工具箱 |
| 二维码 | ZXing 3.5+ | Google 开源，一行代码生成溯源二维码 |
| 文件存储 | 本地磁盘 + 静态资源映射 | 毕设够用，不需要云 OSS |
| 构建工具 | Maven | 比 Gradle 简单，依赖管理稳定 |

### 不用的技术及理由

| 技术 | 为什么不用 |
|:---|:---|
| Redis | 毕设单机场景，不需要分布式缓存，少一个中间件少一个坑 |
| 微服务 | 一个人 4 周，微服务是自杀行为。单体按包分模块足够清晰 |
| Spring Security | 配置复杂，Sa-Token 够用 |
| JPA/Hibernate | 复杂查询不如 MyBatis 灵活 |
| Node.js | 可以替代，但毕设答辩 Java 更主流 |

### 后端项目结构

```
src/main/java/com/recycle/
├── config/            # 配置类（跨域、Sa-Token、Swagger、文件上传）
├── controller/        # 接口层
│   ├── user/          # 用户端接口
│   ├── collector/     # 回收员端接口
│   ├── institution/   # 机构端接口
│   └── admin/         # 管理后台接口
├── service/           # 业务层
│   └── impl/
├── mapper/            # MyBatis-Plus Mapper
├── entity/            # 数据库实体
├── dto/               # 请求/响应对象
├── common/            # 通用：Result 封装、全局异常处理、常量
└── RecycleApplication.java
```

### 核心依赖 (pom.xml)

```xml
<!-- 核心 -->
spring-boot-starter-web          3.2.x
mybatis-plus-spring-boot3-starter 3.5.x
mysql-connector-j                 8.x

<!-- 鉴权 -->
sa-token-spring-boot3-starter    1.38.x

<!-- 工具 -->
knife4j-openapi3-jakarta-spring-boot-starter  4.x
hutool-all                       5.8.x
lombok                           最新

<!-- 二维码 -->
zxing-core + zxing-javase        3.5.x
```

---

## 四、数据库：MySQL 8.0

| 项目 | 说明 |
|:---|:---|
| 版本 | MySQL 8.0（Windows 安装包或 Docker） |
| 管理工具 | Navicat / DBeaver |
| 字符集 | utf8mb4（支持中文和 emoji） |
| 引擎 | InnoDB（事务支持） |
| 索引 | 主键自增 + 常用查询字段加索引（order_no、user_id、status） |

---

## 五、开发与部署

### 开发工具

| 工具 | 用途 |
|:---|:---|
| IntelliJ IDEA / VS Code | 后端 Java 开发 |
| HBuilderX | UniApp 小程序开发 |
| 微信开发者工具 | 小程序预览调试 |
| Navicat / DBeaver | 数据库可视化管理 |
| Apifox / Postman | 接口调试 |

### 部署方案

```
方案 A：直接部署（最简单，推荐答辩用）
├── MySQL 8.0             → 本机安装
├── Spring Boot           → java -jar recycle.jar
├── Vue 管理后台 (dist/)   → Nginx 托管静态文件
└── 小程序                 → 微信开发者工具预览（不需要上线）

方案 B：Docker Compose（一键启动）
├── docker-compose.yml
│   ├── mysql:8.0
│   ├── recycle-backend    (Dockerfile)
│   └── recycle-admin      (nginx + dist/)
└── 小程序 → 微信开发者工具预览
```

---

## 六、技术栈全景表

| 层级 | 技术 | 版本 | 一句话理由 |
|:---|:---|:---|:---|
| 小程序框架 | UniApp (Vue 3) | 3.x | 一套代码三端复用 |
| 小程序 UI | uView Plus | 3.x | UniApp 生态最成熟组件库 |
| 小程序 IDE | HBuilderX | 最新 | UniApp 官方 IDE |
| **管理后台 UI** | **Element Plus** | **最新** | **后台管理事实标准，组件最全** |
| 管理后台模板 | vue-pure-admin | 最新 | 基于 Element Plus，省 3-4 天 |
| 管理后台构建 | Vite | 5.x | 秒级热更新 |
| 后端框架 | Spring Boot | 3.2+ | 毕设标配，生态最全 |
| ORM | MyBatis-Plus | 3.5+ | 单表零 SQL |
| 鉴权 | Sa-Token | 1.38+ | 比 Spring Security 简单 10 倍 |
| API 文档 | Knife4j | 4.x | 自动生成接口文档 |
| 工具库 | Hutool | 5.8+ | 一站式工具箱 |
| 数据库 | MySQL | 8.0 | 国内毕设标配 |
| 图表 | ECharts | 5.x | 数据看板（P1 功能） |
| 部署 | Docker Compose | 最新 | 一键启动 |

---

## 七、关键决策 FAQ

### Q: 答辩时老师问"为什么不用微服务"？

**A**: 项目用户规模有限，单体架构在当前场景下是最优解。微服务引入的注册中心、网关、服务间通信等复杂度远超项目需求，属于过度设计。

### Q: Sa-Token 够用吗？

够用。微信小程序登录 3 行代码：

```java
StpUtil.login(userId);           // 登录
StpUtil.getTokenValue();         // 拿 token 返回前端
StpUtil.checkLogin();            // 校验（注解或拦截器）
```

### Q: 文件存储为什么不用云 OSS？

毕设阶段本地磁盘足够。Spring Boot 做静态资源映射即可对外提供图片访问。后续如需上云，只需替换存储实现类，接口不变。

### Q: 为什么不用 Redis？

单机单用户场景不需要分布式缓存。Sa-Token 默认内存存储 Session，积分余额直接查库。少部署一个服务 = 少一个出错点。

---

## 八、本地开发启动命令

在 VSCode 中打开项目根目录 `ClothesRecycle`，使用 bash 终端分别启动后端和前端。

### 终端 1：Spring Boot 后端（热重载）

```bash
cd clothes-recycle-server
JAVA_HOME="/c/Program Files/Java/jdk-17" /c/Maven/apache-maven-3.6.3/bin/mvn spring-boot:run
```

- 项目已引入 `spring-boot-devtools`，启动后修改 Java 文件保存即自动重启
- 默认端口 `8080`，API 文档地址：`http://localhost:8080/doc.html`

### 终端 2：小程序前端（热重载）

```bash
cd clothes-recycle-mini
npm run dev:mp-weixin
```

- 编译输出目录：`clothes-recycle-mini/dist/dev/mp-weixin`
- 打开**微信开发者工具**，导入上述目录作为项目
- 修改 `.vue` 文件保存后，Vite 自动编译，微信开发者工具自动刷新预览

### 仅编译（不启动服务）

```bash
# 后端编译检查
cd clothes-recycle-server
JAVA_HOME="/c/Program Files/Java/jdk-17" /c/Maven/apache-maven-3.6.3/bin/mvn compile -q

# 前端生产构建
cd clothes-recycle-mini
npm run build:mp-weixin
```

---

## 九、开发环境检查清单

开发前确认以下环境就绪：

- [ ] JDK 17+（Spring Boot 3 要求）
- [ ] Maven 3.8+
- [ ] MySQL 8.0（已创建数据库 `clothes_recycle`，字符集 utf8mb4）
- [ ] Node.js 18+（管理后台构建）
- [ ] HBuilderX（UniApp 开发）
- [ ] 微信开发者工具
- [ ] 微信小程序测试号 AppID
- [ ] Git
