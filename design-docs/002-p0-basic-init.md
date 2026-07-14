# Core Identity P0：工程与边界基础详细设计

版本：P0
目标：建立四子项目的工程骨架、职责边界、通信契约、安全基线、构建发布与基础交互体验。

---

# 一、P0 的核心目标

P0 不追求“用户已经可以登录”，而是先确保以后实现登录、组织、权限、SSO 时，不需要推翻工程结构。

P0 必须解决六个问题：

```text
1. 四个子项目分别负责什么
2. 哪个项目拥有身份数据
3. 用户端和管理端如何隔离
4. Admin Backend 如何调用 Identity Backend
5. 前后端契约如何统一
6. SQLite、MySQL、构建、测试、发布如何标准化
```

P0 完成后的系统应当具备：

```text
四个子项目可以独立开发
两个后端可以独立启动
两个前端可以独立运行
根项目可以一键构建
前后端可以分别打包
管理端不能直接访问 Identity 数据库
所有 API 边界已经固定
基础日志、审计、幂等、事件机制已经建立
```

---

# 二、P0 明确不做什么

P0 不实现：

```text
用户注册
用户登录
找回密码
组织管理
成员邀请
角色权限
JWT 用户令牌
API Key
MFA
SSO
SCIM
```

这些属于 P1 之后。

P0 可以提供：

```text
服务间认证基础
开发环境访问保护
系统状态页面
接口版本检查
健康检查
数据库迁移
技术审计
事件 Outbox
幂等基础
```

不要为了让页面“看起来完整”，提前放入假的用户、假的组织和假的权限模型。

---

# 三、总体工程结构

```text
core-identity/
├── pom.xml
├── package.json
├── pnpm-workspace.yaml
├── README.md
├── LICENSE
├── CHANGELOG.md
├── SECURITY.md
├── .editorconfig
├── .gitignore
│
├── core-identity-backend/
├── core-identity-web/
├── core-identity-admin-backend/
├── core-identity-admin-web/
│
├── contracts/
├── docs/
├── scripts/
└── distribution/
```

四个子项目：

```text
core-identity-backend
core-identity-web
core-identity-admin-backend
core-identity-admin-web
```

根目录的其他文件夹不是独立子项目，只负责规范、契约、构建和发行。

---

# 四、四个子项目的准确职责

## 4.1 core-identity-backend

定位：

> Identity 的唯一核心服务和唯一身份数据所有者。

P0 负责：

```text
数据库初始化
Flyway 迁移
公共 API 基础框架
内部 API 基础框架
统一错误模型
服务间认证
审计记录
幂等记录
Outbox 事件
运行信息
健康检查
配置校验
```

以后负责：

```text
User
Organization
Membership
Credential
Session
Role
Permission
Token
MFA
SSO
```

只有它可以访问 Identity 数据库。

禁止：

```text
依赖 Admin Backend
依赖 Admin Web
保存管理控制台页面状态
为管理端复制一套 UserService
```

---

## 4.2 core-identity-web

定位：

> 普通用户与组织管理员的身份自助门户。

P0 负责：

```text
前端工程骨架
路由系统
公共布局
账户布局
API Client
系统初始化检查
版本兼容检查
错误页面
加载状态
离线状态
无权限状态
```

P1 以后负责：

```text
注册
登录
个人中心
安全中心
组织切换
组织成员管理
组织角色管理
API Key
授权应用
```

P0 不需要做假的登录流程。

---

## 4.3 core-identity-admin-backend

定位：

> 平台管理控制台的 BFF 和管理操作编排层。

P0 负责：

```text
管理 API 基础框架
调用 Identity Backend 内部 API
服务间认证
管理请求上下文
管理审计上下文
数据脱敏框架
系统状态聚合
API 版本兼容检查
```

以后负责：

```text
跨组织管理查询
管理操作编排
批量任务
导出任务
高危操作二次确认
平台安全运营
后台统计聚合
```

它不是第二套 Identity 服务。

禁止：

```text
直接连接 Identity 数据库
定义 User Repository
定义 Organization Repository
修改 identity_user
自行实现密码重置
自行实现账号冻结
```

---

## 4.4 core-identity-admin-web

定位：

> 平台管理员、安全人员、审计人员使用的管理控制台。

P0 负责：

```text
管理端工程骨架
管理控制台布局
侧边栏
顶部状态栏
系统状态页
服务诊断页
版本信息页
错误与离线状态
开发环境保护
危险操作交互规范
```

P1 以后负责：

```text
用户管理
组织管理
登录记录
审计日志
安全事件
SSO 配置
访问治理
```

---

# 五、依赖与调用边界

## 5.1 正确调用关系

```text
core-identity-web
        │
        ▼
core-identity-backend
        │
        ▼
Identity Database
```

```text
core-identity-admin-web
        │
        ▼
core-identity-admin-backend
        │
        ▼
core-identity-backend
        │
        ▼
Identity Database
```

