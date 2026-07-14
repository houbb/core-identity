# Core Identity P3：平台级身份服务详细设计

版本：P3
前置依赖：P0 工程边界、P1 Identity MVP、P2 组织与权限体系
目标：让其他 Core、第三方应用、命令行工具、自动化程序和服务账号，都能通过标准身份协议安全访问平台。

---

# 一、P3 核心目标

P2 已经解决：

```text
用户是谁
用户属于哪些组织
用户在组织中拥有哪些角色
角色包含哪些权限
```

P3 继续解决：

```text
哪个应用正在代表用户访问？
应用被允许访问哪些能力？
Token 是签发给哪个服务的？
后台程序如何获得身份？
用户如何撤销第三方授权？
API Key 如何创建、使用和轮换？
其他 Core 如何独立验证 Token？
```

P3 必须完成：

```text
OAuth Client
Authorization Code
PKCE
Access Token
Refresh Token
ID Token
JWKS
Token Revocation
Token Introspection
Scope
Audience
Consent
API Key
Service Account
Client Credentials
密钥轮换
开发者中心
应用授权管理
服务身份审计
```

P3 完成后，Identity 应支持三类访问：

```text
1. 用户通过浏览器访问平台
2. 第三方应用代表用户访问平台
3. 后台服务和自动化程序访问平台
```

---

# 二、P3 暂时不做什么

P3 不实现：

```text
MFA
Passkey
风险登录
企业强制安全策略
SAML
企业 OIDC SSO
SCIM
身份提供商联合
访问审批
临时权限
职责分离
Token Exchange
跨域身份代理
动态客户端注册
设备授权码登录
完整多数据中心密钥系统
```

这些分别属于 P4、P5、P6 和 P7。

P3 的核心范围是：

> 平台自己作为身份与授权中心，对内部 Core 和外部应用签发可信身份。

---

# 三、P3 的总体身份模型

P3 需要区分四种完全不同的身份对象。

## 3.1 User

真实用户。

来源：

```text
注册
管理员创建
未来企业身份同步
```

可以：

```text
交互式登录
加入组织
拥有角色
授权第三方应用
创建个人 API Key
```

---

## 3.2 Platform Operator

平台管理员。

本质仍然是 User，但具有平台管理身份。

使用：

```text
core-identity-admin-web
```

不能与普通用户 Token、Cookie 和授权流程混用。

---

## 3.3 Service Account

非人的服务身份。

用于：

```text
服务间调用
定时任务
CI/CD
服务器程序
企业自动化
```

特点：

```text
不允许交互式登录
没有邮箱和密码
属于特定组织或平台
拥有独立角色和权限
通过 Client Credentials 获得 Token
```

---

## 3.4 API Client

代表一个应用，而不是代表人。

例如：

```text
core-storage-web
第三方桌面应用
命令行工具
移动客户端
企业内部应用
Marketplace 插件
```

API Client 决定：

```text
允许使用哪些授权流程
允许申请哪些 Scope
允许访问哪些 Audience
回调地址是什么
是否需要 Client Secret
```

必须明确：

> Client 是应用，User 是用户，Service Account 是非人主体，API Key 是凭证。

四者不能混为一张表。

---

# 四、四个子项目在 P3 的职责

```text
core-identity/
├── core-identity-backend/
├── core-identity-web/
├── core-identity-admin-backend/
└── core-identity-admin-web/
```

---

## 4.1 core-identity-backend

P3 新增职责：

```text
OAuth Authorization Server
OIDC Provider
Client Registry
Scope Registry
Audience Registry
Authorization Code
Consent
Access Token 签发
Refresh Token 轮换
ID Token 签发
JWKS
Signing Key
Token Revocation
Token Introspection
API Key
Service Account
Service Credential
授权事件
安全审计
```

它仍然是唯一身份事实来源。

只有它可以：

```text
验证用户会话
验证 Client
签发 Token
撤销 Token
创建 API Key
管理 Service Account
计算最终授权范围
```

---

## 4.2 core-identity-web

P3 新增用户侧能力：

```text
应用授权确认页
授权错误页
已授权应用列表
撤销应用授权
开发者应用管理
API Key 管理
Service Account 管理
密钥创建与轮换
Token 使用说明
OAuth 回调结果页
```

面向：

```text
普通用户
组织管理员
应用开发者
组织技术管理员
```

组织级 Service Account 和组织级应用，应在用户侧组织门户管理。

---

## 4.3 core-identity-admin-backend

P3 新增管理编排能力：

```text
跨组织 Client 查询
平台级 Client 审核
高风险 Scope 审核
Service Account 治理
API Key 风险查询
Token 撤销编排
Signing Key 状态聚合
授权统计
异常 Client 查询
密钥泄露处置
开发者应用封禁
```

仍然不能：

```text
直接访问 Identity 数据库
自行签发 Token
自行生成 API Key
自行验证 Client Secret
```

---

## 4.4 core-identity-admin-web

P3 新增管理页面：

```text
OAuth Client 管理
Client 审核
Service Account 管理
API Key 风险管理
Scope 目录
Audience 目录
授权记录
Token 撤销
签名密钥
安全事件
开发者应用统计
```

平台管理端治理全平台应用和凭证。

普通组织管理员仍通过：

```text
core-identity-web
```

管理自己组织下的应用和 Service Account。

---

# 五、P3 信任边界

P3 引入四个安全边界：

```text
用户浏览器
外部应用
内部 Core 服务
Identity 授权服务器
```

调用关系：

```text
用户浏览器
    │
    ▼
第三方应用
    │
    ▼
core-api-gateway
    │
    ▼
业务 Core
```

身份签发：

```text
第三方应用
    │
    ▼
core-identity-backend
    │
    ├── 验证 Client
    ├── 验证用户会话
    ├── 获取用户同意
    ├── 计算 Scope
    └── 签发 Token
```

业务 Core 不应：

```text
直接读取 Identity 数据库
自行验证用户密码
自行签发平台 Token
信任未经验证的用户 Header
```

---

# 六、五类凭证必须严格区分

## 6.1 Browser Session

用于：

```text
core-identity-web
core-identity-admin-web
```

特点：

```text
HttpOnly Cookie
服务端可撤销
不暴露给 JavaScript
不用于调用第三方 API
```

---

## 6.2 Authorization Code

用于：

```text
OAuth Authorization Code Flow
```

特点：

```text
一次性
短期有效
绑定 Client
绑定 Redirect URI
绑定 PKCE
使用后失效
```

它不是 Access Token。

---

## 6.3 Access Token

用于：

```text
调用具体 Core API
```

特点：

```text
短期
具有 Audience
具有 Scope
由 Identity 签名
资源服务可以本地验证
```

---

## 6.4 Refresh Token

用于：

```text
获取新的 Access Token
```

特点：

```text
长期程度高于 Access Token
只返回给允许使用 Refresh Token 的 Client
必须轮换
数据库只保存 Hash
可以撤销整个 Token Family
```

---

## 6.5 API Key

用于：

```text
脚本
简单自动化
个人开发工具
无需完整 OAuth 的服务调用
```

特点：

```text
长随机字符串
只显示一次
数据库只保存 Hash
绑定用户或 Service Account
绑定组织
绑定 Scope
支持过期与撤销
```

API Key 不能直接当作用户密码。

---

# 七、Token 类型矩阵

| 类型                 | 代表主体                          | 用途              | 是否长期 |     是否可撤销 |
| ------------------ | ----------------------------- | --------------- | ---: | --------: |
| Browser Session    | User                          | 浏览器登录           |   中期 |         是 |
| Admin Session      | Platform Operator             | 管理控制台           |   中期 |         是 |
| Authorization Code | User + Client                 | 换取 Token        |   极短 |     使用后失效 |
| Access Token       | User/Service Account + Client | API 调用          |   短期 |      间接撤销 |
| Refresh Token      | User + Client                 | 刷新 Token        |  较长期 |         是 |
| ID Token           | User                          | 向 Client 表达登录身份 |   短期 | 不作 API 凭证 |
| API Key            | User/Service Account          | 脚本调用            |  可配置 |         是 |
| Client Secret      | Client                        | 证明应用身份          | 长期凭证 |         是 |

必须禁止：

```text
使用 ID Token 调业务 API
使用 Client Secret 代表用户
使用 API Key 登录网页
使用 Access Token 代替 Refresh Token
```

---

# 八、Scope 与 Permission 的关系

这是 P3 最关键的设计之一。

## 8.1 Permission

Permission 表示：

> 主体在某个组织中，业务上能做什么。

例如：

