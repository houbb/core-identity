# Changelog

## 0.10.0 (2026-07-18) — 目录结构优化：前后端分离独立化

### 变更背景

按照 [设计文档 010](design-docs/010-struct-opt.md) 要求，实现前后端完全分离，代码结构保持独立性。

### 变更内容

- **删除根 `pom.xml`**：不再使用 Maven 父 POM 聚合，两个后端模块各自独立
- **删除根 `package.json`**：不再使用 npm workspace，两个前端项目各自独立管理依赖
- **`core-identity-backend`**：去掉 `<parent>` 引用，合并版本管理、插件配置，成为独立的 Spring Boot 项目
- **`core-identity-admin-backend`**：同上，独立化的轻量 BFF 模块
- **`scripts/build-all.bat` / `.sh`**：从根 `mvn` 改为逐个进入模块目录构建
- **`README.md`**：更新架构描述，强调"无父 POM、无 monorepo"

### 目标结构

```
core-identity/
├── core-identity-backend/       # 独立 Maven 项目（Spring Boot）
├── core-identity-web/           # 独立 npm 项目（Vue 3）
├── core-identity-admin-backend/ # 独立 Maven 项目（Spring Boot）
├── core-identity-admin-web/     # 独立 npm 项目（Vue 3）
├── contracts/                   # API 契约
├── scripts/                     # 构建脚本
└── design-docs/                 # 设计文档
```

### 启动方式（不变）

```bash
# 后端
cd core-identity-backend && mvn spring-boot:run     # :8101
cd core-identity-admin-backend && mvn spring-boot:run  # :8102

# 前端
cd core-identity-web && npm install && npm run dev       # :5173
cd core-identity-admin-web && npm install && npm run dev  # :5174
```

---

## 0.9.0 (2026-07-16) — P7 高可用与规模化基础设施

### 核心能力

P7 将 Core Identity 从单实例部署升级为支持多实例、可水平扩展、可容灾的平台级基础设施。坚持关键原则：**社区版 SQLite 单机仍可直接启动，Redis/Kafka/Kubernetes 全部可选，不做强制云原生。**

### P7.0 — 部署模式与数据库抽象

**三种部署模式**
- `standalone`：SQLite + Caffeine 缓存 + 无集群（默认，零配置启动）
- `standard`：MySQL + 可选 Redis + HTTP 事件中继
- `enterprise`：MySQL/PostgreSQL + Redis 缓存 + 全量可观测性

**配置文件**
- `application-standalone.yml`：单机模式（当前默认行为）
- `application-standard.yml`：中小团队生产部署
- `application-enterprise.yml`：大型企业高并发部署
- `application-redis.yml`：Redis 覆盖层（可叠加到任何模式）

**Database Adapter 抽象**
- `DatabaseAdapter` 接口：统一 DataSource / DatabaseType(SQLITE/MYSQL/POSTGRESQL/H2) / 只读副本感知
- `ConsistencyContext`：ThreadLocal 读写一致性控制（`PRIMARY_REQUIRED` / `REPLICA_ALLOWED`）
- `SqliteDatabaseAdapter` / `MySqlDatabaseAdapter` / `H2DatabaseAdapter`：按 `core.database.type` 条件装配
- `CoreIdentityProperties`：`@ConfigurationProperties(prefix="core")` 统一配置类，14 个嵌套属性组，全部带默认值

---

### P7.1 — 无状态多实例

**SessionStore 抽象**
- `SessionStore` 接口：与 `SessionRepository` 分离，代表"分布式会话状态"契约
- `DatabaseSessionStore`：默认实现，直接委托给 `SessionRepository`（零变化）
- `RedisSessionStore`：Redis 热缓存 + DB 回退，Redis 故障不影响登录
- `core.session.store-type=database|redis` 配置选择

**跨节点状态保障**
- `AuthenticationChallenge`、`AuthorizationCode`、`RefreshToken` 全部已 DB 持久化 → 天然支持跨节点
- 不依赖 Sticky Session：任意节点可验证其他节点创建的 Session/Code/Challenge

**集群节点管理**
- `identity_cluster_node` 表（P7 首张新表）：节点 ID、实例 ID、服务类型、版本、状态、心跳
- `ClusterNodeService`：启动注册 → 15 秒心跳 → 关闭排空 → 超时标记 UNAVAILABLE
- 6 种节点状态：HEALTHY / DEGRADED / DRAINING / UNAVAILABLE / INCOMPATIBLE
- `ClusterHealthController`：`GET /api/v1/health` 返回节点状态 + 集群健康节点数 + 数据库类型

---

### P7.2 — 分布式缓存抽象

**CacheManager 接口**
- 统一 `CacheManager` port：`get(key, type)` / `put(key, value, ttl)` / `invalidate(key)` / `invalidateByPattern(pattern)` / `invalidateAll()`
- 与安全原则对齐：从不缓存密码/Secret/私钥，只缓存授权结果和配置元数据

**CaffeineCacheManager**
- 重构为 `CacheManager` 接口实现，保留完整的向后兼容 API
- 旧 API（`get(userId, orgId)` / `put(userId, orgId, roleIds, codes, version)`）全部继续工作
- 前缀模式匹配：`permission:{userId}:{orgId}:{authVersion}`

**CompositeCacheManager**
- 多级缓存：L1 Caffeine（进程内）→ L2 Redis（分布式）→ DB 兜底
- 写入时同步所有层，读取时按优先级回退
- 单层故障不影响其他层

---

### P7.3 — HTTP 事件中继（无 Kafka）

**OutboxRelayService**
- 轮询 `identity_outbox_event` 表 → HTTP POST 到注册的 Webhook 订阅者
- 指数退避重试（1s → 2s → 4s → 8s → 16s），最多 5 次
- DEAD_LETTER 后可通过 API 重放
- 事件模式匹配：支持 `identity.user.*` / `identity.*` / 精确匹配

**事件订阅管理**
- `identity_event_subscription` 表：WEBHOOK 类型，HMAC 签名密钥引用
- `identity_event_delivery_attempt` 表：每次投递独立记录（状态/响应码/错误/下次重试时间）
- `identity_inbox_event` 表：UNIQUE(consumer_name, event_id) 防重复消费

---

### P7.4 — 基于数据库的分布式协调

**Leader Election**
- `LeaderElectionPort` 接口：`tryAcquireLease` / `renewLease` / `releaseLease` / `getCurrentLeader` / `getFencingToken`
- `DatabaseLeaderElection`：使用 `identity_runtime_lease` 表 + 乐观锁 + Fencing Token 实现
- 零外部依赖：不需要 Kubernetes / ZooKeeper / Redis 锁

**分布式任务**
- `identity_distributed_job` 表：job_type + job_key 唯一约束，状态机 PENDING→RUNNING→COMPLETED/FAILED
- `identity_job_execution` 表：每次执行独立记录（节点 ID / 尝试次数 / 心跳 / 指标）
- Fencing Token 保护：旧节点恢复后不能继续执行已转移的任务

---

### P7.5 — 生命周期与可观测性

**GracefulShutdownHandler**
- 优雅关闭序列：标记 DRAINING → 释放所有 Lease → 完成进行中请求 → 关闭
- 与 ClusterNodeService 集成：关闭时自动标记节点状态

**TraceContext**
- MDC 上下文工具：trace_id / request_id / organization_id / client_id
- 加密敏感信息不入 Trace（密码/Token/TOTP/Client Secret/SAML Assertion）

**健康端点**
- `/api/v1/health`：节点状态（nodeId / clusterEnabled / healthyNodes）+ 数据库类型
- 与 Spring Actuator `/actuator/health` 互补

---

### P7.6 — 韧性降级与系统状态

**DegradationManager**
- 5 个可降级组件：Redis / Notification / Billing / Storage / Event Broker
- 组件状态追踪：healthy / failureReason / failureTime / recoveryTime
- 降级原则：不能静默跳过安全校验（Redis 降级不能跳过 Session 验证）

**系统状态 API**
- `SystemStatusController`：`GET /api/v1/system/status`
- 返回：overall(HEALTHY/DEGRADED) + cluster 信息 + dependencies 状态

---

### 数据迁移

**7 张新表（MySQL + SQLite 双迁移）**

| 迁移编号 | 表名 | 用途 |
|---|---|---|
| V0_7_0_001 | `identity_cluster_node` | 集群节点注册与心跳 |
| V0_7_0_010 | `identity_event_subscription` | 事件订阅 Webhook |
| V0_7_0_011 | `identity_event_delivery_attempt` | 事件投递记录 |
| V0_7_0_012 | `identity_inbox_event` | 幂等事件消费 |
| V0_7_0_020 | `identity_runtime_lease` | Leader 选举租约 |
| V0_7_0_021 | `identity_distributed_job` | 分布式任务 |
| V0_7_0_022 | `identity_job_execution` | 任务执行记录 |

---

### 新增文件清单

**Domain 实体 × 5**
`ClusterNode`、`EventSubscription`、`EventDeliveryAttempt`、`InboxEvent`、`DistributedJob`、`RuntimeLease`

**Port 接口 × 9**
`SessionStore`、`ClusterNodeRepository`、`CacheManager`、`EventSubscriptionRepository`、`EventDeliveryAttemptRepository`、`LeaderElectionPort`、`RuntimeLeaseRepository`、`DistributedJobRepository`

