我建议 **P0 不写任何业务代码**。

很多团队第一天就在写 `UserController`、`LoginService`，一年后发现每个模块结构都不一样，只能重构。

**P0 的目标只有一个：**

> **建立整个 Core Platform 的工程规范（Engineering Foundation）。**

后面 `core-ai`、`core-storage`、`core-notification`、`core-billing` 等所有模块都直接复制这一套骨架。

---

# Phase 0：Engineering Foundation（工程基座）

## 目标

一句话：

> **让未来十几个 Core 模块拥有完全一致的开发体验。**

P0 不关心登录。

P0 不关心用户。

P0 关心的是：

* 项目结构
* 开发规范
* UI规范
* API规范
* 数据规范
* 目录规范
* 命名规范
* 代码规范

这一步完成之后，以后新增一个 Core：

```text
core-payment
```

基本就是：

```bash
cp -r core-template core-payment
```

即可。

---

# 一、工程结构

我建议整个仓库就是一个 Monorepo。

```
core-platform
│
├── docs/
│
├── backend/
│   │
│   ├── core-common
│   ├── core-starter
│   ├── core-bom
│   │
│   └── core-identity
│       │
│       ├── backend
│       ├── frontend
│       ├── admin-backend
│       └── admin-frontend
│
├── scripts
│
├── docker
│
├── README.md
└── LICENSE
```

为什么？

以后：

```
core-ai
core-storage
core-billing
```

全部复制。

整个工程永远一致。

---

# 二、Backend 工程

建议 Maven 多模块。

```
backend

pom.xml

core-common

core-starter

core-bom

core-identity

core-storage

core-ai
```

---

## core-common

只放：

真正公共代码。

例如：

```
Result

Page

BaseEntity

BaseException

BaseController

PageRequest

PageResult

DateUtil

JsonUtil
```

禁止：

```
UserService

LoginService
```

---

## core-starter

负责：

自动配置。

例如：

```
Global Exception

Jackson

CORS

OpenAPI

SQLite

MySQL

Spring Security
```

以后：

所有 Core：

直接：

```xml
<dependency>

core-starter

</dependency>
```

即可。

---

## core-bom

统一版本。

例如：

```
SpringBoot

JUnit

MapStruct

Lombok
```

所有 Module：

禁止：

自己指定版本。

---

# 三、单个 Core 的目录

例如：

```
core-identity

backend

frontend

admin-backend

admin-frontend
```

永远保持。

以后：

```
core-ai
```

也是：

```
backend

frontend

admin-backend

admin-frontend
```

---

# 四、Backend 包结构

统一：

```
controller

service

repository

entity

dto

vo

mapper

config

security

event

exception

util
```

不要：

有人：

```
controller

biz

domain

infra

application
```

有人：

```
serviceImpl

common

helper
```

整个平台统一。

---

# 五、API 规范

统一：

```
/api/v1/
```

管理员：

```
/admin-api/v1/
```

例如：

```
POST /api/v1/login

GET /api/v1/profile

POST /admin-api/v1/user
```

以后：

所有模块一致。

---

# 六、统一返回对象

统一：

```json
{
  "code":0,
  "message":"success",
  "data":{}
}
```

分页：

```json
{
  "code":0,
  "message":"success",
  "data":{
      "page":1,
      "size":20,
      "total":100,
      "records":[]
  }
}
```

不要：

每个接口：

返回格式不同。

---

# 七、异常规范

统一：

```
BusinessException

SystemException

ValidationException
```

禁止：

```
throw RuntimeException
```

---

# 八、日志规范

统一：

```
INFO

WARN

ERROR
```

禁止：

```
System.out.println
```

日志统一：

```
traceId

userId

requestId

time
```

以后方便定位。

---

# 九、数据库规范

默认：

SQLite

所有表：

```
id

create_time

update_time

deleted
```

统一：

```
snake_case
```

不要：

```
gmt_create

createDate

CreateTime
```

---

# 十、配置规范

整个平台：

只有：

```
application.yml
```

放：

```
server

datasource

logging
```

业务配置：

以后：

全部：

```
core-config
```

---

# 十一、前端规范

Vue3

统一：

```
src

api

components

layouts

pages

router

stores

styles

utils

assets
```

以后：

所有模块一致。

---

# 十二、Admin 前端

管理员：

也是：

```
pages

components

stores

router
```

不要：

用户端：

一套规范。

后台：

另一套规范。

---

# 十三、UI Design System（重点）

P0 就应该建立统一设计语言。

例如：

## Layout

永远：

```
Header

Sidebar

Content

Footer
```

---

用户端：

```
顶部导航

↓

内容
```

后台：

```
左菜单

↓

顶部

↓

内容
```

以后：

所有后台一致。

---

## Button

统一：

```
Primary

Secondary

Danger

Text
```

颜色：

统一。

---

## Table

统一：

```
分页

搜索

排序

空状态

加载动画
```

全部一致。

---

## Form

统一：

```
Label

Placeholder

Required

Help Text

Validation
```

以后：

不用每个页面重新设计。

---

# 十四、UX 规范

P0 最重要的其实是 UX。

例如：

## Loading

统一：

Skeleton。

不是：

```
Loading...
```

---

## Empty

统一：

```
📭

暂无数据

点击创建
```

不要：

白屏。

---

## Error

统一：

```
网络异常

重新加载
```

不要：

500。

---

## Success

统一：

```
✓ 保存成功
```

位置统一。

---

## Confirm

危险操作：

统一：

```
删除？

取消

确认
```

不要：

每个页面不同。

---

# 十五、Icon

统一：

一套。

不要：

```
HeroIcons

Lucide

Element

Ant

FontAwesome
```

混用。

---

# 十六、Theme

建议第一天：

支持：

```
Dark

Light
```

以后：

全部自动。

---

# 十七、注意点（这是 P0 最重要的部分）

## 1. 不提前抽象

例如：

不要为了未来支持：

```
Redis

MQ

Mongo

Kafka
```

写几十个接口。

今天：

SQLite。

明天：

SQLite。

够了。

---

## 2. 不提前微服务

永远：

```
一个 SpringBoot。
```

以后：

真有需要：

再拆。

---

## 3. 不提前插件化

不要：

```
SPI

OSGI

ClassLoader
```

今天：

没意义。

---

## 4. 不提前多租户

今天：

只有：

```
System
```

以后：

再加：

```
Tenant
```

---

## 5. 不提前分布式

不要：

```
Redis

MQ

Gateway

Nacos
```

---

## 6. API 优先

任何页面：

先：

```
OpenAPI

↓

Backend

↓

Frontend
```

不要：

边写页面边改接口。

---

## 7. 每一个 Core 都必须长得一样

这是整个 P0 最重要的一条约束。

以后无论新增：

```
core-ai

core-storage

core-payment

core-notification

core-workflow
```

目录、命名、接口风格、返回格式、UI、管理后台都必须遵循同一套模板。

**这样做的价值不在于今天能节省多少代码，而是在两三年后，当平台拥有十几个甚至几十个 `core-*` 模块时，任何开发者只需要学会一个模块，就能立即理解整个 Core Platform。**

这也是我认为 P0 真正应该交付的成果：**不是一个登录功能，而是一套能够支撑整个开源生态持续演进的统一工程规范。**
