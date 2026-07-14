# P0 工程基座继续建设 — 执行计划

## 目标

将 `core-identity` 打造成一个完全可复制的工程模板。完成后新增 `core-ai` 只需 `cp -r core-identity core-ai` 即可获得一致的工程骨架。

---

## 本次实施内容总览

共 4 个后端新模块 + 2 个前端脚手架 + 2 个现有模块改造：

| 模块 | 动作 | 说明 |
|---|---|---|
| `pom.xml`（根） | **新建** | 轻量 parent，声明子模块 + 统一 Java 21 |
| `core-bom` | **新建** | 版本物料清单，统一 Spring Boot/jjwt/SQLite 版本 |
| `core-common` | **新建** | 提取 ApiResponse/PageResult/PageRequest/BusinessException/ErrorCode |
| `core-starter` | **新建** | 自动配置：GlobalExceptionHandler/Jackson/CORS/OpenAPI |
| `backend` | **改造** | 删除重复类，改 import，ErrorCode 重构为 IdentityErrorCode |
| `admin-backend` | **改造** | 删除重复 ApiResponse，改 import |
| `frontend` | **新建** | Vue3 + Element Plus + 用户端 Layout（Header + Content） |
| `admin-frontend` | **新建** | Vue3 + Element Plus + 管理端 Layout（Sidebar + Header + Content） |

---

## 一、根 pom.xml

```xml
<groupId>com.coreplatform</groupId>
<artifactId>core-identity</artifactId>
<version>1.0.0-SNAPSHOT</version>
<packaging>pom</packaging>
<modules>
    <module>core-bom</module>
    <module>core-common</module>
    <module>core-starter</module>
    <module>backend</module>
    <module>admin-backend</module>
</modules>
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.3.0</spring-boot.version>
    <jjwt.version>0.12.5</jjwt.version>
</properties>
<!-- dependencyManagement 引入 core-bom -->
```

---

## 二、core-bom（pom.xml 纯版本管理）

`dependencyManagement` 中统一：
- `spring-boot-dependencies:3.3.0`（import scope）
- `jjwt-api/jjwt-impl/jjwt-jackson:0.12.5`
- `sqlite-jdbc:3.45.3.0`

---

## 三、core-common（关键设计）

### 3.1 ErrorCode 拆分设计

```
core-common:
  ErrorCode（接口）— getCode() + getMessage()
  CommonErrorCode（枚举）— VALIDATION_ERROR, SYSTEM_ERROR
  BusinessException — 构造参数改为 ErrorCode 接口

backend:
  IdentityErrorCode（枚举 implements ErrorCode）— 保留 10001~90001 四大类错误码
```

这样 core-ai、core-storage 等未来模块都可以定义自己的 `AiErrorCode`，都实现同一个接口。

### 3.2 从 backend 提取的类

| 原位置 | 新位置（core-common） |
|---|---|
| `entity/ApiResponse.java` | `com.coreplatform.common.response.ApiResponse` |
| `entity/PageResult.java` | `com.coreplatform.common.response.PageResult` |
| `exception/BusinessException.java` | `com.coreplatform.common.exception.BusinessException` |

### 3.3 新增的类

- `com.coreplatform.common.exception.ErrorCode`（接口）
- `com.coreplatform.common.exception.CommonErrorCode`（枚举，仅 2 个通用码）
- `com.coreplatform.common.dto.PageRequest`（`{page, size}`，带默认值 page=1, size=20）

### 3.4 测试

新增：`ApiResponseTest`、`PageResultTest`、`PageRequestTest`、`BusinessExceptionTest`、`CommonErrorCodeTest`

---

## 四、core-starter

### 自动配置类（4 个）

| 配置 | 说明 |
|---|---|
| `GlobalExceptionAutoConfiguration` | 注册 GlobalExceptionHandler（从 backend 移入，使用 CommonErrorCode） |
| `JacksonAutoConfiguration` | snake_case 命名、ISO 日期、null 不序列化 |
| `CorsAutoConfiguration` | 允许所有来源的 CORS（从 backend SecurityConfig 提取） |
| `OpenApiAutoConfiguration` | SpringDoc OpenAPI 3 基础配置（info.title 等由各模块覆盖） |

### 测试

`GlobalExceptionAutoConfigurationTest`：验证 bean 加载 + 异常响应格式

---

## 五、backend 改造（影响面最大）

### 删除 4 个文件

- `entity/ApiResponse.java`
- `entity/PageResult.java`
- `exception/BusinessException.java`
- `exception/GlobalExceptionHandler.java`

### 重命名 + 重构

- `exception/ErrorCode.java` → `exception/IdentityErrorCode.java`
- 改为 `enum IdentityErrorCode implements com.coreplatform.common.exception.ErrorCode`
- 错误码值保持不变

### pom.xml 变更

- `<parent>` 从 spring-boot-starter-parent 改为根 pom
- 添加 `core-common`、`core-starter` 依赖