## 5.2 禁止调用关系

```text
core-identity-web
    × core-identity-admin-backend

core-identity-admin-web
    × core-identity-backend 公共用户 API

core-identity-admin-backend
    × Identity Database

core-identity-backend
    × core-identity-admin-backend
```

## 5.3 依赖矩阵

| 调用方              | 可以调用                      | 禁止调用                 |
| ---------------- | ------------------------- | -------------------- |
| Identity Web     | Identity Backend 公共 API   | Admin API、内部 API     |
| Admin Web        | Admin Backend             | Identity Backend、数据库 |
| Admin Backend    | Identity Backend 内部 API   | Identity 数据库         |
| Identity Backend | 数据库、外部 Core 接口            | Admin Backend        |
| 其他 Core          | Identity 公共协议、JWKS、内部授权协议 | Identity 数据库         |

---

# 六、根项目设计

## 6.1 Maven 根项目

根 `pom.xml` 只管理两个 Java 子项目：

```xml
<packaging>pom</packaging>

<modules>
    <module>core-identity-backend</module>
    <module>core-identity-admin-backend</module>
</modules>
```

根 Maven 管理：

```text
Java 21
Spring Boot 版本
插件版本
编译参数
测试插件
JaCoCo
Checkstyle
SpotBugs
依赖安全检查
构建信息
```

前端不强行伪装成 Maven Module。

---

## 6.2 pnpm Workspace

```yaml
packages:
  - core-identity-web
  - core-identity-admin-web
```

根前端命令：

```bash
pnpm install
pnpm dev:web
pnpm dev:admin
pnpm build
pnpm test
```

---

## 6.3 根构建命令

开发构建：

```bash
mvn clean verify
pnpm install
pnpm build
```

统一构建：

```bash
scripts/build-all.bat
```

Linux/macOS：

```bash
scripts/build-all.sh
```

最终产物：

```text
distribution/
├── core-identity-backend.jar
├── core-identity-admin-backend.jar
├── core-identity-web.zip
├── core-identity-admin-web.zip
├── checksums.txt
└── release-notes.md
```

P0 阶段先保持四个产物独立。

以后可以增加组合发行：

```text
core-identity.jar
core-identity-admin.jar
```

但组合发行不能改变四个源码子项目的边界。

---

# 七、契约目录设计

为了避免前后端手写四套 DTO，根目录增加：

```text
contracts/
├── public/
│   └── identity-public-api.yaml
├── internal/
│   └── identity-internal-api.yaml
├── admin/
│   └── identity-admin-api.yaml
├── events/
│   └── identity-events.yaml
└── errors/
    └── identity-error-codes.yaml
```

## 7.1 Public API

面向：

```text
core-identity-web
其他上层产品
第三方客户端
```

路径：

```text
/api/v1/identity/**
```

## 7.2 Internal API

面向：

```text
core-identity-admin-backend
其他受信任 Core 服务
```

路径：

```text
/internal/v1/identity/**
```

必须经过服务身份认证。

## 7.3 Admin API

面向：

```text
core-identity-admin-web
```

路径：

```text
/admin-api/v1/identity/**
```

只由 Admin Backend 提供。

## 7.4 契约生成

根据 OpenAPI 自动生成：

```text
Identity Web TypeScript Client
Admin Web TypeScript Client
Admin Backend Identity Internal Client
```

禁止前端自行定义：

```ts
interface UserResponse
```

然后后端再写一个不同结构的：

```java
class UserResponse
```

P0 就要建立契约生成机制。

---

# 八、core-identity-backend 详细结构

```text
core-identity-backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/io/coreplatform/identity/
    │   │   ├── CoreIdentityApplication.java
    │   │   ├── api/
    │   │   ├── application/
    │   │   └── infrastructure/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-sqlite.yml
    │       ├── application-mysql.yml
    │       └── db/migration/
    └── test/
```

只保留三层。

---

## 8.1 API 层

```text
api/
├── publicapi/
│   ├── controller/
│   ├── request/
│   └── response/
│
├── internal/
│   ├── controller/
│   ├── request/
│   └── response/
│
├── exception/
├── filter/
└── validation/
```

P0 API：

```text
GET /api/v1/identity/meta
GET /api/v1/identity/capabilities

POST /internal/v1/identity/service-tokens
GET  /internal/v1/identity/system/info
GET  /internal/v1/identity/system/health
POST /internal/v1/identity/audit-events
```

公共元信息示例：

```json
{
  "service": "core-identity",
  "version": "0.1.0",
  "apiVersion": "v1",
  "status": "INITIALIZING",
  "capabilities": []
}
```

P0 的能力列表为空或只包含：

```text
SYSTEM_META
INTERNAL_SERVICE_AUTH
AUDIT_FOUNDATION
OUTBOX_FOUNDATION
```

不要谎报：

```text
LOGIN
ORGANIZATION
RBAC
```

已经可用。

---

## 8.2 Application 层

