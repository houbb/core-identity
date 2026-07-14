我认为 **Audit Runtime** 其实比很多人想象得重要。

很多开源项目把 Audit 做成：

```text
audit_log
```

一张表。

结束。

但是如果你的目标是：

> **Core Platform 的基石**

那么 Audit 应该是一个**完整的 Runtime**。

它负责回答整个平台最重要的三个问题：

> **谁（Who）在什么时间（When）对什么资源（What）执行了什么操作（Action），结果如何（Result）？**

这是：

* 安全
* 合规
* 排障
* 运维
* AI 分析

共同的数据来源。

---

# Phase 7：Audit Runtime ⭐⭐⭐⭐⭐

## 一、目标

一句话：

> **建立整个 Platform 的统一审计运行时。**

以后：

所有 Core：

```text
core-identity

core-storage

core-notification

core-workflow

core-billing

core-ai
```

全部：

写 Audit。

禁止：

自己：

建：

audit_log。

---

# 为什么 Audit 独立？

例如：

修改用户。

传统：

```java
userService.update()

↓

insert audit_log
```

Storage：

```java
upload()

↓

insert audit_log
```

Workflow：

```java
execute()

↓

insert audit_log
```

以后：

整个：

Platform：

几十张：

audit。

根本：

无法：

统一。

所以：

Audit：

必须：

平台：

统一。

---

# Runtime

整个：

Runtime：

建议：

```text
Request

↓

ExecutionContext

↓

AuditInterceptor

↓

AuditBuilder

↓

AuditStore

↓

AuditQuery
```

任何：

Core。

都：

不直接：

写：

数据库。

统一：

AuditRuntime。

---

# 二、设计原则

## 原则一

业务：

不关心：

Audit。

例如：

```java
identityService.create();
```

不要：

```java
auditService.save(...)
```

全部：

框架：

完成。

---

## 原则二

Audit：

不可修改。

只能：

Append。

不要：

Update。

以后：

可信。

---

## 原则三

Audit：

不能：

删除。

只能：

Archive。

---

## 原则四

Audit：

统一：

格式。

所有：

Core：

一致。

---

# 三、Domain Model

建议：

七个核心对象。

---

## AuditRecord

真正：

审计。

建议字段：

```text
id

trace_id

request_id

identity_id

organization_id

workspace_id

module

resource

action

result

risk_level

ip

user_agent

create_time
```

这张：

就是：

整个：

平台：

最重要：

的一张：

表。

---

## AuditDetail

建议：

正文：

独立。

为什么？

例如：

Payload：

很大。

不要：

放：

AuditRecord。

字段：

```text
audit_id

before_data

after_data

request_body

response_body
```

以后：

按需：

加载。

---

## AuditActor

谁：

执行。

以后：

不仅：

User。

还有：

```text
USER

SYSTEM

AGENT

API_KEY

WORKFLOW
```

全部：

统一。

---

## AuditResource

操作：

什么。

例如：

```text
Identity

Organization

StorageFile

Workflow

BillingOrder
```

统一：

抽象。

---

## AuditAction

统一：

动作。

建议：

枚举。

```text
CREATE

READ

UPDATE

DELETE

LOGIN

LOGOUT

UPLOAD

DOWNLOAD

EXPORT

IMPORT

EXECUTE
```

不要：

字符串：

乱写。

---

## AuditResult

建议：

统一：

```text
SUCCESS

FAILED

DENIED

ERROR
```

---

## AuditPolicy

哪些：

操作：

必须：

Audit。

以后：

管理员：

配置。

例如：

```text
Identity Update

√

Password Reset

√

View Profile

×

Download File

√
```

---

# 四、数据库

建议：

四张：

表。

```text
audit_record

audit_detail

audit_policy

audit_archive
```

Archive：

以后：

冷热：

分离。

第一版：

可以：

不用。

---

# 五、Backend API

管理员：

查询：

```http
GET /admin-api/v1/audits
```

---

详情：

```http
GET /admin-api/v1/audits/{id}
```

---

导出：

```http
POST /admin-api/v1/audits/export
```

以后：

CSV。

Excel。

---

策略：

```http
GET /admin-api/v1/audit-policy
```

修改：

```http
PUT /admin-api/v1/audit-policy
```

---

# 六、用户端

普通用户：

建议：

增加：

```text
账号安全

↓

最近操作
```

例如：

```text
今天

修改头像

昨天

登录

昨天

绑定邮箱
```

帮助：

发现：

异常。

---

# 七、Admin UX

新增：

菜单。

