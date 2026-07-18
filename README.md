# Core Identity

统一身份认证与访问管理平台（IAM），为 Core Platform 提供用户、组织、权限、SSO 等完整能力。

## 项目结构

四个独立子项目，前后端分离，无父 POM，无 monorepo 包装：

```
core-identity/
├── core-identity-backend/        # 身份核心服务（唯一持有数据库）— 独立 Maven 项目
├── core-identity-web/            # 用户自助门户（Vue 3）— 独立 npm 项目
├── core-identity-admin-backend/  # 管理后台 BFF（通过 Internal API 调用核心服务）— 独立 Maven 项目
├── core-identity-admin-web/      # 管理控制台（Vue 3，暗色主题）— 独立 npm 项目
├── contracts/                    # OpenAPI 契约
├── design-docs/                  # 设计文档
└── scripts/                      # 构建脚本
```

## 职责划分

| 模块 | 职责 | 端口 |
|------|------|------|
| **core-identity-backend** | 用户、组织、认证、授权、RBAC、OAuth2/OIDC、SSO、审计 — **唯一有数据库访问权的服务** | 8101 |
| **core-identity-web** | 注册、登录、个人中心、组织管理、角色权限 — 面向终端用户和组织成员 | 5173 |
| **core-identity-admin-backend** | 平台超管操作 — **BFF 模式，不直接访问数据库，通过 Internal API 调用核心服务** | 8102 |
| **core-identity-admin-web** | 超管控制台：用户管理、系统健康、安全审计、合规治理 | 5174 |

## 快速开始

### 前置要求

- Java 17+
- Maven 3.8+
- Node.js 18+

### 一键构建

```bash
# Windows
scripts/build-all.bat

# Linux / macOS
bash scripts/build-all.sh
```

### 启动服务

```bash
# 1. 启动身份核心服务（端口 8101）
cd core-identity-backend
mvn spring-boot:run

# 2. 启动管理 BFF（端口 8102）
cd core-identity-admin-backend
mvn spring-boot:run

# 3. 启动用户门户（端口 5173）
cd core-identity-web
npm install && npm run dev

# 4. 启动管理控制台（端口 5174）
cd core-identity-admin-web
npm install && npm run dev
```

启动后访问：
- 用户门户: http://localhost:5173
- 管理控制台: http://localhost:5174

## 数据库

默认使用 SQLite（`./data/core-identity.db`），零配置即可启动。生产环境支持 MySQL。

支持三种部署模式：

| 模式 | 数据库 | 缓存 | 适用场景 |
|------|--------|------|----------|
| `standalone` | SQLite | Caffeine（本地） | 开发 / 单机部署（默认） |
| `standard` | MySQL | 可选 Redis | 中小团队生产 |
| `enterprise` | MySQL/PostgreSQL | Redis | 大型企业高并发 |

切换方式：`--spring.profiles.active=standard`

数据库迁移使用 Flyway，支持 SQLite + MySQL 双套迁移脚本。

## 配置示例

### Identity Backend (`application.yml`)

```yaml
server.port: 8101
core.identity.instance-name: Core Identity
core.internal-auth.token-ttl-seconds: 600
core.database.type: sqlite          # sqlite | mysql
```

### Admin Backend (`application.yml`)

```yaml
server.port: 8102
core.identity.base-url: http://localhost:8101
core.admin.development-access: true  # 生产环境应关闭
```

## 主要 API

### Public API（8101）

| 端点 | 说明 |
|------|------|
| `GET /api/v1/identity/meta` | 服务元数据 |
| `GET /api/v1/identity/capabilities` | 可用能力列表 |
| `GET /.well-known/openid-configuration` | OIDC Discovery |
| `GET /.well-known/jwks.json` | JWKS 公钥 |

### Internal API（8101，需服务认证）

| 端点 | 说明 |
|------|------|
| `POST /internal/v1/identity/service-tokens` | 签发服务令牌 |
| `GET /internal/v1/identity/system/info` | 系统信息 |
| `GET /internal/v1/identity/system/health` | 健康检查 |
| `POST /internal/v1/identity/audit-events` | 记录审计事件 |

### Admin API（8102）

| 端点 | 说明 |
|------|------|
| `GET /admin-api/v1/identity/system/overview` | 聚合状态概览 |
| `GET /admin-api/v1/identity/system/health` | 健康聚合 |
| `GET /admin-api/v1/identity/system/contracts` | 契约兼容性 |

完整 API 契约见 [contracts/](contracts/) 目录。

## 功能成熟度

| 阶段 | 版本 | 能力 |
|------|------|------|
| P0 | v0.1.0 | 工程基础：审计、发件箱、幂等、内部服务认证 |
| P1 | v0.3.0 | 身份 MVP：注册、登录、会话、组织 |
| P2 | v0.4.0 | 组织与权限：RBAC、角色、邀请、授权 |
| P3 | v0.5.0 | 平台能力：OAuth2/OIDC、API Key、服务账号 |
| P4 | v0.6.0 | 账户安全：TOTP、WebAuthn、风控、恢复 |
| P5 | v0.7.0 | 企业 SSO：OIDC/SAML 联邦、SCIM、JIT |
| P6 | v0.8.0 | 企业治理：访问包、SoD、访问审查、合规 |
| P7 | v0.9.0 | 高可用：多实例、分布式缓存、选举、事件中继 |

当前最新版本：**v0.10.0**

## License

MIT