```text
application/
├── system/
├── audit/
├── internalclient/
├── idempotency/
├── outbox/
├── port/
└── exception/
```

P0 应用服务：

```text
SystemInfoService
InternalClientService
InternalTokenService
AuditService
IdempotencyService
OutboxService
```

示例：

```java
public interface AuditService {
    String record(AuditCommand command);
}
```

应用层定义端口：

```java
public interface AuditRepository {
    void save(AuditEvent event);
}
```

禁止应用层依赖：

```text
JdbcTemplate
SQLite
MySQL
HTTP Servlet
Spring Security 实现类
```

---

## 8.3 Infrastructure 层

```text
infrastructure/
├── persistence/
│   ├── audit/
│   ├── outbox/
│   ├── idempotency/
│   ├── internalclient/
│   └── instance/
│
├── security/
├── configuration/
├── observability/
├── clock/
└── json/
```

负责：

```text
SQLite/MySQL Repository 实现
密码哈希
内部 Token 签发
日志脱敏
配置绑定
Request ID
Flyway
时钟实现
JSON 序列化
```

---

# 九、core-identity-admin-backend 详细结构

```text
core-identity-admin-backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/io/coreplatform/identity/admin/
    │   │   ├── CoreIdentityAdminApplication.java
    │   │   ├── api/
    │   │   ├── application/
    │   │   └── infrastructure/
    │   └── resources/
    └── test/
```

---

## 9.1 API 层

P0 提供：

```text
GET /admin-api/v1/identity/bootstrap
GET /admin-api/v1/identity/system/overview
GET /admin-api/v1/identity/system/health
GET /admin-api/v1/identity/system/version
GET /admin-api/v1/identity/system/contracts
```

不提供：

```text
/admin-api/v1/identity/users
/admin-api/v1/identity/organizations
```

因为 P0 还没有用户和组织。

---

## 9.2 Application 层

P0 服务：

```text
SystemOverviewService
BackendHealthAggregationService
ContractCompatibilityService
AdminOperationContextService
```

`SystemOverviewService` 聚合：

```text
Admin Backend 状态
Identity Backend 状态
数据库状态
Flyway 状态
契约版本
应用版本
启动时间
```

---

## 9.3 Infrastructure 层

```text
infrastructure/
├── identityclient/
├── security/
├── configuration/
├── observability/
└── masking/
```

Identity Client 必须由 OpenAPI 生成基础接口，再进行薄封装。

禁止在各个 Service 中直接散落：

```java
restClient.get()
    .uri("http://localhost:8101/internal/...")
```

统一封装为：

```java
public interface IdentityInternalClient {
    IdentitySystemInfo getSystemInfo();
    IdentityHealthInfo getHealthInfo();
    void recordAudit(AdminAuditCommand command);
}
```

---

# 十、服务间认证设计

P0 虽然没有用户登录，但必须建立 Admin Backend 调用 Identity Backend 的安全基础。

## 10.1 内部客户端

Admin Backend 使用：

```text
client_id
client_secret
```

向 Identity Backend 请求短期内部 Token。

流程：

```text
Admin Backend
    │
    │ client_id + client_secret
    ▼
POST /internal/v1/identity/service-tokens
    │
    ▼
Identity Backend 校验凭证
    │
    ▼
返回短期 Service Token
```

Token 建议有效期：

```text
5～15 分钟
```

Admin Backend 在内存中缓存，到期前刷新。

---

## 10.2 内部 Token 内容

```json
{
  "sub": "core-identity-admin-backend",
  "type": "service",
  "aud": "core-identity-backend",
  "scope": [
    "identity.system.read",
    "identity.audit.write"
  ],
  "iat": 1784040000,
  "exp": 1784040600
}
```

P0 不需要完整 OAuth Server，但令牌结构应与未来服务账号体系兼容。

---

## 10.3 凭证初始化

禁止在代码中写死：

```text
admin
admin123
```

支持两种方式。

### 开发环境

环境变量：

```text
CORE_INTERNAL_CLIENT_ID
CORE_INTERNAL_CLIENT_SECRET
```

Identity Backend 首次启动时，根据显式配置创建或更新内部客户端。

### 生产环境

通过初始化脚本生成：

```bash
scripts/bootstrap-internal-client.sh
```

脚本输出完整 Secret 一次。

数据库只保存 Secret Hash。

---

# 十一、P0 数据库设计

P0 不创建：

```text
identity_user
identity_organization
identity_role
identity_permission
```

只创建技术基础表。

---

## 11.1 identity_instance_metadata

用途：

```text
标识当前 Identity 实例
记录安装信息
记录最近启动信息
提供诊断信息
```

字段：