**Application Service × 5**
- `ClusterNodeService`：节点注册/心跳/排空/超时检测
- `OutboxRelayService`：事件轮询 → HTTP 投递 → 重试 → 死信
- `DegradationManager`：依赖健康追踪与降级决策

**Infrastructure × 16**
- `database/`：DatabaseAdapter + DatabaseType + ConsistencyContext + SqliteAdapter + MySqlAdapter + H2Adapter（6 个文件）
- `session/`：DatabaseSessionStore + RedisSessionStore
- `cache/`：CompositeCacheManager
- `task/`：DatabaseLeaderElection
- `observability/`：GracefulShutdownHandler + TraceContext

**API Controller × 2**
- `ClusterHealthController`：`GET /api/v1/health`
- `SystemStatusController`：`GET /api/v1/system/status`

**Configuration × 1**
- `CoreIdentityProperties`：14 组嵌套配置属性，全部 `@ConfigurationProperties(prefix="core")`

**YAML Configuration × 4**
- `application-standalone.yml` / `application-standard.yml` / `application-enterprise.yml` / `application-redis.yml`

**Flyway Migration × 14**（7 MySQL + 7 SQLite）
- V0_7_0_001 ~ V0_7_0_022

---

### 明确不做（P8 或未来）

- Kafka / Event Broker 集成（只用 HTTP Outbox Relay）
- Redis Sentinel / Cluster（单机 Redis + DB 回退）
- Kubernetes HPA / PodDisruptionBudget（仅代码预留）
- Blue-Green / Canary 部署（仅 Rolling Update 兼容）
- 多区域 Active-Passive / Active-Active
- 插件 Runtime（Level 3/4）— 仅定义扩展点接口文档
- Marketplace 治理
- 多语言 SDK
- SLO / 错误预算（仅暴露指标给 Prometheus）
- Admin 运维控制台（仅 health/status API）
- PostgreSQL 实现（仅 adapter 接口预留）

---

### 编译状态

- 所有 Java 源文件编译通过（BUILD SUCCESS）
- 向后兼容：P0–P6 已有功能无回归
- SQLite 单机模式零配置启动

---

## 0.8.0 (2026-07-16) — P6 企业治理与合规

### 核心能力

**访问治理 (Access Governance)**
- Access Package（访问套餐）：将相关权益打包为 STANDARD / PRIVILEGED / TEMPORARY / EXTERNAL / EMERGENCY 五种类型
- Access Request（访问申请）：用户通过套餐申请访问权限，含业务理由、工单引用、有效期
- 多级审批流程：支持 SINGLE / ALL / ANY_N / SEQUENTIAL / PARALLEL 五种审批模式
- 审批决定只追加不覆盖（`identity_approval_decision`），防止审批被篡改
- 自动授予 (Grant) 与自动到期

**权限来源追溯**
- 每项权限可追溯至：Built-in Role / SCIM Group / Access Request / Direct Grant / Emergency Grant
- `identity_access_grant` 记录完整的授予链：谁批准、何时生效、何时到期、来源是什么

**特权访问 (Privileged Access / JIT)**
- Eligible / Active 双状态模型：用户长期拥有激活资格，但特权仅在排障时临时激活
- 激活需业务理由 + 工单编号 + 强认证等级（AUTH_LEVEL_2/3）
- 最长激活时长限制（默认 4 小时），超时自动结束
- `identity_privileged_activation` 记录完整激活生命周期

**职责分离 (Separation of Duties)**
- 静态 SoD 策略：定义互斥的 Entitlement 对（如"创建退款" X "批准退款"）
- 自动冲突检测：授予或激活时扫描所有活跃 SoD 策略
- 冲突例外管理：需风险说明 + 补偿性控制 + 审批人 + 有效期
- 例外不能永久无期限存在

**访问审查 (Access Review)**
- Campaign 审查活动：支持 USER_ACCESS / ROLE_MEMBERSHIP / PRIVILEGED_ACCESS 等类型
- 审查决定：CERTIFY / REVOKE / MODIFY / DELEGATE / NOT_SURE
- REVOKE 决定自动撤销对应 Grant
- 审查项按高风险/长期未使用/外部成员等维度分组

**管理员分权 (Platform Operator Role Separation)**
- 从单一 SUPER_ADMIN 拆分为 8 个细分角色：
  - IDENTITY_ADMIN / SECURITY_ADMIN / AUDIT_ADMIN / SUPPORT_ADMIN / PRIVACY_ADMIN / COMPLIANCE_ADMIN / APPLICATION_ADMIN / READ_ONLY_ADMIN
- 审计员默认只读，不能修改被审计对象

**合规与证据 (Compliance & Evidence)**
- 合规控制目录 (Compliance Control Catalog)：Framework → Requirement → Control → Mapping 通用模型
- 控制状态：PLANNED → IMPLEMENTED → OPERATING → INEFFECTIVE → NOT_APPLICABLE
- 控制测试：AUTOMATED / MANUAL / HYBRID 三种方式
- Finding 管理：从检测到整改关闭的完整生命周期
- 审计证据记录：支持 CONFIGURATION / ACCESS_SNAPSHOT / REVIEW_RESULT 等 9 种证据类型
- 证据校验和 (checksum) + 签名 (signature) 防篡改

**隐私与数据生命周期 (Privacy & Data Lifecycle)**
- 隐私请求 (Privacy Request)：ACCESS / EXPORT / ERASURE / RESTRICTION 等 8 种类型
- 请求状态流转：SUBMITTED → IDENTITY_VERIFICATION → IN_PROGRESS → COMPLETED / PARTIALLY_COMPLETED / REJECTED
- 跨 Core 删除编排：Privacy Request → Data Lifecycle Job → 各 Core Task → 聚合完成报告
- 数据保留策略 (Retention Policy)：按数据类别 + 触发事件 + 保留期限 + 到期动作
- 法律保留 (Legal Hold)：诉讼/调查/监管要求，可作用于用户/组织/数据类别/时间范围
- Legal Hold 优先级最高，可阻止删除和匿名化

---

### core-identity-backend

**新增数据库迁移（MySQL + SQLite，双配置）**

| 迁移编号 | 表名 | 用途 |
|---|---|---|
| V0_6_0_003 | `identity_access_package` | 访问套餐定义 |
| V0_6_0_004 | `identity_access_package_entitlement` | 套餐-权益多对多关联 |
| V0_6_0_005 | `identity_access_request` | 访问申请记录 |
| V0_6_0_006 | `identity_approval_instance` | 审批实例 |
| V0_6_0_007 | `identity_approval_step` | 审批步骤 |
| V0_6_0_008 | `identity_approval_decision` | 审批决定（只追加） |
| V0_6_0_010 | `identity_privileged_activation` | 特权激活记录 |
| V0_6_0_020 | `identity_sod_policy` | SoD 策略定义 |
| V0_6_0_021 | `identity_sod_policy_item` | SoD 策略项（互斥对） |
| V0_6_0_022 | `identity_sod_conflict` | SoD 冲突记录 |
| V0_6_0_023 | `identity_sod_exception` | SoD 冲突例外 |
| V0_6_0_030 | `identity_access_review_campaign` | 访问审查活动 |
| V0_6_0_031 | `identity_access_review_item` | 审查项 |
| V0_6_0_032 | `identity_access_review_decision` | 审查决定 |
| V0_6_0_040 | `identity_platform_operator_role` | 平台管理员角色分配 |
| V0_6_0_050 | 合规表 (6 in 1) | `identity_compliance_control` + `_framework` + `_mapping` + `_assessment` + `_finding` + `_evidence` |
| V0_6_0_060 | 隐私表 (6 in 1) | `identity_privacy_request` + `_task` + `_legal_hold` + `_legal_hold_scope` + `_retention_policy` + `_processing_activity` |

**新增 Domain 实体 × 6**
`SodPolicy`、`PrivilegedActivation`（另有 8 个 P6.1 实体此前已部分存在：AccessPackage、AccessPackageEntitlement、AccessRequest、ApprovalInstance、ApprovalStep、ApprovalDecision、Entitlement、AccessGrant）

**新增 Repository 接口 × 16（`application/port/`）**
`AccessPackageRepository`、`AccessPackageEntitlementRepository`、`AccessRequestRepository`、`ApprovalInstanceRepository`、`ApprovalStepRepository`、`ApprovalDecisionRepository`、`PrivilegedActivationRepository`、`SodPolicyRepository`、`SodDataRepository`、`AccessReviewDataRepository`、`PlatformOperatorRoleRepository`、`ComplianceDataRepository`、`PrivacyDataRepository`

**新增 JDBC 实现 × 16（`infrastructure/persistence/`）**
全部遵循 `@Repository` + `JdbcTemplate` 注入 + RowMapper 内类模式

**新增 Application Service × 9**
- `AccessPackageService` / `AccessPackageServiceImpl` — 套餐 CRUD + 权益关联管理
- `AccessRequestService` / `AccessRequestServiceImpl` — 申请提交 + 取消 + 查询
- `ApprovalService` — 审批流程创建 + 多步骤决定 + 状态流转
- `PrivilegedAccessService` / `PrivilegedAccessServiceImpl` — 特权激活 + 时长限制 + 自动过期
- `SodService` — 策略管理 + 冲突检测 + 例外处理
- `AccessReviewService` — Campaign 创建 + 审查项生成 + 决定记录 + 自动整改
- `AdminRoleService` — 管理员角色分配/撤销/查询
- `ComplianceService` — 控制创建 + Framework 导入 + Finding 管理 + 证据记录 + 评估
- `PrivacyService` — 隐私请求提交 + 身份验证 + 审批 + 保留策略 + Legal Hold

