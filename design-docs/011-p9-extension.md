我认为 **Extension Runtime** 应该作为 `core-identity` 的最后一个 Runtime，也是整个 **Core Platform v1.x** 的收官。

原因很简单：

前面的 Runtime 都是在建设**平台自身**，而 Extension Runtime 是第一次回答：

> **如何让别人扩展我的平台？**

这是一个平台和一个应用最大的区别。

---

# Phase 9：Extension Runtime ⭐⭐⭐⭐☆

## 一、目标

一句话：

> **建立整个 Platform 的统一扩展机制。**

以后：

所有 Core：

```text
core-identity

core-storage

core-ai

core-workflow

core-billing

core-notification
```

全部：

支持：

Extension。

不是：

Plugin。

为什么？

因为：

Plugin：

只是：

Extension：

的一种。

Extension：

更加：

抽象。

---

# 为什么最后做？

因为：

Extension：

依赖：

前面：

所有：

Runtime。

例如：

Extension：

需要：

```text
Identity

Authentication

Authorization

Session

Access

Audit

Event
```

全部：

完成。

否则：

根本：

无法：

开放。

所以：

它：

天然：

放：

最后。

---

# 二、Runtime

建议：

整个：

Runtime：

```text
Extension

↓

Extension Registry

↓

Extension Loader

↓

Extension Lifecycle

↓

Extension Context

↓

Extension API
```

以后：

Marketplace。

直接：

建立：

在：

这里。

---

# 三、Extension 生命周期

统一：

生命周期。

```text
Installed

↓

Enabled

↓

Running

↓

Disabled

↓

Uninstalled
```

以后：

所有：

Extension：

一致。

---

# 四、设计原则

## 原则一

Extension：

绝对：

不能：

直接：

访问：

数据库。

只能：

调用：

Core API。

例如：

```java
storageApi.upload()

identityApi.find()

notificationApi.send()
```

不要：

```java
jdbcTemplate.execute(...)
```

否则：

平台：

无法：

升级。

---

## 原则二

Extension：

必须：

声明：

能力。

例如：

```text
Need:

Storage

Notification

AI
```

不要：

默认：

全部：

权限。

---

## 原则三

Extension：

必须：

声明：

权限。

例如：

```text
identity.user.read

storage.file.upload

notification.email.send
```

安装：

时：

管理员：

确认。

---

## 原则四

Extension：

无状态。

不要：

保存：

平台：

Session。

全部：

通过：

ExecutionContext。

---

# 五、Domain

建议：

七个对象。

---

## Extension

真正：

扩展。

字段：

```text
id

name

version

author

description

status

install_time
```

---

## ExtensionManifest

建议：

采用：

Manifest。

例如：

```yaml
id: demo

version: 1.0

permission:

- identity.user.read

- storage.file.upload

event:

- identity.user.created

api:

- storage.upload
```

以后：

Marketplace：

直接：

读取。

---

## ExtensionPermission

安装：

申请：

权限。

以后：

管理员：

审批。

---

## ExtensionSetting

每个：

Extension：

自己的：

配置。

例如：

```text
Webhook URL

API Key

Timeout
```

不要：

写：

数据库：

配置：

表。

统一。

---

## ExtensionLifecycle

生命周期：

记录。

例如：

```text
Install

Enable

Disable

Upgrade

Remove
```

以后：

Audit：

直接：

接。

---

## ExtensionContext

Extension：

执行：

上下文。

例如：

```java
ctx.identity()

ctx.organization()

ctx.workspace()

ctx.event()

ctx.traceId()
```

不要：

Servlet。

---

## ExtensionRegistry

平台：

所有：

Extension：

统一：

注册。

以后：

Marketplace：

查询。

---

# 六、数据库

建议：

```text
extension

extension_setting

extension_permission

extension_lifecycle
```

第一版：

不用：

更多。

---

# 七、Backend API

Extension

```http
GET /api/v1/extensions

POST /api/v1/extensions/install

POST /api/v1/extensions/enable

POST /api/v1/extensions/disable

DELETE /api/v1/extensions/{id}
```

---

Setting

```http
GET /api/v1/extensions/{id}/settings

PUT /api/v1/extensions/{id}/settings
```

---

Admin

```http
GET /admin-api/v1/extensions
```

---

Marketplace：

以后：

直接：

复用。

---

# 八、Frontend（用户）

新增：

菜单：

```text
Developer

↓

Extensions
```

页面：

Card。

例如：