```text
Platform

↓

Audit
```

下面：

四个。

```text
Audit

Policy

Export

Archive
```

---

Audit

Table：

建议：

```text
时间

操作人

组织

模块

资源

动作

结果
```

支持：

搜索。

---

详情：

Drawer。

建议：

五个：

Tab。

```text
Basic

Request

Response

Change

Context
```

---

Basic：

```text
Identity

IP

Device

Time
```

---

Request：

JSON。

---

Response：

JSON。

---

Change：

Before

↓

After

Diff。

例如：

```text
nickname

Echo

↓

Echo AI
```

不要：

全部：

JSON。

直接：

Diff。

体验：

很好。

---

Context：

```text
TraceId

RequestId

SessionId

Workspace
```

以后：

排查：

神器。

---

# 八、UX

Risk：

颜色。

```text
LOW

绿色

MEDIUM

黄色

HIGH

红色
```

---

Result：

统一。

```text
SUCCESS

FAILED

DENIED
```

---

支持：

Timeline。

例如：

```text
09:20

登录

↓

09:21

修改权限

↓

09:22

删除组织
```

非常：

直观。

---

支持：

Filter。

例如：

```text
Identity

Organization

Module

Action

Result

Risk
```

---

# 九、自动采集（重点）

建议：

采用：

Spring AOP。

例如：

```java
@Audit(
    module="identity",
    resource="user",
    action="create"
)
```

框架：

自动：

生成：

Audit。

不要：

Service：

写：

Audit。

---

Controller：

例如：

```java
@PostMapping
@Audit(...)
```

结束。

---

# 十、ExecutionContext

Audit：

不要：

自己：

拿：

参数。

统一：

ExecutionContext。

例如：

```java
ctx.identity()

ctx.organization()

ctx.session()

ctx.ip()

ctx.device()
```

以后：

所有：

Core：

一致。

---

# 十一、安全注意点

不要：

记录：

密码。

Token。

Secret。

Cookie。

验证码。

---

敏感：

字段：

统一：

Mask。

例如：

```text
********
```

---

Audit：

Append Only。

禁止：

Update。

---

建议：

支持：

Hash。

以后：

证明：

没有：

篡改。

例如：

```text
SHA256
```

第一版：

可以：

预留。

---

# 十二、为什么 Audit 在 Event 前？

这是我建议保留原来规划的重要原因。

因为：

Audit：

属于：

**业务必需能力**。

今天：

只有：

`core-identity`。

你：

就：

需要：

Audit。

例如：

```text
登录

修改密码

绑定邮箱

创建组织

授权角色
```

全部：

必须：

Audit。

但是：

Event：

真正：

价值：

来自：

多个：

Core。

例如：

```text
Identity

↓

Storage

↓

Billing

↓

Notification
```

所以：

Audit：

比：

Event：

优先。

---

# 十三、P7 完成后的能力图

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
Audit Runtime
        ├── Audit Record
        ├── Audit Detail
        ├── Audit Policy
        ├── Audit Interceptor
        ├── Audit Timeline
        ├── Diff Viewer
        └── Export
```

---

# 我建议对 Audit Runtime 做两个重要升级

## 升级一：Audit 与 Event 解耦

虽然 **P8** 才引入 Event Runtime，但 P7 不应该依赖 Event。

建议：

```text
业务请求
      │
      ▼
Audit Runtime
      │
      ├── 写入 audit_record
      └── （预留）发布 AuditCreatedEvent
```

也就是说，**Audit 自己先完成闭环**，未来再把 `AuditCreatedEvent` 接入 Event Runtime，而不是一开始就依赖事件总线。

---

## 升级二：统一 Resource 模型

建议所有 Audit 都采用统一资源模型，而不是每个模块自由填写字符串：

```text
AuditResource
├── module        identity
├── resource      user
├── resourceId    10001
├── action        update
└── displayName   Echo
```

这样未来：

* `core-storage`
* `core-ai`
* `core-workflow`
* `core-billing`

都会生成结构一致的审计记录。

这不仅方便管理员查询，也为未来的 **AI 运维分析**（例如"最近是谁修改了这个对象？""过去 7 天哪些高风险操作最多？"）提供统一的数据基础。

---

**因此，我建议把 Audit Runtime 定位为整个 Core Platform 的"黑匣子（Black Box）"。**

任何能够改变平台状态的操作，都应该留下统一、可查询、可追溯、不可篡改的审计记录。这会成为你后续所有平台（Identity、Storage、Workflow、Billing、AI 等）共同依赖的一项基础能力。