**架构约束**
- 所有 `application` 层服务零直接 JdbcTemplate 依赖，全部通过 Repository 接口隔离
- ArchUnit 测试确保 `application` 不依赖 `org.springframework.jdbc`

**Public API 新增**

| 方法 | 路径 | 功能 |
|---|---|---|
| `GET/POST` | `/api/v1/identity/organizations/{orgId}/access-packages` | 套餐列表/创建 |
| `GET/PATCH/DELETE` | `.../access-packages/{id}` | 套餐详情/更新/删除 |
| `PUT` | `.../access-packages/{id}/entitlements` | 设置套餐权益 |
| `GET/POST` | `/api/v1/identity/me/access-requests` | 我的申请列表/提交 |
| `GET` | `/api/v1/identity/me/access-requests/{id}` | 申请详情 |
| `POST` | `/api/v1/identity/me/access-requests/{id}/cancel` | 取消申请 |
| `POST` | `/api/v1/identity/me/approvals/{stepId}/approve` | 批准 |
| `POST` | `/api/v1/identity/me/approvals/{stepId}/reject` | 拒绝 |
| `GET` | `/api/v1/identity/me/eligible-access` | 可激活特权列表 |
| `GET/POST` | `/api/v1/identity/me/privileged-activations` | 特权激活列表/激活 |
| `POST` | `/api/v1/identity/me/privileged-activations/{id}/end` | 结束特权 |
| `GET/POST` | `/api/v1/identity/organizations/{orgId}/sod-policies` | SoD 策略列表/创建 |
| `POST` | `.../sod-policies/{id}/items` | 添加 SoD 策略项 |
| `GET` | `/api/v1/identity/organizations/{orgId}/sod-conflicts` | SoD 冲突列表 |
| `POST` | `.../sod-conflicts/{id}/accept-risk` | 接受风险例外 |
| `POST` | `.../sod-conflicts/{id}/resolve` | 解决冲突 |
| `GET/POST` | `/api/v1/identity/organizations/{orgId}/access-reviews` | 审查活动列表/创建 |
| `POST` | `.../access-reviews/{id}/launch` | 启动审查 |
| `POST` | `.../access-reviews/{id}/close` | 完成审查 |
| `GET` | `.../access-reviews/{id}` | 审查活动详情 |
| `GET` | `.../access-reviews/{id}/items` | 审查项列表 |
| `POST` | `/api/v1/identity/me/access-reviews/{id}/decisions` | 记录审查决定 |

---

### 测试

- **P6GovernanceUnitTest**（30 个测试）：覆盖 9 个服务
  - AccessPackage: 5 测试（创建 + 空名称拒绝 + 按组织查询 + 权益管理 + 删除）
  - AccessRequest: 3 测试（提交 + 取消自己的 + 不能取消别人的）
  - PrivilegedAccess: 4 测试（激活 + 超长拒绝 + 空理由拒绝 + 提前结束）
  - SoD: 2 测试（创建策略 + 添加策略项）
  - AccessReview: 2 测试（创建活动 + 启动生成审查项）
  - AdminRole: 3 测试（分配 + 拒绝无效角色 + 撤销）
  - Compliance: 3 测试（创建控制 + 创建 Finding + 记录证据）
  - Privacy: 5 测试（隐私请求 + 保留策略 + Legal Hold 创建/释放 + 处理活动）
  - Approval: 3 测试（创建流程 + 批准通过 + 拒绝）

- **架构测试**：ArchUnit 验证 `application` 层不依赖 `org.springframework.jdbc`（0 违反）
- **回归测试**：全部 92 个测试通过，P0–P5 已有功能零回归