### 更新 import（~12 个文件）

| 文件 | 需改的 import |
|---|---|
| `AuthController.java` | ApiResponse |
| `UserController.java` | ApiResponse |
| `AuthService.java` | BusinessException, ErrorCode→IdentityErrorCode |
| `UserService.java` | BusinessException, ErrorCode→IdentityErrorCode |
| `SecurityConfig.java` | 移除 CORS 配置（由 starter 提供） |
| `entity/User.java` | BaseEntity（各模块自有，不改） |
| 测试类 6 个 | ApiResponseTest→挪到 core-common；其他测试改 import |

### 测试

- 新增 `IdentityErrorCodeTest`（确保所有错误码唯一 + 实现 ErrorCode 接口）
- 原有 38 个测试全部通过

---

## 六、admin-backend 改造

### 删除

- `exception/ApiResponse.java`

### pom.xml 变更

- `<parent>` 改为根 pom
- 添加 `core-common`、`core-starter` 依赖

### 更新 import

- `HealthController.java` → 使用 `com.coreplatform.common.response.ApiResponse`

### 测试

- 新增 `HealthControllerTest`（验证返回 `{code:0, message:"success", data:"ok"}`）

---

## 七、frontend（Vue3 用户端）

### 技术栈

Vue3 + TypeScript + Vite + Vue Router + Pinia + Axios + Element Plus + UnoCSS

### 文件清单

```
frontend/
├── package.json           ← 声明依赖
├── vite.config.ts         ← Vite + Element Plus 按需引入 + UnoCSS
├── tsconfig.json
├── tsconfig.node.json
├── index.html
├── env.d.ts
├── uno.config.ts          ← UnoCSS 配置
└── src/
    ├── App.vue            ← 根组件
    ├── main.ts            ← 入口
    ├── api/
    │   └── index.ts       ← axios 实例封装（baseURL /api/v1）
    ├── assets/
    ├── components/
    ├── layouts/
    │   └── DefaultLayout.vue  ← Header + <router-view>
    ├── pages/
    │   └── HomePage.vue       ← 占位首页（显示模块名 + 欢迎信息）
    ├── router/
    │   └── index.ts
    ├── stores/
    ├── styles/
    │   └── global.css
    └── utils/
```

### Layout（用户端）

- Header：顶部导航栏，包含 Logo + 导航菜单（首页、登录、注册）+ 用户下拉
- Content：`<router-view>` 渲染当前路由页面

---

## 八、admin-frontend（Vue3 管理端）

### 技术栈

同 frontend

### 文件清单

```
admin-frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json / tsconfig.node.json
├── index.html
├── env.d.ts
├── uno.config.ts
└── src/
    ├── App.vue
    ├── main.ts
    ├── api/
    │   └── index.ts           ← axios 实例封装（baseURL /admin-api/v1）
    ├── assets/
    ├── components/
    ├── layouts/
    │   └── AdminLayout.vue    ← Sidebar + Header + Content
    ├── pages/
    │   └── DashboardPage.vue  ← 占位仪表盘
    ├── router/
    │   └── index.ts
    ├── stores/
    ├── styles/
    │   └── global.css
    └── utils/
```

### Layout（管理端）

- Sidebar（左侧）：el-menu 可折叠，菜单项「仪表盘」「用户管理」（预留）
- Header（顶部）：Logo + 面包屑 + 用户信息
- Content：`<router-view>`

---

## 九、验证计划

### 自动化测试

| 模块 | 命令 | 预期 |
|---|---|---|
| core-common | `mvn test` | 新增 ~15 个测试通过 |
| core-starter | `mvn test` | 自动配置加载 + 异常响应格式正确 |
| backend | `mvn test` | 38 个原测试 + IdentityErrorCodeTest 通过 |
| admin-backend | `mvn test` | HealthControllerTest 通过 |

### 启动验证

| 模块 | 验证 |
|---|---|
| backend | `mvn spring-boot:run` → 端口 9001，`POST /api/v1/auth/login` 可访问 |
| admin-backend | `mvn spring-boot:run` → 端口 9101，`GET /admin-api/v1/health` 返回 `{code:0}` |
| frontend | `npm run dev` → Vite 启动，默认 Layout 正确渲染 |
| admin-frontend | `npm run dev` → Vite 启动，Sidebar Layout 正确渲染 |

### 回归测试

- `curl` 测试 backend 的 4 个 auth API + 3 个 profile API 全部正常返回
- `curl` 测试 admin-backend health 端点

---

## 十、不做的事

- ❌ 不写业务页面（登录/注册/个人中心）
- ❌ 不修改 Entity 业务字段
- ❌ 不引入 Redis/MQ/微服务
- ❌ 不实现完整的 UI Design System（仅 Layout 骨架）
- ❌ 不在前端接入后端 API（仅预留 api/ 目录和 axios 封装骨架）
- ❌ 不实现 Dark/Light 主题切换（仅预留 CSS 变量结构）