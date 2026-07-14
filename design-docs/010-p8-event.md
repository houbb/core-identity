这里我建议暂停一下前面的规划，做一个比较大的调整。

如果继续按照：

* P7 Audit
* P8 OAuth
* P9 ...

其实价值已经越来越低。

因为到 **P6**，`core-identity` 已经具备了：

* Identity
* Authentication
* Authorization
* Organization
* Session
* Access

对于一个 Identity Platform 来说，已经接近完整。

所以我认为：

> **P7 不应该继续增加 Identity 功能。**

而应该开始解决整个 Core Platform 的另一个核心问题。

---

# 如果站在整个 Core Platform 看

所有 Core：

```text
core-storage

core-ai

core-notification

core-workflow

core-billing

core-marketplace
```

都会不断产生：

```text
上传文件

调用AI

发送邮件

修改权限

支付成功

插件安装
```

这些东西：

共同特点是什么？

都是：

**Event（事件）**

所以：

我建议：

P7：

不是：

Audit Runtime。

而是：

---

# Phase 7：Event Runtime ⭐⭐⭐⭐⭐

一句话：

> **整个 Platform 的神经系统（Nervous System）。**

以后：

任何：

Core。

都：

不直接：

调用：

另一个：

Core。

全部：

发送：

Event。

---

# 为什么？

例如：

现在：

注册。

传统：

```text
UserService

↓

NotificationService

↓

MailService
```

越来越：

耦合。

以后：

Workflow：

来了。

Billing：

来了。

Audit：

来了。

AI：

来了。

整个：

UserService：

越来越长。

---

Event Runtime：

变成：

```text
Identity Created

↓

EventBus

↓

Notification

↓

Workflow

↓

Audit

↓

Billing

↓

AI
```

Identity：

完全：

不知道：

谁：

消费。

---

# 整个 Platform

开始：

真正：

松耦合。

---

# Runtime

建议：

```text
Producer

↓

Domain Event

↓

Event Bus

↓

Subscriber

↓

Handler
```

第一版：

不要：

MQ。

Spring Event：

即可。

以后：

Kafka。

RabbitMQ。

直接：

替换。

---

# 为什么不是 MQ？

因为：

你的原则：

一直：

都是：

> **Zero Dependency**

SQLite。

SpringBoot。

够。

---

# Domain

建议：

新增：

七个：

对象。

---

## Event

统一：

平台：

事件。

字段：

```text
id

event_code

aggregate

aggregate_id

payload

status

create_time
```

例如：

```text
identity.created

storage.file.uploaded

ai.chat.completed

notification.sent
```

统一：

命名。

---

## EventPublisher

统一：

发布。

不要：

```java
applicationEventPublisher
```

到处：

写。

统一：

接口。

---

## EventSubscriber

统一：

订阅。

例如：

```java
@Subscribe("identity.created")
```

即可。

---

## EventHandler

真正：

业务。

例如：

```text
IdentityCreatedHandler
```

以后：

越来越多。

---

## EventStore

第一版：

SQLite。

全部：

保存。

以后：

Event Sourcing：

再说。

---

## DeadLetter

建议：

第一版：

就有。

为什么？

Handler：

失败。

以后：

后台：

可以：

重试。

---

## Retry

简单：

即可。

例如：

```text
retry_count

next_retry_time
```

---

# Event 命名规范（重点）

建议：

统一：

```text
module.resource.action
```

例如：

```text
identity.user.created

identity.user.deleted

storage.file.uploaded

storage.file.deleted

notification.email.sent

workflow.job.finished

ai.chat.completed
```

不要：

```text
createUser

upload
```

以后：

搜索：

很舒服。

---

# Backend API

Event：

```http
GET /admin-api/v1/events
```

查看：

Event。

---

Retry：

```http
POST /admin-api/v1/events/{id}/retry
```

---

Dead Letter：

```http
GET /admin-api/v1/dead-letter
```

---

Subscriber：

```http
GET /admin-api/v1/subscribers
```

方便：

调试。

---

# Frontend

普通用户：

基本：

没有。

因为：

属于：

平台。

---

# Admin

新增：

菜单：

```text
Platform

↓

Event
```

下面：

四个。

```text
Event

Subscriber

Dead Letter

Retry Queue
```

---

Event

Table：

```text
Code

Producer

Time

Status
```

点击：

详情。

---

详情：

Payload。

JSON。

支持：

复制。

---

Subscriber

例如：

```text
IdentityCreatedHandler

MailHandler

WorkflowHandler

AuditHandler
```

支持：

查看：

状态。

---

Dead Letter

重点：

以后：

生产：

排查。

支持：

```text
Retry

Ignore
```

---

# UX

Event：

支持：

Timeline。

例如：

```text
Identity Created

↓

Mail Sent

↓

Workflow Started

↓

Billing Initialized
```

以后：

平台：

特别：

漂亮。

---

Dead Letter：

红色。

Retry：

蓝色。

Success：

绿色。

统一。

---

# 注意点

---

不要：

Spring Event：

直接：

暴露。

统一：

```java
CoreEventPublisher
```

以后：

MQ：

不用：

改：

业务。

---

Event：

一定：

Version。

例如：

```text
identity.user.created.v1
```

以后：

兼容。

---

Payload：

建议：

JSON。

不要：

Java Object。

---

Event：

不可修改。

只能：

新增。

---

Handler：

必须：

幂等。

例如：

```text
收到：

两次：

identity.user.created
```

也：

不能：

创建：

两个：

用户资料。

---

# 为什么 P7 不做 Audit？

这是我建议最大的调整。

很多平台：

Audit：

自己：

写。

后来：

越来越复杂。

其实：

Audit：

本质：

就是：

Event：

的一种：

Subscriber。

例如：

```text
Identity Created

↓

AuditHandler

↓

AuditLog
```

Storage：

也是。

AI：

也是。

Billing：

也是。

全部：

订阅：

Event。

所以：

Audit：

不是：

Runtime。

Audit：

只是：

Event Runtime：

的：

一个：

Consumer。

---

# P7 完成后的整体能力图

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
        │
        ▼
Event Runtime
        ├── Event Publisher
        ├── Event Bus
        ├── Event Store
        ├── Subscriber
        ├── Dead Letter
        ├── Retry
        └── Event Timeline
```

---

# 我对整个 RoadMap 的一个最终建议

做到这里，**`core-identity` 应该结束**。

因为它已经提供了完整的平台身份能力：

* P1：Identity（身份模型）
* P2：Authentication（认证）
* P3：Authorization（授权）
* P4：Organization（组织）
* P5：Session（运行上下文）
* P6：Access（统一访问）
* P7：Event（平台事件）

这七个 Runtime 已经足以支撑 `core-storage`、`core-ai`、`core-notification`、`core-workflow` 等后续所有 Core 模块。

**后面的能力（如 OAuth Provider、LDAP、SAML、多租户、企业 SSO、Passkey 等）建议不要继续作为新的 Phase，而作为 P2/P4/P6 的增强版本（v2、v3）逐步加入。**

这样，你的 `core-identity` 就不会无限膨胀，而是真正成为整个 Core Platform 的稳定基石。
