# Changelog

All notable changes to core-identity will be documented in this file.

---

## [v0.1.0] - 2026-07-14

### 🎯 概述

core-identity 首个版本，实现了 Core Platform 身份基础设施的工程骨架（Phase 0）、身份运行时（Phase 1）和认证运行时（Phase 2）。

### 🏗️ Phase 0 — 工程骨架

- 创建标准化四子项目结构：`backend/`、`frontend/`、`admin-backend/`、`admin-frontend/`
- `backend/`：Spring Boot 3.3 + Java 21 + Maven，默认 SQLite 数据库
- `admin-backend/`：Spring Boot 3.3 独立项目，预留管理端 API 入口
- `frontend/`、`admin-frontend/`：预留空目录，README 占位
- 根目录 `README.md`：模块说明与快速启动指南

### 👤 Phase 1 — Identity Runtime

**数据模型：User → Account → Credential 三层分离**

| 实体 | 说明 |
|---|---|
| `User` | 用户主体，username / email / displayName / avatar / status(ACTIVE\|DISABLED\|LOCKED) |
| `Account` | 登录方式，accountType(EMAIL\|GITHUB\|GOOGLE\|WECHAT\|LDAP\|CUSTOM)，1:N 关联 User |
| `Credential` | 凭证，credentialType(PASSWORD\|API_KEY\|TOTP\|RECOVERY_CODE)，1:N 关联 Account |
| `RefreshToken` | 刷新令牌，支持撤销和过期检测 |

**基础设施**
- `BaseEntity`：统一 `id` / `create_time` / `update_time` / `deleted` (snake_case 映射)
- JPA Repository 层：`UserRepository` / `AccountRepository` / `CredentialRepository` / `RefreshTokenRepository`
- 统一响应体 `ApiResponse<T>`：`{code, message, data}`
- 分页结果 `PageResult<T>`：`{total, page, size, records}`
- 错误码枚举 `ErrorCode`：1xxxx(用户) / 2xxxx(认证) / 3xxxx(校验) / 9xxxx(系统)
- 全局异常处理 `GlobalExceptionHandler`：BusinessException / ValidationException / 兜底 Exception
- DTO 校验：`@Valid` + Jakarta Validation 注解

### 🔐 Phase 2 — Authentication Runtime

**安全配置**
- Spring Security 无状态（`SessionCreationPolicy.STATELESS`）+ CORS 跨域支持
- JWT Filter：`JwtAuthenticationFilter` + `JwtAuthenticationToken`（自定义 Principal）
- `JwtTokenProvider`：签发 / 校验 / 解析 access token + refresh token（jjwt 0.12.x）

**认证 API**

| 端点 | 方法 | 说明 |
|---|---|---|
| `/api/v1/auth/register` | POST | 注册（username + password + email），自动创建 EMAIL Account 和 PASSWORD Credential |
| `/api/v1/auth/login` | POST | 登录，校验密码，返回 access_token + refresh_token + 用户信息 |
| `/api/v1/auth/refresh` | POST | Refresh Token 轮换（rotation），旧 token 撤销，签发新 token 对 |
| `/api/v1/auth/logout` | POST | 登出，撤销该用户所有 refresh token |

**用户 API**

| 端点 | 方法 | 说明 |
|---|---|---|
| `/api/v1/profile` | GET | 获取当前登录用户信息 |
| `/api/v1/profile` | PUT | 修改 displayName / avatar |
| `/api/v1/profile/password` | PUT | 修改密码（需验证旧密码） |

**安全设计**
- 密码加密：BCrypt（`PasswordEncoder` 接口，策略模式可替换）
- Refresh Token Rotation：每次 refresh 旧 token 立即失效，防止泄露滥用
- 用户状态校验：登录时检查 ACTIVE / DISABLED / LOCKED 状态
- JWT secret 配置在 `application.yml`，标注 TODO 迁移到 core-config
- 登录失败次数限制：未实现（MVP 阶段延后）

### 🧪 测试

| 测试类 | 用例数 | 覆盖场景 |
|---|---|---|
| `UserServiceTest` | 5 | 注册成功、重复用户名、重复邮箱、查询存在、查询不存在 |
| `AuthServiceTest` | 9 | 登录成功、密码错误、禁用用户、锁定用户、Refresh 成功、过期/撤销 Refresh、登出、注册并登录 |
| `JwtTokenProviderTest` | 6 | 签发解析、无效 Token、过期 Token、不同用户唯一性 |
| `ErrorCodeTest` | 3 | 错误码分段、唯一性 |
| `ApiResponseTest` | 4 | 成功/错误响应、空数据 |
| `PageResultTest` | 3 | 构造、空页 |
| `RefreshTokenTest` | 4 | 有效/撤销/过期/同时无效 |
| `TokenGeneratorTest` | 3 | 非空、唯一性、长度 |
| `IdentityApplicationTests` | 1 | Spring 上下文加载 |

**总计：38 个测试全部通过**

### 🔧 技术与配置

- **Java**: 21
- **Spring Boot**: 3.3.0
- **JWT**: jjwt 0.12.5（api + impl + jackson）
- **数据库**: SQLite（默认，jdbc 3.45.3.0）+ H2（测试）
- **Hibernate 方言**: `hibernate-community-dialects`（SQLiteDialect）
- **密码**: BCrypt（Spring Security 内置）
- **测试框架**: JUnit5 + Mockito + AssertJ
- **Maven 插件**: compiler 3.8.1（兼容 Maven 3.5.4）

### 📂 目录结构

```
core-identity/
├── backend/           ← 主后端（端口 9001）
├── frontend/          ← 用户前端（待实现）
├── admin-backend/     ← 管理后端（端口 9101）
├── admin-frontend/    ← 管理前端（待实现）
├── design-docs/       ← 设计文档
├── README.md
├── CHANGELOG.md
└── AGENTS.md
```

### ⚠️ 已知限制

- SQLite 并发写入限制（MVP 阶段无影响）
- JWT logout 后 access token 在有效期内仍可用（15min 窗口）
- 邮箱验证码待 core-notification 接入
- 登录失败次数限制待后续版本
- 无 Redis 做 Token 黑名单（依赖短 TTL + refresh token rotation 补偿）

### 🔜 下一版本计划（v0.2.0 — Phase 3）

- Authorization Runtime：Role / Permission / RoleBinding / Policy
- RBAC 权限模型
- admin-backend 用户管理 API