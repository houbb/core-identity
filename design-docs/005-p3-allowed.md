我认为 **P3 是整个 Core Platform 最重要的阶段之一。**

因为到这里，`core-identity` 才真正从**登录系统**升级为**平台身份系统**。

如果说：

* P1 解决的是 **Who are you?（你是谁）**
* P2 解决的是 **Can you prove it?（你如何证明你是你）**

那么：

> **P3 解决的是：What are you allowed to do?（你被允许做什么？）**

这是所有 Core 平台都会依赖的能力。

---

# Phase 3：Authorization Runtime ⭐⭐⭐⭐⭐

## 一、目标

建立整个 Platform 的统一授权运行时。

以后：

```text
core-storage
core-ai
core-notification
core-workflow
core-billing
core-marketplace
```

全部通过这一层判断权限。

任何模块：

**禁止自己判断权限。**

---

# 为什么 Authorization 要独立？

很多系统：

```text
User

↓

Role

↓

Permission
```

看起来简单。

但是以后：

AI

API

Workflow

Plugin

全部来了。

整个权限模型就炸了。

真正的平台应该抽象成：

```text
Identity

↓

Principal

↓

Role

↓

Permission

↓

Policy

↓

Authorization
```

这样：

以后：

AI Agent

机器人

Workflow

API Key

都能授权。

---

# Runtime Architecture

```text
Request

↓

Identity

↓

Principal

↓

Role

↓

Permission

↓

Policy Engine

↓

Authorization Result
```

这里：

Policy Engine

以后：

可以越来越复杂。

但是：

第一版：

非常简单。

---

# 第一版原则

不要：

RBAC + ABAC + ACL

全部一起做。

第一版：

只做：

RBAC。

也就是：

```text
Identity

↓

Role

↓

Permission
```

够了。

---

# 为什么？

因为：

90%

的平台：

RBAC

已经足够。

以后：

ABAC

Policy

再增加。

---

# 一、Domain

新增：

六个核心对象。

---

## Principal

建议：

不要：

Role

直接：

绑定：

Identity。

增加：

Principal。

```text
Identity

↓

Principal

↓

Role
```

为什么？

以后：

API Key

AI Agent

Workflow

全部：

也是：

Principal。

不是：

Identity。

---

字段：

```text
id

identity_id

principal_type

status
```

principal_type：

```text
USER

API

AGENT

SYSTEM
```

以后：

无限扩展。

---

## Role

角色。

例如：

```text
Admin

Operator

Member

Guest
```

字段：

```text
id

name

code

description

system_role
```

建议：

code：

永远：

唯一。

例如：

```text
ROLE_ADMIN

ROLE_USER

ROLE_OPERATOR
```

---

## Permission

权限。

建议：

采用：

资源式。

例如：

```text
identity.read

identity.write

identity.delete

storage.upload

storage.download

ai.chat

notification.send
```

不要：

```text
管理员

普通用户
```

这种：

中文。

---

建议：

统一：

```text
module.action
```

例如：

```text
identity.create

identity.read

identity.update

identity.delete
```

以后：

所有 Core：

一致。

---

## RolePermission

关联。

```text
Role

↓

Permission
```

N:N。

---

## PrincipalRole

关联。

```text
Principal

↓

Role
```

N:N。

以后：

一个：

Principal：

多个：

Role。

---

## AuthorizationDecision

不要：

Controller：

自己：

if。

统一：

Decision。

例如：

```text
ALLOW

DENY
```

以后：

Policy：

直接：

扩展。

---

# 数据库

新增：

```text
principal

role

permission

principal_role

role_permission
```

结束。

---

关系：

```text
Identity

↓

Principal

↓

PrincipalRole

↓

Role

↓

RolePermission

↓

Permission
```

非常清晰。

---

# Backend API

---

Role

```http
GET /admin-api/v1/roles

POST /admin-api/v1/roles

PUT /admin-api/v1/roles/{id}

DELETE /admin-api/v1/roles/{id}
```

---

Permission

```http
GET /admin-api/v1/permissions
```

建议：

Permission：

不能：

新增。

为什么？

Permission：

来自：

代码。

不是：

数据库。

例如：

```java
@Permission("storage.upload")
```

启动：

自动：

扫描。

管理员：

只能：

查看。

不能：

创建。

这是很多成熟平台的做法。

---

Principal

```http
GET /admin-api/v1/principals
```

---

授权