```
Tests run: 92, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 0.7.0 (2026-07-16) — P5 企业 SSO 与身份联合

### 核心能力

**企业身份联合 (Federation)**
- Federation Connection 统一模型：支持 OIDC 和 SAML 2.0 两种协议
- 7 种连接状态：DRAFT → VALIDATING → ACTIVE → DEGRADED → SUSPENDED → ERROR → DELETED
- 一个组织可配置多个 IdP 连接，按优先级路由
- 连接所有权严格绑定 `organization_id`

**域名验证**
- DNS TXT 记录验证（`_core-identity-verification.{domain}`）
- 域名唯一性约束（一个主域名只能被一个 ACTIVE 组织声明）
- 验证状态：PENDING → VERIFIED / FAILED / EXPIRED / REVOKED / CONFLICT
- SHA-256 挑战值 + 24h 有效期的安全验证流程

**身份发现 (Home Realm Discovery)**
- 邮箱域名自动路由到企业 SSO（已验证域名）
- 多 IdP 场景下展示身份提供商选择
- 组织专属入口 `/sso/{organizationSlug}`
- 防止邮箱枚举的公开信息控制

**OIDC Relying Party**
- 手动 RestTemplate + JJWT 实现（无 Spring Security OAuth2 依赖）
- Authorization Code + PKCE (S256) 完整流程
- ID Token 全量验证：issuer / signature / kid / audience / azp / expiration / nonce / state
- Federation State 内存管理（state / nonce / code_verifier），5 分钟 TTL
- Client Secret AES-256-GCM 加密存储

**SAML 2.0 Service Provider**
- OpenSAML 5.x 直接集成（无 Spring Security SAML2 依赖）
- SP-Initiated SSO（HTTP-Redirect AuthnRequest + HTTP-POST Response）
- 全量安全验证：签名、InResponseTo、Destination、Audience、Recipient、时间窗口、重放防护
- SP Metadata 生成 + IdP Metadata 解析
- 证书状态机管理（PENDING → ACTIVE → RETIRING → EXPIRED → REVOKED）

**External Identity 模型**
- 唯一键：`connection_id + external_subject`（永不使用邮箱作为主键）
- 状态机：ACTIVE / SUSPENDED / UNLINKED / CONFLICT / ORPHANED
- 安全绑定优先级：已有匹配 → 用户主动绑定 → 已验证域名 + email_verified → 账号关联确认
- 严禁通过邮箱自动绑定（防账号接管）

**JIT Provisioning**
- 首次 SSO 登录自动创建 User + Email + Membership + ExternalIdentity（一笔事务）
- JIT 策略：ENABLED / DISABLED / REQUIRE_APPROVAL
- 域名白名单 + email_verified 检查
- 默认角色限制（MEMBER / VIEWER，永不授予 OWNER）
- SCIM 生命周期优先（SCIM 已停用成员不被 JIT 恢复）
- 企业套餐座位检查

**SSO 强制策略**
- 4 级强制模式：OPTIONAL → REQUIRED_FOR_MEMBERS → REQUIRED_FOR_DOMAINS → REQUIRED_FOR_ALL_EXCEPT_BREAK_GLASS
- Break-glass 紧急管理员账号（强 MFA 保护、不受 SSO 强制影响、每次使用产生 CRITICAL 事件）
- 策略发布流程：配置 → 测试 → 预览影响 → 宽限期 → 通知 → 正式强制执行
- IdP 故障不明文回退密码登录

**SCIM 2.0 Service Provider**
- 完整 Users + Groups CRUD（POST / GET / PUT / PATCH / DELETE）
- ServiceProviderConfig / ResourceTypes / Schemas 端点
- SCIM Token 认证（独立 Bearer Token，SHA-256 Hash 存储，只显示一次）
- 双 Token 并行有效支持无缝轮换
- ETag 并发控制（`meta.version` + `If-Match`）
- Group → Role 映射（ADD_ONLY / AUTHORITATIVE 两种模式）
- 角色保护（外部组不能授予 OWNER / SUPER_ADMIN / Break-glass Admin）

**SCIM 停权安全边界**
- `DELETE /Users` 默认不物理删除本地 User，只停用 Membership + 撤销 Session + 撤销 Token
- SCIM 只管理对应企业组织下的 Membership，不影响用户在其他组织的身份

**属性映射**
- 每个属性的所有权：LOCAL / JIT / SCIM / EXTERNAL_ALWAYS
- 属性来源优先级：SCIM > JIT > Local（避免登录 Claim 覆盖 SCIM 权威字段）
- OIDC Claim + SAML Attribute → Core Identity 字段可视化映射

**上游 IdP Token 安全**
- 上游 Token 不传给 Billing、Storage、AI Gateway 等下游服务
- 下游服务只能收到 Core Identity 自己的 Session / Access Token
- 统一 issuer、统一 subject、统一 organization_id、统一撤销机制

---

### core-identity-backend

**新增 22 张数据表**

| 表名 | 用途 |
|---|---|
| `identity_federation_connection` | IdP 连接核心配置 |
| `identity_oidc_connection` | OIDC 协议配置（Client Secret 加密存储） |
| `identity_saml_connection` | SAML 协议配置（Metadata XML 加密存储） |
| `identity_federation_certificate` | IdP/SP 签名和加密证书 |
| `identity_verified_domain` | 企业域名验证 |
| `identity_domain_verification` | DNS TXT 验证挑战记录 |
| `identity_external_identity` | 外部 IdP 身份映射（UNIQUE: connection_id + external_subject） |
| `identity_account_link_request` | 账号关联安全确认流程 |
| `identity_attribute_mapping` | 属性映射配置 |
| `identity_jit_policy` | JIT 自动建号策略 |
| `identity_sso_policy` | 组织 SSO 强制策略 |
| `identity_federated_session` | 上游 IdP Session 关联 |
| `identity_scim_client` | SCIM Token 认证 |
| `identity_scim_resource` | SCIM 资源映射（Users/Groups） |
| `identity_scim_group` | SCIM 外部组 |
| `identity_scim_group_member` | 组成员关系 |
| `identity_scim_group_role_mapping` | 组→角色映射 |
| `identity_provisioning_job` | 同步作业跟踪 |
| `identity_provisioning_log` | 同步操作日志 |

**扩展 3 张表**

| 表名 | 新增字段 |
|---|---|
| `identity_membership` | management_source, managed_by_connection_id, external_resource_id, provisioned_at, deprovisioned_at |
| `identity_session` | authentication_source, federation_connection_id, external_identity_id |
| `identity_user` | primary_identity_source |

**新增 Domain 对象 × 19**
`FederationConnection`, `VerifiedDomain`, `DomainVerification`, `OidcConnection`, `SamlConnection`, `FederationCertificate`, `ExternalIdentity`, `AccountLinkRequest`, `AttributeMapping`, `JitPolicy`, `SsoPolicy`, `FederatedSession`, `ScimClient`, `ScimResource`, `ScimGroup`, `ScimGroupMember`, `ScimGroupRoleMapping`, `ProvisioningJob`, `ProvisioningLog`

**新增 Port 接口 × 19**
全部 P5 实体的 Repository 接口

**新增 Application Service × 7**
- `FederationService` / `FederationServiceImpl`：域名验证、连接管理、身份发现（核心编排层）
- `OidcRelyingPartyService` / `OidcRelyingPartyServiceImpl`：OIDC Auth Code + PKCE 完整流程，JJWT ID Token 验证
- `SamlServiceProviderService` / `SamlServiceProviderServiceImpl`：SAML AuthnRequest 构建 + Response 全量验证（OpenSAML）
- `ExternalIdentityService` / `ExternalIdentityServiceImpl`：外部身份绑定/解绑/冲突解决，安全关联确认
- `JitProvisioningService` / `JitProvisioningServiceImpl`：事务内自动建号+入组织+分配角色
- `SsoPolicyService` / `SsoPolicyServiceImpl`：SSO 强制检查 + Break-glass 管理
- `ScimService` / `ScimServiceImpl`：SCIM 2.0 Users/Groups CRUD + 组角色映射 + 生命周期停权
- `FederationSessionService` / `FederationSessionServiceImpl`：联合会话创建 + 上游关联

**新增 Infrastructure**
- `OidcRelyingPartyServiceImpl.FederationStateEntry`：内存态 PKCE state/nonce/code_verifier 管理（LinkedHashMap LRU + TTL）

**Public API**

| 方法 | 路径 | 功能 |
|---|---|---|
| `POST` | `/api/v1/identity/auth/discovery` | 身份发现（邮箱 → 组织 → IdP） |
| `GET` | `/api/v1/identity/federation/{connectionKey}/login` | 企业 SSO 登录入口 |
| `GET` | `/api/v1/identity/federation/oidc/{connectionKey}/callback` | OIDC 回调 |
| `POST` | `/api/v1/identity/federation/saml/{connectionKey}/acs` | SAML ACS 端点 |
| `GET` | `/sso/{organizationSlug}` | 组织专属 SSO 入口 |
| `POST` | `/api/v1/identity/organizations/{orgId}/domains` | 添加域名 |
| `POST` | `/api/v1/identity/organizations/{orgId}/domains/{id}/verify` | 验证域名 |
| `POST` | `/api/v1/identity/organizations/{orgId}/federation-connections` | 创建连接 |
| `POST` | `/api/v1/identity/organizations/{orgId}/federation-connections/{id}/activate` | 激活连接 |
| `POST` | `/api/v1/identity/organizations/{orgId}/federation-connections/{id}/suspend` | 暂停连接 |
| `GET` | `/api/v1/identity/me/external-identities` | 我的外部身份列表 |
| `DELETE` | `/api/v1/identity/me/external-identities/{id}` | 解绑外部身份 |
| `GET/POST` | `/api/v1/identity/account-link-requests/{id}` | 账号关联确认/拒绝 |
| `GET` | `/scim/v2/ServiceProviderConfig` | SCIM 服务配置 |
| `GET` | `/scim/v2/ResourceTypes` | SCIM 资源类型 |
| `GET` | `/scim/v2/Schemas` | SCIM Schema |
| `POST/GET/PUT/PATCH/DELETE` | `/scim/v2/Users`, `/scim/v2/Users/{id}` | SCIM 用户 CRUD |
| `POST/GET/PATCH/DELETE` | `/scim/v2/Groups`, `/scim/v2/Groups/{id}` | SCIM 组 CRUD |

**GlobalExceptionHandler 新增**
- `FederationException` → 30+ 错误码映射到 HTTP 状态码

**错误码（30+）**
`IDENTITY_DOMAIN_NOT_VERIFIED`, `IDENTITY_DOMAIN_CONFLICT`, `IDENTITY_FEDERATION_CONNECTION_NOT_FOUND`, `IDENTITY_FEDERATION_CONNECTION_NOT_ACTIVE`, `IDENTITY_OIDC_DISCOVERY_FAILED`, `IDENTITY_OIDC_STATE_INVALID`, `IDENTITY_OIDC_ID_TOKEN_INVALID`, `IDENTITY_SAML_RESPONSE_INVALID`, `IDENTITY_SAML_SIGNATURE_INVALID`, `IDENTITY_EXTERNAL_IDENTITY_CONFLICT`, `IDENTITY_ACCOUNT_LINK_REQUIRED`, `IDENTITY_JIT_DISABLED`, `IDENTITY_SSO_REQUIRED`, `IDENTITY_SSO_BREAK_GLASS_REQUIRED`, `IDENTITY_SCIM_UNAUTHORIZED`, `IDENTITY_SCIM_RESOURCE_CONFLICT`, ...

**Flyway 迁移**
- 新增 22 个 MySQL 迁移文件 + 22 个 SQLite 迁移文件（V0_5_0_001 ~ V0_5_0_022）

---

### 测试

- **回归测试**：P0 + P1 + P2 + P3 + P4 + P5 = 62 个测试全部通过（0 失败）

```
Tests run: 62, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 0.6.0 (2026-07-16) — P4 账号安全与零信任基础

### 核心能力

**认证器统一模型**
- 5 种认证器类型：PASSWORD / TOTP / WEBAUTHN / RECOVERY_CODE_SET / EMAIL_RECOVERY
- 5 种状态：PENDING → ACTIVE → SUSPENDED / COMPROMISED / REVOKED
- 3 级认证强度：AUTH_LEVEL_1（密码）→ AUTH_LEVEL_2（密码+TOTP / Passkey）→ AUTH_LEVEL_3（硬件安全密钥）
- 抗钓鱼标记（phishing_resistant）+ 用户验证能力标记（user_verification_capable）
- 认证挑战（AuthenticationChallenge）：登录/注册/升级三种类型，状态机管理
- 升级授权（StepUpGrant）：临时提升认证等级，支持一次性消费和短时复用

**TOTP 多因子认证**
- RFC 6238 标准 TOTP（aerogear-otp-java 实现，SHA1 / 6 位 / 30 秒周期）
- QR Code URI 生成（otpauth:// 格式）+ 手动密钥
- 重放防护（last_accepted_step 记录）
- TOTP Secret AES-256-GCM 加密存储，独立密钥管理
- 密钥版本追踪（支持未来密钥轮换）
- PENDING 注册 → 验证码确认 → ACTIVE 流程

**恢复码**
- 首次启用 MFA 时自动生成 10 个恢复码
- SHA-256 Hash 存储，明文仅展示一次
- 支持重新生成（旧码全部失效）
- 一次性使用，使用后立即标记 USED

**WebAuthn / Passkey**
- 完全自实现（无外部 WebAuthn 库依赖）：
  - COSE_Key 解析器（EC2 P-256 + RSA 公钥提取）
  - WebAuthn 签名验证（SHA256withECDSA / SHA256withRSA）
  - 最小 CBOR 解码器
- 注册流程：create PENDING Authenticator → navigator.credentials.create() → verify attestation + signature → ACTIVE
- 认证流程：generate assertion options → navigator.credentials.get() → verify signature + challenge → 登录成功
- Origin / RP ID 验证
- Sign Count 追踪 + 备份状态记录

**设备上下文**
- Device 实体：Cookie Hash 标识、浏览器/OS 解析、首次/最近见到时间
- 不收集精确 GPS / 字体列表 / Canvas 指纹

