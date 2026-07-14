我会把 **core-identity** 当成整个 Core Platform 的 **Kernel（内核）** 去设计。

它不是一个登录系统。

而是：

> **整个数字世界中，"谁（Who）可以在什么时候（When），以什么身份（Identity），对什么资源（Resource），执行什么操作（Action）" 的统一运行时。**

所以它未来不仅服务于登录，还服务于 AI、Workflow、Billing、Marketplace、所有业务平台。

---

# core-identity RoadMap

```
Phase 1
├── Identity Runtime
├── Authentication Runtime
├── Authorization Runtime
├── Organization Runtime
├── API Runtime
├── Session Runtime
├── Audit Runtime
└── Extension Runtime
```

这里面并不是一次做完，而是逐步演进。

---

# Phase 0：Project Foundation ⭐⭐⭐⭐⭐

## 目标

先把整个工程骨架搭起来。

```
core-identity/

backend/
frontend/
admin-backend/
admin-frontend/
docs/
```

Backend

```
controller

service

repository

entity

dto

config

security

event

exception
```

Frontend

```
pages

components

stores

router

api
```

管理员端同样结构。

---

为什么？

以后所有 Core：

```
core-ai

core-storage

core-notification
```

全部复制这一套。

整个生态保持100%一致。

---

# Phase 1：Identity Runtime ⭐⭐⭐⭐⭐

这是整个 Platform 的第一块。

实现：

```
User

Profile

Account

Credential

System
```

这里要注意：

不要把 User 和 Login 混在一起。

建议拆成：

```
User
    ↓

Account
    ↓

Credential
```

例如：

一个 User：

```
Echo
```

可以拥有：

```
Email

Github

Google

API Key

未来微信

未来企业账号
```

全部属于 Account。

为什么？

这是以后 OAuth、多登录方式的基础。

---

# Phase 2：Authentication Runtime ⭐⭐⭐⭐⭐

完成真正登录。

包括：

```
Email Login

Password Login

Email Verify Code

JWT

Refresh Token

Remember Me

Logout
```

为什么第二阶段？

因为：

登录只是 User 的一种能力。

不是 Identity 本身。

---

# Phase 3：Authorization Runtime ⭐⭐⭐⭐⭐

权限系统。

实现：

```
Permission

Role

RoleBinding

Policy
```

权限模型：

```
User

↓

Role

↓

Permission
```

例如：

```
user.read

user.write

user.delete

ai.chat

ai.admin

billing.read
```

以后所有 Core 都依赖它。

---

# Phase 4：Organization Runtime ⭐⭐⭐⭐☆

支持组织。

实现：

```
Organization

Department

Member

Invitation

Owner
```

以后：

```
个人

↓

团队

↓

企业
```

都可以支持。

为什么不第一天做？

因为 MVP 可以只有个人账号。

Organization 后续加入。

---

# Phase 5：Session Runtime ⭐⭐⭐⭐☆

登录以后：

增加：

```
Session

Device

Online User

Kick Offline

Login History
```

管理员可以：

```
强制下线

封禁设备

查看登录记录
```

---

# Phase 6：API Runtime ⭐⭐⭐⭐⭐

这是很多项目没有的。

实现：

```
API Key

Secret

Rate Limit

Scope

Token

Expire Time
```

以后：

AI

OpenAPI

SDK

全部依赖它。

例如：

```
POST /v1/chat

Authorization:
Bearer xxxx
```

就是这里负责。

---

# Phase 7：Audit Runtime ⭐⭐⭐⭐☆

以后所有行为：

```
登录

删除

修改密码

授权

API 调用

组织修改
```

全部：

```
Audit Log
```

以后：

Billing

Workflow

Notification

全部统一。

---

# Phase 8：Extension Runtime ⭐⭐⭐⭐☆

最后增加：

```
OAuth

Github

Google

LDAP

OIDC

SAML
```

未来：

企业客户：

```
企业微信

飞书

钉钉
```

也属于这一层。

---

# 数据模型演进

第一阶段不要设计几十张表。

建议按下面顺序扩展。

## 第一阶段（MVP）

```
user

account

credential
```

即可。

---

第二阶段

```
role

permission

role_permission

user_role
```

---

第三阶段

```
organization

organization_member
```

---

第四阶段

```
api_key

session

audit_log
```

---

以后再增加：

```
oauth_account

device

invitation

tenant
```

---

# 为什么这样排序？

整个顺序遵循一个原则：

> **先解决"是谁"，再解决"如何登录"，然后解决"能做什么"，最后解决"属于哪个组织"。**

依赖关系如下：

```
Identity Runtime
        │
        ▼
Authentication Runtime
        │
        ▼
Authorization Runtime
        │
        ▼
Organization Runtime
        │
        ▼
Session Runtime
        │
        ▼
API Runtime
        │
        ▼
Audit Runtime
        │
        ▼
Extension Runtime
```

每一层都建立在上一层之上，没有前置能力就不引入后续复杂性。

---

# 最终能力图（v1）

```
core-identity
│
├── Identity Runtime
│   ├── User
│   ├── Account
│   ├── Credential
│   └── Profile
│
├── Authentication Runtime
│   ├── Email Login
│   ├── Verification Code
│   ├── JWT
│   ├── Refresh Token
│   └── Logout
│
├── Authorization Runtime
│   ├── Role
│   ├── Permission
│   ├── Policy
│   └── Resource Access
│
├── Organization Runtime
│   ├── Organization
│   ├── Department
│   ├── Member
│   └── Invitation
│
├── Session Runtime
│   ├── Session
│   ├── Device
│   ├── Online User
│   └── Login History
│
├── API Runtime
│   ├── API Key
│   ├── Secret
│   ├── Scope
│   └── Rate Limit
│
├── Audit Runtime
│   ├── Login Log
│   ├── Operation Log
│   ├── Security Log
│   └── Audit Query
│
└── Extension Runtime
    ├── OAuth2
    ├── OIDC
    ├── LDAP
    ├── SAML
    └── Enterprise SSO
```

## 核心设计原则

这套 RoadMap 最大的特点是**按运行时（Runtime）能力组织，而不是按功能页面组织**。这样 `core-identity` 就不仅是一个登录模块，而是整个 Core Platform 的身份基础设施。未来 `core-ai`、`core-notification`、`core-billing`、`core-workflow` 等模块都只依赖它提供的统一身份、权限和 API 能力，而不需要各自维护用户体系。这也是大型平台（云平台、开发平台、SaaS 平台）普遍采用的长期演进方向。