```text
storage.object.read
storage.object.delete
billing.invoice.read
identity.member.invite
```

来源于 P2 的：

```text
Membership
Role
Permission
```

---

## 8.2 Scope

Scope 表示：

> 某个 Client 或凭证，被允许代表主体申请哪些能力。

例如：

```text
profile.read
organization.read
storage.read
storage.write
billing.read
offline_access
```

Scope 更适合公开给应用开发者。

---

## 8.3 最终授权公式

最终允许操作必须同时满足：

```text
主体拥有 Permission
AND
Client 被允许申请对应 Scope
AND
用户同意了 Scope
AND
Access Token 实际包含 Scope
AND
Token Audience 匹配目标服务
AND
资源服务本地业务规则允许
```

可表达为：

```text
Effective Authorization =
    Subject Permissions
    ∩ Client Allowed Scopes
    ∩ User Granted Scopes
    ∩ Token Scopes
    ∩ Resource Server Policy
```

---

## 8.4 Scope 与 Permission 映射

例如：

```text
Scope: storage.read

映射权限：
storage.object.read
storage.folder.read
```

```text
Scope: storage.write

映射权限：
storage.object.create
storage.object.update
storage.folder.create
```

Scope 不应直接等于所有细粒度 Permission。

否则第三方应用授权页面会出现几百个难以理解的权限码。

---

# 九、Scope 分类

P3 支持以下 Scope 类型。

## 9.1 身份 Scope

```text
openid
profile
email
organization
```

---

## 9.2 业务 Scope

```text
storage.read
storage.write
billing.read
workflow.execute
ai.use
notification.send
```

---

## 9.3 高风险 Scope

```text
identity.organization.manage
identity.member.manage
storage.delete
billing.manage
ai.provider.manage
```

高风险 Scope 需要：

```text
更明显的授权提示
Client 审核
管理员批准，可配置
更严格的审计
```

P3 只实现高风险提示和平台审核。

复杂审批放在 P6。

---

## 9.4 特殊 Scope

```text
offline_access
```

表示允许签发 Refresh Token。

Client 没有申请或未获授权时，不得返回 Refresh Token。

---

# 十、Audience 设计

Access Token 必须明确签发给哪个资源服务。

Audience 示例：

```text
core-storage
core-billing
core-ai-gateway
core-workflow
core-notification
core-identity
```

一个 Access Token 默认只包含一个主要 Audience。

不建议默认签发一个可以访问全部 Core 的超级 Token。

例如：

```text
aud = core-storage
scope = storage.read storage.write
```

`core-billing` 收到后必须拒绝。

---

## 10.1 Audience 与 Scope 的关系

每个 Scope 必须声明：

```text
适用于哪个 Audience
```

例如：

```text
storage.read
    audience: core-storage

billing.read
    audience: core-billing
```

申请不匹配组合时拒绝：

```text
audience = core-storage
scope = billing.read
```

---

# 十一、Access Token 设计

建议使用签名 JWT 作为 Access Token。

核心 Claims：

```json
{
  "iss": "https://identity.example.com",
  "sub": "user-id",
  "subject_type": "user",
  "aud": "core-storage",
  "client_id": "client-id",
  "organization_id": "organization-id",
  "scope": "storage.read storage.write",
  "authorization_version": 12,
  "session_id": "session-id",
  "jti": "token-id",
  "iat": 1784040000,
  "exp": 1784040900
}
```

Service Account Token：

```json
{
  "sub": "service-account-id",
  "subject_type": "service_account",
  "aud": "core-storage",
  "client_id": "client-id",
  "organization_id": "organization-id",
  "scope": "storage.write",
  "authorization_version": 8,
  "jti": "token-id",
  "iat": 1784040000,
  "exp": 1784040900
}
```

---

## 11.1 不应放入 Access Token 的内容

```text
完整用户资料
完整角色列表
数百个 Permission
密码信息
邮箱验证 Token
内部风险评分
Client Secret
Refresh Token
```

权限数量可能不断增加，因此 Access Token 不应成为完整权限数据库。

---

## 11.2 Access Token 有效期

建议保持短期，例如：

```text
5～20 分钟
```

具体值可配置。

短期 Access Token 的目的：

```text
减少撤销延迟
降低泄露影响
避免长期权限快照过期
```

---

# 十二、ID Token 设计

当 Client 请求：

```text
openid
```

Identity 可以签发 ID Token。

ID Token 用于告诉 Client：

```text
用户已经登录
用户 ID 是什么
登录发生于何时
```

基础 Claims：

```json
{
  "iss": "https://identity.example.com",
  "sub": "user-id",
  "aud": "client-id",
  "exp": 1784040900,
  "iat": 1784040000,
  "auth_time": 1784039900,
  "nonce": "client-nonce"
}
```

只有获得相应 Scope 后才能包含：

```text
profile
email
organization
```

ID Token 不用于访问 Core API。

---

# 十三、Authorization Code + PKCE 流程

P3 面向第三方应用的标准用户授权流程：

```text
Authorization Code + PKCE
```

流程：

```text
应用生成：
code_verifier
code_challenge

应用跳转：
GET /oauth2/authorize
    │
    ▼
Identity 检查用户 Session
    │
    ├── 未登录 → 登录
    └── 已登录 → 继续
    │
    ▼
展示授权确认页
    │
    ▼
用户同意
    │
    ▼
Identity 生成 Authorization Code
    │
    ▼
重定向到 Client Redirect URI
    │
    ▼
Client 使用 Code + code_verifier 换 Token
```

---

## 13.1 Authorization 请求

```text
GET /oauth2/authorize
```

参数：

```text
response_type=code
client_id
redirect_uri
scope
state
code_challenge
code_challenge_method=S256
nonce
organization_id
audience
```

---

## 13.2 Redirect URI

必须精确匹配预注册地址。

禁止宽松匹配：

```text
https://example.com/*
```

应注册完整地址：

```text
https://example.com/oauth/callback
```

开发环境可以注册：

```text
http://localhost:3000/oauth/callback
```

生产 Client 默认不允许任意 HTTP 回调。

---

## 13.3 Authorization Code

要求：

```text
高随机性
只保存 Hash
一次性使用
绑定 Client
绑定 Redirect URI
绑定 User
绑定 Organization
绑定 Scope
绑定 Audience
绑定 PKCE Challenge
短期有效
```

建议有效期：

```text
几十秒至几分钟
```

---

# 十四、授权确认页 UX

页面展示：

```text
应用名称
应用 Logo
应用开发者
当前用户
当前组织
请求的权限
权限风险
授权有效范围
```

示例：

```text
“Analytics Desktop” 请求访问 Acme Studio

它将能够：
✓ 查看文件和文件夹
✓ 查看组织基本信息

它不能：
× 删除文件
× 查看账单
× 管理组织成员
```

主要操作：

```text
允许访问
取消
```

按钮不能只写：

```text
确定
```

---

## 14.1 高风险 Scope

高风险权限单独分组：

```text
高风险访问

该应用将能够：
• 删除文件
• 修改组织成员
```

需要更明显的确认。

P3 可要求用户勾选：

```text
我理解该应用将获得高风险权限
```

避免无差别弹窗疲劳，只有真正高风险 Scope 才增加确认步骤。

---

## 14.2 组织选择

如果用户属于多个组织，且应用支持组织级访问：

```text
选择要授权的组织
```

只显示用户具备相应权限的组织。

例如应用请求：

```text
storage.write
```

用户在 A 组织有写权限，在 B 组织只有读权限。

则：

```text
A 组织可以授权 storage.write
B 组织不能授权 storage.write
```

不能先让用户选择，再在回调时莫名失败。

---

## 14.3 已经授权过的应用

如果：

```text
Client 相同
用户相同
组织相同
Scope 没有增加
```

可以配置为跳过重复授权页。

如果 Scope 增加：

```text
必须再次展示授权页
```

页面突出新增权限：

```text
该应用新增请求：

• 删除文件
```

---

# 十五、Token Endpoint

```text
POST /oauth2/token
```

P3 支持：

```text
authorization_code
refresh_token
client_credentials
```

暂不支持：

```text
password
implicit
device_code
token_exchange
```

尤其禁止 Password Grant。

第三方应用不得收集平台用户密码。

---

# 十六、Refresh Token Rotation

每次使用 Refresh Token 时：

```text
旧 Refresh Token 立即失效
签发新的 Access Token
签发新的 Refresh Token
```

形成 Token Family：

```text
Family
 ├── Refresh Token 1：USED
 ├── Refresh Token 2：USED
 └── Refresh Token 3：ACTIVE
```