**风险引擎**
- 规则化评分引擎（非 ML），完全可解释
- 风险信号：新设备(+20)、IP 变化(+15)、快速 IP 切换(+25)、多次失败(+25)、密码喷洒(+20)、特权账号(+20)、凭证填充(+30)、Refresh Token 重放(+90)
- 4 级风险：LOW → MEDIUM（要求 MFA）→ HIGH（要求抗钓鱼认证）→ CRITICAL（阻止+撤销会话）
- 可配置阈值（medium/high/critical）
- 机器可读原因码：NEW_DEVICE / IP_CHANGED / RAPID_IP_CHANGES / MULTIPLE_FAILURES / PASSWORD_SPRAY_SUSPECTED 等

**安全事件中心**
- SecurityEvent 实体：用户/组织级安全事件记录
- 严重级别：INFO / LOW / MEDIUM / HIGH / CRITICAL
- 生命周期：OPEN → INVESTIGATING → RESOLVED / FALSE_POSITIVE
- CRITICAL 事件自动创建（登录阻止、Refresh Token 重放）

**账号恢复**
- 3 级恢复模型：
  - Level 1：拥有认证器 → 自助恢复
  - Level 2：丢失认证器但有邮箱+密码 → 30 分钟冷静期
  - Level 3：全部丢失 → 管理员审核
- 恢复完成后：security_version+1 → 全部 Session 失效 → Refresh Token 撤销 → 旧认证器暂停 → 强制重设密码

**组织安全策略**
- SecurityPolicy 实体：MFA 要求、抗钓鱼要求、允许的认证器类型、受信设备天数、会话时长
- 策略状态：DRAFT → ENFORCING（14 天宽限期）→ ACTIVE / SUSPENDED
- 发布前预览受影响成员数量
- SecurityExemption：临时豁免，必须有过期时间

**登录攻击防护**
- 多维限流：USER / EMAIL_HASH / IP / DEVICE / CLIENT
- LoginThrottle 持久化记录
- 渐进式延迟而非固定阈值硬锁

**密码安全升级**
- CompromisedPasswordChecker 接口（本地常见密码列表 + 可选 HIBP API）
- 密码泄露检测集成点
- 透明重新哈希（Hash 参数低于安全策略时自动升级）

---

### core-identity-backend

**新增 17 张数据表**

| 表名 | 用途 |
|---|---|
| `identity_authenticator` | 统一认证器注册表 |
| `identity_totp_authenticator` | TOTP Secret（AES-256-GCM 加密） |
| `identity_webauthn_credential` | WebAuthn 公钥凭证 |
| `identity_recovery_code_set` | 恢复码集合 |
| `identity_recovery_code` | 恢复码 Hash |
| `identity_authentication_challenge` | 认证挑战（登录/注册/升级） |
| `identity_step_up_grant` | 升级授权令牌 |
| `identity_device` | 用户设备记录 |
| `identity_risk_assessment` | 风险评估结果 |
| `identity_risk_signal` | 风险信号详情 |
| `identity_security_event` | 安全事件 |
| `identity_security_policy` | 组织安全策略 |
| `identity_security_exemption` | 策略豁免 |
| `identity_account_recovery` | 账号恢复流程 |
| `identity_login_throttle` | 多维登录限流 |

**扩展 3 张表**

| 表名 | 新增字段 |
|---|---|
| `identity_user` | security_version, security_status, risk_level, mfa_enrolled, phishing_resistant_enrolled, recovery_state, last_security_review_at |
| `identity_session` | device_id, authentication_level, authentication_methods_json, strong_auth_at, risk_level, reauth_required_at, security_version, last_risk_evaluated_at |
| `identity_credential` | hash_policy_version, last_rehashed_at, compromised_detected_at |

**P3→P4 数据迁移**
- BACKFILL：现有 PASSWORD credential 自动映射为 PASSWORD Authenticator

**新增 Domain 对象 × 17**
`Authenticator`, `AuthenticationChallenge`, `StepUpGrant`, `TotpAuthenticator`, `RecoveryCodeSet`, `RecoveryCode`, `WebAuthnCredential`, `Device`, `RiskAssessment`, `SecurityEvent`, `AccountRecovery`, `SecurityPolicy`

**新增 Port 接口 × 10**
`AuthenticatorRepository`, `AuthenticationChallengeRepository`, `StepUpGrantRepository`, `TotpAuthenticatorRepository`, `RecoveryCodeSetRepository`, `RecoveryCodeRepository`, `WebAuthnCredentialRepository`, `DeviceRepository`, `CompromisedPasswordChecker`

**新增 Application Service × 8**
- `AuthenticatorService` / `AuthenticatorServiceImpl`：认证器生命周期（createPending → activate → suspend → markCompromised → revoke），有效认证等级计算
- `TotpService` / `TotpServiceImpl`：TOTP 注册/确认/验证/取消，重放防护
- `RecoveryCodeService` / `RecoveryCodeServiceImpl`：恢复码生成/验证/状态查询/重新生成
- `WebAuthnService` / `WebAuthnServiceImpl`：Passkey 注册/认证，自实现密码学
- `RiskEngine`：规则化风险评估，可配置阈值，安全事件自动创建
- `AccountRecoveryService`：分级恢复流程，30 分钟冷静期，完成后全局撤销
- `SecurityPolicyService`：策略 CRUD，DRAFT→ENFORCING(14天宽限期)→ACTIVE

**新增 Infrastructure**
- `TotpSecretEncryptor`：AES-256-GCM 加密/解密 TOTP Secret
- `CoseKeyParser`：COSE_Key（RFC 8152）→ java.security.PublicKey
- `WebAuthnSignatureVerifier`：ECDSA / RS256 签名验证
- `JdbcSecurityEventRepository`：安全事件 JDBC 持久化

**Public API**

| 方法 | 路径 | 功能 |
|---|---|---|
| `GET` | `/api/v1/identity/me/authenticators` | 列出认证器 |
| `DELETE` | `/api/v1/identity/me/authenticators/{id}` | 删除认证器 |
| `POST` | `/api/v1/identity/me/authenticators/{id}/rename` | 重命名 |
| `POST` | `/api/v1/identity/me/authenticators/{id}/report-compromised` | 报告泄露 |
| `POST` | `/api/v1/identity/me/authenticators/totp/enroll` | TOTP 注册 |
| `POST` | `/api/v1/identity/me/authenticators/totp/confirm` | TOTP 确认 |
| `POST` | `/api/v1/identity/me/authenticators/totp/cancel` | TOTP 取消 |
| `POST` | `/api/v1/identity/webauthn/registration/options` | WebAuthn 注册选项 |
| `POST` | `/api/v1/identity/webauthn/registration/verify` | WebAuthn 注册验证 |
| `POST` | `/api/v1/identity/webauthn/authentication/options` | WebAuthn 认证选项 |
| `POST` | `/api/v1/identity/webauthn/authentication/verify` | WebAuthn 认证验证 |
| `POST` | `/api/v1/identity/me/recovery-codes` | 生成恢复码 |
| `GET` | `/api/v1/identity/me/recovery-codes/status` | 恢复码状态 |
| `GET` | `/api/v1/identity/me/security-events` | 安全事件列表 |
| `POST` | `/api/v1/identity/me/security-events/{id}/confirm` | 确认事件 |
| `POST` | `/api/v1/identity/me/security-events/{id}/report` | 报告事件 |
| `POST` | `/api/v1/identity/account-recoveries` | 发起账号恢复 |
| `GET` | `/api/v1/identity/account-recoveries/{id}` | 恢复状态 |
| `POST` | `/api/v1/identity/account-recoveries/{id}/verify` | 恢复验证 |
| `POST` | `/api/v1/identity/account-recoveries/{id}/cancel` | 取消恢复 |
| `POST` | `/api/v1/identity/account-recoveries/{id}/complete` | 完成恢复 |
| `GET` | `/api/v1/identity/organizations/{id}/security-policy` | 查看安全策略 |
| `PUT` | `/api/v1/identity/organizations/{id}/security-policy` | 更新安全策略 |
| `POST` | `/api/v1/identity/organizations/{id}/security-policy/publish` | 发布策略 |
| `POST` | `/api/v1/identity/organizations/{id}/security-policy/suspend` | 暂停策略 |

**Admin API**

| 方法 | 路径 | 功能 |
|---|---|---|
| `GET` | `/admin-api/v1/identity/security/overview` | 安全总览 |
| `GET` | `/admin-api/v1/identity/security/events` | 安全事件列表 |
| `GET` | `/admin-api/v1/identity/security/risky-users` | 风险用户 |
| `GET` | `/admin-api/v1/identity/security/users/{id}` | 用户安全详情 |
| `POST` | `/admin-api/v1/identity/security/users/{id}/lock` | 紧急锁定 |
| `POST` | `/admin-api/v1/identity/security/users/{id}/unlock` | 解锁 |
| `POST` | `/admin-api/v1/identity/security/users/{id}/revoke-sessions` | 撤销所有会话 |
| `POST` | `/admin-api/v1/identity/security/users/{id}/require-password-reset` | 要求重置密码 |
| `POST` | `/admin-api/v1/identity/security/users/{id}/require-mfa-reset` | 要求重置 MFA |
| `GET` | `/admin-api/v1/identity/security/recoveries` | 待处理恢复 |
| `POST` | `/admin-api/v1/identity/security/recoveries/{id}/approve` | 批准恢复 |
| `POST` | `/admin-api/v1/identity/security/recoveries/{id}/reject` | 拒绝恢复 |
| `GET` | `/admin-api/v1/identity/security/authenticator-metrics` | 认证器统计 |
| `GET` | `/admin-api/v1/identity/security/login-metrics` | 登录统计 |

