这里我会调整一下前面的设计。

**P1** 我们建立的是 **Identity Runtime**（身份模型）。

那么 **P2** 不应该叫 Login，而应该叫：

> **Authentication Runtime（认证运行时）**

这里的关键词不是 Login，而是：

> **证明（Prove）当前请求来自哪个 Identity。**

这是整个平台安全体系的第一步。

---

# Phase 2：Authentication Runtime ⭐⭐⭐⭐⭐

## 一、目标

一句话：

> **让 Identity 可以被安全地认证（Authenticate）。**

完成以后：

```text
Identity
        │
        ▼
Authentication
        │
        ▼
Authenticated Identity
```

从这一刻开始：

整个 Platform 才真正拥有：

* 登录
* Token
* 会话
* 邮箱验证码
* Password

但是：

还没有权限（Permission）。

权限属于 P3。

---

# 二、为什么 Authentication 独立？

很多项目：

```text
User

↓

Login

↓

Role

↓

Permission
```

全部耦合。

以后：

OAuth

SSO

API Key

全部重写。

我们拆成：

```text
Identity

↓

Authentication

↓

Authorization
```

完全解耦。

以后：

任何认证方式：

最后都变成：

```text
Authenticated Identity
```

即可。

---

# 三、Runtime Architecture

```text
Credential

↓

Authenticator

↓

Authentication

↓

Identity

↓

Token
```

这里：

Authenticator

以后：

越来越多。

例如：

```text
Password

EmailCode

Github

Google

OIDC

LDAP
```

全部：

实现：

```java
Authenticator
```

接口即可。

---

# 四、Authentication Pipeline

建议：

整个认证：

统一：

```text
Client

↓

Authentication Request

↓

Authenticator

↓

Credential Verify

↓

Identity Resolve

↓

Token Generate

↓

Authentication Success
```

以后：

邮箱。

密码。

OAuth。

全部：

同一套流程。

---

# 五、Domain

P2：

建议：

新增：

三个对象。

---

## Authentication

一次：

认证。

例如：

```text
Login

↓

Authentication
```

建议：

```text
id

identity_id

auth_type

status

ip

device

user_agent

create_time
```

以后：

登录历史：

直接：

来自：

Authentication。

---

## Token

不要：

JWT

散落。

统一：

Token。

例如：

```text
id

identity_id

token

expire_time

status
```

以后：

JWT。

Opaque Token。

都属于：

Token。

---

## Verification

统一：

验证码。

例如：

```text
EMAIL

SMS

OTP
```

不要：

Email：

自己：

一套。

SMS：

自己：

一套。

统一：

Verification。

---

# 六、Authentication Provider

建议：

采用：

Provider。

例如：

```text
AuthenticationProvider

↓

PasswordProvider

↓

EmailCodeProvider

↓

Future GithubProvider

↓

Future OIDCProvider
```

第一阶段：

只有：

两个：

Provider。

---

Password

Email Code

---

以后：

直接：

新增：

Provider。

---

# 七、Backend API

## Login

```http
POST /api/v1/auth/login/password
```

---

```http
POST /api/v1/auth/login/email
```

---

## Logout

```http
POST /api/v1/auth/logout
```

---

## Refresh

```http
POST /api/v1/auth/refresh
```

---

## Current

```http
GET /api/v1/auth/me
```

返回：

```text
Identity

Token

Expire Time
```

---

## Verification

发送：

```http
POST /api/v1/auth/verification/email/send
```

验证：

```http
POST /api/v1/auth/verification/email/verify
```

以后：

SMS：

不用：

改。

---

# 八、Frontend（用户）

终于：

出现：

登录页。

---

## Login UX

第一页：

两个：

Tab。

```text
密码登录

邮箱验证码
```

不要：

两个页面。

---

Password：

```text
Email

Password

Remember Me
```

---

Email：

```text
Email

Verification Code

获取验证码
```

---