如果已经使用过的 Refresh Token 再次出现：

```text
判定可能发生 Token 泄露
撤销整个 Token Family
撤销对应授权
记录安全事件
要求重新登录授权
```

---

# 十七、Token Revocation

提供：

```text
POST /oauth2/revoke
```

支持撤销：

```text
Refresh Token
API Key
Client Secret
Service Credential
完整 Grant
```

JWT Access Token 通常不需要全部入库。

短期 Access Token 可以通过：

```text
短有效期
用户 Session 撤销
Authorization Version
高风险 JTI 黑名单
```

控制。

P3 可建立有限的撤销记录，处理：

```text
明确泄露的 Access Token
高危账号禁用
紧急全局撤销
```

不建议把每个 Access Token 都存入数据库。

---

# 十八、Token Introspection

提供内部端点：

```text
POST /oauth2/introspect
```

面向：

```text
无法本地验证的旧系统
高风险操作
需要实时撤销状态的 Core
```

返回：

```json
{
  "active": true,
  "sub": "user-id",
  "subjectType": "user",
  "clientId": "client-id",
  "organizationId": "organization-id",
  "aud": "core-storage",
  "scope": "storage.read",
  "exp": 1784040900
}
```

Introspection 必须要求受信任的服务身份。

不能向普通浏览器公开。

---

# 十九、JWKS 与签名密钥

Identity 提供：

```text
/.well-known/openid-configuration
/.well-known/jwks.json
```

其他 Core 可以：

```text
获取公钥
缓存公钥
本地验证 Access Token
```

---

## 19.1 Signing Key 生命周期

状态：

```text
PENDING
ACTIVE
RETIRING
RETIRED
REVOKED
```

流程：

```text
创建新 Key
    ↓
PENDING
    ↓
发布公钥
    ↓
ACTIVE
    ↓
旧 Key 进入 RETIRING
    ↓
等待旧 Token 全部自然过期
    ↓
RETIRED
```

不能：

```text
直接删除仍用于验证旧 Token 的公钥
```

---

## 19.2 私钥存储

P3 默认单节点可以：

```text
使用主密钥加密后存入数据库
或存储在受保护的本地密钥文件
```

生产环境禁止：

```text
明文私钥写入 application.yml
明文私钥提交 Git
管理 API 返回私钥
```

P7 再接入专业密钥管理服务。

---

# 二十、OAuth Client 模型

## 20.1 Client 类型

```text
PUBLIC
CONFIDENTIAL
```

### PUBLIC

适用于：

```text
SPA
移动应用
桌面应用
CLI
```

不能安全保存 Client Secret。

必须使用：

```text
Authorization Code + PKCE
```

### CONFIDENTIAL

适用于：

```text
服务端 Web 应用
后端服务
企业内部系统
```

可以持有 Client Secret。

---

## 20.2 Client 所有权

Client 可以属于：

```text
PLATFORM
ORGANIZATION
USER
MARKETPLACE_APP
```

P3 实际支持：

```text
PLATFORM
ORGANIZATION
USER
```

Marketplace 集成可以先预留字段。

---

## 20.3 Client 状态

```text
DRAFT
PENDING_REVIEW
ACTIVE
SUSPENDED
REVOKED
```

### DRAFT

尚未可用。

### PENDING_REVIEW

申请了高风险 Scope 或公开发布，等待平台审核。

### ACTIVE

正常可用。

### SUSPENDED

暂时禁用，不允许发起新授权和 Token。

### REVOKED

永久终止。

---

# 二十一、Client 注册 UX

用户侧开发者中心：

```text
/developer/applications
/developer/applications/new
/developer/applications/:clientId
```

创建字段：

```text
应用名称
应用描述
应用主页
应用 Logo
Client 类型
Redirect URI
所需 Audience
所需 Scope
隐私政策地址
服务条款地址
```

P3 可将隐私政策和服务条款设为：

```text
公开第三方应用必填
内部组织应用可选
```

---

## 21.1 创建后展示

显示：

```text
Client ID
Client 类型
Client 状态
Redirect URI
允许的 Scope
允许的 Audience
创建时间
```

如果是 Confidential Client：

```text
Client Secret 只显示一次
```

用户关闭弹窗后不可再次查看完整 Secret。

---

## 21.2 Client Secret 轮换

支持：

```text
创建新 Secret
设置旧 Secret 过渡期
撤销旧 Secret
```

轮换流程：

```text
Secret A：ACTIVE
创建 Secret B：ACTIVE
应用切换到 Secret B
撤销 Secret A
```

不强制创建新 Secret 后立即让旧应用中断。

---

# 二十二、Consent 与 Grant

必须区分：

```text
Consent
Grant
Token
```

## 22.1 Consent

用户表示：

> 我允许这个 Client 请求这些 Scope。

## 22.2 Grant

系统记录：

> 某 User 在某 Organization 下，授予某 Client 哪些 Scope。

## 22.3 Token

某一次具体签发的短期访问凭证。

撤销 Grant 后：

```text
Refresh Token 全部撤销
不能继续刷新
新授权必须重新征得同意
现有短期 Access Token 等待过期或进入紧急撤销
```

---

# 二十三、已授权应用 UX

用户侧：

```text
/account/authorized-apps
```

列表显示：

```text
应用名称
开发者
授权组织
已授权 Scope
授权时间
最近使用时间
状态
```

操作：

```text
查看详情
撤销授权
```

撤销确认：

```text
撤销后，该应用将无法继续访问 Acme Studio。
它已经获取并保存的数据不会自动从应用方删除。
```

必须明确最后一句。

Identity 只能停止未来访问，不能删除第三方已经复制的数据。

---

# 二十四、Service Account 设计

Service Account 用于非人访问。

例如：

```text
nightly-backup
invoice-exporter
ai-batch-worker
github-deployment
```

---

## 24.1 Service Account 归属

可属于：

```text
ORGANIZATION
PLATFORM
```

组织级 Service Account：

```text
只能访问所属组织资源
```

平台级 Service Account：

```text
只用于 Core Platform 内部服务
必须由平台管理员创建
```

普通组织管理员不能创建平台级 Service Account。

---

## 24.2 Service Account 状态

```text
ACTIVE
DISABLED
REVOKED
```

---

## 24.3 Service Account 权限

组织级 Service Account 通过角色获得 Permission。

关系：

```text
Service Account
    ↓
Service Account Role
    ↓
Role
    ↓
Permission
```

不能直接随意输入 Permission 字符串。

最终 Token Scope 仍受：

```text
Service Account Permission
∩ Client Allowed Scope
∩ Credential Allowed Scope
```

限制。

---

## 24.4 Service Account UX

组织设置中增加：

```text
/organizations/:organizationId/service-accounts
```

列表：

```text
名称
状态
角色
最近使用
凭证数量
创建时间
```

创建流程：

```text
输入名称
填写用途说明
分配角色
选择允许的 Audience
选择最大 Scope
创建
```

创建成功后再创建凭证。

---

## 24.5 Service Account 凭证

可以拥有多个 Credential。

字段：

```text
Credential 名称
Client ID
Client Secret
允许 Audience
允许 Scope
过期时间
最近使用时间
```

完整 Secret 只显示一次。

支持并行轮换。

---

# 二十五、Client Credentials 流程

用于 Service Account：

```text
POST /oauth2/token
grant_type=client_credentials
client_id=...
client_secret=...
scope=...
audience=...
```

Identity 验证：

```text
Client 状态
Service Account 状态
Credential 状态
Credential 有效期
请求 Scope 是否允许
请求 Audience 是否允许
Service Account 是否拥有对应 Permission
Organization 是否 ACTIVE
```

然后签发：

```text
subject_type = service_account
sub = service_account_id
```

不得签发：

```text
Refresh Token
ID Token
```

Client Credentials 一般只需要短期 Access Token。

---

# 二十六、API Key 设计

API Key 是较轻量的机器访问方式。

可由：

```text
User
Service Account
```

创建。

---

## 26.1 API Key 格式

建议包含可识别前缀：

```text
cik_live_ab12_xxxxxxxxxxxxxxxxx
```

例如：

```text
cik
    Core Identity Key

live
    环境

ab12
    Key Prefix

后部
    随机 Secret
```

数据库只保存：

```text
prefix
secret_hash
```

---

## 26.2 API Key 属性

```text
名称
所有者类型
所有者 ID
组织 ID
允许 Audience
允许 Scope
状态
过期时间
最后使用时间
最后使用 IP
创建时间
```

---

## 26.3 API Key UX

用户创建 API Key：