**依赖变更**
- 新增 `org.jboss.aerogear:aerogear-otp-java:1.0.0`（TOTP RFC 6238 实现）

**Flyway 迁移**
- 新增 22 个 MySQL 迁移文件（V0_4_0_100 ~ V0_4_0_161）
- SQLite 迁移并行维护（含合并迁移文件）

---

### 编译状态

- 232 个 Java 源文件全部编译通过（BUILD SUCCESS）

---

### AGENTS.md 更新

- 新增「沟通语言」章节：强制全程中文沟通
- Unknowns Management 重写：外部 Skill 依赖改为内联流程（识别→分类→提问→记录），确保 Skill 不存在时仍可执行
- 触发条件新增「任何 P0-P7 设计文档的实现任务」



### 核心能力

**OAuth 2.0 / OIDC 授权服务器**
- Authorization Code + PKCE (S256) 标准流程
- Client Credentials（Service Account 机器间认证）
- Refresh Token 轮换 + 重放检测（Token Family 全部撤销）
- Access Token（RS256 签名 JWT）+ ID Token
- Token Introspection + Revocation（RFC 7662 / RFC 7009）
- JWKS 端点 + OpenID Connect Discovery
- Scope-Audience 交叉校验

**OAuth Client 管理**
- PUBLIC / CONFIDENTIAL 两种 Client 类型
- PLATFORM / ORGANIZATION / USER 三种所有权
- Client Secret 创建、轮换、撤销（只显示一次）
- Redirect URI 注册

**API Key**
- 用户或 Service Account 创建，绑定组织/Scope/Audience/有效期
- 实时 Introspection（Gateway 验证）+ JWT 交换
- 创建/撤销/轮换

**Service Account**
- 组织级非人身份，通过角色获得权限
- Service Credential（client_id + client_secret）独立管理
- Client Credentials 流程获取短期 Access Token

**授权与同意**
- Authorization Grant 记录用户-Client-组织授权关系
- Grant Scope 关联表
- 用户可查看/撤销已授权应用

---

### core-identity-backend

**新增 22 张数据表**

| 表名 | 用途 |
|---|---|
| `identity_scope` | OAuth Scope 目录，含风险等级、授权文案 |
| `identity_scope_permission` | Scope → Permission 映射 |
| `identity_audience` | 资源服务注册（core-storage 等） |
| `identity_signing_key` | RSA 签名密钥，AES-256-GCM 加密私钥 |
| `identity_token_revocation` | JTI 级紧急撤销 |
| `identity_oauth_client` | OAuth 应用注册 |
| `identity_oauth_client_redirect_uri` | 回调地址精确匹配 |
| `identity_oauth_client_secret` | Client Secret Hash 存储 |
| `identity_oauth_client_scope` | Client 允许的 Scope |
| `identity_oauth_client_audience` | Client 允许的 Audience |
| `identity_authorization_code` | 一次性授权码（PKCE 绑定） |
| `identity_authorization_grant` | 用户-Client 授权记录 |
| `identity_authorization_grant_scope` | Grant → Scope 关联 |
| `identity_refresh_token_family` | Refresh Token 族（轮换+重放检测） |
| `identity_refresh_token` | Refresh Token Hash 存储 |
| `identity_service_account` | 服务账号主体 |
| `identity_service_account_role` | 服务账号角色分配 |
| `identity_service_credential` | 服务账号 OAuth 凭证 |
| `identity_api_key` | API Key（只存 Hash） |
| `identity_api_key_scope` | API Key Scope 关联 |
| `identity_api_key_audience` | API Key Audience 关联 |

**新增 Domain 对象 × 13**
`Scope`, `ScopePermission`, `Audience`, `SigningKey`, `OAuthClient`, `OAuthClientSecret`, `AuthorizationCode`, `AuthorizationGrant`, `AuthorizationGrantScope`, `RefreshToken`, `RefreshTokenFamily`, `ServiceAccount`, `ServiceAccountRole`, `ServiceCredential`, `ApiKey`

**新增 Port 接口 × 16**
全部 P3 实体的 Repository 接口 + `TokenRevocationRepository`

**新增 Application Service × 8**
- `ScopeCatalogService`：Scope/Audience 目录管理，幂等同步
- `SigningKeyManager`：RSA 2048 密钥生命周期（PENDING→ACTIVE→RETIRING→RETIRED→REVOKED）
- `OAuthTokenService`：JWT 签发/验证（RS256），JWKS 构建
- `OAuthClientService`：Client 注册/查询/暂停/Secret 轮换
- `OAuthAuthorizationService`：Authorize/Token/Introspect/Revoke 完整 OAuth 流程
- `ApiKeyService`：API Key 创建/撤销/Introspection/交换 JWT
- `ServiceAccountService`：Service Account 创建/暂停/查询

**Public API**

| 方法 | 路径 | 功能 |
|---|---|---|
| `GET` | `/.well-known/openid-configuration` | OIDC Discovery |
| `GET` | `/.well-known/jwks.json` | JWKS 公钥 |
| `GET` | `/oauth2/authorize` | OAuth 授权端点（PKCE） |
| `POST` | `/oauth2/token` | Token 端点（authorization_code / refresh_token / client_credentials） |
| `POST` | `/oauth2/introspect` | Token 内省 |
| `POST` | `/oauth2/revoke` | Token 撤销 |
| `GET` | `/userinfo` | OIDC UserInfo |
| `GET` | `/api/v1/identity/scopes` | Scope 目录 |
| `GET/POST` | `/api/v1/identity/developer/clients` | 开发者 Client 管理 |
| `POST` | `/api/v1/identity/developer/clients/{id}/secrets` | Client Secret 轮换 |
| `POST` | `/api/v1/identity/developer/clients/{id}/suspend` | 暂停 Client |
| `GET/POST/DELETE` | `/api/v1/identity/developer/api-keys` | API Key 管理 |
| `POST` | `/api/v1/identity/developer/service-accounts` | 创建 Service Account |
| `GET` | `/api/v1/identity/developer/grants` | 用户授权列表 |
| `POST` | `/api/v1/identity/developer/grants/{id}/revoke` | 撤销授权 |

**Internal API**

| 方法 | 路径 | 功能 |
|---|---|---|
| `PUT` | `/internal/v1/identity/scope-sources/{service}` | 其他 Core 同步 Scope 清单 |
| `PUT` | `/internal/v1/identity/scope-sources/{code}/permissions` | 同步 Scope-Permission 映射 |
| `POST` | `/internal/v1/identity/api-keys/introspect` | API Key 内省（Gateway 用） |
| `POST` | `/internal/v1/identity/tokens/introspect` | JWT Token 内省 |

**安全与基础设施**
- Signing Key 自动初始化（`SigningKeyBootstrapRunner`）
- 私钥 AES-256-GCM 加密存储，主密钥通过环境变量注入
- OAuth 异常统一返回 RFC 6749 错误格式
- Session 正确验证（Hash 查库）替代错误的 Cookie 截取
- Scope-Audience 交叉校验防止跨服务 Token 滥用

**Flyway 迁移**
- 新增 22 个 MySQL 迁移文件 + 22 个 SQLite 迁移文件

---

### 测试

- **P3PlatformUnitTest**（19 个测试）：
  - Scope Catalog：幂等同步、按服务筛选
  - Signing Key & JWT：创建/激活、签发/验证、JWKS 输出
  - OAuth Client：创建+Secret、Secret 校验、Secret 轮换
  - Authorization Code + PKCE：生成/校验、非活跃 Client 拒绝
  - Refresh Token：轮换流程、重放检测
  - Service Account：创建、组织范围查询
  - API Key：创建/交换 JWT、撤销、用户范围查询
  - Token Introspection：活跃/失效

- **回归测试**：P0 + P1 + P2 + P3 = 62 个测试 + Admin Backend 4 个测试 = 66 个测试全部通过