以后：

增加：

```text
Github

Google
```

也是：

Tab。

---

# 九、登录成功

不要：

跳：

首页。

统一：

```text
Login Success

↓

Load Profile

↓

Load System Config

↓

Load Menu

↓

Redirect
```

以后：

所有平台一致。

---

# 十、Token Strategy

建议：

第一阶段：

只有：

```text
Access Token

Refresh Token
```

即可。

不要：

Redis。

不要：

Session。

不要：

Gateway。

---

SQLite：

保存：

Refresh Token。

JWT：

作为：

Access Token。

---

# 十一、Remember Me

不要：

浏览器：

永久登录。

建议：

Remember Me：

控制：

Refresh Token

有效期。

例如：

```text
7 天

30 天
```

---

# 十二、管理后台

新增：

Authentication。

菜单：

```text
Identity

Authentication

Verification
```

Authentication：

列表：

```text
Identity

Type

Device

IP

Status

Time
```

---

Verification：

```text
Email

Status

Expire Time

Send Time
```

方便：

排查：

验证码问题。

---

# 十三、UX

---

## 登录

页面：

尽量：

简单。

一个：

Card。

中间：

Logo。

下面：

Tab。

不要：

复杂。

---

## 登录中

Button：

变成：

Loading。

不要：

Toast：

"登录中..."

---

## 登录成功

Toast：

统一：

```text
✓ 登录成功
```

然后：

跳转。

---

## 登录失败

不要：

```text
账号不存在

密码错误
```

统一：

```text
账号或密码错误
```

减少：

账户枚举风险。

---

## 获取验证码

按钮：

60 秒：

倒计时。

不要：

无限点。

---

# 十四、安全注意点（P2 必须完成）

这一阶段是整个平台第一次涉及安全，因此建议把以下能力作为 **P2 的完成标准**，即使实现保持简单，也不要缺失：

### 1. 密码存储

* 绝不保存明文密码。
* 使用强哈希算法（如 BCrypt、Argon2 等）保存密码摘要。

---

### 2. Token 生命周期

* Access Token：短生命周期（例如 15～30 分钟）。
* Refresh Token：长生命周期（例如 7～30 天）。
* Refresh Token 必须支持失效（退出登录、修改密码等）。

---

### 3. 验证码

即使暂时不用 Redis，也应设计：

* 验证码有效期（例如 5 分钟）
* 使用一次立即失效
* 同一邮箱发送频率限制（如 60 秒）

可以先存 SQLite，后续再替换为 Redis，而不改变业务接口。

---

### 4. 登录失败限制

建议增加：

```text
同一账号：

连续失败：

5 次

↓

锁定：

15 分钟
```

第一版可直接存数据库，不需要引入缓存。

---

### 5. 审计事件

每一次认证都产生事件：

```text
AUTH_LOGIN_SUCCESS

AUTH_LOGIN_FAILED

AUTH_LOGOUT

AUTH_REFRESH
```

虽然 P7 才会做完整的 `core-audit`，但 P2 就应该把事件定义好，未来直接接入审计系统。

---

# 十五、P2 完成后的整体能力

到 P2 结束时，`core-identity` 已经具备一个完整但仍保持简洁的认证体系：

```text
Identity Runtime
        │
        ▼
Authentication Runtime
        ├── Password Authentication
        ├── Email Verification Authentication
        ├── Access Token
        ├── Refresh Token
        ├── Verification Service
        ├── Authentication History
        └── Authentication Provider Framework
```

此时，平台已经能够安全地识别"当前是谁"，但**还不知道"他能做什么"**。

因此下一阶段 **P3** 将专注于 **Authorization Runtime（授权运行时）**，建立统一的 Role、Permission、Policy 和 Resource Access 模型。Authentication 负责回答 **"你是谁？"**，而 Authorization 将回答 **"你可以做什么？"**。这是两个应当长期保持独立演进的运行时。