| 字段              | 类型           | 说明                     |
| --------------- | ------------ | ---------------------- |
| instance_id     | VARCHAR(36)  | 实例唯一 ID                |
| instance_name   | VARCHAR(100) | 实例名称                   |
| installation_id | VARCHAR(36)  | 安装标识                   |
| edition         | VARCHAR(30)  | COMMUNITY / ENTERPRISE |
| current_version | VARCHAR(30)  | 当前版本                   |
| schema_version  | VARCHAR(50)  | 数据库结构版本                |
| installed_at    | BIGINT       | 安装时间                   |
| last_started_at | BIGINT       | 最近启动时间                 |
| created_at      | BIGINT       | 创建时间                   |
| updated_at      | BIGINT       | 更新时间                   |

主键：

```text
instance_id
```

注意：

`schema_version` 仅用于展示，真正迁移状态仍以 Flyway 为准。

---

## 11.2 identity_internal_client

用途：

```text
保存 Admin Backend 和其他 Core 服务的内部调用身份
```

字段：

| 字段                 | 类型           | 说明                |
| ------------------ | ------------ | ----------------- |
| id                 | VARCHAR(36)  | 主键                |
| client_id          | VARCHAR(100) | 客户端 ID            |
| client_secret_hash | VARCHAR(255) | Secret Hash       |
| display_name       | VARCHAR(150) | 显示名称              |
| client_type        | VARCHAR(30)  | SERVICE           |
| scopes_json        | TEXT         | Scope 列表          |
| status             | VARCHAR(20)  | ACTIVE / DISABLED |
| expires_at         | BIGINT       | 过期时间，可空           |
| last_used_at       | BIGINT       | 最近使用时间            |
| created_at         | BIGINT       | 创建时间              |
| updated_at         | BIGINT       | 更新时间              |
| version            | BIGINT       | 乐观锁版本             |

约束：

```text
UNIQUE(client_id)
```

索引：

```text
idx_identity_internal_client_status
idx_identity_internal_client_expires_at
```

完整 Secret 永远不入库。

---

## 11.3 identity_audit_event

用途：

```text
记录系统启动、内部客户端操作、管理端技术操作
为 P1 业务审计提供统一基础
```

字段：

| 字段             | 类型           | 说明                       |
| -------------- | ------------ | ------------------------ |
| id             | VARCHAR(36)  | 审计事件 ID                  |
| event_type     | VARCHAR(100) | 事件类型                     |
| actor_type     | VARCHAR(30)  | SYSTEM / SERVICE / ADMIN |
| actor_id       | VARCHAR(100) | 操作者 ID                   |
| action         | VARCHAR(100) | 操作                       |
| target_type    | VARCHAR(100) | 目标类型                     |
| target_id      | VARCHAR(100) | 目标 ID                    |
| result         | VARCHAR(20)  | SUCCESS / FAILURE        |
| reason         | VARCHAR(500) | 操作原因                     |
| request_id     | VARCHAR(64)  | 请求 ID                    |
| source_service | VARCHAR(100) | 来源服务                     |
| source_ip      | VARCHAR(64)  | 来源 IP                    |
| user_agent     | VARCHAR(500) | User-Agent               |
| metadata_json  | TEXT         | 扩展信息                     |
| occurred_at    | BIGINT       | 发生时间                     |

索引：

```text
idx_identity_audit_event_occurred_at
idx_identity_audit_event_event_type
idx_identity_audit_event_actor
idx_identity_audit_event_request_id
idx_identity_audit_event_target
```

原则：

```text
只追加
不更新
不软删除
```

P0 不必做哈希链和不可篡改存储，这属于 P6。

---

## 11.4 identity_outbox_event

用途：

```text
建立可靠事件发布基础
避免业务事务提交成功但事件丢失
```

字段：

| 字段              | 类型           | 说明                           |
| --------------- | ------------ | ---------------------------- |
| id              | VARCHAR(36)  | 事件 ID                        |
| event_type      | VARCHAR(120) | 事件类型                         |
| event_version   | INTEGER      | 事件版本                         |
| aggregate_type  | VARCHAR(100) | 聚合类型                         |
| aggregate_id    | VARCHAR(100) | 聚合 ID                        |
| payload_json    | TEXT         | 事件内容                         |
| headers_json    | TEXT         | 事件头                          |
| status          | VARCHAR(20)  | PENDING / PUBLISHED / FAILED |
| attempt_count   | INTEGER      | 尝试次数                         |
| next_attempt_at | BIGINT       | 下次重试时间                       |
| published_at    | BIGINT       | 成功时间                         |
| last_error      | TEXT         | 最后错误                         |
| created_at      | BIGINT       | 创建时间                         |
| updated_at      | BIGINT       | 更新时间                         |
| version         | BIGINT       | 乐观锁版本                        |

索引：

```text
idx_identity_outbox_status_next_attempt
idx_identity_outbox_created_at
idx_identity_outbox_event_type
```

P0 可以只实现：

```text
写入
查询
重试状态管理
```

暂不接 MQ。

---

## 11.5 identity_idempotency_record

用途：

```text
为内部写操作、未来注册和管理操作提供幂等基础
```

字段：