```
Tests run: 66, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 0.4.0 (2026-07-15) — P2 组织与权限体系

### 核心能力

**组织与 RBAC**
- 团队组织（TEAM）创建、Slug 自动生成、组织切换器
- 统一权限目录：`{domain}.{resource}.{action}` 命名，Caffeine 本地缓存
- 4 级风险标签（LOW/MEDIUM/HIGH/CRITICAL）
- 内置角色：OWNER → ADMIN → MEMBER → VIEWER，系统保护
- 自定义角色：创建、编辑、权限分配、删除（使用中保护）
- 成员 → 角色 → 权限 授权链，取并集计算
- 组织所有权独立标识（`owner_user_id`），所有权转移密码验证

**成员生命周期**
- 邮箱邀请/接受/拒绝/撤销/重发，7 天有效期
- Token SHA-256 Hash 存储，一次使用
- 成员加入来源追踪（OWNER_CREATED / INVITATION）
- 成员 SUSPEND/REACTIVATE/REMOVE/LEAVE
- 所有者保护（不可移除/不可退出/不可暂停）
- 最少角色规则（至少保留一个角色）

**授权决策**
- `AuthorizationService.check(userId, orgId, permCode)` → ALLOW / DENY_*
- 组织冻结后读允许、写拒绝
- Caffeine 进程内缓存（5min TTL，按 authorization_version 失效）
- 权限快照 API 供前端菜单渲染

**P1→P2 平滑升级**
- `membership_type` → `MembershipRole` 立即切换（不双读）
- 旧 PERSONAL 组织自动创建内置角色并绑定

---

### core-identity-backend

**新增 6 张数据表**

| 表名 | 用途 |
|---|---|
| `identity_permission` | 统一权限目录，UNIQUE(permission_code) |
| `identity_permission_source` | 服务权限清单同步记录 |
| `identity_role` | 组织角色，BUILT_IN/CUSTOM，system_protected |
| `identity_role_permission` | 角色-权限关联 |
| `identity_membership_role` | 成员-角色关联 |
| `identity_invitation` | 成员邀请，SHA-256 Token Hash |

**扩展 3 张表**

| 表名 | 新增字段 |
|---|---|
| `identity_organization` | owner_user_id, description, suspended_at/reason, deletion_requested_at/effective_at, logo_object_id, authorization_version |
| `identity_membership` | source, left_at/removed_at/suspended_at, last_accessed_at, created_by |
| `identity_session` | last_organization_id, permission_version |

**新增 Domain 对象 × 7**
`Permission`, `PermissionSource`, `Role`, `RolePermission`, `MembershipRole`, `Invitation`

**新增 Port 接口 × 8**
`PermissionRepository`, `PermissionSourceRepository`, `RoleRepository`, `RolePermissionRepository`, `MembershipRoleRepository`, `InvitationRepository`

**新增 Application Service × 6**
- `PermissionCatalogService`：幂等同步权限清单，校验 checksum 跳过未变化
- `RoleService`：CRUD 角色，权限分配，内置角色初始化
- `OrganizationService`：创建 TEAM 组织，slug 生成，所有权转移，冻结/恢复/解散
- `MembershipService`：成员列表、角色调整、暂停/恢复/移除/退出
- `InvitationService`：邀请创建/接受/拒绝/撤销/重发
- `AuthorizationService`：权限检查、require、有效权限计算、快照相片

**Public API**

| 方法 | 路径 | 功能 |
|---|---|---|
| `GET` | `/api/v1/identity/permissions` | 权限目录（按 service/resource/riskLevel 筛选） |
| `GET` | `/api/v1/identity/me/organizations` | 我的组织列表 |
| `POST` | `/api/v1/identity/me/current-organization` | 切换组织上下文 |
| `GET` | `/api/v1/identity/me/organizations/{orgId}/permissions` | 权限快照 |
| `POST` | `/api/v1/identity/organizations` | 创建 TEAM 组织 |
| `GET/PATCH` | `/api/v1/identity/organizations/{orgId}` | 组织详情/更新 |
| `POST` | `.../transfer-ownership` | 转移所有权 |
| `POST` | `.../request-deletion` / `.../cancel-deletion` | 解散/取消 |
| `GET/POST` | `/api/v1/identity/organizations/{orgId}/roles` | 角色列表/创建 |
| `GET/PATCH/DELETE` | `.../roles/{roleId}` | 角色详情/更新/删除 |
| `PUT` | `.../roles/{roleId}/permissions` | 分配权限 |
| `GET` | `/api/v1/identity/organizations/{orgId}/members` | 成员列表 |
| `PUT` | `.../members/{id}/roles` | 调整成员角色 |
| `DELETE` | `.../members/{id}` | 移除成员 |
| `POST` | `.../leave` | 退出组织 |
| `POST/GET` | `/api/v1/identity/organizations/{orgId}/invitations` | 邀请管理 |
| `GET` | `/api/v1/identity/invitations/resolve?token=` | 解析邀请 |
| `POST` | `/api/v1/identity/invitations/accept` / `/decline` | 接受/拒绝 |

**Internal API**

| 方法 | 路径 | 功能 |
|---|---|---|
| `PUT` | `/internal/v1/identity/permission-sources/{service}` | 其他 Core 同步权限清单 |
| `POST` | `/internal/v1/identity/authorization/check` | 内部授权检查 |

**基础设施**
- `CaffeineCacheManager`：TTL 5min，max 10000，按 userId+orgId 做 key
- `TokenUtils`：提取 `generateRandomToken()` / `hashToken()` / `hashEmail()` 复用
- `PermissionBootstrapRunner`：启动时自注册 Identity 权限清单（YAML）
- 权限清单：`permissions/identity-permissions.yml`（19 个权限码）

**Flyway 迁移**
- 新增 14 个迁移文件（SQLite + MySQL 各 14 个，含 ALTER TABLE 扩展 + CREATE TABLE 新建 + 数据迁移）

---

### core-identity-web

**新增路由（13个）**

| 路由 | 页面 |
|---|---|
| `/organizations/new` | 创建团队组织 |
| `/organizations/:organizationId` | 组织首页 |
| `/organizations/:organizationId/settings` | 组织设置 |
| `/organizations/:organizationId/members` | 成员管理 |
| `/organizations/:organizationId/invitations` | 邀请记录 |
| `/organizations/:organizationId/invite` | 邀请成员 |
| `/organizations/:organizationId/roles` | 角色管理 |
| `/organizations/:organizationId/roles/:roleId` | 编辑角色权限 |
| `/organizations/:organizationId/danger` | 危险区域（转移所有权/解散） |
| `/invitations/:token` | 接受邀请 |
| `/personal` | 个人空间 |

**新增组件（3个）**
- `AppLayout.vue`：左侧侧边栏布局 + 组织导航 + 权限菜单
- `OrganizationSwitcher.vue`：组织切换下拉（最近使用→团队→个人空间）

**新增 Store**
- `organizationStore`：组织列表、切换、权限快照、`hasPermission()` 方法

**路由守卫**
- `router.beforeEach`：全局认证检查，未登录 → `/login`

**新增 API 模块**
`organizations.ts`：组织/角色/权限/成员/邀请的全部 API 封装

---

### 测试

- **P2PermissionUnitTest**（9 个测试）：权限同步幂等、checksum 跳过、废弃/恢复、名称更新、按服务/风险等级/关键词筛选
- **回归测试**：P0 + P1 全部 34 个测试 + P2 9 个测试 = 43 个测试全部通过

```
Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 0.3.0 (2026-07-15) — P1 Identity MVP

### 核心能力

**技术基线**
- 数据库：SQLite（开发/测试，持久化存储），MySQL 迁移脚本并行维护
- 密码安全：BCrypt（spring-security-crypto），最少 8 位，无复杂度要求
- 通知：Console 开发适配器（验证链接输出到 stdout），预留 core-notification 接口
- 会话：空闲 2 小时自动过期 / 绝对 24 小时强制过期，Opaque Session Token
- 安全：手动 CSRF Filter + 进程内限流 Filter，不引入 Spring Security 框架
- 前端：Vue 3 + Pinia + Vue Router，复用 Apple UI Design Token 体系

---

### core-identity-backend

**新增 9 张数据表（SQLite + MySQL Flyway 迁移）**

| 表名 | 用途 |
|---|---|
| `identity_user` | 用户主体，含状态、锁定期、禁用信息 |
| `identity_user_email` | 邮箱，UNIQUE 约束 + 标准化存储 |
| `identity_credential` | BCrypt 密码凭证，失败计数 + 强制修改标记 |
| `identity_organization` | 资源归属容器，PERSONAL / TEAM |
| `identity_membership` | 用户-组织关系，OWNER / MEMBER |
| `identity_session` | 登录会话，USER_WEB / ADMIN_WEB，idle/absolute 双过期 |
| `identity_one_time_token` | 一次性 Token：邮箱验证、密码重置、账户设置 |
| `identity_login_attempt` | 登录审记：成功/失败/锁定，支持 IP + 用户级查询 |
| `identity_platform_operator` | 平台管理员标记，SUPER_ADMIN，独立生命周期 |

**新增 Domain 对象 × 9**
`User`、`UserEmail`、`Credential`、`Organization`、`Membership`、`Session`、`OneTimeToken`、`LoginAttempt`、`PlatformOperator`

**新增 Port 接口**
- `PasswordHasher`：BCrypt 实现（`BCryptPasswordHasher`）
- `IdentityNotificationPort`：Console 适配器（`ConsoleNotificationAdapter`）
- 9 张表的 Repository 接口 + JDBC 实现

**新增 Application Service**
- `AuthService` / `AuthServiceImpl` — 核心认证逻辑：
  - 注册（事务保护：User + Email + Credential + Org + Membership + Token + Audit + Outbox）
  - 邮箱验证（一次性 Token、过期/已用/撤回状态检查）
  - 重新发送验证邮件（作废旧 Token → 新建）
  - 密码登录（标准化邮箱 → IP/用户限流 → 凭证验证 → 失败计数 → 自动锁定）
  - 安全退出（Cookie 清除 + Session Revoke）
  - 修改密码（当前密码验证 → 新旧不同检查 → 撤销其他会话 → 通知）
  - 忘记密码 / 密码重置（Token 模式，重置后全会话失效）

**Public API**