```text
GitHub Login

Enabled

Version 1.0
```

支持：

Enable。

Disable。

---

# 九、Admin UX

新增：

菜单：

```text
Platform

↓

Extension
```

下面：

四个。

```text
Extension

Permission

Lifecycle

Marketplace
```

---

Extension

Table。

例如：

```text
名称

版本

状态

作者

安装时间
```

点击：

详情。

---

详情：

五个：

Tab。

```text
Basic

Permission

Setting

Lifecycle

Audit
```

---

Permission

例如：

```text
identity.user.read

storage.file.upload

workflow.execute
```

支持：

查看。

---

Lifecycle

Timeline。

例如：

```text
Installed

↓

Enabled

↓

Upgraded

↓

Disabled
```

---

# 十、Extension API（重点）

建议：

所有：

Core：

统一：

暴露：

SDK。

例如：

```java
IdentityApi

StorageApi

WorkflowApi

NotificationApi

BillingApi

AIApi
```

不要：

直接：

Controller。

以后：

Java。

Rust。

Go。

都：

可以：

实现。

---

# 十一、Event 集成

P8：

完成：

Event。

以后：

Extension：

支持：

订阅。

例如：

```java
@Subscribe(
 "identity.user.created"
)
```

Extension：

直接：

收到。

不要：

Hook。

---

# 十二、安全注意点

Extension：

不能：

默认：

全部：

权限。

必须：

声明。

---

Extension：

所有：

调用。

统一：

Audit。

---

Extension：

所有：

API。

统一：

Rate Limit。

---

Extension：

不能：

绕过：

Authorization。

必须：

走：

Access Runtime。

---

# 十三、为什么不是 Plugin Runtime？

这是我建议坚持的一个命名。

Plugin：

只是：

一种：

实现。

以后：

可能：

有：

```text
Plugin

Script

Workflow

AI Agent

Remote App
```

它们：

本质：

都是：

Extension。

所以：

Runtime：

建议：

叫：

Extension。

更加：

稳定。

---

# 十四、P9 完成后的能力图

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
        │
        ▼
Event Runtime
        │
        ▼
Extension Runtime
        ├── Registry
        ├── Manifest
        ├── Lifecycle
        ├── Permission
        ├── Context
        ├── SDK
        └── Marketplace Foundation
```

---

# 我建议对 Extension Runtime 做一个最大的升级：不要设计成"插件系统"，而是设计成"Capability Provider"

这是我认为整个 Core Platform 最关键的一步，也是与你以前规划最大的不同。

不要把 Extension 定义成：

> **可以安装一段代码。**

而是定义成：

> **可以向平台注册新的能力（Capability）。**

例如：

一个 OCR 扩展安装后，不只是增加一个插件，而是向平台注册：

```text
Capability
├── code: vision.ocr
├── name: OCR
├── input: image/*
├── output: text/plain
├── permissions:
│   └── ai.vision.invoke
└── events:
    ├── vision.ocr.started
    └── vision.ocr.completed
```

一个支付扩展注册：

```text
payment.wechat.pay
```

一个通知扩展注册：

```text
notification.telegram.send
```

一个 AI 扩展注册：

```text
ai.deepseek.chat
```

这样：

* `core-workflow` 不关心是谁提供 OCR，只调用 `vision.ocr`。
* `core-ai` 不关心是 OpenAI 还是 DeepSeek，只调用 `ai.chat` 能力。
* `core-notification` 不关心是 SMTP、企业微信还是 Telegram，只调用 `notification.send`。

也就是说：

```text
Extension
        │
        ▼
Capability
        │
        ▼
Platform
```

而不是：

```text
Plugin
        │
        ▼
Platform
```

## 这会让整个 Core Platform 的架构形成一个完整闭环

```text
Identity Runtime
        │
Authentication Runtime
        │
Authorization Runtime
        │
Organization Runtime
        │
Session Runtime
        │
Access Runtime
        │
Audit Runtime
        │
Event Runtime
        │
Extension Runtime
        │
Capability Runtime（由 Extension 提供能力）
        │
────────────────────────────────────
        │
core-storage
core-notification
core-ai
core-workflow
core-billing
...
```

这也是我认为最符合你长期目标的定位：

**`core-identity` 不只是一个认证中心，而是整个 Core Platform 的 Platform Kernel（平台内核）。** 它负责提供统一的身份、安全、事件、扩展和能力注册机制，而所有其他 `core-*` 模块都建立在这个内核之上。
