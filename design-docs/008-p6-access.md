这里开始，我会调整整个 RoadMap。

前面：

* P1 Identity Runtime
* P2 Authentication Runtime
* P3 Authorization Runtime
* P4 Organization Runtime
* P5 Session Runtime

到这里，其实**Identity 已经完整了。**

如果继续做：

* OAuth
* LDAP
* SAML

我认为价值已经不高。

因为对于整个 Core Platform 来说，更重要的是：

> **如何让其它 Core 模块能够统一、安全地使用 Identity。**

所以我建议：

P6 不叫 OpenAPI。

而叫：

# Phase 6：Access Runtime ⭐⭐⭐⭐⭐

为什么不用 API Runtime？

因为以后：

访问 Platform 的：

不仅仅是 HTTP API。

还有：

* SDK
* CLI
* Agent
* Plugin
* Workflow
* Webhook
* Cron Job
* 内部 Service

它们本质都是：

> **Access（访问）**

不是：

API。

---

# 一、目标

一句话：

> **建立整个 Platform 的统一访问运行时。**

以后：

所有：

```text
Browser

↓

CLI

↓

SDK

↓

Workflow

↓

AI Agent

↓

Plugin

↓

Webhook
```

全部：

统一：

进入：

Access Runtime。

然后：

再：

进入：

Core。

---

# 为什么这一层重要？

如果没有：

Access Runtime。

以后：

每个：

Core：

都会：

自己：

判断：

```text
API Key

JWT

Permission

RateLimit

Signature
```

最后：

整个：

Platform：

越来越乱。

所以：

统一：

入口。

---

# Runtime

建议：

整个：

Access Runtime：

```text
Client

↓

Access Request

↓

Credential Resolver

↓

Identity Resolver

↓

Authorization

↓

Quota

↓

Rate Limit

↓

Audit Event

↓

Core Service
```

这里：

所有：

Core：

都：

不用：

关心：

Token。

---

# 二、Domain

建议：

新增：

七个对象。

---

# AccessKey

统一：

API Key。

不要：

散：

各个：

模块。

字段：

```text
id

identity_id

name

access_key

secret

status

expire_time
```

以后：

OpenAPI。

SDK。

全部：

依赖：

它。

---

# AccessClient

访问者。

例如：

```text
Browser

CLI

SDK

Workflow

Plugin

Agent
```

字段：

```text
id

client_type

version

platform
```

以后：

统计：

来源。

---

# AccessPolicy

不要：

写：

代码。

例如：

```text
JWT

↓

API Key

↓

Anonymous
```

都是：

Policy。

例如：

```text
JWT Required

API Key Required

Internal Only

Anonymous
```

以后：

Controller：

直接：

引用。

---

# Quota

统一：

额度。

例如：

以后：

AI：

```text
100 次/天
```

Storage：

```text
10GB
```

Notification：

```text
1000 封邮件
```

全部：

统一：

Quota。

Billing：

以后：

直接：

接。

---

# RateLimit

统一：

限流。

例如：

```text
10/s

100/min

1000/day
```

不要：

Spring：

写：

注解。

统一：

Runtime。

---

# AccessLog

所有：

访问：

统一：

记录。

以后：

Audit：

消费。

---

# ClientRegistration

以后：

SDK。

CLI。

Plugin。

全部：

注册。

例如：

```text
Poiesis CLI

Poiesis SDK

Poiesis Agent
```

---

# 数据库

新增：

```text
access_key

access_client

quota

rate_limit

access_log

client_registration
```

结束。

---

# Backend API

Access Key

```http
GET /api/v1/access-keys

POST /api/v1/access-keys

DELETE /api/v1/access-keys/{id}
```

---

Quota

```http
GET /api/v1/quota
```

---

Access Client

```http
GET /api/v1/clients
```

---

Admin

```http
GET /admin-api/v1/access-log
```

---

RateLimit

```http
GET /admin-api/v1/rate-limit
```

---

# Frontend（用户）

新增：

菜单：

```text
Developer
```

里面：

四个：

Tab。

```text
API Key

SDK

Quota

Usage
```

以后：

Marketplace：

直接：

依赖。

---

API Key

例如：

```text
Name

Key

Expire

Last Used
```

按钮：

```text
Create

Disable

Delete
```

---

Quota

Card。

例如：

```text
AI

32/100
```

```text
Storage

5GB/20GB
```

以后：

Billing：

直接：

更新。

---

Usage

Chart。

例如：

```text
Today

Week

Month
```

以后：

Developer：

很喜欢。

---

# Admin UX

新增：

菜单：

```text
Access

Quota

Rate Limit

API Client

Access Log
```

---

Access Log

Table。

```text
Identity

Client

IP

API

Status

Latency
```

支持：

搜索。

---

Rate Limit

支持：

修改。

例如：

```text
AI

100/s
```

以后：

不用：

改：

代码。

---

Quota

支持：

调整。

例如：

```text
AI

1000

↓

5000
```

---

# UX

API Key：

创建：

只显示：

一次。

例如：

```text
ak_xxxxxx

**************
```

复制：

按钮。

以后：

不再：

显示。

---

Quota：

采用：

Progress。

不要：

数字。

更：

直观。

---

Access Log：

支持：

Timeline。

以后：

排查：

方便。

---

# 安全注意点

---

Secret：

绝对：

不能：

明文。

数据库：

保存：

Hash。

---

API Key：

支持：

Expire。

---

API Key：

支持：

Disable。

不要：

Delete。

---

Rate Limit：

第一版：

数据库：

实现。

不要：

Redis。

---

Quota：

不要：

Billing：

实现。

Runtime：

自己：

实现。

Billing：

只是：

充值。

---

# 为什么这一版不做 Gateway？

很多平台：

做到：

这里。

开始：

Spring Cloud Gateway。

Nacos。

Apollo。

我建议：

不要。

因为：

现在：

整个：

Platform：

只有：

一个：

SpringBoot。

Gateway：

没有：

意义。

等：

以后：

真正：

拆：

微服务。

Access Runtime：

直接：

升级：

Gateway。

不用：

改：

业务。

---

# P6 完成后的能力图

```text
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
Access Runtime
        ├── API Key
        ├── Access Client
        ├── Access Policy
        ├── Rate Limit
        ├── Quota
        ├── Access Log
        └── Developer Center
```

---

# 我建议对 P6 做一个更大的升级：从「Access Runtime」提升为「Capability Runtime」

如果站在整个 Core Platform 的长期规划来看，**访问（Access）只是能力（Capability）的一种消费方式**。

未来真正要管理的是：

```text
Capability（平台能力）
        │
        ├── AI Chat
        ├── File Upload
        ├── Send Email
        ├── Execute Workflow
        ├── Generate Image
        └── Invoke Plugin
```

而 Access Runtime 中的：

* API Key
* Quota
* Rate Limit
* Permission

本质上都是：

> **围绕 Capability 的访问控制策略。**

因此，我建议从 P6 开始，把平台所有可调用的功能都注册为 **Capability**：

```text
Capability
├── code
├── name
├── category
├── permission
├── quota
├── rate_limit
├── billing_policy（后续）
└── visibility
```

这样：

* `core-ai` 注册 `ai.chat`、`ai.embedding`
* `core-storage` 注册 `storage.upload`
* `core-notification` 注册 `notification.email.send`

以后任何新 Core，只需要注册自己的 Capability，就能自动接入权限、额度、限流、计费、开发者中心，而不需要重复实现这些横向能力。

**这会成为整个 Core Platform 最重要的横向扩展机制之一。**