```http
POST /admin-api/v1/principal/{id}/roles
```

---

角色：

绑定：

权限。

```http
POST /admin-api/v1/role/{id}/permissions
```

---

# 权限注册机制（重点）

建议：

不要：

手动：

插数据库。

采用：

启动：

扫描。

例如：

```java
@Permission(
    code="identity.read",
    name="查看用户"
)
```

启动：

扫描：

所有：

Controller。

自动：

生成：

Permission。

为什么？

否则：

开发：

新增：

一个接口。

还要：

改数据库。

容易：

漏。

---

# 权限检查

Controller：

建议：

统一：

```java
@RequirePermission("identity.read")
```

框架：

自动：

检查。

不要：

```java
if(user.isAdmin())
```

到处：

写。

---

# Frontend（用户）

P3：

用户：

几乎：

没有变化。

因为：

权限：

后台。

用户：

不用：

看见。

---

# Admin UX

新增：

菜单。

```text
Identity

Authentication

Role

Permission

Authorization
```

---

Role

列表：

```text
角色名称

Code

系统角色

人数
```

点击：

进入。

---

Role Detail

四个：

Tab。

```text
Basic

Permission

Member

Operation Log
```

---

Permission

树形。

例如：

```text
Identity

    Read

    Create

    Update

Storage

    Upload

    Download

AI

    Chat

    Admin
```

不要：

1000 行：

Table。

---

授权：

建议：

左右穿梭。

```text
Available

>>>>>>>>>

Selected
```

非常直观。

---

Principal

详情：

增加：

```text
Roles
```

Tab。

管理员：

勾选：

Role。

完成。

---

# UX

---

角色：

颜色：

统一。

例如：

```text
Admin

红色

Operator

蓝色

Member

绿色
```

方便：

识别。

---

Permission：

支持：

搜索。

因为：

以后：

几百个。

---

Role：

支持：

复制。

例如：

```text
复制：

Operator

↓

Operator Copy
```

修改。

非常方便。

---

# 安全注意点

---

## 不允许写死 Admin

禁止：

```java
if(user=="admin")
```

全部：

Role。

---

## 不允许硬编码

禁止：

```java
if(role=="xxx")
```

全部：

Permission。

---

## Permission 不允许删除

为什么？

删除：

代码：

还在。

数据库：

没了。

容易：

混乱。

建议：

Permission：

永远：

来自：

代码。

数据库：

只保存：

RolePermission。

---

## Role 可以删除

Role：

是：

业务配置。

Permission：

不是。

---

## 超级管理员

建议：

只有：

一个：

```text
ROLE_SUPER_ADMIN
```

拥有：

全部：

Permission。

不用：

数据库：

配置。

框架：

自动：

全部：

Allow。

---

# P3 完成后的能力图

```text
Identity Runtime
        │
        ▼
Authentication Runtime
        │
        ▼
Authorization Runtime
        ├── Principal
        ├── Role
        ├── Permission
        ├── Role Binding
        ├── Authorization Decision
        ├── Permission Registration
        └── Permission Interceptor
```

---

# 我建议再做一个重要调整：把 Authorization 从「RBAC」升级为「Resource-Based Authorization」

虽然 **P3 的实现仍然采用 RBAC**，但整个模型建议从第一天就围绕**资源（Resource）**设计，而不是围绕菜单设计。

统一采用下面的命名规范：

```text
identity.user.read
identity.user.create
identity.user.update
identity.user.delete

identity.role.read
identity.role.assign

storage.file.upload
storage.file.download
storage.file.delete

notification.email.send
notification.template.manage

ai.chat.invoke
ai.model.manage
```

也就是：

```text
<module>.<resource>.<action>
```

而不是简单的：

```text
identity.read
storage.upload
```

这样做有三个长期优势：

1. **权限粒度更稳定**：同一个模块可以自然扩展多个资源，而不会出现 `user.read`、`user.read2` 之类的问题。
2. **菜单与权限解耦**：菜单可以变化，但资源权限模型保持稳定，不会因为 UI 调整而修改权限体系。
3. **为 P4（Organization Runtime）做好准备**：未来增加组织、部门、项目空间时，可以很自然地扩展到「谁可以对哪个资源执行什么操作」，而无需推翻 P3 的设计。

因此，P3 的实现建议坚持**简单 RBAC**，但**资源导向的权限命名**，这是一个能支撑后续十年演进的基础设计。
