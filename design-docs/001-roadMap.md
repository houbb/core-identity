# Core Identity Roadmap

版本：v1.0
目标：从可独立运行的身份认证 MVP，逐步演化为企业级身份与访问管理平台。

---

# 一、项目定位

`core-identity` 负责整个 Core Platform 的统一身份基础设施：

```text
用户 User
组织 Organization
成员 Membership
认证 Authentication
授权 Authorization
角色 Role
权限 Permission
会话 Session
服务身份 Service Identity
单点登录 SSO
身份联合 Federation
安全策略 Security Policy
审计 Audit
```

它不负责：

```text
支付与订阅
业务用户画像
文件存储
通知发送
AI 额度
具体业务权限逻辑
```

其他 Core 服务不得重新建立：

```text
user
organization
role
permission
session
credential
```

等身份基础模型。

---

# 二、四子项目结构

```text
core-identity/
├── core-identity-backend/
├── core-identity-web/
├── core-identity-admin-backend/
├── core-identity-admin-web/
│
├── docs/
├── scripts/
├── pom.xml
├── pnpm-workspace.yaml
├── README.md
└── LICENSE
```

## 1. core-identity-backend

身份核心服务，是整个 Identity 的事实来源。

负责：

```text
用户
组织
成员
角色
权限
凭证
认证
Token
会话
MFA
SSO
身份联合
审计
安全策略
```

它是唯一可以读写 Identity 数据库的服务。

---

## 2. core-identity-web

用户侧前端。

面向：

```text
普通用户
组织成员
组织管理员
开发者
```

负责：

```text
登录
注册
找回密码
个人中心
安全设置
会话管理
组织切换
组织成员管理
邀请成员
组织角色配置
API Key
授权应用
```

组织管理员仍然属于租户内部用户，不属于平台管理员。

---

## 3. core-identity-admin-backend

平台管理控制台后端。

它不是第二套 Identity 服务，而是管理 BFF。

负责：

```text
平台管理员鉴权
管理接口聚合
跨组织查询
管理操作编排
高危操作二次确认
管理操作审计
导出任务
批量任务
敏感字段脱敏
后台统计聚合
```

它通过内部 API 调用：

```text
core-identity-backend
```

禁止直接访问 Identity 数据库。

---

## 4. core-identity-admin-web

平台管理控制台前端。

面向：

```text
平台超级管理员
安全管理员
客服人员
审计人员
运营管理员
```

负责：

```text
全平台用户管理
组织管理
角色与权限模板
安全事件
登录记录
审计日志
SSO 配置
身份提供商配置
系统安全策略
批量导入导出
运行状态
```

---

# 三、调用关系

```text
普通用户
   │
   ▼
core-identity-web
   │
   ▼
core-identity-backend
   │
   ▼
Identity Database
```

```text
平台管理员
   │
   ▼
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

核心规则：

```text
core-identity-web
    不能调用 admin-backend

core-identity-admin-web
    不能直接调用 identity-backend 内部接口

core-identity-admin-backend
    不能直接访问 Identity 数据库

core-identity-backend
    是唯一身份数据所有者
```

这样既保证四个子项目独立，又不会产生两套身份领域逻辑。

---

# 四、部署形态

开发阶段：

```text
core-identity-backend       :8101
core-identity-web           :5173

core-identity-admin-backend :8102
core-identity-admin-web     :5174
```

生产阶段建议构建为两个运行单元：

```text
core-identity.jar
```

包含：

```text
core-identity-backend
core-identity-web 构建产物
```

以及：

```text
core-identity-admin.jar
```

包含：

```text
core-identity-admin-backend
core-identity-admin-web 构建产物
```

因此源码是四个子项目，但部署只需要两个 Java 进程。

---

# 五、Roadmap 总览

```text
P0  工程与边界基础
P1  Identity MVP
P2  组织与权限体系
P3  平台级身份服务
P4  账号安全与零信任基础
P5  企业 SSO 与身份联合
P6  企业治理与合规
P7  高可用、规模化与生态扩展
```

每一阶段都必须形成可独立发布的稳定版本。

---

# P0：工程与边界基础

## 目标

先确定四个子项目的职责、依赖和构建规则，避免功能增加以后重新拆分。

## core-identity-backend

实现：

```text
Spring Boot 基础工程
SQLite 数据源
Flyway
统一错误模型
统一日志
Request ID
OpenAPI
健康检查
配置校验
基础安全配置
审计基础接口
```

后端只保留三层：

```text
api
application
infrastructure
```

结构：

```text
core-identity-backend/
└── src/main/java/io/coreplatform/identity/
    ├── api/
    ├── application/
    └── infrastructure/