| 字段              | 类型           | 说明                              |
| --------------- | ------------ | ------------------------------- |
| id              | VARCHAR(36)  | 主键                              |
| idempotency_key | VARCHAR(150) | 幂等键                             |
| scope           | VARCHAR(100) | 作用域                             |
| request_hash    | VARCHAR(128) | 请求摘要                            |
| status          | VARCHAR(20)  | PROCESSING / SUCCEEDED / FAILED |
| response_status | INTEGER      | HTTP 状态码                        |
| response_body   | TEXT         | 可选响应                            |
| locked_until    | BIGINT       | 处理锁过期时间                         |
| expires_at      | BIGINT       | 记录过期时间                          |
| created_at      | BIGINT       | 创建时间                            |
| updated_at      | BIGINT       | 更新时间                            |

约束：

```text
UNIQUE(scope, idempotency_key)
```

索引：

```text
idx_identity_idempotency_expires_at
idx_identity_idempotency_status
```

注意：

不保存密码、Token 和 Secret 等敏感响应。

---

# 十二、数据库迁移设计

目录：

```text
core-identity-backend/
└── src/main/resources/db/migration/
    ├── common/
    ├── sqlite/
    └── mysql/
```

建议迁移：

```text
V0_1_0_001__create_instance_metadata.sql
V0_1_0_002__create_internal_client.sql
V0_1_0_003__create_audit_event.sql
V0_1_0_004__create_outbox_event.sql
V0_1_0_005__create_idempotency_record.sql
```

原则：

```text
发布后的迁移不可修改
只能新增迁移
SQLite 和 MySQL 都必须执行测试
禁止 ddl-auto=update
禁止应用启动时手写 ALTER TABLE
```

---

# 十三、core-identity-web 交互设计

P0 的用户侧页面不是完整产品，而是验证工程与运行状态的可用壳层。

## 13.1 路由

```text
/
 /login
 /account
 /system-unavailable
 /upgrade-required
 /404
```

其中 `/login` 和 `/account` 暂时不实现业务。

---

## 13.2 首次进入流程

```text
用户打开页面
    │
    ▼
前端读取本地构建版本
    │
    ▼
请求 GET /api/v1/identity/meta
    │
    ├── 成功且版本兼容
    │      ▼
    │   进入欢迎页
    │
    ├── 后端不可用
    │      ▼
    │   系统不可用页
    │
    └── API 版本不兼容
           ▼
        升级提示页
```

---

## 13.3 P0 欢迎页

页面内容：

```text
Core Identity Logo
服务名称
版本
运行状态
当前能力
登录功能状态
文档入口
```

示例：

```text
Core Identity

身份服务正在初始化。

当前版本：0.1.0
服务状态：运行正常
登录能力：将在 P1 提供
```

不要放一个可以点击却永远失败的“登录”按钮。

可以显示禁用状态：

```text
登录功能尚未启用
```

---

## 13.4 后端不可用页面

必须说明：

```text
是浏览器网络错误
还是 Identity Backend 不可用
是否可以重试
如何查看技术信息
```

交互：

```text
重新连接
复制诊断信息
查看状态详情
```

不要只显示：

```text
Something went wrong
```

---

## 13.5 版本不兼容页面

当 Web 与 Backend API 版本不兼容时：

```text
当前前端版本
当前后端版本
要求的 API 范围
建议操作
```

示例：

```text
前端版本 0.2.0 需要 Identity API >= 0.2.0，
当前后端版本为 0.1.0。

请升级后端，或使用匹配版本的前端。
```

这对独立部署四个子项目非常重要。

---

# 十四、core-identity-admin-web 交互设计

P0 的管理端重点是系统状态和工程诊断。

## 14.1 页面结构

```text
/admin
/admin/system
/admin/system/health
/admin/system/contracts
/admin/system/configuration
```

P0 导航：

```text
系统概览
服务健康
接口契约
运行信息
```

不显示空的：

```text
用户管理
组织管理
权限管理
```

---

## 14.2 系统概览页

页面卡片：

```text
Identity Backend
Admin Backend
Database
Schema Migration
Public API
Internal API
```

每张卡片展示：

```text
状态
版本
最近检查时间
响应延迟
错误摘要
```

状态统一：

```text
HEALTHY
DEGRADED
UNAVAILABLE
INCOMPATIBLE
```

不要只使用红绿灯；同时显示文字和原因，避免颜色成为唯一信息。

---

## 14.3 健康详情交互

点击服务卡片，打开侧边抽屉：

```text
服务名称
服务版本
启动时间
Java 版本
数据库类型
数据库连接状态
Flyway 版本
API 契约版本
最近错误
Request ID
```

提供：

```text
复制诊断信息
重新检查
```

P0 不提供“在线修改数据库配置”，避免管理端误操作。

---

## 14.4 管理端访问保护

因为 P0 还没有正式平台管理员账号，管理控制台采取两种模式。

### 开发模式

仅允许：

```text
localhost
127.0.0.1
```

访问。

由配置启用：

