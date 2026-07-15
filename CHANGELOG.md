# Changelog

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