| 方法 | 路径 | 功能 |
|---|---|---|
| `POST` | `/api/v1/identity/auth/register` | 注册（支持 Idempotency-Key） |
| `POST` | `/api/v1/identity/auth/email-verifications` | 重新发送验证邮件 |
| `POST` | `/api/v1/identity/auth/email-verifications/confirm` | 确认邮箱验证 |
| `POST` | `/api/v1/identity/auth/login` | 邮箱+密码登录（设置 Session Cookie） |
| `POST` | `/api/v1/identity/auth/logout` | 退出（清除 Cookie） |
| `GET` | `/api/v1/identity/auth/session` | 检查会话有效性 |
| `POST` | `/api/v1/identity/auth/password-resets` | 发起密码重置 |
| `POST` | `/api/v1/identity/auth/password-resets/confirm` | 完成密码重置 |
| `GET` | `/api/v1/identity/me` | 当前用户信息（含邮箱、组织） |
| `PATCH` | `/api/v1/identity/me` | 修改个人资料 |
| `POST` | `/api/v1/identity/me/password` | 修改密码 |
| `GET` | `/api/v1/identity/me/sessions` | 活跃会话列表 |
| `DELETE` | `/api/v1/identity/me/sessions/{id}` | 撤销单个会话 |
| `DELETE` | `/api/v1/identity/me/sessions` | 撤销所有其他会话 |

**Internal API（供 Admin Backend BFF 调用）**

| 方法 | 路径 | 功能 |
|---|---|---|
| `POST` | `/internal/v1/identity/admin-auth/login` | 管理员登录（验证 PlatformOperator） |
| `POST` | `/internal/v1/identity/admin-auth/introspect` | 管理员会话验证 |
| `POST` | `/internal/v1/identity/admin-auth/logout` | 管理员退出 |
| `GET` | `/internal/v1/identity/users` | 用户列表（分页+筛选） |
| `POST` | `/internal/v1/identity/users` | 管理员创建用户 |
| `GET` | `/internal/v1/identity/users/{id}` | 用户详情 |
| `POST` | `/internal/v1/identity/users/{id}/disable` | 禁用用户 |
| `POST` | `/internal/v1/identity/users/{id}/enable` | 启用户 |
| `POST` | `/internal/v1/identity/users/{id}/revoke-sessions` | 撤销所有会话 |
| `POST` | `/internal/v1/identity/users/{id}/resend-verification` | 重新发送验证 |
| `POST` | `/internal/v1/identity/users/{id}/send-password-reset` | 发起密码重置 |
| `GET` | `/internal/v1/identity/users/{id}/login-attempts` | 登录记录查询 |

**安全加固**
- `CsrfFilter`：X-CSRF-TOKEN Header 验证（读操作豁免）
- `RateLimitFilter`：进程内 IP 级限流（20次/分钟窗口，仅登录/注册端点）
- 登录保护：用户连续失败 5 次 → 15 分钟临时锁定（`LOCKED` 状态）
- 错误统一映射：内部 `EMAIL_NOT_FOUND` / `PASSWORD_MISMATCH` 统一对外为 `IDENTITY_INVALID_CREDENTIALS`
- 敏感数据脱敏：Token/Session 仅保存 SHA-256 Hash，日志不输出密码/Token
- 密码必须 Hash 保存，管理员不能查看或设置用户长期密码
- 邮件标准化：`lowercase + trim`

**Outbox 事件（10 个）**
`identity.user.registered`、`identity.user.email_verified`、`identity.user.activated`、`identity.user.disabled`、`identity.user.enabled`、`identity.organization.created`、`identity.membership.created`、`identity.session.created`、`identity.session.revoked`、`identity.password.changed`、`identity.password.reset`

---

### core-identity-web

**新增页面（10 个）**

| 路由 | 页面 | 功能 |
|---|---|---|
| `/register` | 注册页 | 显示名称+邮箱+密码+确认密码+同意条款，密码实时反馈+Caps Lock提示 |
| `/check-email` | 邮箱待验证页 | 脱敏邮箱显示+重新发送（60s冷却） |
| `/verify-email` | 验证结果页 | 成功→跳转登录 / 失败→显示原因 |
| `/login` | 登录页 | 邮箱+密码，失败保留邮箱清空密码 |
| `/forgot-password` | 忘记密码页 | 输入邮箱→静默发送（防枚举） |
| `/reset-password` | 重置密码页 | Token+邮箱+新密码+确认密码 |
| `/account` | 账户首页 | 状态卡片+快捷入口（资料/安全/会话/退出） |
| `/account/profile` | 个人资料页 | 编辑显示名称 |
| `/account/security` | 安全设置页 | 修改密码（当前密码+新密码+确认） |
| `/account/sessions` | 会话管理页 | 设备列表+单个/全部撤销+当前设备标记 |

**新增 Store**
- `authStore`：`checkSession`、`login`、`register`、`logout`、`verifyEmail`、`requestPasswordReset`、`completePasswordReset`

**路由守卫**
- 未登录访问 `/account/*` → 重定向 `/login`
- 已登录访问 `/login` → 重定向 `/account`

---

### core-identity-admin-backend

**Admin BFF 代理层**

| 方法 | 路径 | 功能 |
|---|---|---|
| `POST` | `/admin-api/v1/identity/auth/login` | 管理员登录代理（设置 Admin Cookie） |
| `POST` | `/admin-api/v1/identity/auth/logout` | 管理员退出 |
| `GET` | `/admin-api/v1/identity/auth/me` | 当前管理员信息 |
| `GET` | `/admin-api/v1/identity/users` | 用户列表 |
| `POST` | `/admin-api/v1/identity/users` | 创建用户 |
| `GET` | `/admin-api/v1/identity/users/{id}` | 用户详情 |
| `POST` | `/admin-api/v1/identity/users/{id}/disable` | 禁用用户 |
| `POST` | `/admin-api/v1/identity/users/{id}/enable` | 启用户 |
| `POST` | `/admin-api/v1/identity/users/{id}/revoke-sessions` | 撤销会话 |
| `POST` | `/admin-api/v1/identity/users/{id}/resend-verification` | 重新发送验证 |
| `POST` | `/admin-api/v1/identity/users/{id}/send-password-reset` | 发送密码重置 |
| `GET` | `/admin-api/v1/identity/users/{id}/login-attempts` | 登录记录 |

**IdentityInternalClient 接口扩展**：新增 admin-auth + user 管理的全部代理方法。
**边界保障**：Admin Backend 零 JDBC 依赖，全部通过 Internal API（Bearer Token 鉴权）调用 Identity Backend。

---

### core-identity-admin-web

**新增页面（3 个）**

| 路由 | 页面 | 功能 |
|---|---|---|
| `/admin/login` | 管理员登录页 | 邮箱+密码，独立 Cookie `core_identity_admin_session` |
| `/admin/users` | 用户列表 | 搜索+统计卡片+侧边抽屉创建用户 |
| `/admin/users/:id` | 用户详情 | 基本信息+账号状态+管理操作（禁用/启用/撤销会话/重发验证/密码重置） |

**新增 Store**
- `adminAuthStore`：管理员登录/登出/会话验证
- `userManagementStore`：用户 CRUD + 状态管理

**路由守卫**
- 无 Admin Session → `/admin/login`
- 非 PlatformOperator → 返回 403

---

### 脚本与工具

- `scripts/bootstrap-admin.bat`：Windows 管理员初始化脚本
- `scripts/bootstrap-admin.sh`：Linux/Mac 管理员初始化脚本

---

### 测试

- **P1IdentityUnitTest**（8 个断言测试）：
  - 正常注册（User + Email + Credential + Org + Membership 全创建）
  - 重复邮箱拒册
  - 邮箱验证成功（Token 校验 + 用户激活）
  - 无效 Token 拒绝
  - 正确凭据登录
  - 错误密码失败
  - 未验证用户登录拒绝
  - BCrypt Hash 正确性验证

- **回归测试**：P0 全部 30 个测试通过，ArchUnit 架构边界保持
- **Admin Backend 测试**：4 个 stub 测试全部通过

```
Tests run: 38, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 0.1.0 (2026-07-15) — P0 Engineering Foundation

- **core-identity-backend**: Identity core service with 5 technical tables
  - Flyway migrations (SQLite + MySQL)
  - Three-layer architecture (api/application/infrastructure)
  - Public API: meta, capabilities
  - Internal API: service tokens, system info, health, audit events
  - Service-to-service authentication (JWT-based internal tokens)
  - Request ID filter, unified error model (RFC 7807)
  - Audit foundation, outbox event pattern, idempotency records

- **core-identity-admin-backend**: Management BFF
  - IdentityInternalClient for calling Identity Backend
  - Service token caching and refresh
  - Admin API: bootstrap, overview, health, version, contracts
  - Development access protection (localhost-only in production)
  - Request ID pass-through

- **core-identity-web**: User self-service portal (Vue 3 + Vite)
  - Welcome page with version check
  - System unavailable page with diagnostics
  - Version incompatibility page
  - 404 page
  - Placeholder login/account pages

- **core-identity-admin-web**: Admin console (Vue 3 + Vite, dark theme)
  - System overview dashboard with status cards
  - Service health page
  - Contract compatibility page
  - Sidebar layout with navigation
  - Detail drawer for service diagnostics

- **Contracts**: OpenAPI 3.0 YAML specifications
  - Public API, Internal API, Admin API, Events, Error Codes

- **Build**: Maven parent POM, npm scripts, build-all scripts (Windows + Linux)

- **Tests**: Unit tests for all services, architecture boundary tests (ArchUnit)