```yaml
core:
  admin:
    development-access: true
```

生产环境禁止默认启用。

### 生产模式

P0 默认关闭 Admin Web 对外访问。

显示：

```text
管理控制台将在平台管理员认证启用后开放
```

不要在 P0 创建默认管理员密码。

---

# 十五、基础 UX 规范

## 15.1 用户端与管理端必须视觉区分

用户端：

```text
轻量
自助
面向个人和组织成员
```

管理端：

```text
严谨
信息密度更高
突出风险和审计
```

但两者共享：

```text
设计 Token
字体
基础组件
错误语言
状态颜色规则
```

不要让两个系统看起来像完全不同的产品，也不要让用户误以为平台管理控制台是组织管理页面。

---

## 15.2 所有异步操作必须有四种状态

```text
初始
加载
成功
失败
```

不能只有 Loading 和 Data。

系统状态检查还需要：

```text
超时
降级
版本不兼容
```

---

## 15.3 错误信息分层

用户看到：

```text
发生了什么
是否影响当前操作
下一步应该做什么
```

技术详情放在折叠区域：

```text
errorCode
requestId
timestamp
service
```

---

## 15.4 Request ID 可复制

每次错误都显示：

```text
请求编号：01J...
```

用户提交问题时可以复制。

后端日志、Admin Backend 和 Identity Backend 必须使用同一个 Request ID。

---

## 15.5 不允许伪造可用能力

P0 页面不得显示：

```text
注册
登录
组织
权限
```

为“已完成”。

正确显示：

```text
未启用
规划于 P1
```

---

# 十六、统一错误模型

所有后端统一返回：

```json
{
  "type": "https://core.example/problems/service-unavailable",
  "title": "Identity service unavailable",
  "status": 503,
  "detail": "The identity backend could not be reached.",
  "errorCode": "IDENTITY_BACKEND_UNAVAILABLE",
  "requestId": "01J...",
  "timestamp": "2026-07-14T15:30:00Z"
}
```

P0 错误码建议：

```text
IDENTITY_CONFIGURATION_INVALID
IDENTITY_DATABASE_UNAVAILABLE
IDENTITY_SCHEMA_MIGRATION_FAILED
IDENTITY_INTERNAL_CLIENT_INVALID
IDENTITY_INTERNAL_CLIENT_DISABLED
IDENTITY_INTERNAL_TOKEN_EXPIRED
IDENTITY_INTERNAL_SCOPE_DENIED
IDENTITY_CONTRACT_INCOMPATIBLE
IDENTITY_BACKEND_UNAVAILABLE
IDENTITY_IDEMPOTENCY_CONFLICT
IDENTITY_INTERNAL_ERROR
```

错误码一旦公开，不随意修改。

---

# 十七、日志与可观测性

P0 必须统一日志字段：

```text
timestamp
level
service
environment
request_id
trace_id
actor_type
actor_id
operation
duration_ms
status
error_code
```

两个后端服务名固定：

```text
core-identity-backend
core-identity-admin-backend
```

禁止记录：

```text
client_secret
完整 service token
数据库密码
Authorization Header
Cookie
```

健康指标至少包括：

```text
HTTP 请求数量
HTTP 错误数量
请求延迟
数据库连接状态
Flyway 状态
内部 Token 签发数量
内部认证失败数量
Outbox 待处理数量
```

P0 暂时不需要接入 Prometheus 服务，但需要保留标准指标端点。

---

# 十八、配置设计

## 18.1 Identity Backend

```yaml
server:
  port: 8101

spring:
  profiles:
    active: sqlite

core:
  identity:
    instance-name: Core Identity

  internal-auth:
    issuer: core-identity
    token-ttl-seconds: 600

  database:
    type: sqlite

  outbox:
    enabled: true
    poll-interval: 5s
```

---

## 18.2 Admin Backend

```yaml
server:
  port: 8102

core:
  identity:
    base-url: http://localhost:8101

  internal-client:
    client-id: ${CORE_INTERNAL_CLIENT_ID}
    client-secret: ${CORE_INTERNAL_CLIENT_SECRET}

  admin:
    development-access: false
```

---

## 18.3 配置校验

以下配置缺失时禁止静默启动：

```text
生产环境内部客户端 Secret
生产环境签名密钥
数据库文件目录
数据库迁移权限
```

启动失败必须明确输出：

```text
缺少哪个配置
应通过哪个环境变量提供
不能输出 Secret 当前值
```

---

# 十九、和其他 Core 模块的交互

P0 的原则：

> 定义交互方式，但不建立不必要的运行依赖。

---

## 19.1 与 core-api-gateway

P0 交互：

```text
定义 Identity Public API 路由
定义 Internal API 禁止公开转发
透传 Request ID
保留客户端 IP
统一错误格式
```

路由建议：

```text
/api/v1/identity/**       可公开
/admin-api/v1/identity/** 仅管理网络
/internal/v1/identity/**  永远不向公网暴露
```