```text
输入名称
选择组织
选择 Audience
选择 Scope
选择过期时间
确认创建
```

创建成功页面：

```text
这是你唯一一次看到完整 API Key。
请立即复制并保存到安全位置。
```

操作：

```text
复制 API Key
下载 .env 示例
完成
```

用户离开后：

```text
不允许再次显示完整 Key
```

---

## 26.4 API Key 列表

展示：

```text
名称
Prefix
组织
Audience
Scope
状态
创建时间
过期时间
最后使用
```

操作：

```text
撤销
轮换
编辑名称
缩小 Scope
```

增加 Scope 不应静默修改现有 Key。

建议：

```text
扩大权限时创建新 Key
缩小权限可以直接修改
```

---

## 26.5 API Key 验证

Gateway 或资源服务收到：

```text
Authorization: Bearer cik_live_...
```

可以调用 Identity：

```text
POST /internal/v1/identity/api-keys/introspect
```

P3 初期 API Key 使用实时 Introspection。

原因：

```text
API Key 需要立即撤销
API Key 通常长期有效
不能像 JWT 一样只依赖过期时间
```

后续可以增加：

```text
短期缓存
版本缓存
Gateway 集中验证
```

---

# 二十七、API Key 与 OAuth 的选择

建议规则：

## 使用 OAuth

适合：

```text
第三方应用代表用户
需要用户授权页面
需要 Refresh Token
需要标准 OIDC 登录
多用户应用
公开应用
```

## 使用 Service Account

适合：

```text
后台服务
组织自动化
服务器到服务器
CI/CD
长期运行程序
```

## 使用 API Key

适合：

```text
个人脚本
简单 CLI
单一用途集成
快速开发测试
```

不要把 API Key 当作所有集成的唯一方案。

---

# 二十八、开发者中心信息架构

用户侧新增：

```text
/developer
/developer/applications
/developer/applications/new
/developer/applications/:clientId
/developer/applications/:clientId/secrets
/developer/applications/:clientId/redirect-uris
/developer/applications/:clientId/scopes

/developer/api-keys
/developer/api-keys/new

/organizations/:organizationId/service-accounts
/organizations/:organizationId/service-accounts/new
/organizations/:organizationId/service-accounts/:id

/account/authorized-apps
```

---

# 二十九、开发者中心首页 UX

首页展示三种入口：

```text
应用
为其他用户构建 OAuth 应用

Service Account
为组织后台程序创建身份

API Key
为脚本和个人工具创建凭证
```

每个入口明确说明使用场景。

避免所有功能统一叫：

```text
创建 Token
```

因为用户无法理解区别。

---

# 三十、开发者应用详情页

分区：

```text
概览
基本信息
Redirect URI
Scope
Audience
Client Secret
授权用户
事件
危险操作
```

概览展示：

```text
Client ID
Client 类型
状态
所有者
授权用户数
最近 Token 签发
最近错误
```

危险操作：

```text
暂停应用
撤销所有授权
撤销全部 Secret
删除应用
```

删除应用采用逻辑终止。

---

# 三十一、平台 Admin Console

新增路由：

```text
/admin/oauth-clients
/admin/oauth-clients/:clientId
/admin/service-accounts
/admin/api-keys
/admin/scopes
/admin/audiences
/admin/authorizations
/admin/signing-keys
/admin/token-events
```

---

## 31.1 OAuth Client 审核

需要审核的情况：

```text
公开第三方应用
请求高风险 Scope
请求多个组织管理 Scope
Redirect URI 使用特殊协议
应用所有权异常
安全信息不完整
```

审核页展示：

```text
应用资料
开发者
所属组织
Redirect URI
请求 Scope
风险等级
隐私政策
历史版本
安全事件
```

操作：

```text
批准
拒绝
要求修改
暂停
```

拒绝必须填写原因。

---

## 31.2 Client 暂停

暂停后：

```text
不能发起新授权
不能换取新 Token
Refresh Token 不能继续使用
已有短期 Access Token 等待过期
高风险时可进入紧急撤销
```

---

## 31.3 API Key 风险治理

管理员默认不能查看完整 API Key。

可以看到：

```text
Prefix
Owner
Organization
Audience
Scope
最后使用时间
最后使用 IP
风险状态
```

操作：

```text
撤销
标记泄露
要求轮换
查看审计
```

---

# 三十二、公共协议端点

```text
GET  /.well-known/openid-configuration
GET  /.well-known/jwks.json

GET  /oauth2/authorize
POST /oauth2/token
POST /oauth2/revoke
POST /oauth2/introspect

GET  /userinfo
```

用户授权相关 API：

```text
GET  /api/v1/identity/authorizations
GET  /api/v1/identity/authorizations/{grantId}
DELETE /api/v1/identity/authorizations/{grantId}
```

---

# 三十三、用户侧 Client API

```text
GET    /api/v1/identity/developer/clients
POST   /api/v1/identity/developer/clients
GET    /api/v1/identity/developer/clients/{clientId}
PATCH  /api/v1/identity/developer/clients/{clientId}

POST   /api/v1/identity/developer/clients/{clientId}/secrets
DELETE /api/v1/identity/developer/clients/{clientId}/secrets/{secretId}

PUT    /api/v1/identity/developer/clients/{clientId}/redirect-uris
PUT    /api/v1/identity/developer/clients/{clientId}/scopes
PUT    /api/v1/identity/developer/clients/{clientId}/audiences

POST   /api/v1/identity/developer/clients/{clientId}/submit-review
POST   /api/v1/identity/developer/clients/{clientId}/suspend
```

---

# 三十四、Service Account API

```text
GET    /api/v1/identity/organizations/{organizationId}/service-accounts
POST   /api/v1/identity/organizations/{organizationId}/service-accounts
GET    /api/v1/identity/organizations/{organizationId}/service-accounts/{id}
PATCH  /api/v1/identity/organizations/{organizationId}/service-accounts/{id}

PUT    /api/v1/identity/organizations/{organizationId}/service-accounts/{id}/roles

POST   /api/v1/identity/organizations/{organizationId}/service-accounts/{id}/credentials
DELETE /api/v1/identity/organizations/{organizationId}/service-accounts/{id}/credentials/{credentialId}

POST   /api/v1/identity/organizations/{organizationId}/service-accounts/{id}/disable
POST   /api/v1/identity/organizations/{organizationId}/service-accounts/{id}/enable
```

---

# 三十五、API Key API

用户 API Key：

```text
GET    /api/v1/identity/me/api-keys
POST   /api/v1/identity/me/api-keys
GET    /api/v1/identity/me/api-keys/{keyId}
DELETE /api/v1/identity/me/api-keys/{keyId}
POST   /api/v1/identity/me/api-keys/{keyId}/rotate
```

Service Account API Key：

```text
GET  /api/v1/identity/organizations/{organizationId}/service-accounts/{id}/api-keys
POST /api/v1/identity/organizations/{organizationId}/service-accounts/{id}/api-keys
```

---

# 三十六、Admin API

```text
GET  /admin-api/v1/identity/oauth-clients
GET  /admin-api/v1/identity/oauth-clients/{clientId}

POST /admin-api/v1/identity/oauth-clients/{clientId}/approve
POST /admin-api/v1/identity/oauth-clients/{clientId}/reject
POST /admin-api/v1/identity/oauth-clients/{clientId}/suspend
POST /admin-api/v1/identity/oauth-clients/{clientId}/reactivate
POST /admin-api/v1/identity/oauth-clients/{clientId}/revoke-grants

GET  /admin-api/v1/identity/service-accounts
GET  /admin-api/v1/identity/api-keys
POST /admin-api/v1/identity/api-keys/{keyId}/revoke

GET  /admin-api/v1/identity/scopes
GET  /admin-api/v1/identity/audiences

GET  /admin-api/v1/identity/signing-keys
POST /admin-api/v1/identity/signing-keys/rotate

GET  /admin-api/v1/identity/token-events
```

---

# 三十七、Internal API

供其他 Core 使用：

```text
POST /internal/v1/identity/api-keys/introspect
POST /internal/v1/identity/tokens/introspect
POST /internal/v1/identity/authorization/check

GET  /internal/v1/identity/subjects/{subjectType}/{subjectId}
GET  /internal/v1/identity/organizations/{organizationId}/authorization-version
```

权限和 Scope 注册：

```text
PUT /internal/v1/identity/permission-sources/{service}
PUT /internal/v1/identity/scope-sources/{service}
PUT /internal/v1/identity/audience-sources/{service}
```

---

# 三十八、P3 数据表总览

P3 新增：

