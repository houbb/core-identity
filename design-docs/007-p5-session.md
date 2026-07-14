这里开始，我会和前面的规划有一个比较大的调整。

前面四个 Phase：

* P1 Identity
* P2 Authentication
* P3 Authorization
* P4 Organization

实际上解决的是：

> **"谁（Who）"**

到了这里，这条主线其实已经完整了。

所以 **P5 不应该继续往 Identity 上堆功能（例如 OAuth、SSO）**。

我建议把 P5 定义为：

> **Session Runtime（会话运行时）**

原因是：

前四个 Runtime 都是**静态能力**。

而 Session 是第一个**动态 Runtime**。

它回答的问题不是：

> 你是谁？

而是：

> **你现在正在做什么？你当前在线吗？你的设备是什么？你的登录状态是什么？**

这是整个 Platform 开始真正"运行"的地方。

---

# Phase 5：Session Runtime ⭐⭐⭐⭐⭐

## 一、目标

一句话：

> **建立整个 Platform 的统一在线运行时。**

完成之后：

```text
Identity

↓

Authentication

↓

Session

↓

Online Runtime
```

以后：

所有平台：

```text
AI

Workflow

Notification

Billing

Storage
```

全部依赖：

Session。

---

# 为什么要独立 Session？

很多系统：

JWT

就是：

Session。

其实：

不是。

JWT：

只是：

一种 Token。

Session：

代表：

**一次在线运行。**

例如：

```text
Echo

↓

Windows

↓

Chrome

↓

今天上午登录
```

这就是：

一个：

Session。

以后：

手机：

再登录。

就是：

第二个。

---

# Runtime

```text
Identity

↓

Authentication

↓

Session

↓

Device

↓

Activity

↓

Online
```

---

# 一、Domain

建议：

新增：

五个对象。

---

# Session

真正：

在线。

字段：

```text
id

identity_id

organization_id

workspace_id

access_token

refresh_token

device_id

login_time

expire_time

status
```

注意：

Session：

关联：

Context。

不是：

只有：

Identity。

以后：

切换：

Workspace。

Session：

自然：

变化。

---

# Device

建议：

独立。

以后：

一个人：

很多：

设备。

例如：

```text
Windows

MacBook

iPhone

Android
```

字段：

```text
id

identity_id

device_name

platform

browser

fingerprint

last_active_time
```

不要：

Browser：

写：

Session。

以后：

Device：

越来越复杂。

---

# Activity

整个：

操作轨迹。

例如：

```text
Login

Open AI

Upload File

Create Workflow

Logout
```

以后：

Audit：

直接：

消费。

字段：

```text
id

session_id

activity_type

resource

resource_id

time
```

---

# OnlineStatus

建议：

独立。

为什么？

以后：

Notification：

需要：

知道：

用户：

在线。

Workflow：

需要：

知道：

Agent：

在线。

AI：

需要：

知道：

Worker：

在线。

字段：

```text
identity_id

status

last_active_time
```

状态：

```text
ONLINE

AWAY

OFFLINE
```

---

# LoginHistory

不要：

Authentication：

兼顾。

建议：

单独。

方便：

后台：

查询。

---

# 数据库

新增：

```text
session

device

activity

online_status

login_history
```

---

# Backend API

Session

```http
GET /api/v1/session/current

GET /api/v1/session

DELETE /api/v1/session/{id}
```

删除：

就是：

踢下线。

---

Device

```http
GET /api/v1/device

DELETE /api/v1/device/{id}
```

以后：

移除：

设备。

---

Activity

```http
GET /api/v1/activity
```

以后：

Timeline。

---

Online

```http
GET /api/v1/online
```

---

# Frontend（用户）

新增：

菜单：

```text
账号安全
```

里面：

四个：

Tab。

```text
Session

Device

Login History

Security
```

---

Session

例如：

```text
当前在线：

Windows Chrome

北京

今天 10:21
```

下面：

其他：

Session。

按钮：

```text
退出登录
```

---

Device

例如：

```text
MacBook Pro

Windows

Pixel 9
```

点击：

移除。

---

Login History

Timeline。

例如：

```text
09:20

Chrome

登录成功

东京
```

---

# Admin UX

新增：

菜单：

```text
Session

Device

Online
```

---

Session

Table：

```text
Identity

Organization

Workspace

Platform

IP

Login Time
```

支持：

搜索。

---

Device

Table：

```text
Identity

Device

Platform

Browser

Last Active
```

---

Online

Dashboard。

例如：

```text
当前在线：

132
```

下面：

列表。

---

# UX

Session：

不要：

Table。

建议：

Card。

例如：

```text
Windows

Chrome

Tokyo

当前设备
```

更加：

现代。

---

Device：

支持：

Rename。

例如：

```text
MacBook Air
```

以后：

方便。

---

Login History：

Timeline。

不要：

Table。

体验：

好很多。

---

# Activity

建议：

Timeline。

例如：

```text
登录

↓

打开 AI

↓

上传文件

↓

创建 Workflow
```

以后：

整个：

平台：

统一。

---

# 安全注意点

---

一个：

Refresh Token。

只能：

对应：

一个：

Session。

---

踢下线：

立即：

失效：

Refresh Token。

---

修改密码：

自动：

全部：

Session：

失效。

---

删除：

Device。

自动：

Session：

全部：

失效。

---

Online：

采用：

Heartbeat。

不要：

Socket。

第一版：

简单：

即可。

---

# 为什么这一版不做 OAuth？

很多项目：

第五步：

就是：

Github Login。

Google Login。

我建议：

不要。

为什么？

OAuth：

只是：

一种：

Authentication Provider。

属于：

P2。

不是：

新的 Runtime。

Session：

才是真正：

Platform：

开始：

运行。

---

# P5 完成后的能力图

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
        ├── Session
        ├── Device
        ├── Online Status
        ├── Activity
        ├── Login History
        ├── Session Management
        └── Current Context
```

---

# 我建议对 P5 再做一个升级：从「Session Runtime」提升为「Execution Context Runtime」

如果站在整个 Core Platform 的视角来看，真正贯穿所有模块的并不是 Session，而是**当前执行上下文（Execution Context）**。

建议定义一个统一对象：

```text
ExecutionContext
├── Identity
├── Principal
├── Organization
├── Workspace
├── Session
├── Device
├── Locale
├── Timezone
├── Client IP
├── User Agent
├── Trace ID
└── Request ID
```

整个请求生命周期中，无论进入 `core-ai`、`core-storage`、`core-workflow`、`core-notification`，都只传递一个 `ExecutionContext`。

例如：

```java
ExecutionContext ctx = ExecutionContextHolder.current();

ctx.identity();
ctx.organization();
ctx.workspace();
ctx.session();
ctx.traceId();
```

这样有几个长期优势：

1. **所有 Core 模块不再依赖 HTTP**，CLI、定时任务、Workflow、AI Agent 都可以构造同样的执行上下文。
2. **日志、审计、通知、AI 调用天然拥有一致的上下文信息**，后续接入 `core-audit` 和 `core-workflow` 几乎零成本。
3. **未来支持 Agent、机器人、系统任务** 时，不需要修改业务接口，因为它们同样拥有 `ExecutionContext`。

因此，我建议 **P5 的最终交付物不仅是 Session 管理，而是整个 Core Platform 的统一执行上下文运行时**。后面的 `core-notification`、`core-storage`、`core-ai` 等所有模块，都应以 `ExecutionContext` 作为基础输入。