P0 不要求 `core-api-gateway` 存在。

没有 Gateway 时，四个子项目仍可独立运行。

---

## 19.2 与 core-notification

P0 不实际发送邮件。

只在 Identity Backend Application 层预留：

```java
public interface IdentityNotificationPort {
}
```

P1 再增加：

```text
发送邮箱验证码
发送密码重置
发送安全通知
```

禁止 P0 直接引入：

```text
JavaMailSender
SMTP 配置
短信厂商 SDK
```

未来调用方式优先：

```text
Identity Backend
    → core-notification API
```

或者写入事件：

```text
identity.notification.requested
```

---

## 19.3 与 core-billing

P0 无运行依赖。

只规定未来身份标识格式：

```text
user_id
organization_id
```

Billing 不得复制 Identity 用户表。

未来交互：

```text
Identity 提供用户与组织身份
Billing 管理组织套餐、额度与订阅
```

P0 不在 Identity 中增加：

```text
plan
subscription
quota
```

字段。

---

## 19.4 与 core-storage

P0 无运行依赖。

未来用户头像和组织 Logo 只保存：

```text
storage_object_id
```

不在 Identity 数据库保存：

```text
文件二进制
本地磁盘路径
完整对象存储 URL
```

P0 不创建头像字段，因为用户表还不存在。

---

## 19.5 与 core-workflow

P0 的 `identity_outbox_event` 为未来 Workflow 打基础。

未来事件：

```text
identity.user.created
identity.organization.created
identity.member.invited
identity.account.disabled
```

P0 只完成统一事件结构，不接入 Workflow。

Workflow 不得直接读取 Identity Outbox 表，而应通过正式事件投递接口消费。

---

## 19.6 与 core-ai-gateway

P0 无任何依赖。

Identity 不使用 AI 判断身份、权限或登录结果。

未来 AI 可以用于：

```text
安全事件辅助分析
异常登录解释
审计摘要
```

但 AI 不能成为认证和授权的最终决策者。

---

## 19.7 与 core-marketplace

P0 只预留正式扩展点概念，不实现插件加载。

未来可扩展：

```text
IdentityProviderAdapter
MfaProvider
PasswordPolicyProvider
AuditSink
RiskEvaluator
```

P0 禁止插件：

```text
访问 Identity 数据库
替换核心认证流程
任意注入 Spring Bean
```

---

# 二十、四子项目内部交互时序

## 20.1 用户端启动

```text
Identity Web
    │
    │ GET /api/v1/identity/meta
    ▼
Identity Backend
    │
    │ 返回版本、状态、能力
    ▼
Identity Web
    │
    ├── 兼容：进入欢迎页
    ├── 不兼容：升级提示
    └── 不可用：故障页
```

---

## 20.2 管理端启动

```text
Admin Web
    │
    │ GET /admin-api/v1/identity/system/overview
    ▼
Admin Backend
    │
    │ 获取或刷新 Service Token
    ▼
Identity Backend
    │
    │ 返回系统与数据库状态
    ▼
Admin Backend
    │
    │ 聚合自身状态
    ▼
Admin Web
```

---

## 20.3 内部 Token 获取

```text
Admin Backend
    │
    │ client_id + client_secret
    ▼
Identity Backend
    │
    ├── 验证 Secret Hash
    ├── 验证状态
    ├── 验证有效期
    ├── 记录审计
    └── 签发短期 Token
```

---

# 二十一、测试设计

## 21.1 Identity Backend

必须测试：

```text
SQLite 启动
MySQL 启动
Flyway 全新迁移
Flyway 版本升级
内部客户端认证成功
错误 Secret 被拒绝
禁用客户端被拒绝
Token 过期被拒绝
Scope 不足被拒绝
审计写入
Outbox 写入
幂等冲突
```

---

## 21.2 Admin Backend

必须测试：

```text
Identity Backend 正常
Identity Backend 不可用
Identity Backend 超时
内部 Token 获取失败
API 版本不兼容
健康状态聚合
敏感字段脱敏
Request ID 透传
```

---

## 21.3 两个前端

必须测试：

```text
启动成功
后端不可用
请求超时
API 不兼容
错误页面
404
重复点击
加载状态
复制诊断信息
```

---

## 21.4 架构测试

自动检查：

```text
API 层不能依赖 Infrastructure Repository
Application 层不能依赖 Spring Web
Identity Backend 不能依赖 Admin Backend
Admin Backend 不能包含 Identity Entity
Admin Backend 不能配置 Identity 数据源
```

特别建议增加一条构建检查：

> `core-identity-admin-backend` 的依赖中，不允许出现 Identity Backend 的持久化包和数据库迁移文件。

---

# 二十二、CI 流程

每次提交执行：

```text
1. Java 格式检查
2. Java 单元测试
3. 架构边界测试
4. SQLite 集成测试
5. MySQL 集成测试
6. 前端类型检查
7. 前端单元测试
8. OpenAPI 校验
9. OpenAPI 破坏性变更检查
10. 四子项目构建
11. 依赖漏洞扫描
12. 许可证检查
```