```text
identity_oauth_client
identity_oauth_client_redirect_uri
identity_oauth_client_secret
identity_oauth_client_scope
identity_oauth_client_audience

identity_scope
identity_scope_permission
identity_audience

identity_authorization_code
identity_authorization_grant
identity_authorization_grant_scope

identity_refresh_token_family
identity_refresh_token

identity_signing_key
identity_token_revocation

identity_service_account
identity_service_account_role
identity_service_credential

identity_api_key
identity_api_key_scope
identity_api_key_audience

identity_oauth_event
```

---

# 三十九、identity_oauth_client

| 字段                 | 类型            | 说明                             |
| ------------------ | ------------- | ------------------------------ |
| id                 | VARCHAR(36)   | 内部 ID                          |
| client_id          | VARCHAR(120)  | 对外 Client ID                   |
| owner_type         | VARCHAR(30)   | PLATFORM / ORGANIZATION / USER |
| owner_id           | VARCHAR(36)   | 所有者 ID                         |
| client_type        | VARCHAR(30)   | PUBLIC / CONFIDENTIAL          |
| name               | VARCHAR(150)  | 应用名称                           |
| description        | VARCHAR(1000) | 应用说明                           |
| homepage_url       | VARCHAR(500)  | 应用主页                           |
| logo_object_id     | VARCHAR(36)   | Logo                           |
| privacy_policy_url | VARCHAR(500)  | 隐私政策                           |
| terms_url          | VARCHAR(500)  | 服务条款                           |
| status             | VARCHAR(30)   | Client 状态                      |
| review_status      | VARCHAR(30)   | 审核状态                           |
| consent_required   | INTEGER       | 是否强制授权页                        |
| created_by         | VARCHAR(36)   | 创建者                            |
| created_at         | BIGINT        | 创建时间                           |
| updated_at         | BIGINT        | 更新时间                           |
| version            | BIGINT        | 乐观锁                            |

约束：

```text
UNIQUE(client_id)
```

---

# 四十、identity_oauth_client_redirect_uri

| 字段           | 类型            | 说明                       |
| ------------ | ------------- | ------------------------ |
| id           | VARCHAR(36)   | 主键                       |
| client_id    | VARCHAR(36)   | Client 内部 ID             |
| redirect_uri | VARCHAR(1000) | 完整回调 URI                 |
| environment  | VARCHAR(20)   | DEVELOPMENT / PRODUCTION |
| status       | VARCHAR(20)   | ACTIVE / DISABLED        |
| created_at   | BIGINT        | 创建时间                     |

约束：

```text
UNIQUE(client_id, redirect_uri)
```

---

# 四十一、identity_oauth_client_secret

| 字段            | 类型           | 说明               |
| ------------- | ------------ | ---------------- |
| id            | VARCHAR(36)  | Secret ID        |
| client_id     | VARCHAR(36)  | Client           |
| secret_prefix | VARCHAR(30)  | 前缀               |
| secret_hash   | VARCHAR(255) | Hash             |
| name          | VARCHAR(100) | 凭证名称             |
| status        | VARCHAR(20)  | ACTIVE / REVOKED |
| expires_at    | BIGINT       | 过期时间             |
| last_used_at  | BIGINT       | 最近使用             |
| created_at    | BIGINT       | 创建时间             |
| revoked_at    | BIGINT       | 撤销时间             |

完整 Secret 不入库。

---

# 四十二、identity_scope

| 字段              | 类型           | 说明                  |
| --------------- | ------------ | ------------------- |
| id              | VARCHAR(36)  | Scope ID            |
| scope_code      | VARCHAR(150) | Scope               |
| source_service  | VARCHAR(100) | 来源服务                |
| name            | VARCHAR(150) | 展示名称                |
| description     | VARCHAR(500) | 用户说明                |
| risk_level      | VARCHAR(20)  | 风险等级                |
| consent_display | VARCHAR(500) | 授权页文案               |
| assignable      | INTEGER      | 是否可申请               |
| status          | VARCHAR(30)  | ACTIVE / DEPRECATED |
| created_at      | BIGINT       | 创建时间                |
| updated_at      | BIGINT       | 更新时间                |
| version         | BIGINT       | 乐观锁                 |

约束：

```text
UNIQUE(scope_code)
```

---

# 四十三、identity_scope_permission

| 字段            | 类型          | 说明         |
| ------------- | ----------- | ---------- |
| scope_id      | VARCHAR(36) | Scope      |
| permission_id | VARCHAR(36) | Permission |
| created_at    | BIGINT      | 创建时间       |

主键：

```text
scope_id + permission_id
```

Scope 映射到多个 Permission。

---

# 四十四、identity_audience

| 字段                | 类型           | 说明                |
| ----------------- | ------------ | ----------------- |
| id                | VARCHAR(36)  | Audience ID       |
| audience_code     | VARCHAR(120) | 例如 core-storage   |
| service_name      | VARCHAR(120) | 服务名               |
| issuer_allowed    | INTEGER      | 是否允许签发            |
| token_ttl_seconds | INTEGER      | 默认 Token 时长       |
| status            | VARCHAR(20)  | ACTIVE / DISABLED |
| created_at        | BIGINT       | 创建时间              |
| updated_at        | BIGINT       | 更新时间              |
| version           | BIGINT       | 乐观锁               |

约束：

```text
UNIQUE(audience_code)
```

---

# 四十五、Client Scope 与 Audience

关联表：

```text
identity_oauth_client_scope
identity_oauth_client_audience
```

`identity_oauth_client_scope`：

| 字段         | 类型          |
| ---------- | ----------- |
| client_id  | VARCHAR(36) |
| scope_id   | VARCHAR(36) |
| created_at | BIGINT      |

`identity_oauth_client_audience`：

| 字段          | 类型          |
| ----------- | ----------- |
| client_id   | VARCHAR(36) |
| audience_id | VARCHAR(36) |
| created_at  | BIGINT      |

---

# 四十六、identity_authorization_code

| 字段                    | 类型            | 说明                      |
| --------------------- | ------------- | ----------------------- |
| id                    | VARCHAR(36)   | Code 记录                 |
| code_hash             | VARCHAR(255)  | Code Hash               |
| client_id             | VARCHAR(36)   | Client                  |
| user_id               | VARCHAR(36)   | 用户                      |
| organization_id       | VARCHAR(36)   | 组织                      |
| redirect_uri          | VARCHAR(1000) | 回调地址                    |
| audience              | VARCHAR(120)  | Audience                |
| scopes_json           | TEXT          | 已批准 Scope               |
| code_challenge        | VARCHAR(255)  | PKCE                    |
| code_challenge_method | VARCHAR(20)   | S256                    |
| nonce                 | VARCHAR(255)  | OIDC Nonce              |
| status                | VARCHAR(20)   | ACTIVE / USED / REVOKED |
| expires_at            | BIGINT        | 过期时间                    |
| used_at               | BIGINT        | 使用时间                    |
| created_at            | BIGINT        | 创建时间                    |
| version               | BIGINT        | 乐观锁                     |

索引：

```text
UNIQUE(code_hash)
idx_identity_auth_code_expires
```

---

# 四十七、identity_authorization_grant

| 字段               | 类型          | 说明               |
| ---------------- | ----------- | ---------------- |
| id               | VARCHAR(36) | Grant ID         |
| client_id        | VARCHAR(36) | Client           |
| user_id          | VARCHAR(36) | 用户               |
| organization_id  | VARCHAR(36) | 组织               |
| status           | VARCHAR(20) | ACTIVE / REVOKED |
| first_granted_at | BIGINT      | 首次授权             |
| last_used_at     | BIGINT      | 最近使用             |
| revoked_at       | BIGINT      | 撤销时间             |
| created_at       | BIGINT      | 创建时间             |
| updated_at       | BIGINT      | 更新时间             |
| version          | BIGINT      | 乐观锁              |

约束：

```text
UNIQUE(client_id, user_id, organization_id)
```

Scope 放入：

```text
identity_authorization_grant_scope
```

---

# 四十八、Refresh Token 表

## identity_refresh_token_family

| 字段             | 类型           | 说明                             |
| -------------- | ------------ | ------------------------------ |
| id             | VARCHAR(36)  | Family ID                      |
| grant_id       | VARCHAR(36)  | 授权                             |
| client_id      | VARCHAR(36)  | Client                         |
| user_id        | VARCHAR(36)  | 用户                             |
| session_id     | VARCHAR(36)  | 用户会话                           |
| status         | VARCHAR(20)  | ACTIVE / REVOKED / COMPROMISED |
| revoked_reason | VARCHAR(500) | 撤销原因                           |
| created_at     | BIGINT       | 创建时间                           |
| revoked_at     | BIGINT       | 撤销时间                           |