```

---

## core-identity-web

实现：

```text
Vue3
TypeScript
Vite
Pinia
Vue Router
Axios
基础设计系统
公开页面 Layout
账户页面 Layout
路由权限框架
统一错误处理
```

暂时只创建页面骨架：

```text
/login
/register
/account
```

---

## core-identity-admin-backend

实现：

```text
Spring Boot 基础工程
管理员 Token 校验
Identity 内部客户端
管理操作上下文
操作人传递
管理审计基础能力
统一异常
健康检查
```

管理员调用 Identity 时必须传递：

```text
operator_id
operator_role
request_id
operation_reason
```

---

## core-identity-admin-web

实现：

```text
管理控制台 Layout
登录失效处理
菜单权限
按钮权限
表格与表单基础组件
危险操作确认框架
```

页面骨架：

```text
/admin
/admin/users
/admin/organizations
/admin/audit
```

---

## P0 完成标准

```text
四个子项目可以独立开发
根目录可以统一构建
两个后端可以独立启动
两个前端可以独立启动
前端可以打包进入对应后端
Admin Backend 不访问 Identity 数据库
API 与内部 API 边界明确
```

## 为什么先做

四子项目架构最大的风险不是代码多，而是职责混乱。

P0 不解决边界，后面很容易出现：

```text
两个 UserService
两套权限判断
两套用户 DTO
两个管理员登录逻辑
后台直接改数据库
```

---

# P1：Identity MVP

## 目标

提供一个真正可以被其他产品使用的最小身份服务。

MVP 必须完成：

```text
注册
登录
退出
找回密码
基础用户管理
基础组织模型
基础审计
```

不追求企业 SSO、复杂 RBAC 和高级安全策略。

---

## core-identity-backend

### 用户能力

```text
用户注册
邮箱与密码登录
退出登录
刷新会话
当前用户查询
修改基本资料
修改密码
忘记密码
重置密码
账号状态
```

账号状态：

```text
PENDING
ACTIVE
LOCKED
DISABLED
DELETED
```

### 最小组织模型

MVP 就应建立：

```text
Organization
Membership
```

用户注册时自动创建个人组织：

```text
Personal Workspace
```

同时创建成员关系：

```text
OWNER
```

这样后面不会被迫把“单用户系统”重新迁移为“多组织系统”。

### 会话

浏览器默认使用：

```text
HttpOnly Cookie
Secure
SameSite
CSRF 防护
```

服务端记录：

```text
session_id
user_id
created_at
expires_at
last_active_at
ip
user_agent
revoked_at
```

### 基础审计

记录：

```text
注册
登录成功
登录失败
退出
密码修改
密码重置
账号禁用
账号启用
```

---

## core-identity-web

实现：

```text
注册页
登录页
忘记密码
重置密码
个人中心
修改密码
退出登录
基础安全提示
```

账户首页显示：

```text
昵称
邮箱
账号状态
注册时间
最近登录
当前组织
```

---

## core-identity-admin-backend

提供管理能力：

```text
用户分页查询
用户详情
创建用户
禁用用户
启用用户
强制退出全部会话
触发密码重置
查询登录记录
查询基础审计
```

所有写操作必须调用 Identity Backend 内部管理 API。

例如：

```text
POST /internal/v1/admin/users/{id}/disable
POST /internal/v1/admin/users/{id}/enable
POST /internal/v1/admin/users/{id}/revoke-sessions
```

Admin Backend 对外暴露：

```text
POST /admin-api/v1/users/{id}/disable
```

---

## core-identity-admin-web

实现：

```text
用户列表
用户搜索
用户详情
禁用用户
启用用户
强制退出
重置密码
登录记录
基础审计列表
```

高危操作要求输入原因：

```text
禁用原因
强制退出原因
人工重置原因
```

---

## MVP 数据表

```text
identity_user
identity_credential
identity_session
identity_organization
identity_membership
identity_email_token
identity_password_reset
identity_login_attempt
identity_audit_event
```

---

## P1 完成标准

```text
用户可以完成完整注册登录流程
用户可以找回密码
每个用户至少属于一个组织
管理员可以管理用户状态
所有高危操作都有审计
其他 Core 可以识别当前登录用户
默认 SQLite 可以直接运行
```

## 为什么 MVP 就加入 Organization

因为 Identity 不只是博客登录系统，而是未来所有产品的基础。

Billing、Storage、AI Gateway 和 Workflow 最终都需要回答：

```text
资源属于哪个组织
额度属于哪个组织
账单由哪个组织支付
文件由哪个组织拥有
```

组织模型越晚加入，迁移成本越高。

---

# P2：组织与权限体系

## 目标

让 Identity 从“登录系统”升级为真正的平台权限中心。

核心能力：

```text
组织
成员
邀请
角色
权限
组织切换
组织内管理
```

---

## core-identity-backend

### 组织能力

```text
创建组织
修改组织资料
冻结组织
解散组织
成员邀请
接受邀请
移除成员
主动退出组织
转移组织所有权
```

### RBAC

建立：

```text
Permission
Role
RolePermission
MembershipRole
```

权限命名：

```text
identity.organization.read
identity.organization.update
identity.member.read
identity.member.invite
identity.member.remove
identity.role.manage
```

内置组织角色：

```text
OWNER
ADMIN
MEMBER
VIEWER
```

内置角色可以修改权限集合，但不可删除核心角色。

### 当前组织上下文

所有组织级请求明确携带：

```text
organization_id
```

后端必须验证：

```text
当前用户是否属于组织
成员状态是否有效
角色是否拥有权限
组织是否处于可用状态
```

不能只相信前端传入的组织 ID。

---

## core-identity-web

增加：

```text
组织切换器
创建组织
组织设置
成员列表
邀请成员
成员角色调整
移除成员
邀请记录
角色管理
权限查看
```

普通组织管理员在用户侧前端完成组织管理。

这部分不能全部塞进平台 Admin Console。

---

## core-identity-admin-backend

增加平台级能力：

```text
跨组织查询
组织冻结
组织恢复
组织风险标记
组织成员查询
角色模板查询
组织数据统计
批量状态修改
```

管理人员可以查看组织权限，但不能无审计地修改组织成员权限。

跨组织操作必须记录：

```text
平台管理员
目标组织
目标成员
操作理由
操作前状态
操作后状态
```

---

## core-identity-admin-web

增加：

```text
组织列表
组织详情
组织状态管理
组织成员查看
组织权限查看
异常组织筛选
跨组织用户查询
邀请记录查询
```

---

## 新增数据表

```text
identity_role
identity_permission
identity_role_permission
identity_membership_role
identity_invitation
identity_organization_domain
```

---

## P2 完成标准

```text
一个用户可以属于多个组织
用户可以切换当前组织
组织拥有独立成员和权限
组织管理员只能管理自己的组织
平台管理员可以跨组织治理
所有组织级资源拥有明确 organization_id
```

## 为什么第二阶段做 RBAC

MVP 阶段先验证身份闭环。

当 Billing、Storage、AI Gateway 开始接入时，才真正需要统一回答：

```text
谁
在什么组织
拥有什么权限
能对什么资源
执行什么操作
```

这时建立 RBAC 最合适，既不过早，也不拖延。

---

# P3：平台级身份服务

## 目标

让其他 `core-*` 和第三方应用能够正式依赖 Identity。

核心能力：

```text
Access Token
Refresh Token
JWKS
API Client
API Key
Service Account
Scope
应用授权
```

---

## core-identity-backend

### Token 服务

提供：

```text
Access Token
Refresh Token
Token 撤销
Token 轮换
JWKS 公钥
Token Introspection
```

Token 至少包含：

```text
sub
iss
aud
exp
organization_id
session_id
scope
```

其他 Core 通过 JWKS 本地验证 Token，不应每次请求都同步调用 Identity。

### API Client

支持：

```text
客户端注册
Client ID
Client Secret
Redirect URI
Allowed Scope
Client 状态
密钥轮换
```

### Service Account

支持服务间调用：

```text
core-billing
core-storage
core-ai-gateway
core-notification
```

服务账号拥有独立 Scope：

```text
identity.user.read
identity.organization.read
notification.message.send
billing.usage.write
```

### API Key

支持：

```text
创建
命名
Scope
过期时间
最后使用时间
撤销
轮换
```

数据库只保存：

```text
key_prefix
key_hash
```

完整 Key 只展示一次。

---

## core-identity-web

增加：

```text
授权应用列表
撤销应用授权
API Key 管理
创建 API Key
设置 Scope
设置过期时间
查看最近使用
会话与设备列表
```

---

## core-identity-admin-backend

增加：

```text
API Client 审核
Service Account 管理
异常 Token 撤销
全局 API Key 查询
密钥风险处置
应用授权统计
```

---

## core-identity-admin-web

增加：

```text
API Client 列表
Service Account
Scope 管理
授权应用
Token 撤销
密钥风险监控
```

---

## 新增数据表

```text
identity_api_client
identity_api_client_secret
identity_authorization
identity_refresh_token
identity_api_key
identity_service_account
identity_service_credential
identity_scope
```

---

## P3 完成标准

```text
其他 Core 可以本地验证用户 Token
服务之间可以使用服务身份调用
开发者可以创建 API Key
Token 和密钥可以撤销与轮换
权限通过 Scope 明确表达
```

## 为什么现在才做 OAuth 和服务身份

在用户、组织、角色和权限还不稳定时设计 Token Claim 和 Scope，后面必然频繁破坏兼容性。

先稳定身份模型，再向其他服务公开身份协议。

---

# P4：账号安全与零信任基础

## 目标

从“能登录”升级为“能够抵抗常见账号攻击”。

核心能力：

```text
MFA
Passkey
设备管理
风险登录
账号恢复
安全策略
登录保护
安全事件
```

---

## core-identity-backend

增加：

### MFA

```text
TOTP
恢复码
邮件二次验证
MFA 强制策略
高危操作二次验证
```

后续增加：

```text
WebAuthn
Passkey
安全密钥
```

### 登录保护

```text
IP 限速
账号限速
失败次数限制
临时锁定
异常地理位置
异常设备
密码喷洒检测
撞库风险检测
```

第一阶段使用：

```text
数据库记录
进程内限流
```

暂时不强制 Redis。

### 会话安全

```text
查看设备
撤销单个会话
撤销全部会话
密码修改后撤销旧会话
管理员禁用后立即失效
Refresh Token Rotation
```

### 账号恢复

```text
恢复码
可信邮箱
管理员协助恢复
恢复冷静期
恢复操作审计
```

---

## core-identity-web

增加安全中心：

```text
MFA 设置
Passkey 管理
恢复码
设备与会话
登录历史
安全通知
账号恢复方式
敏感操作二次验证
```

---

## core-identity-admin-backend

增加：

```text
安全事件聚合
风险账号识别
强制 MFA
强制密码重置
强制撤销会话
风险登录处置
账号恢复审批
安全策略编排
```

---

## core-identity-admin-web

增加安全运营中心：

```text
风险登录
异常 IP
锁定账号
MFA 覆盖率
高风险用户
安全事件时间线
账号恢复审批
策略配置
```

---

## 新增数据表

```text
identity_mfa_factor
identity_recovery_code
identity_webauthn_credential
identity_trusted_device
identity_security_event
identity_account_recovery
identity_security_policy
```

---

## P4 完成标准

```text
支持至少一种标准 MFA
支持设备和会话撤销
支持高危操作再次验证
支持账号风险锁定
安全事件拥有完整审计
企业可以强制成员启用 MFA
```

## 为什么安全能力不能全部塞进 MVP

MVP 需要保证基础密码安全、限流和审计，但不能一开始就实现完整风控平台。

安全能力必须建立在稳定的：

```text
用户
会话
组织
权限
Token
```

之上，否则只会产生大量不可维护的特例。

---

# P5：企业 SSO 与身份联合

## 目标

让企业能够接入自己的身份系统，并自动管理员工身份。

核心能力：

```text
OIDC
OAuth 2.0
企业 SSO
SAML
域名验证
JIT Provisioning
SCIM
外部身份绑定
```

---

## core-identity-backend

### 外部登录

支持：

```text
OIDC Provider
OAuth Provider
企业身份提供商
外部身份绑定
账号合并
账号解绑
```

### 企业 SSO

组织可以配置：

```text
企业域名
Identity Provider
登录策略
强制 SSO
自动加入组织
默认角色
```

### SAML

企业版增加：

```text
SAML 2.0
Metadata
证书轮换
NameID 映射
Attribute Mapping
```

### SCIM

支持企业用户自动同步：

```text
创建用户
更新用户
停用用户
创建组
更新组
成员同步
```

### JIT Provisioning

用户首次通过企业 SSO 登录时：

```text
自动创建账号
自动加入组织
分配默认角色
绑定外部身份
```

---

## core-identity-web

增加：

```text
第三方登录
企业 SSO 登录
身份绑定
身份解绑
企业域名提示
SSO 跳转
账号合并确认
```

---

## core-identity-admin-backend

增加：

```text
Identity Provider 配置
域名验证
SSO 测试
SAML Metadata 管理
证书管理
SCIM Token
同步任务
失败记录
身份映射规则
```

---

## core-identity-admin-web

增加：

```text
SSO 配置向导
OIDC 配置
SAML 配置
域名验证
属性映射
SCIM 配置
同步状态
同步错误
证书到期提醒
```

企业自己的 SSO 设置，原则上应在用户侧组织设置中完成。

平台 Admin Console 用于：

```text
排障
审核
全局策略
风险治理
```

---

## 新增数据表

```text
identity_external_identity
identity_identity_provider
identity_sso_configuration
identity_verified_domain
identity_attribute_mapping
identity_scim_token
identity_provisioning_job
identity_provisioning_log
```

---

## P5 完成标准

```text
企业可以配置 OIDC SSO
企业可以强制成员使用 SSO
支持外部身份绑定
支持 JIT 用户创建
支持 SCIM 用户生命周期同步
支持 SAML 企业接入
```

## 为什么 SSO 放在第五阶段

企业 SSO 不是增加一个“使用 Google 登录”按钮。

它依赖：

```text
组织模型
域名归属
角色模型
外部身份映射
账号合并
安全策略
Token 体系
审计能力
```

这些基础不稳定，SSO 越早做，后期兼容成本越大。

---

# P6：企业治理与合规

## 目标

让 Identity 能进入真正的企业环境，而不仅是功能丰富的登录系统。

核心能力：

```text
权限治理
访问审查
职责分离
审计导出
数据保留
隐私请求
管理员分权
策略中心
```

---

## core-identity-backend

### 管理员分权

平台管理员不再只有一个：

```text
SUPER_ADMIN
```

而是拆分：

```text
PLATFORM_ADMIN
SECURITY_ADMIN
AUDIT_ADMIN
SUPPORT_ADMIN
BILLING_ADMIN
READ_ONLY_ADMIN
```

### 权限治理

支持：

```text
权限模板
角色模板
职责分离
高危权限标记
临时权限
权限过期
审批授权
```

### Access Review

企业可以定期发起：

```text
成员访问审查
管理员权限审查
高危角色审查
长期未使用账号审查
```

### 隐私与保留

支持：

```text
用户数据导出
用户数据删除申请
匿名化
数据保留策略
审计保留策略
法务保留
```

### 审计

审计事件支持：

```text
不可变记录
完整操作上下文
查询
导出
签名
外部投递
```

---

## core-identity-web

增加：

```text
个人数据导出
账号删除申请
权限申请
临时权限查看
访问审查任务
隐私设置
```

组织管理员增加：

```text
组织审计
成员权限审查
管理员权限审查
账号生命周期策略
```

---

## core-identity-admin-backend

增加：

```text
访问审查编排
审批流
审计导出
数据匿名化任务
隐私请求处理
合规报表
SIEM 投递
管理员职责分离
```

---

## core-identity-admin-web

增加治理中心：

```text
访问审查
高危权限
临时授权
隐私请求
审计导出
数据保留策略
管理员分权
合规报表
SIEM 配置
```

---

## 新增数据表

```text
identity_access_request
identity_access_review
identity_access_review_item
identity_temporary_grant
identity_admin_role
identity_privacy_request
identity_retention_policy
identity_audit_export
identity_legal_hold
```

---

## P6 完成标准

```text
管理员拥有细分职责
高危权限可以审批和过期
企业可以执行访问审查
审计可以安全导出
隐私请求拥有完整处理链路
数据删除与审计保留可以同时满足
```

## 为什么“企业级”不等于“支持 SSO”

SSO 只是企业接入能力。

真正企业级还要求：

```text
谁修改了权限
为什么修改
谁审批
什么时候过期
能否复查
能否导出证据
能否满足隐私和保留要求
```

---

# P7：高可用、规模化与生态扩展

## 目标

在保持社区版零依赖体验的同时，支持大型企业部署。

核心能力：

```text
多实例
高可用
大规模组织
异步事件
缓存
灾备
扩展点
运营可观测性
```

---

## core-identity-backend

增加可选基础设施适配：

```text
MySQL
PostgreSQL
Redis
消息队列
外部密钥系统
外部审计系统
外部对象存储
```

这些都是可选实现。

默认社区版仍然保持：

```text
SQLite
单节点
无 Redis
无 MQ
```

### 多实例能力

```text
共享会话状态
分布式 Token 撤销
分布式限流
安全事件异步处理
一致性缓存失效
任务抢占
集群密钥轮换
```

### 可观测性

```text
指标
链路追踪
安全指标
登录成功率
Token 签发延迟
SSO 可用率
SCIM 同步延迟
权限检查延迟
```

---

## core-identity-web

增加：

```text
大组织成员检索
虚拟列表
批量操作
异步导入
导入进度
大规模权限管理
可访问性增强
国际化
```

---

## core-identity-admin-backend

增加：

```text
批量任务系统
分布式任务
大规模导出
异步报表
集群管理
运行诊断
依赖健康聚合
安全指标聚合
```

---

## core-identity-admin-web

增加运维控制台：

```text
实例状态
数据库状态
队列状态
任务状态
SSO 可用性
同步延迟
错误趋势
Token 签发指标
安全事件趋势
```

---

## 扩展点

允许扩展：

```text
PasswordPolicyProvider
IdentityProviderAdapter
RiskEvaluator
MfaProvider
AuditSink
NotificationAdapter
UserProvisioningProvider
PermissionPolicyProvider
```

插件只能通过正式扩展点接入，不能直接修改核心数据库。

---

## P7 完成标准

```text
支持多实例部署
支持 MySQL 或 PostgreSQL
支持共享缓存和消息系统
支持灾难恢复
支持关键指标和链路追踪
支持安全扩展点
社区版仍可零依赖启动
```

## 为什么最后才引入 Redis 和 MQ

只有出现以下需求时，它们才真正有价值：

```text
多实例共享状态
分布式限流
大量异步安全事件
大规模 SCIM 同步
大量审计导出
高并发 Token 撤销
```

在这些问题出现以前引入中间件，只会增加安装、测试、部署和故障处理成本。

---

# 六、各阶段能力矩阵

| 能力              | P1 | P2 | P3 | P4 | P5 | P6 | P7 |
| --------------- | -: | -: | -: | -: | -: | -: | -: |
| 用户注册登录          |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |
| 基础组织            |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |
| 组织成员管理          | 轻量 |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |
| RBAC            |  — |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |
| API Key         |  — |  — |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |
| Service Account |  — |  — |  ✓ |  ✓ |  ✓ |  ✓ |  ✓ |
| MFA             |  — |  — |  — |  ✓ |  ✓ |  ✓ |  ✓ |
| Passkey         |  — |  — |  — |  ✓ |  ✓ |  ✓ |  ✓ |
| OIDC SSO        |  — |  — |  — |  — |  ✓ |  ✓ |  ✓ |
| SAML            |  — |  — |  — |  — |  ✓ |  ✓ |  ✓ |
| SCIM            |  — |  — |  — |  — |  ✓ |  ✓ |  ✓ |
| 访问审查            |  — |  — |  — |  — |  — |  ✓ |  ✓ |
| 合规治理            |  — |  — |  — |  — |  — |  ✓ |  ✓ |
| 多实例             |  — |  — |  — |  — |  — |  — |  ✓ |

---

# 七、最关键的架构约束

## 约束一：Identity Backend 是唯一数据所有者

禁止：

```text
Admin Backend 直接查询 identity_user
Admin Backend 直接修改 identity_role
Admin Backend 自己实现密码重置
Admin Backend 自己实现用户冻结
```

否则四子项目会演变成两套身份系统。

---

## 约束二：区分组织管理员与平台管理员

组织管理员使用：

```text
core-identity-web
```

管理自己组织内部的：

```text
成员
邀请
角色
组织设置
SSO
```

平台管理员使用：

```text
core-identity-admin-web
```

管理全平台：

```text
用户风险
组织冻结
安全事件
审计
全局策略
系统配置
```

两者不能混为一谈。

---

## 约束三：Admin Backend 是 BFF，不是第二个领域服务

Admin Backend 可以拥有：

```text
管理查询聚合
批量任务
导出任务
后台审计
脱敏策略
管理端缓存
```

但不能拥有：

```text
User 领域规则
Organization 领域规则
Role 领域规则
Credential 领域规则
Session 领域规则
```

这些始终属于 Identity Backend。

---

## 约束四：每个阶段都必须可发布

不能采用：

```text
先搭两年企业架构
最后才有用户可以登录
```

正确路线是：

```text
P1 可以登录
P2 可以管理组织
P3 可以被其他 Core 使用
P4 可以安全运行
P5 可以进入企业
P6 可以接受治理审查
P7 可以大规模部署
```

---

# 八、推荐优先级

真正开发时建议严格按以下顺序：

```text
1. 工程骨架和四子项目边界
2. 用户、凭证、会话
3. 最小组织模型
4. 用户侧完整登录体验
5. 平台用户管理
6. 组织成员与 RBAC
7. Token、API Key、服务账号
8. MFA 与安全中心
9. 企业 SSO
10. SCIM
11. 企业治理
12. 多实例与分布式设施
```

不要提前开发：

```text
复杂 SAML
动态权限表达式
无密码登录全家桶
AI 风险识别
分布式会话
Redis Token 黑名单
MQ 安全事件总线
插件市场
```

这些都不是 MVP 的决定性能力。

---

# 九、最终产品形态

Core Identity 最终不是一个简单的登录模块，而是三个层次的产品：

```text
第一层：Authentication

回答：
你是谁？
```

```text
第二层：Authorization

回答：
你在什么组织？
你拥有什么权限？
你能做什么？
```

```text
第三层：Identity Governance

回答：
为什么拥有这些权限？
是谁授予的？
什么时候过期？
是否仍然合理？
是否可以审计？
```

四个子项目分别承担：

```text
core-identity-backend
    身份事实与规则中心

core-identity-web
    用户与组织自助中心

core-identity-admin-backend
    平台治理与管理编排中心

core-identity-admin-web
    平台安全与运营控制台
```

这四者不是四套系统，而是围绕同一个 Identity 领域形成的四个清晰边界。