主分支额外执行：

```text
完整启动测试
前后端联调测试
数据库升级测试
发行产物生成
```

---

# 二十三、P0 分阶段实施顺序

## P0.1：仓库与构建

完成：

```text
根 Maven
pnpm Workspace
四个子项目
统一命令
统一版本
统一代码规范
```

理由：

先确保工程可以稳定构建，再开始写基础设施。

---

## P0.2：Identity Backend 基础

完成：

```text
三层结构
SQLite
MySQL Profile
Flyway
错误模型
日志
Request ID
健康检查
五张技术表
```

理由：

Identity Backend 是事实来源，必须先稳定。

---

## P0.3：内部通信与 Admin Backend

完成：

```text
内部客户端
短期 Service Token
Internal API
Admin Backend Identity Client
系统状态聚合
```

理由：

先证明 Admin Backend 不访问数据库也可以完成管理编排。

---

## P0.4：两个前端壳层

完成：

```text
路由
Layout
API Client
版本检查
故障状态
系统状态页
诊断交互
```

理由：

前端越早验证契约和错误状态，越能避免后端 API 设计只适合 Swagger、不适合真实产品。

---

## P0.5：质量与发布

完成：

```text
架构测试
契约测试
SQLite/MySQL CI
构建脚本
发行包
部署文档
安全文档
```

理由：

P0 的完成标志不是“本地能跑”，而是“任何开发者克隆后能按文档稳定运行”。

---

# 二十四、P0 验收标准

## 工程

```text
四个子项目目录清晰
根项目一键构建
两个后端独立启动
两个前端独立启动
```

## 边界

```text
Identity Backend 是唯一数据库所有者
Admin Backend 没有 Identity 数据源
Admin Backend 只能通过 Internal API 修改 Identity
Web 不能调用 Admin API
Admin Web 不能直接调用 Identity Backend
```

## 数据库

```text
SQLite 全新启动成功
MySQL 全新启动成功
Flyway 迁移成功
五张技术表完整
无用户、组织、角色业务表
```

## 安全

```text
内部接口必须认证
Secret 不明文入库
无默认管理员密码
生产环境 Admin 开发访问默认关闭
日志不输出敏感值
```

## UX

```text
后端不可用有明确页面
API 不兼容有明确页面
所有错误带 Request ID
管理控制台可以查看系统状态
页面不伪装 P1 能力已经存在
```

## 质量

```text
单元测试通过
集成测试通过
架构测试通过
OpenAPI 校验通过
四个发行产物生成成功
```

---

# 二十五、P0 最重要的注意点

## 1. 不要让 Admin Backend 连接 Identity 数据库

这是整个四子项目架构能否长期成立的决定性约束。

一旦 Admin Backend 可以查表，后续开发者一定会为了方便直接修改表。

---

## 2. 不要在 P0 创建假的用户系统

P0 的意义是稳定地基，而不是做一个表面可演示、后续必须删除的登录系统。

---

## 3. 不要建立第五个公共业务项目

可以有：

```text
contracts/
docs/
scripts/
```

但不要立刻创建：

```text
core-identity-common
core-identity-domain
core-identity-shared
```

否则四个项目很快会通过 Common 重新耦合。

真正需要共享的是契约和工程规范，不是业务实现类。

---

## 4. 不要让两个前端共用一个运行时状态

两个前端可以共享：

```text
设计 Token
基础 UI 包
错误格式
OpenAPI 生成方式
```

但不要共享：

```text
Pinia Store
路由
登录状态
菜单状态
业务页面
```

用户门户和平台管理控制台是两个安全边界。

---

## 5. 不要把健康检查当成管理权限

`/actuator/health` 只能暴露有限信息。

数据库 URL、磁盘路径、环境变量和内部客户端信息不能公开返回。

Admin Backend 可以看到更多诊断信息，但也要进行脱敏。

---

## 6. P0 就要测试 API 版本不兼容

四个子项目可以独立发布，版本错配一定会发生。

不在 P0 建立版本检查，后续用户只会看到莫名其妙的字段缺失和页面报错。

---

# 二十六、P0 最终成果

P0 完成后，`core-identity` 应当呈现为：

```text
core-identity-backend
    已经是唯一数据与规则中心
    但尚未实现用户业务

core-identity-web
    已经具备完整用户门户壳层
    但不伪造登录能力

core-identity-admin-backend
    已经通过内部 API 管理 Identity
    但不访问 Identity 数据库

core-identity-admin-web
    已经具备平台控制台壳层
    可以查看真实运行状态
```

P0 最终回答的不是：

> 用户能不能登录？

而是：

> 当 P1 开始实现用户登录时，代码应该放在哪里、数据归谁所有、管理端如何调用、错误如何展示、服务之间如何认证、以后如何独立发布，这些问题是否已经有唯一答案？