## identity_refresh_token

| 字段             | 类型           | 说明                      |
| -------------- | ------------ | ----------------------- |
| id             | VARCHAR(36)  | Token ID                |
| family_id      | VARCHAR(36)  | Token Family            |
| token_hash     | VARCHAR(255) | Hash                    |
| status         | VARCHAR(20)  | ACTIVE / USED / REVOKED |
| expires_at     | BIGINT       | 过期                      |
| used_at        | BIGINT       | 使用时间                    |
| replaced_by_id | VARCHAR(36)  | 新 Token                 |
| created_at     | BIGINT       | 创建时间                    |
| version        | BIGINT       | 乐观锁                     |

---

# 四十九、identity_signing_key

| 字段                    | 类型           | 说明      |
| --------------------- | ------------ | ------- |
| id                    | VARCHAR(36)  | Key ID  |
| key_id                | VARCHAR(100) | JWT kid |
| algorithm             | VARCHAR(30)  | 签名算法    |
| public_key            | TEXT         | 公钥      |
| encrypted_private_key | TEXT         | 加密私钥    |
| status                | VARCHAR(20)  | 生命周期状态  |
| active_from           | BIGINT       | 生效时间    |
| retire_after          | BIGINT       | 退休时间    |
| created_at            | BIGINT       | 创建时间    |
| updated_at            | BIGINT       | 更新时间    |

约束：

```text
UNIQUE(key_id)
```

---

# 五十、identity_token_revocation

用于紧急撤销少量 Access Token。

| 字段         | 类型           | 说明          |
| ---------- | ------------ | ----------- |
| id         | VARCHAR(36)  | 主键          |
| token_jti  | VARCHAR(120) | Token JTI   |
| subject_id | VARCHAR(36)  | 主体          |
| reason     | VARCHAR(500) | 原因          |
| expires_at | BIGINT       | Token 原到期时间 |
| created_at | BIGINT       | 创建时间        |

过期后可以清理。

---

# 五十一、identity_service_account

| 字段              | 类型           | 说明                      |
| --------------- | ------------ | ----------------------- |
| id              | VARCHAR(36)  | Service Account ID      |
| organization_id | VARCHAR(36)  | 所属组织，可空表示平台级            |
| account_type    | VARCHAR(30)  | ORGANIZATION / PLATFORM |
| name            | VARCHAR(150) | 名称                      |
| description     | VARCHAR(500) | 用途                      |
| status          | VARCHAR(20)  | 状态                      |
| last_used_at    | BIGINT       | 最近使用                    |
| created_by      | VARCHAR(36)  | 创建者                     |
| created_at      | BIGINT       | 创建时间                    |
| updated_at      | BIGINT       | 更新时间                    |
| version         | BIGINT       | 乐观锁                     |

---

# 五十二、identity_service_account_role

| 字段                 | 类型          |
| ------------------ | ----------- |
| service_account_id | VARCHAR(36) |
| role_id            | VARCHAR(36) |
| assigned_by        | VARCHAR(36) |
| created_at         | BIGINT      |

必须验证 Service Account 与 Role 属于同一组织。

---

# 五十三、identity_service_credential

| 字段                 | 类型           | 说明              |
| ------------------ | ------------ | --------------- |
| id                 | VARCHAR(36)  | 凭证 ID           |
| service_account_id | VARCHAR(36)  | Service Account |
| client_id          | VARCHAR(120) | Client ID       |
| secret_prefix      | VARCHAR(30)  | 前缀              |
| secret_hash        | VARCHAR(255) | Hash            |
| name               | VARCHAR(100) | 名称              |
| status             | VARCHAR(20)  | 状态              |
| expires_at         | BIGINT       | 过期              |
| last_used_at       | BIGINT       | 最近使用            |
| created_at         | BIGINT       | 创建时间            |
| revoked_at         | BIGINT       | 撤销时间            |

---

# 五十四、identity_api_key

| 字段              | 类型           | 说明                         |
| --------------- | ------------ | -------------------------- |
| id              | VARCHAR(36)  | Key ID                     |
| key_prefix      | VARCHAR(40)  | 可识别前缀                      |
| key_hash        | VARCHAR(255) | Hash                       |
| name            | VARCHAR(120) | 名称                         |
| owner_type      | VARCHAR(30)  | USER / SERVICE_ACCOUNT     |
| owner_id        | VARCHAR(36)  | Owner                      |
| organization_id | VARCHAR(36)  | 组织                         |
| status          | VARCHAR(20)  | ACTIVE / REVOKED / EXPIRED |
| expires_at      | BIGINT       | 过期                         |
| last_used_at    | BIGINT       | 最近使用                       |
| last_used_ip    | VARCHAR(64)  | 最近 IP                      |
| created_at      | BIGINT       | 创建时间                       |
| revoked_at      | BIGINT       | 撤销时间                       |
| version         | BIGINT       | 乐观锁                        |

完整 API Key 不入库。

Scope 和 Audience 使用关联表：

```text
identity_api_key_scope
identity_api_key_audience
```

---

# 五十五、事务设计

## 55.1 Authorization Code 创建

同一事务：

```text
验证用户 Session
验证 Client
验证 Redirect URI
验证 Audience
验证 Scope
验证用户 Permission
创建或更新 Grant
创建 Authorization Code
写 Audit
写 OAuth Event
```

---

## 55.2 Authorization Code 换 Token

同一事务：

```text
锁定 Authorization Code
验证 Client
验证 Redirect URI
验证 PKCE
验证 Code 未使用且未过期
标记 Code USED
必要时创建 Refresh Token Family
创建 Refresh Token
写 Audit
```

Access Token 和 ID Token 在事务成功后生成并返回。

---

## 55.3 Refresh Token Rotation

同一事务：

```text
锁定 Refresh Token
验证 Token ACTIVE
验证 Family ACTIVE
验证 Client
标记旧 Token USED
创建新 Refresh Token
更新 replaced_by_id
写 Audit
```

检测到旧 Token 重放：

```text
Family → COMPROMISED
撤销 Family 全部 Token
撤销 Grant，可配置
记录安全事件
```

---

## 55.4 创建 API Key

同一事务：

```text
验证 Owner
验证组织
验证 Permission
验证 Scope
验证 Audience
生成随机 Key
保存 Hash
写 Audit
```

明文 Key 在事务提交成功后返回一次。

---

## 55.5 撤销应用授权

同一事务：

```text
Grant → REVOKED
Refresh Token Family → REVOKED
关联 Refresh Token → REVOKED
写 Audit
发布事件
```

---

# 五十六、P3 事件

新增：

```text
identity.oauth_client.created
identity.oauth_client.approved
identity.oauth_client.suspended
identity.oauth_client.revoked
identity.oauth_client.secret_rotated

identity.authorization.granted
identity.authorization.updated
identity.authorization.revoked

identity.token.issued
identity.refresh_token.rotated
identity.refresh_token.reuse_detected
identity.token.revoked

identity.api_key.created
identity.api_key.rotated
identity.api_key.revoked

identity.service_account.created
identity.service_account.roles_changed
identity.service_account.disabled
identity.service_credential.created
identity.service_credential.revoked

identity.signing_key.created
identity.signing_key.activated
identity.signing_key.retired
identity.signing_key.revoked
```

`identity.token.issued` 不建议发送到全平台事件总线。

它可以只进入安全审计或指标系统，避免事件量过大。

---

# 五十七、审计事件

P3 必须审计：

```text
OAUTH_CLIENT_CREATED
OAUTH_CLIENT_UPDATED
OAUTH_CLIENT_APPROVED
OAUTH_CLIENT_REJECTED
OAUTH_CLIENT_SUSPENDED
CLIENT_SECRET_CREATED
CLIENT_SECRET_REVOKED

AUTHORIZATION_GRANTED
AUTHORIZATION_SCOPE_EXPANDED
AUTHORIZATION_REVOKED

TOKEN_ISSUED
TOKEN_REFRESHED
REFRESH_TOKEN_REUSE_DETECTED
TOKEN_REVOKED

API_KEY_CREATED
API_KEY_ROTATED
API_KEY_REVOKED
API_KEY_AUTHENTICATION_FAILED

SERVICE_ACCOUNT_CREATED
SERVICE_ACCOUNT_DISABLED
SERVICE_ACCOUNT_ROLES_CHANGED
SERVICE_CREDENTIAL_CREATED
SERVICE_CREDENTIAL_REVOKED

SIGNING_KEY_ROTATED
```

审计中禁止保存：

```text
Access Token
Refresh Token
Authorization Code
Client Secret
API Key
Signing Private Key
```

只保存：

```text
Prefix
JTI
Client ID
Subject ID
Audience
Scope
结果
```

---

# 五十八、与 core-api-gateway 的交互

P3 后，Gateway 承担：

```text
提取 Bearer Token
识别 JWT 与 API Key
验证 JWT 签名
验证 issuer
验证 audience
验证 exp
验证基础 scope
API Key Introspection
Request ID
客户端限流
```

Gateway 可以向后传递经过签名或内部可信通道保护的身份上下文：

```text
subject_id
subject_type
organization_id
client_id
scope
authorization_version
```

但业务 Core 仍必须验证：

```text
具体资源归属
具体 Permission
业务状态
```

Gateway 不是最终授权中心。

---

# 五十九、与 core-billing 的交互

Billing 可以按以下维度统计：

```text
organization_id
user_id
service_account_id
client_id
api_key_id
```

用途：

```text
API 用量
开发者套餐
应用调用额度
Service Account 调用额度
组织计费
```

Billing 不负责签发或撤销身份凭证。

Identity 可以在签发 Token 前调用 Billing 检查：

```text
是否拥有某项平台权益
```

但 P3 建议只对：

```text
创建 Client 数量
Service Account 数量
API Key 数量
高级 Scope
```

做权益限制。

不要让每次 Access Token 签发都强依赖 Billing 实时可用。

---

# 六十、与 core-notification 的交互

P3 通知场景：

```text
Client Secret 创建
Client Secret 即将过期
API Key 创建
API Key 即将过期
API Key 被撤销
Service Account 创建
高风险授权
应用授权被撤销
Refresh Token 重放检测
Signing Key 异常
```

通知中不能包含完整 Secret 或 API Key。

创建 Secret 时只在页面展示一次。

---

# 六十一、与 core-storage 的交互

Storage 作为 Resource Server：

```text
audience = core-storage
```

Scope：

```text
storage.read
storage.write
storage.delete
```

Storage 验证：

```text
Token Audience
Token Scope
Subject Permission
Resource organization_id
Storage 业务规则
```

应用有：

```text
storage.write
```

不表示可以写入任意组织。

Token 必须绑定：

```text
organization_id
```

---

# 六十二、与 core-ai-gateway 的交互

AI Gateway 是 P3 的重要使用方。

典型访问：

```text
用户 API Key
组织 Service Account
第三方 AI 应用 OAuth Token
```

Audience：

```text
core-ai-gateway
```

Scope 示例：

```text
ai.use
ai.model.read
ai.prompt.read
ai.agent.execute
ai.provider.manage
```

AI Gateway 可以按：

```text
organization_id
subject_id
client_id
api_key_id
```

记录用量和费用。

---

# 六十三、与 core-workflow 的交互

Workflow 可以使用 Service Account 运行自动化。

例如：

```text
Workflow Definition
    ↓
绑定 Service Account
    ↓
执行时获取短期 Access Token
    ↓
调用 Storage、Notification、AI Gateway
```

Workflow 不应在数据库中保存长期用户 Access Token。

需要代表用户长期执行时，应使用：

```text
明确的 Grant
Refresh Token
或者组织 Service Account
```

不能偷偷复用用户 Cookie。

---

# 六十四、与 core-marketplace 的交互

Marketplace 应用安装后可以创建或关联：

```text
OAuth Client
所需 Scope
所需 Audience
Redirect URI
应用资料
```

安装流程：

```text
用户选择安装应用
    ↓
Marketplace 提交 Client 信息
    ↓
Identity 展示授权确认
    ↓
创建组织级 Grant
```

Marketplace 不能：

```text
直接创建 Token
直接写入授权表
直接绕过 Consent
```

---

# 六十五、错误码

新增：

```text
IDENTITY_OAUTH_CLIENT_NOT_FOUND
IDENTITY_OAUTH_CLIENT_INVALID
IDENTITY_OAUTH_CLIENT_SUSPENDED
IDENTITY_OAUTH_CLIENT_REVIEW_REQUIRED
IDENTITY_CLIENT_SECRET_INVALID
IDENTITY_REDIRECT_URI_INVALID

IDENTITY_AUTHORIZATION_REQUEST_INVALID
IDENTITY_AUTHORIZATION_CODE_INVALID
IDENTITY_AUTHORIZATION_CODE_EXPIRED
IDENTITY_AUTHORIZATION_CODE_ALREADY_USED
IDENTITY_PKCE_VALIDATION_FAILED
IDENTITY_STATE_REQUIRED
IDENTITY_NONCE_INVALID

IDENTITY_SCOPE_INVALID
IDENTITY_SCOPE_NOT_ALLOWED
IDENTITY_SCOPE_PERMISSION_DENIED
IDENTITY_SCOPE_REQUIRES_REVIEW
IDENTITY_AUDIENCE_INVALID
IDENTITY_AUDIENCE_NOT_ALLOWED

IDENTITY_REFRESH_TOKEN_INVALID
IDENTITY_REFRESH_TOKEN_EXPIRED
IDENTITY_REFRESH_TOKEN_REVOKED
IDENTITY_REFRESH_TOKEN_REUSE_DETECTED

IDENTITY_TOKEN_INVALID
IDENTITY_TOKEN_EXPIRED
IDENTITY_TOKEN_REVOKED
IDENTITY_TOKEN_AUDIENCE_MISMATCH

IDENTITY_API_KEY_INVALID
IDENTITY_API_KEY_EXPIRED
IDENTITY_API_KEY_REVOKED
IDENTITY_API_KEY_SCOPE_DENIED

IDENTITY_SERVICE_ACCOUNT_NOT_FOUND
IDENTITY_SERVICE_ACCOUNT_DISABLED
IDENTITY_SERVICE_CREDENTIAL_INVALID

IDENTITY_AUTHORIZATION_GRANT_NOT_FOUND
IDENTITY_AUTHORIZATION_GRANT_REVOKED

IDENTITY_SIGNING_KEY_UNAVAILABLE
IDENTITY_SIGNING_KEY_ROTATION_FAILED
```

OAuth 协议端点对外还需要使用标准错误字段：

```json
{
  "error": "invalid_grant",
  "error_description": "The authorization code is invalid or expired."
}
```

内部日志可以保留更具体的 `errorCode`。

---

# 六十六、安全注意点

## 66.1 不自创 OAuth 流程

不要创建：

```text
POST /login-and-get-token
POST /exchange-password
```

让第三方应用直接提交用户密码。

---

## 66.2 禁止 Implicit Flow

Access Token 不应直接通过浏览器 URL Fragment 返回。

统一使用：

```text
Authorization Code + PKCE
```

---

## 66.3 Redirect URI 必须精确匹配

不能使用模糊域名和任意回调。

---

## 66.4 Refresh Token 必须轮换

长期不变的 Refresh Token 泄露后很难发现。

---

## 66.5 Secret 只展示一次

包括：

```text
Client Secret
Service Credential
API Key
```

---

## 66.6 不把完整 Permission 塞入 JWT

否则：

```text
Token 过大
角色变化不能及时生效
权限目录变化导致兼容问题
```

---

## 66.7 Client 不能自行申请任意 Scope

Client 只能请求：

```text
已经在 Client 配置中允许的 Scope
```

用户也只能授权自己实际拥有 Permission 的 Scope。

---

## 66.8 用户授权不能提升用户自身权限

例如用户没有：

```text
storage.delete
```

即使 Client 请求：

```text
storage.delete
```

也不能授权成功。

---

## 66.9 Service Account 不等于超级管理员

组织创建 Service Account 时，只能分配该组织内允许的角色。

---

# 六十七、UX 注意点

## 67.1 不要把所有凭证都叫 Token

界面使用：

```text
应用
应用密钥
API Key
Service Account
Service Credential
已授权应用
```

避免：

```text
Token 1
Token 2
Token 3
```

---

## 67.2 授权页使用人类语言

不要只显示：

```text
scope=storage.object.read
```

应显示：

```text
查看文件和文件夹
```

同时可以在详情中展示技术 Scope。

---

## 67.3 高风险权限必须明确

用户必须知道应用能否：

```text
删除数据
修改成员
管理账单
代表用户长期运行
```

---

## 67.4 创建 Secret 后必须强提醒

用户关闭页面前：

```text
你是否已经保存该密钥？
离开后将无法再次查看。
```

但不能通过阻止关闭制造无法退出的页面。

---

## 67.5 撤销操作说明实际影响

撤销 Client Secret：

```text
使用该 Secret 的服务将立即无法获得新 Token。
```

撤销 Grant：

```text
应用不能继续刷新 Token，但已复制的数据不会自动删除。
```

撤销 API Key：

```text
使用该 Key 的脚本将立即失败。
```

---

# 六十八、测试设计

## 68.1 Authorization Code

```text
正常授权
未登录跳转登录
Client 不存在
Client 被暂停
Redirect URI 不匹配
Scope 不允许
Audience 不允许
用户没有对应 Permission
PKCE 正常
PKCE 失败
Code 过期
Code 重复使用
State 正确透传
Nonce 正确进入 ID Token
```

---

## 68.2 Refresh Token

```text
正常刷新
Token 轮换
旧 Token 重放
Family 全部撤销
Client 不匹配
Client 被暂停
Grant 被撤销
用户 Session 被撤销
用户被禁用
组织被冻结
```

---

## 68.3 JWT

```text
签名正确
签名错误
kid 不存在
issuer 错误
audience 错误
Token 过期
Scope 不足
Signing Key 轮换期间旧 Token 仍可验证
```

---

## 68.4 Client

```text
Public Client 不允许 Client Secret 流程
Confidential Client Secret 正常
Secret 轮换
旧 Secret 撤销
Redirect URI 精确匹配
高风险 Scope 进入审核
跨组织 Client 管理被拒绝
```

---

## 68.5 API Key

```text
正常创建
只显示一次
数据库无明文
正常验证
错误 Key
过期 Key
撤销 Key
Scope 不足
Audience 不匹配
用户被禁用
组织被冻结
Service Account 被禁用
```

---

## 68.6 Service Account

```text
组织管理员创建
普通成员创建被拒绝
角色跨组织绑定被拒绝
Client Credentials 正常
Service Account 禁用后 Token 获取失败
Credential 轮换
平台级 Service Account 只能平台管理员创建
```

---

## 68.7 授权越权

```text
用户授权自己没有的 Permission
Client 请求未配置 Scope
Client 请求未配置 Audience
A 组织 Grant 访问 B 组织资源
User Token 被当作 Service Account Token
ID Token 被用于访问 API
普通 Client 调用 Introspection
```

---

## 68.8 数据库升级

```text
P2 升级 P3
SQLite 全新安装
MySQL 全新安装
Signing Key 初始化
并发使用 Authorization Code
并发刷新 Refresh Token
并发轮换 Client Secret
并发撤销 Grant
```

---

# 六十九、P3 实施顺序

## P3.1：Scope 与 Audience

完成：

```text
Scope
ScopePermission
Audience
ClientScope
ClientAudience
各 Core Scope 清单
```

理由：

先定义 Token 能表达什么，再实现 Token。

---

## P3.2：Signing Key 与 JWT

完成：

```text
Signing Key
JWKS
JWT Access Token
JWT 验证库
Key Rotation
OpenID Configuration
```

理由：

先建立可信签名基础。

---

## P3.3：OAuth Client

完成：

```text
Client
Redirect URI
Client Secret
Client 类型
Client 状态
开发者中心
平台审核
```

理由：

授权流程必须有明确应用身份。

---

## P3.4：Authorization Code + PKCE

完成：

```text
Authorize Endpoint
Consent
Authorization Code
PKCE
Token Endpoint
ID Token
UserInfo
```

理由：

先完成第三方应用代表用户访问的标准闭环。

---

## P3.5：Refresh Token 与授权管理

完成：

```text
Grant
Consent
Refresh Token Family
Token Rotation
Reuse Detection
Revocation
已授权应用页面
```

理由：

长期授权必须在基础授权流程稳定后加入。

---

## P3.6：Service Account

完成：

```text
Service Account
Role Assignment
Service Credential
Client Credentials
组织管理页面
平台管理页面
```

理由：

再支持非人主体访问。

---

## P3.7：API Key

完成：

```text
API Key
Scope
Audience
Introspection
轮换
撤销
开发者 UX
```

理由：

API Key 是便利能力，不能先于标准身份模型。

---

## P3.8：跨 Core 集成

完成：

```text
Gateway JWT 验证
Storage Resource Server
Billing 调用主体
AI Gateway API Key
Workflow Service Account
Marketplace OAuth Client
Notification 安全提醒
```

---

## P3.9：安全与质量

完成：

```text
密钥轮换
Refresh Token 重放检测
Token 撤销
Secret 脱敏
协议测试
SQLite/MySQL 测试
安全扫描
端到端 OAuth 测试
```

---

# 七十、P3 验收标准

## OAuth 与 OIDC

```text
第三方应用可以使用 Authorization Code + PKCE
用户可以查看授权内容
用户可以授权特定组织
用户可以撤销应用授权
Client 可以获得 Access Token
符合条件时可以获得 Refresh Token
Client 可以获得 ID Token
```

## Token

```text
Access Token 具有明确 Audience
Access Token 具有明确 Scope
业务 Core 可以本地验证 JWT
Signing Key 可以安全轮换
Refresh Token 使用轮换机制
重放可以被发现并处置
```

## Service Account

```text
组织可以创建 Service Account
Service Account 可以绑定组织角色
Service Account 可以使用 Client Credentials
Service Account 禁用后不能获取新 Token
平台 Service Account 只能由平台管理员创建
```

## API Key

```text
用户可以创建 API Key
Service Account 可以创建 API Key
完整 Key 只显示一次
数据库不保存明文 Key
Key 可以设置 Scope、Audience 和有效期
Key 可以立即撤销
```

## 管理

```text
平台管理员可以审核 Client
平台管理员可以暂停风险 Client
平台管理员不能查看完整 Secret
平台管理员可以撤销风险 API Key
签名密钥轮换有完整审计
```

## 边界

```text
Admin Backend 不直接访问 Identity 数据库
其他 Core 不签发平台 Token
Client Secret 不代表用户
ID Token 不用于 API 访问
API Key 不用于网页登录
```

---

# 七十一、P3 最重要的注意点

## 1. Scope 不等于 Permission

Permission 决定主体本身能做什么。

Scope 决定应用或凭证最多能代表主体做什么。

最终授权必须取交集。

---

## 2. 不要让所有 Core 每次请求 Identity

JWT 应允许 Resource Server 本地验证：

```text
签名
issuer
audience
expiration
scope
```

只有以下场景需要远程调用：

```text
API Key
高风险实时授权
Token Introspection
权限版本失效
```

---

## 3. 不要长期保存所有 Access Token

应使用：

```text
短期 JWT
Refresh Token 数据库存储
有限紧急撤销表
```

而不是把每次 API 调用的 Access Token 全部写入数据库。

---

## 4. Service Account 必须拥有独立生命周期

不能创建一个假用户：

```text
email = workflow@system.local
password = never-login
```

来模拟 Service Account。

---

## 5. API Key 必须有限制

每个 API Key 至少绑定：

```text
Owner
Organization
Audience
Scope
Expiration
```

不能生成一个永久、全平台、无范围的万能 Key。

---

## 6. Client Secret 不是加密用户数据的密钥

Client Secret 只用于证明 Client 身份。

不能用于：

```text
加密数据库
签名业务数据
保存用户密码
```

---

## 7. 浏览器登录仍然保留 Cookie Session

P3 增加 OAuth Token，不代表 Identity Web 必须把自己的 Cookie Session 全部替换为 JWT。

第一方浏览器应用继续使用：

```text
HttpOnly Session Cookie
```

更容易撤销和管理。

---

# 七十二、P3 最终成果

P3 完成后：

```text
core-identity-backend

成为整个 Core Platform 的授权服务器、
Token 发行中心、开发者身份中心和服务身份中心。
```

```text
core-identity-web

成为用户账户中心、开发者中心、
应用授权中心和组织服务身份管理门户。
```

```text
core-identity-admin-backend

成为全平台 Client、Token、API Key、
Service Account 和密钥风险的治理编排层。
```

```text
core-identity-admin-web

成为应用审核、凭证治理、授权审计、
签名密钥和身份安全运营控制台。
```

P3 最终建立四条完整链路：

```text
用户
→ 浏览器 Session
→ 第一方平台
```

```text
用户
→ OAuth Consent
→ 第三方 Client
→ Access Token
→ Core API
```

```text
Service Account
→ Client Credentials
→ Access Token
→ Core API
```

```text
用户或 Service Account
→ API Key
→ Gateway Introspection
→ Core API
```

从这一阶段开始，Core Identity 不再只是一个登录系统，而真正成为所有 Core 服务共同依赖的平台身份基石。
