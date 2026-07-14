# Core Identity P6：企业治理与合规

版本：P6
前置依赖：P0–P5
目标：建立访问治理、特权控制、职责分离、周期复核、审计证据、隐私请求和数据生命周期管理能力。

---

# 一、P6 核心目标

P5 已经解决：

```text
企业员工如何通过 SSO 登录
企业目录如何通过 SCIM 管理成员
外部身份如何映射本地用户
员工离职后如何终止组织访问
```

P6 继续解决：

```text
用户为什么拥有某项权限？
权限由谁批准？
权限是否应该自动过期？
管理员是否拥有过多权限？
关键职责是否集中在同一个人身上？
离职或转岗后权限是否已经清理？
企业是否定期复核访问权？
审计记录能否形成可信证据？
个人数据如何导出、纠正、限制和删除？
法律保留与删除请求冲突时如何处理？
```

P6 必须完成：

```text
访问申请
访问套餐
多级审批
临时授权
权限到期
即时特权激活
职责分离
高风险权限治理
周期性访问审查
孤儿身份治理
服务账号治理
管理员分权
审计证据
控制目录
控制测试
合规映射
隐私请求
数据导出
删除与匿名化
处理限制
数据保留
法律保留
跨 Core 删除编排
```

---

# 二、P6 不等于“自动合规”

P6 提供：

```text
控制执行
审批记录
访问复核
证据采集
数据生命周期
合规映射
审计导出
```

P6 不直接保证：

```text
ISO/IEC 27001 认证
SOC 2 报告
GDPR 全面合规
任何国家或行业许可
```

原因是合规不仅取决于软件功能，还取决于：

```text
组织制度
人员职责
实际业务流程
合同关系
数据处理目的
法律适用范围
控制是否持续运行
外部审计结论
```

ISO 官方将 ISO/IEC 27001 描述为信息安全管理体系要求，并明确组织可以选择实施标准，也可以进一步接受认证；SOC 则是注册会计师围绕组织控制提供的鉴证服务。

因此本阶段的定位是：

> 建立可被审计、可被证明、可持续运行的身份治理控制系统。

---

# 三、P6 的六条能力主线

```text
1. Access Governance
2. Privileged Access
3. Separation of Duties
4. Access Review
5. Audit & Evidence
6. Privacy & Data Lifecycle
```

六条能力相互关联，但不能混成一个“合规任务”模块。

---

# 四、四个子项目职责

```text
core-identity/
├── core-identity-backend/
├── core-identity-web/
├── core-identity-admin-backend/
└── core-identity-admin-web/
```

## 4.1 core-identity-backend

P6 新增职责：

```text
Access Request
Access Package
Approval Policy
Approval Decision
Temporary Grant
Privileged Activation
SoD Policy
SoD Evaluation
Access Review Campaign
Review Decision
Entitlement Inventory
Admin Role
Privacy Request
Retention Policy
Legal Hold
Audit Evidence
Compliance Control
Control Assessment
Control Exception
```

它仍然是以下事实的唯一所有者：

```text
谁拥有什么权限
权限为何产生
权限何时失效
谁批准或复核
什么治理规则适用
```

---

## 4.2 core-identity-web

面向普通用户、组织成员和组织管理员。

新增：

```text
申请访问权限
查看申请进度
批准下属或项目成员申请
激活临时特权
查看自己的权限来源
参与访问审查
查看隐私数据
提交数据请求
下载数据导出
查看数据删除进度
管理组织治理策略
```

组织内部治理仍使用用户侧门户。

---

## 4.3 core-identity-admin-backend

新增平台治理编排：

```text
跨组织治理统计
平台级高权限审批
平台管理员角色治理
全平台访问审查
审计证据聚合
合规控制映射
隐私请求编排
跨 Core 数据导出
跨 Core 删除任务
法律保留编排
控制测试编排
证据包生成
```

禁止：

```text
直接修改 Identity 数据库
直接跳过审批授予权限
直接删除审计记录
直接伪造控制证据
```

---

## 4.4 core-identity-admin-web

新增：

```text
治理总览
高风险权限
访问申请
审批队列
特权激活
职责冲突
访问审查
孤儿账号
服务账号治理
管理员分权
隐私请求
保留策略
法律保留
控制目录
证据中心
合规映射
```

---

# 五、治理对象模型

P6 必须区分：

```text
Permission
Role
Entitlement
Access Package
Grant
Activation
```

## 5.1 Permission

细粒度操作能力：

```text
billing.refund.approve
storage.export.all
identity.member.remove
```

## 5.2 Role

权限集合：

```text
Billing Manager
Security Administrator
Auditor
```

## 5.3 Entitlement

可被治理和复核的一项访问权益。

可以表示：

```text
某个 Role
某个 Permission
某个 OAuth Scope
某个 Service Account Credential
某个管理控制台身份
```

## 5.4 Access Package

面向用户申请的一组访问权益。

例如：

```text
财务只读访问
项目管理员访问
临时生产排障访问
供应商审计访问
```

## 5.5 Grant

已经授予主体的长期或临时访问权。

记录：

```text
授予什么
授予给谁
为何授予
谁批准
何时生效
何时到期
来源是什么
```

## 5.6 Activation

主体暂时激活一项“可激活特权”。

例如：

```text
用户长期有资格成为 Billing Admin
但只有在排障时激活 2 小时
```

---

# 六、访问套餐 Access Package

Access Package 解决：

> 用户不应该面对数百个 Permission 复选框。

例如：

```text
套餐：财务查看者

包含：
billing.invoice.read
billing.payment.read
billing.subscription.read
```

```text
套餐：生产排障人员

包含：
storage.system.read
workflow.run.inspect
ai.request.audit.read

有效期：
最长 4 小时

要求：
AUTH_LEVEL_3
工单编号
直属主管批准
系统所有者批准
```

---

## 6.1 Access Package 属性

```text
名称
描述
组织
适用主体
包含的角色和权限
风险等级
是否允许申请
默认有效期
最大有效期
审批策略
认证强度要求
职责冲突策略
是否允许自动续期
数据所有者
业务所有者
```

---

## 6.2 Package 类型

```text
STANDARD
PRIVILEGED
TEMPORARY
EXTERNAL
EMERGENCY
```

### STANDARD

常规工作访问。

### PRIVILEGED

高权限访问，需要更严格审批和激活。

### TEMPORARY

项目或任务期间访问。

### EXTERNAL

供应商、顾问、审计人员访问。

### EMERGENCY

紧急处理使用，事后必须复核。

---

# 七、访问申请流程

```text
用户选择访问套餐
    │
    ▼
填写业务理由
    │
    ▼
选择需要的有效期
    │
    ▼
填写工单或项目编号
    │
    ▼
Identity 执行资格与冲突检查
    │
    ├── 不符合资格
    │     → 拒绝提交
    │
    ├── 存在职责冲突
    │     → 阻止或进入风险例外
    │
    └── 通过
          ▼
       创建审批实例
          ▼
       多级审批
          ▼
       创建 Grant
          ▼
       更新授权版本
          ▼
       通知申请人
```

---

## 7.1 申请理由

理由不能只是可选文本。

高风险访问必须包括：

```text
业务目的
目标系统或组织
所需时间
关联项目或工单
为何现有权限不足
```

可以提供模板：

```text
生产故障处理
月度财务结算
外部审计
客户支持调查
安全事件响应
```

---

## 7.2 申请资格

Access Package 可以限制：

```text
指定组织
指定成员类型
指定部门
指定现有角色
完成强认证
完成安全培训
账号无高风险事件
仅企业 SSO 用户
仅受 SCIM 管理员工
```

P6 不需要构建任意脚本语言。

首版使用结构化条件：

```text
AND
OR
EQUALS
IN
EXISTS
```

---

# 八、审批策略

审批步骤可以包括：

```text
直属主管
资源所有者
角色所有者
组织安全管理员
数据所有者
平台管理员
合规负责人
```

---

## 8.1 审批模式

```text
SINGLE
ALL
ANY_N
SEQUENTIAL
PARALLEL
```

### SINGLE

一人批准。

### ALL

所有指定审批人批准。

### ANY_N

例如三人中至少两人批准。

### SEQUENTIAL

按顺序审批：

```text
主管
→ 系统所有者
→ 安全管理员
```

### PARALLEL

多个审批组同时审批。

---

## 8.2 审批决定

```text
APPROVED
REJECTED
RETURNED
CANCELLED
EXPIRED
```

审批人必须看到：

```text
申请人
当前角色
申请内容
新增权限
风险等级
职责冲突
有效期
业务理由
历史申请
最近安全事件
```

---

## 8.3 防止自己批准自己

默认禁止：

```text
申请人 = 审批人
资源所有者为自己批准自己的高权限
管理员创建策略后立即为自己授予
```

必要例外必须：

```text
进入双人批准
产生高危审计事件
要求事后复核
```

---

# 九、临时授权

临时 Grant 具有：

```text
starts_at
expires_at
max_duration
renewal_policy
```

到期后自动：

```text
撤销角色或权限
增加 authorization_version
撤销相关特权 Token
关闭 Privileged Session
写入审计
通知用户和所有者
```

不能只在前端把权限标记“已过期”，数据库授权仍继续生效。

---

## 9.1 自动延期

P6 默认不自动延期高权限。

续期必须：

```text
重新确认业务需要
重新检查职责冲突
重新审批
```

低风险套餐可以允许资源所有者配置有限续期。

---

# 十、即时特权访问 JIT/PAM 基础

这里的 JIT 指：

```text
Just-in-Time Privileged Access
```

不是 P5 的：

```text
Just-in-Time User Provisioning
```

两者必须在命名上区分。

---

## 10.1 Eligible 与 Active

特权访问分为：

```text
ELIGIBLE
ACTIVE
```

### ELIGIBLE

用户有资格在需要时激活。

### ACTIVE

用户当前正在使用特权。

例如：

```text
Ayşe
Eligible：Security Administrator
Active：无
```

发生安全事件时：

```text
激活 Security Administrator 2 小时
```

---

## 10.2 激活要求

可以要求：

```text
业务理由
工单编号
AUTH_LEVEL_2 或 AUTH_LEVEL_3
无未解决安全风险
审批
最大时长
指定设备
指定 IP 范围
```

---

## 10.3 特权 Session

激活成功后创建：

```text
Privileged Session
```

记录：

```text
主体
激活角色
目标组织
开始时间
结束时间
认证强度
激活原因
审批记录
Session ID
Token JTI
```

特权 Token 必须：

```text
短时有效
明确 privileged=true
绑定 Activation ID
绑定组织
绑定 Audience
不能刷新超过 Activation 到期时间
```

---

## 10.4 特权 UX

管理控制台顶部持续显示：

```text
特权访问已激活

角色：Security Administrator
剩余：01:42:18
原因：INC-2048
```

提供：

```text
提前结束
查看允许操作
```

不要让用户忘记自己当前处于高权限模式。

---

# 十一、Break-glass 治理

P5 已有紧急 SSO Break-glass。

P6 将其治理化。

每次使用必须：

```text
要求最强认证
创建 CRITICAL 安全事件
通知安全负责人
记录全部操作
限制有效时长
禁止创建长期凭证
事后强制复核
```

Break-glass 结束后：

```text
自动撤销会话
轮换临时凭证
生成事后审查任务
要求解释每项高风险操作
```

---

# 十二、职责分离 SoD

职责分离解决：

> 同一个人不应同时掌握相互冲突的关键权力。

例如：

```text
创建供应商
+
批准供应商付款
```

```text
创建退款
+
批准退款
```

```text
修改身份策略
+
审计身份策略
```

```text
开发生产代码
+
独立批准生产发布
```

---

## 12.1 静态职责冲突

当两个角色或权限不能同时持有：

```text
Billing Payment Creator
X
Billing Payment Approver
```

如果用户申请第二项：

```text
直接阻止
或进入风险例外审批
```

---

## 12.2 动态职责冲突

用户可以拥有两个能力，但不能对同一业务对象执行冲突动作。

例如：

```text
可以创建退款
也可以批准退款
但不能批准自己创建的退款
```

动态 SoD 的业务对象规则由对应 Core 执行。

Identity 管理：

```text
冲突定义
策略元数据
主体授权
```

Billing 等业务 Core 管理：

```text
谁创建了这笔退款
当前是否由同一人审批
```

---

## 12.3 SoD 决策

```text
ALLOW
DENY
REQUIRE_EXCEPTION
REQUIRE_ADDITIONAL_APPROVAL
```

---

## 12.4 冲突例外

例外必须包括：

```text
风险说明
补偿性控制
审批人
生效时间
过期时间
复核时间
```

示例：

```text
小型团队暂时允许财务负责人同时创建和批准，
补偿性控制为每周由组织所有者复核全部交易。
```

例外不能永久无期限存在。

---

# 十三、访问审查 Access Review

访问审查回答：

> 用户今天仍然需要这些权限吗？

审查对象：

```text
组织成员
高风险角色
平台管理员
Service Account
API Key
OAuth Client
Client Secret
外部组映射
Break-glass 身份
长期未使用权限
```

---

## 13.1 审查活动 Campaign

Campaign 属性：

```text
名称
范围
审查对象
审查人
开始时间
截止时间
提醒策略
升级策略
默认决定
是否需要理由
是否自动执行撤销
```

---

## 13.2 Campaign 类型

```text
USER_ACCESS
ROLE_MEMBERSHIP
PRIVILEGED_ACCESS
SERVICE_ACCOUNT
APPLICATION_ACCESS
EXTERNAL_ACCESS
ORPHAN_ACCESS
```

---

## 13.3 审查决定

```text
CERTIFY
REVOKE
MODIFY
DELEGATE
NOT_SURE
```

### CERTIFY

确认继续保留。

### REVOKE

撤销访问。

### MODIFY

缩小角色、权限或有效期。

### DELEGATE

转交更合适的审查人。

### NOT_SURE

进入升级处理，不应默认继续永久保留。

---

## 13.4 审查证据

审查人应看到：

```text
用户身份
职位和部门
管理来源
组织角色
具体权限
权限风险
授予来源
批准人
授予时间
到期时间
最近使用时间
最近登录
安全事件
SCIM 状态
```

---

## 13.5 未按时审查

截止后策略可以：

```text
自动撤销
自动暂停
升级给上级
延长一次
保持但创建违规记录
```

高风险特权推荐：

```text
未审查自动暂停
```

普通低风险访问可以：

```text
升级提醒
```

---

# 十四、孤儿身份治理

孤儿对象包括：

```text
无有效所有者的 Service Account
创建人已离职的 API Key
所有者已离开组织的 OAuth Client
无人负责的自定义角色
无业务负责人的 Access Package
失去 SCIM 来源的 External Identity
```

处理：

```text
重新指定所有者
暂停
撤销
合并
归档
```

不能让凭证因为创建人离职而永久无人治理。

---

# 十五、平台管理员分权

P1 的单一：

```text
SUPER_ADMIN
```

在 P6 必须拆分。

建议平台管理角色：

```text
PLATFORM_IDENTITY_ADMIN
PLATFORM_SECURITY_ADMIN
PLATFORM_AUDIT_ADMIN
PLATFORM_SUPPORT_ADMIN
PLATFORM_PRIVACY_ADMIN
PLATFORM_COMPLIANCE_ADMIN
PLATFORM_APPLICATION_ADMIN
PLATFORM_READ_ONLY_ADMIN
```

---

## 15.1 PLATFORM_IDENTITY_ADMIN

可以：

```text
管理用户状态
处理组织身份问题
管理 SSO 和 SCIM
```

不能：

```text
删除审计
关闭安全事件
批准自己的特权访问
```

---

## 15.2 PLATFORM_SECURITY_ADMIN

可以：

```text
处理安全事件
撤销会话
管理安全策略
应急锁定账号
```

不能：

```text
修改合规证据
批准隐私删除豁免
```

---

## 15.3 PLATFORM_AUDIT_ADMIN

可以：

```text
读取审计
导出证据
执行访问审查
```

不能：

```text
修改用户权限
删除审计事件
```

---

## 15.4 PLATFORM_SUPPORT_ADMIN

可以：

```text
查看有限用户资料
发起密码恢复
查看脱敏诊断
```

不能：

```text
查看完整安全凭证
修改平台管理员
导出全量个人数据
```

---

## 15.5 PLATFORM_PRIVACY_ADMIN

可以：

```text
处理数据主体请求
管理保留和删除流程
查看数据处理清单
```

不能：

```text
修改业务权限
访问与请求无关的明文数据
```

---

# 十六、管理员代理访问

客服或平台管理员有时需要查看用户视角。

禁止直接：

```text
以用户身份静默登录
使用用户 Session
获取用户密码
```

应建立：

```text
Impersonation / Support Session
```

---

## 16.1 代理访问要求

```text
明确目的
目标用户
目标组织
允许操作范围
只读或有限写入
短时有效
强认证
审批，可配置
用户通知，可配置
完整审计
```

代理 Session 必须标记：

```text
actor_id = 管理员
subject_id = 用户
impersonation = true
```

审计同时记录：

```text
真实操作者
被代理用户
```

不能只记录用户 ID。

---

# 十七、审计证据体系

P0–P5 已有审计事件。

P6 将其升级为：

```text
审计记录
控制证据
证据快照
证据包
证据签名
证据保留
```

---

## 17.1 审计不可变原则

审计事件：

```text
只追加
不原地修改
不软删除
纠正通过新增更正事件
```

可以增加：

```text
previous_hash
event_hash
sequence_number
```

形成按分区或时间段的哈希链。

哈希链不能防止拥有数据库最高权限的人重写整个数据库，但可以提高篡改检测能力。

企业部署可增加：

```text
外部 WORM 存储
对象锁
外部 SIEM
独立审计账号
签名时间戳
```

---

## 17.2 Evidence

Evidence 表示某个控制在某段时间内运行的证明。

例如：

```text
控制：
所有平台管理员必须启用强 MFA

证据：
平台管理员清单
认证器状态
策略配置
不符合项
采集时间
数据查询版本
```

---

## 17.3 Evidence 类型

```text
CONFIGURATION
ACCESS_SNAPSHOT
REVIEW_RESULT
APPROVAL_RECORD
AUDIT_EXPORT
SECURITY_METRIC
POLICY_DOCUMENT
TEST_RESULT
EXCEPTION_RECORD
```

---

## 17.4 证据包

证据包可以包含：

```text
控制说明
适用范围
采集时间
证据文件
数据摘要
查询条件
系统版本
Schema 版本
生成者
校验和
数字签名
```

导出格式：

```text
ZIP
├── manifest.json
├── control.json
├── evidence/
├── audit/
├── reports/
└── checksums.txt
```

---

# 十八、控制目录 Compliance Control Catalog

不要在代码中硬编码：

```text
ISO27001=true
SOC2=true
GDPR=true
```

应建立通用模型：

```text
Framework
Requirement
Control
Implementation
Evidence
Assessment
Exception
```

---

## 18.1 Framework

例如：

```text
Internal Security Baseline
ISO/IEC 27001:2022 Mapping
SOC 2 Mapping
NIST SP 800-53 Mapping
GDPR Privacy Mapping
Customer Contract Controls
```

NIST SP 800-53 是可定制的安全与隐私控制目录；ISO/IEC 27001 是信息安全管理体系要求；SOC 2 信任服务标准涵盖安全、可用性、处理完整性、保密性和隐私。应通过映射复用同一技术控制，而不是为每种框架重复实现一套功能。

---

## 18.2 Control

例如：

```text
CTRL-IAM-001
平台管理员必须使用强认证

CTRL-IAM-002
高风险权限必须经过独立审批

CTRL-IAM-003
特权访问必须自动过期

CTRL-IAM-004
组织访问必须定期复核

CTRL-PRIV-001
个人数据删除必须跨服务追踪
```

---

## 18.3 Control 状态

```text
PLANNED
IMPLEMENTED
OPERATING
INEFFECTIVE
NOT_APPLICABLE
```

---

## 18.4 控制测试

测试方式：

```text
AUTOMATED
MANUAL
HYBRID
```

例如：

```text
自动测试：
是否存在未启用 MFA 的平台管理员

人工测试：
抽样检查高风险访问申请的业务理由

混合测试：
系统筛选例外，审计员人工判断合理性
```

---

# 十九、持续控制监控

定期执行：

```text
特权账号 MFA 覆盖
长期未使用权限
已过期但仍有效的 Grant
无人所有的 Service Account
过期 API Key
未审查高风险角色
职责冲突
失效证书
Break-glass 使用
审计投递失败
```

每次检测产生：

```text
Control Assessment
Finding
Remediation Task
```

不要只生成红色数量卡片。

每个 Finding 应具有：

```text
具体对象
违反的控制
风险等级
发现时间
负责人
整改截止时间
当前状态
证据
```

---

# 二十、隐私治理模型

GDPR 等隐私法规强调数据主体访问、纠正、删除、处理限制、可携带和反对等权利，也要求维护处理活动记录；具体是否适用、如何执行，需要依据组织角色、地区和处理目的由法律与隐私专业人员判断。

P6 应实现通用隐私工作流，而不是把某个司法辖区规则写死在代码中。

---

# 二十一、数据处理目录

Identity 维护处理活动登记：

```text
处理活动名称
数据控制者
数据处理者
处理目的
数据主体类别
数据类别
法律或合同依据
接收方
跨境传输
保留期限
安全措施
负责团队
```

GDPR 第30条列举的处理活动记录包括处理目的、数据主体和数据类别、接收方、跨境传输、删除期限以及安全措施等内容。

---

## 21.1 数据类别

建议：

```text
IDENTITY
CONTACT
AUTHENTICATION
AUTHORIZATION
SECURITY
DEVICE
AUDIT
BILLING_REFERENCE
USAGE_REFERENCE
FEDERATION
SUPPORT
```

不要把所有字段都简单标记为：

```text
PII=true
```

还需要：

```text
敏感等级
处理目的
保留规则
是否可导出
是否可删除
是否需脱敏
```

---

# 二十二、数据主体请求

支持类型：

```text
ACCESS
EXPORT
RECTIFICATION
ERASURE
RESTRICTION
OBJECTION
PORTABILITY
CONSENT_WITHDRAWAL
```

具体名称可以根据地区配置。

---

## 22.1 请求状态

```text
DRAFT
SUBMITTED
IDENTITY_VERIFICATION
TRIAGE
IN_PROGRESS
WAITING_EXTERNAL_SYSTEM
WAITING_LEGAL_REVIEW
COMPLETED
PARTIALLY_COMPLETED
REJECTED
CANCELLED
```

---

## 22.2 身份验证

隐私请求不能只因当前浏览器提交就直接导出所有数据。

根据风险要求：

```text
当前登录 Session
强再认证
企业身份验证
恢复流程
人工审核
```

管理员代用户提交时必须记录：

```text
真实操作人
法律或支持依据
目标用户
```

---

# 二十三、数据访问与导出

用户数据导出应聚合：

```text
Identity
Membership
Roles
Sessions
External Identities
OAuth Grants
API Keys 元数据
Security Events
Notification Preferences
其他 Core 返回的数据
```

不应导出：

```text
密码 Hash
TOTP Secret
完整 API Key
完整 Client Secret
其他用户个人数据
内部风险检测规则
可能危害系统安全的密钥材料
```

---

## 23.1 导出格式

```text
JSON
CSV
可阅读 HTML
ZIP
```

数据可携带场景优先采用结构化、常用、机器可读格式；GDPR 第20条对符合条件的数据可携带请求明确提出了这一形式。

---

## 23.2 导出安全

```text
异步生成
短期下载链接
强再认证
加密文件，可配置
下载次数限制
过期自动删除
完整审计
```

---

# 二十四、数据纠正

用户可以自行修改：

```text
显示名称
语言
时区
部分联系方式
```

受企业管理的数据：

```text
企业邮箱
员工编号
部门
SCIM 属性
```

必须回到对应权威来源修改。

界面显示：

```text
该字段由 Acme 企业目录管理，
请联系企业管理员修改。
```

---

# 二十五、删除与匿名化

删除请求不能等于：

```text
DELETE FROM identity_user
```

需要先构建数据影响图：

```text
Identity
Billing
Storage
Notification
AI Gateway
Workflow
Marketplace
Audit
Backup
```

---

## 25.1 删除决策

每个数据对象判断：

```text
DELETE
ANONYMIZE
RETAIN
RESTRICT
LEGAL_HOLD
MANUAL_REVIEW
```

例如：

```text
未使用的个人资料
→ DELETE

需要保留的安全审计
→ RETAIN 或 PSEUDONYMIZE

财务法定记录
→ RETAIN

公开内容作者名称
→ ANONYMIZE

正在诉讼中的记录
→ LEGAL_HOLD
```

GDPR 的删除权并非绝对权利，其适用存在条件和例外；系统因此必须支持“部分完成”和“保留原因”，而不是承诺所有请求都会物理清除所有记录。

---

## 25.2 用户删除生命周期

```text
用户提交删除
    │
    ▼
强身份验证
    │
    ▼
风险与法律检查
    │
    ▼
检查组织所有权
    │
    ▼
检查 Billing、合同和 Legal Hold
    │
    ▼
进入冷静期
    │
    ▼
允许取消
    │
    ▼
冻结新处理
    │
    ▼
跨 Core 删除编排
    │
    ▼
匿名化 Identity
    │
    ▼
撤销全部凭证
    │
    ▼
生成删除报告
```

---

## 25.3 组织所有者

用户是 TEAM 组织所有者时，不能直接删除。

必须先：

```text
转移所有权
解散组织
或进入人工处理
```

---

# 二十六、处理限制

Restriction 不等于删除。

限制状态下：

```text
允许保存
禁止一般业务处理
仅允许特定法律、安全或争议用途
```

系统需要：

```text
processing_restricted=true
restriction_reason
restriction_scope
restriction_started_at
restriction_ends_at
```

各 Core 收到限制事件后必须停止非必要处理。

---

# 二十七、数据保留策略

Retention Policy 按以下维度定义：

```text
数据类别
数据来源
组织
地区
业务目的
记录状态
保留期限
触发事件
到期动作
```

触发事件示例：

```text
记录创建
最后使用
用户离开组织
账号关闭
合同终止
安全事件关闭
```

到期动作：

```text
DELETE
ANONYMIZE
ARCHIVE
REVIEW
```

---

## 27.1 保留优先级

可能同时存在：

```text
组织保留策略
平台保留策略
合同要求
司法辖区要求
法律保留
用户删除请求
```

优先级不能写死成简单数字。

建议决策：

```text
Legal Hold
> 强制法律或合同保留
> 安全和审计最低保留
> 组织策略
> 默认平台策略
> 用户偏好
```

最终法律优先级应可由部署组织配置并接受法律审查。

---

# 二十八、法律保留 Legal Hold

Legal Hold 用于：

```text
诉讼
调查
监管要求
安全事件
合同争议
```

可以作用于：

```text
用户
组织
数据类别
时间范围
业务对象
审计事件
```

---

## 28.1 Legal Hold 要求

```text
保留原因
法律或案件编号
创建人
批准人
作用范围
生效时间
复核时间
解除时间
```

在 Hold 范围内：

```text
删除任务暂停
匿名化任务暂停
用户请求标记部分完成
保留原因仅向授权人员展示
```

---

# 二十九、跨 Core 数据编排

Identity 不能直接删除其他 Core 数据库。

正确链路：

```text
Privacy Request
    │
    ▼
Identity 创建 Data Lifecycle Job
    │
    ▼
向各 Core 发送命令
    │
    ├── Billing
    ├── Storage
    ├── Notification
    ├── AI Gateway
    ├── Workflow
    └── Marketplace
    │
    ▼
各 Core 返回处理结果
    │
    ▼
Identity 聚合完成报告
```

---

## 29.1 子任务状态

```text
PENDING
IN_PROGRESS
COMPLETED
PARTIALLY_COMPLETED
BLOCKED
FAILED
NOT_APPLICABLE
```

每个 Core 返回：

```text
删除了什么
匿名化了什么
保留了什么
保留原因
失败原因
证据摘要
```

---

# 三十、用户侧 UX

新增路由：

```text
/account/access
/account/access/requests
/account/access/requests/new
/account/access/privileged
/account/access/reviews

/account/privacy
/account/privacy/data
/account/privacy/requests
/account/privacy/export
/account/privacy/delete
```

---

## 30.1 我的访问权

显示：

```text
组织
角色
权限
授予来源
批准人
有效期
最近使用
风险等级
```

按来源区分：

```text
内置成员角色
人工授权
访问套餐
SCIM Group
临时特权
平台身份
```

用户终于可以理解：

> 我为什么有这个权限？

---

## 30.2 申请访问 UX

步骤：

```text
1. 选择所需能力
2. 说明业务理由
3. 选择有效期
4. 查看新增权限和风险
5. 提交审批
```

不要让用户直接选择：

```text
identity.permission.472
```

应选择人类可理解的套餐。

---

## 30.3 审批 UX

审批卡片必须突出：

```text
用户将新增什么能力
是否包含高风险权限
何时自动到期
是否存在冲突
```

主要按钮：

```text
批准
拒绝
要求补充信息
```

批准高风险权限不能只有一个绿色对勾。

---

## 30.4 访问审查 UX

审查列表不能一次塞入几千项。

支持按：

```text
高风险优先
长期未使用
外部成员
特权角色
服务账号
异常来源
```

分组。

提供批量认证低风险项，但高风险项必须逐项确认。

---

# 三十一、Admin Web 信息架构

```text
/admin/governance
/admin/governance/access-requests
/admin/governance/approvals
/admin/governance/access-packages
/admin/governance/grants
/admin/governance/privileged-access
/admin/governance/sod
/admin/governance/reviews
/admin/governance/orphans

/admin/compliance
/admin/compliance/frameworks
/admin/compliance/controls
/admin/compliance/assessments
/admin/compliance/findings
/admin/compliance/evidence
/admin/compliance/exceptions

/admin/privacy
/admin/privacy/requests
/admin/privacy/inventory
/admin/privacy/retention
/admin/privacy/legal-holds
/admin/privacy/jobs
```

---

# 三十二、治理总览

展示：

```text
待审批高风险申请
即将到期权限
未完成访问审查
职责冲突
孤儿 Service Account
长期未使用特权
未启用强认证的管理员
未解决控制 Finding
未完成隐私请求
被 Legal Hold 阻止的删除
```

每张指标卡必须可以进入具体对象列表。

---

# 三十三、API 设计

## 33.1 Access Package

```text
GET    /api/v1/identity/organizations/{organizationId}/access-packages
POST   /api/v1/identity/organizations/{organizationId}/access-packages
GET    /api/v1/identity/organizations/{organizationId}/access-packages/{packageId}
PATCH  /api/v1/identity/organizations/{organizationId}/access-packages/{packageId}
DELETE /api/v1/identity/organizations/{organizationId}/access-packages/{packageId}
```

---

## 33.2 Access Request

```text
GET  /api/v1/identity/me/access-requests
POST /api/v1/identity/me/access-requests
GET  /api/v1/identity/me/access-requests/{requestId}
POST /api/v1/identity/me/access-requests/{requestId}/cancel
```

审批：

```text
GET  /api/v1/identity/me/approvals
POST /api/v1/identity/me/approvals/{approvalId}/approve
POST /api/v1/identity/me/approvals/{approvalId}/reject
POST /api/v1/identity/me/approvals/{approvalId}/return
```

---

## 33.3 Privileged Activation

```text
GET  /api/v1/identity/me/eligible-access
POST /api/v1/identity/me/privileged-activations
GET  /api/v1/identity/me/privileged-activations
POST /api/v1/identity/me/privileged-activations/{activationId}/end
```

---

## 33.4 Access Review

```text
GET  /api/v1/identity/me/access-reviews
GET  /api/v1/identity/me/access-reviews/{reviewId}
POST /api/v1/identity/me/access-reviews/{reviewId}/decisions
POST /api/v1/identity/me/access-reviews/{reviewId}/complete
```

---

## 33.5 Privacy

```text
GET  /api/v1/identity/me/privacy-data
GET  /api/v1/identity/me/privacy-requests
POST /api/v1/identity/me/privacy-requests
GET  /api/v1/identity/me/privacy-requests/{requestId}
POST /api/v1/identity/me/privacy-requests/{requestId}/cancel
GET  /api/v1/identity/me/privacy-requests/{requestId}/download
```

---

# 三十四、Admin API

```text
GET  /admin-api/v1/identity/governance/overview

GET  /admin-api/v1/identity/access-requests
GET  /admin-api/v1/identity/access-requests/{id}
POST /admin-api/v1/identity/access-requests/{id}/approve
POST /admin-api/v1/identity/access-requests/{id}/reject

GET  /admin-api/v1/identity/privileged-access
POST /admin-api/v1/identity/privileged-access/{id}/terminate

GET  /admin-api/v1/identity/sod/conflicts
POST /admin-api/v1/identity/sod/conflicts/{id}/accept-risk
POST /admin-api/v1/identity/sod/conflicts/{id}/remediate

GET  /admin-api/v1/identity/access-reviews
POST /admin-api/v1/identity/access-reviews
POST /admin-api/v1/identity/access-reviews/{id}/launch
POST /admin-api/v1/identity/access-reviews/{id}/close

GET  /admin-api/v1/identity/privacy/requests
POST /admin-api/v1/identity/privacy/requests/{id}/verify
POST /admin-api/v1/identity/privacy/requests/{id}/approve
POST /admin-api/v1/identity/privacy/requests/{id}/reject

GET  /admin-api/v1/identity/compliance/controls
GET  /admin-api/v1/identity/compliance/evidence
POST /admin-api/v1/identity/compliance/evidence-packages
```

---

# 三十五、Internal API

供其他 Core 进行治理检查：

```text
POST /internal/v1/identity/governance/access/check
POST /internal/v1/identity/governance/sod/evaluate
POST /internal/v1/identity/governance/step-up/check
GET  /internal/v1/identity/governance/grants/{subjectId}
```

隐私与数据生命周期：

```text
POST /internal/v1/identity/privacy/jobs/{jobId}/results
GET  /internal/v1/identity/privacy/requests/{requestId}/scope
POST /internal/v1/identity/legal-holds/check
```

证据：

```text
POST /internal/v1/identity/evidence/submit
```

其他 Core 只能提交自己产生的证据，不能修改其他控制证据。

---

# 三十六、P6 数据表总览

新增：

```text
identity_entitlement
identity_access_package
identity_access_package_entitlement
identity_access_request
identity_approval_instance
identity_approval_step
identity_approval_decision
identity_access_grant
identity_privileged_activation

identity_sod_policy
identity_sod_policy_item
identity_sod_conflict
identity_sod_exception

identity_access_review_campaign
identity_access_review_item
identity_access_review_decision

identity_admin_role
identity_platform_operator_role

identity_compliance_framework
identity_compliance_requirement
identity_compliance_control
identity_control_mapping
identity_control_assessment
identity_control_finding
identity_control_exception
identity_evidence
identity_evidence_package

identity_processing_activity
identity_data_category
identity_privacy_request
identity_privacy_request_task
identity_retention_policy
identity_legal_hold
identity_legal_hold_scope
identity_data_lifecycle_job
```

---

# 三十七、identity_entitlement

| 字段                    | 类型           | 说明                      |
| --------------------- | ------------ | ----------------------- |
| id                    | VARCHAR(36)  | Entitlement ID          |
| organization_id       | VARCHAR(36)  | 可空，平台级                  |
| entitlement_type      | VARCHAR(30)  | ROLE/PERMISSION/SCOPE 等 |
| target_id             | VARCHAR(36)  | 目标对象                    |
| code                  | VARCHAR(180) | 稳定编码                    |
| name                  | VARCHAR(150) | 展示名称                    |
| risk_level            | VARCHAR(20)  | 风险                      |
| owner_user_id         | VARCHAR(36)  | 业务所有者                   |
| status                | VARCHAR(20)  | ACTIVE/DISABLED         |
| review_frequency_days | INTEGER      | 复核周期                    |
| created_at            | BIGINT       | 创建时间                    |
| updated_at            | BIGINT       | 更新时间                    |
| version               | BIGINT       | 乐观锁                     |

---

# 三十八、identity_access_package

| 字段                       | 类型            |
| ------------------------ | ------------- |
| id                       | VARCHAR(36)   |
| organization_id          | VARCHAR(36)   |
| package_code             | VARCHAR(120)  |
| name                     | VARCHAR(150)  |
| description              | VARCHAR(1000) |
| package_type             | VARCHAR(30)   |
| risk_level               | VARCHAR(20)   |
| requestable              | INTEGER       |
| default_duration_seconds | BIGINT        |
| max_duration_seconds     | BIGINT        |
| required_auth_level      | VARCHAR(30)   |
| owner_user_id            | VARCHAR(36)   |
| approval_policy_json     | TEXT          |
| eligibility_policy_json  | TEXT          |
| status                   | VARCHAR(30)   |
| created_at               | BIGINT        |
| updated_at               | BIGINT        |
| version                  | BIGINT        |

关联：

```text
identity_access_package_entitlement
```

---

# 三十九、identity_access_request

| 字段                  | 类型            |
| ------------------- | ------------- |
| id                  | VARCHAR(36)   |
| requester_user_id   | VARCHAR(36)   |
| target_subject_type | VARCHAR(30)   |
| target_subject_id   | VARCHAR(36)   |
| organization_id     | VARCHAR(36)   |
| access_package_id   | VARCHAR(36)   |
| business_reason     | VARCHAR(2000) |
| ticket_reference    | VARCHAR(255)  |
| requested_start_at  | BIGINT        |
| requested_end_at    | BIGINT        |
| status              | VARCHAR(30)   |
| risk_level          | VARCHAR(20)   |
| sod_result          | VARCHAR(30)   |
| submitted_at        | BIGINT        |
| completed_at        | BIGINT        |
| created_at          | BIGINT        |
| updated_at          | BIGINT        |
| version             | BIGINT        |

---

# 四十、审批表

## identity_approval_instance

```text
id
request_type
request_id
status
current_step
created_at
completed_at
version
```

## identity_approval_step

```text
id
approval_instance_id
step_order
approval_mode
required_approvals
approver_type
approver_reference
status
due_at
created_at
```

## identity_approval_decision

```text
id
approval_step_id
approver_user_id
decision
reason
decided_at
authentication_level
request_id
```

审批决定只追加，不覆盖历史决定。

---

# 四十一、identity_access_grant

| 字段              | 类型           |
| --------------- | ------------ |
| id              | VARCHAR(36)  |
| subject_type    | VARCHAR(30)  |
| subject_id      | VARCHAR(36)  |
| organization_id | VARCHAR(36)  |
| entitlement_id  | VARCHAR(36)  |
| source_type     | VARCHAR(30)  |
| source_id       | VARCHAR(36)  |
| grant_type      | VARCHAR(30)  |
| status          | VARCHAR(20)  |
| valid_from      | BIGINT       |
| expires_at      | BIGINT       |
| granted_by      | VARCHAR(36)  |
| revoked_by      | VARCHAR(36)  |
| revoked_at      | BIGINT       |
| revoke_reason   | VARCHAR(500) |
| last_used_at    | BIGINT       |
| created_at      | BIGINT       |
| updated_at      | BIGINT       |
| version         | BIGINT       |

`source_type`：

```text
DIRECT
ACCESS_REQUEST
SCIM_GROUP
BUILT_IN
REVIEW_REMEDIATION
EMERGENCY
```

---

# 四十二、identity_privileged_activation

| 字段                   | 类型            |
| -------------------- | ------------- |
| id                   | VARCHAR(36)   |
| grant_id             | VARCHAR(36)   |
| user_id              | VARCHAR(36)   |
| organization_id      | VARCHAR(36)   |
| role_id              | VARCHAR(36)   |
| reason               | VARCHAR(1000) |
| ticket_reference     | VARCHAR(255)  |
| status               | VARCHAR(20)   |
| authentication_level | VARCHAR(30)   |
| session_id           | VARCHAR(36)   |
| activated_at         | BIGINT        |
| expires_at           | BIGINT        |
| ended_at             | BIGINT        |
| created_at           | BIGINT        |
| updated_at           | BIGINT        |
| version              | BIGINT        |

---

# 四十三、SoD 表

## identity_sod_policy

```text
id
organization_id
name
policy_type
enforcement_mode
status
owner_user_id
created_at
updated_at
version
```

## identity_sod_policy_item

```text
id
policy_id
left_entitlement_id
right_entitlement_id
conflict_type
risk_level
```

## identity_sod_conflict

```text
id
policy_id
subject_id
left_grant_id
right_grant_id
status
detected_at
resolved_at
resolution
```

## identity_sod_exception

```text
id
conflict_id
reason
compensating_control
approved_by
valid_from
expires_at
review_at
status
```

---

# 四十四、Access Review 表

## identity_access_review_campaign

```text
id
organization_id
name
campaign_type
scope_json
reviewer_policy_json
status
starts_at
due_at
completed_at
created_by
created_at
updated_at
version
```

## identity_access_review_item

```text
id
campaign_id
subject_type
subject_id
entitlement_id
grant_id
reviewer_user_id
risk_level
last_used_at
status
created_at
updated_at
version
```

## identity_access_review_decision

```text
id
review_item_id
reviewer_user_id
decision
reason
new_expiry_at
replacement_entitlement_id
decided_at
```

---

# 四十五、合规控制表

## identity_compliance_framework

```text
id
framework_code
name
version
publisher
status
```

## identity_compliance_requirement

```text
id
framework_id
requirement_code
title
description
```

## identity_compliance_control

```text
id
control_code
name
description
control_type
owner_user_id
frequency
status
```

## identity_control_mapping

```text
control_id
requirement_id
mapping_type
notes
```

同一个控制可以映射多个框架要求。

---

# 四十六、证据表

## identity_evidence

```text
id
control_id
evidence_type
source_service
source_reference
period_start
period_end
content_location
checksum
signature
collected_at
collected_by
status
```

## identity_evidence_package

```text
id
name
framework_id
scope_json
period_start
period_end
status
manifest_location
checksum
generated_at
expires_at
```

大文件本体放 `core-storage`，Identity 只保留元数据和引用。

---

# 四十七、隐私表

## identity_processing_activity

```text
id
organization_id
activity_code
name
purpose
controller
processor
data_subject_categories_json
data_categories_json
recipient_categories_json
transfer_details_json
retention_summary
security_measures_summary
owner_user_id
status
created_at
updated_at
version
```

## identity_privacy_request

```text
id
user_id
organization_id
request_type
jurisdiction
status
verification_level
submitted_at
due_at
completed_at
rejection_reason
created_at
updated_at
version
```

## identity_privacy_request_task

```text
id
privacy_request_id
target_service
task_type
status
result_summary
retention_reason
evidence_reference
started_at
completed_at
updated_at
version
```

---

# 四十八、保留与 Legal Hold 表

## identity_retention_policy

```text
id
organization_id
data_category
trigger_type
retention_seconds
expiration_action
jurisdiction
priority
status
owner_user_id
created_at
updated_at
version
```

## identity_legal_hold

```text
id
organization_id
case_reference
name
reason
status
approved_by
effective_at
review_at
released_at
created_by
created_at
updated_at
version
```

## identity_legal_hold_scope

```text
id
legal_hold_id
scope_type
scope_reference
data_category
period_start
period_end
```

---

# 四十九、事务设计

## 49.1 批准访问申请

同一事务：

```text
锁定 Access Request
验证当前审批步骤
记录 Approval Decision
判断审批是否完成
重新执行 SoD 检查
创建 Access Grant
应用 Role/Permission
增加 authorization_version
Access Request → APPROVED
写 Audit
写 Outbox
```

---

## 49.2 Grant 到期

同一事务：

```text
锁定 Grant
Grant → EXPIRED
撤销关联角色或权限
终止 Privileged Activation
增加 authorization_version
写 Audit
写 Outbox
```

---

## 49.3 Access Review 撤销决定

同一事务：

```text
记录 Review Decision
锁定 Grant
Grant → REVOKED
撤销授权
增加 authorization_version
Review Item → COMPLETED
写 Audit
写 Outbox
```

---

## 49.4 隐私删除完成

不能跨数据库事务。

采用 Saga：

```text
Privacy Request
→ Data Lifecycle Job
→ 每个 Core Task
→ 聚合结果
→ Identity 匿名化
→ 撤销身份
→ 生成完成报告
```

---

# 五十、与其他 Core 的交互

## 50.1 core-api-gateway

负责：

```text
识别 Privileged Token
限制特权 Token Audience
透传 Activation ID
限制代理 Session
记录 Request ID
阻止过期 Grant
```

业务服务仍需验证具体业务对象。

---

## 50.2 core-billing

Billing 提供治理对象：

```text
财务查看者
支付管理员
退款创建者
退款批准者
结算管理员
```

典型 SoD：

```text
退款创建
X
同一退款批准
```

Billing 必须在业务对象层执行动态 SoD。

---

## 50.3 core-storage

Storage 负责：

```text
证据文件
隐私导出文件
法律保留对象锁
数据删除任务
数据归档
```

Identity 不能将证据 ZIP 大量存入 SQLite BLOB。

---

## 50.4 core-notification

通知：

```text
待审批
审批超时
权限即将到期
访问审查
特权激活
职责冲突
隐私请求进度
Legal Hold 影响
控制 Finding
```

---

## 50.5 core-workflow

Workflow 可以执行：

```text
审批提醒
访问审查任务
控制测试任务
隐私请求编排
整改任务
证据采集计划
```

但以下状态仍由 Identity 管理：

```text
Grant
Approval
Review Decision
SoD Conflict
Privacy Request
Legal Hold
```

不能让 Workflow 自己修改 Identity 表。

---

## 50.6 core-ai-gateway

治理对象：

```text
模型供应商密钥访问
Prompt 管理
Agent 高权限 Tool
敏感数据导出
请求日志访问
预算调整
```

AI 可以辅助：

```text
总结审计证据
归类 Finding
解释权限差异
```

AI 不能：

```text
自动批准高权限
自动拒绝隐私请求
自动解除 Legal Hold
自动确认合规
```

---

## 50.7 core-marketplace

Marketplace 插件需要声明：

```text
所需权限
数据类别
处理目的
保留期限
外部传输
卸载后的数据处理
```

安装高风险插件可以触发：

```text
访问申请
数据处理评估
组织管理员批准
```

---

# 五十一、P6 事件

```text
identity.access_request.submitted
identity.access_request.approved
identity.access_request.rejected
identity.access_grant.created
identity.access_grant.expired
identity.access_grant.revoked

identity.privileged_access.activated
identity.privileged_access.ended
identity.break_glass.used

identity.sod.conflict_detected
identity.sod.exception_created
identity.sod.conflict_resolved

identity.access_review.launched
identity.access_review.decision_recorded
identity.access_review.completed

identity.control.finding_created
identity.control.finding_resolved
identity.evidence.collected
identity.evidence_package.generated

identity.privacy_request.submitted
identity.privacy_request.verified
identity.privacy_request.completed
identity.privacy_request.partially_completed

identity.retention_policy.applied
identity.legal_hold.created
identity.legal_hold.released
```

---

# 五十二、安全注意点

## 52.1 审批不能等于授权者手工写数据库

任何批准都必须经过 Application Service 和正式 Grant。

## 52.2 权限来源必须可追踪

每个有效权限都必须能追溯到：

```text
Built-in Role
SCIM Group
Access Request
Direct Grant
Emergency Grant
```

## 52.3 审计员不能修改被审计对象

审计角色默认只读。

## 52.4 隐私管理员不能查看全部凭证秘密

隐私请求不应导出：

```text
密码 Hash
TOTP Secret
API Key Secret
Client Secret
```

## 52.5 Legal Hold 不能成为万能拒绝删除理由

Hold 必须具有：

```text
明确范围
案件编号
审批
复核日期
解除流程
```

## 52.6 合规证据不能由页面截图充当唯一证据

截图可以辅助，但主要证据应来自：

```text
结构化查询
不可变审计
配置快照
签名导出
控制测试结果
```

---

# 五十三、测试设计

## 53.1 Access Request

```text
正常申请
无资格申请
高风险审批
自己批准自己
审批超时
申请取消
审批完成后 Grant 创建
Grant 自动到期
```

## 53.2 SoD

```text
静态角色冲突
权限冲突
跨套餐冲突
例外审批
例外到期
动态 SoD 上下文
```

## 53.3 Privileged Access

```text
正常激活
认证等级不足
审批缺失
超过最大时长
Token 超出 Activation
提前结束
Break-glass 使用
```

## 53.4 Access Review

```text
Campaign 创建
审查项生成
批准保留
撤销权限
修改有效期
审查超时
自动暂停
重复决定
```

## 53.5 管理员分权

```text
Audit Admin 修改用户被拒绝
Support Admin 导出全量数据被拒绝
Privacy Admin 修改角色被拒绝
Identity Admin 删除审计被拒绝
```

## 53.6 隐私请求

```text
身份验证
数据导出
部分删除
Legal Hold 阻止删除
组织所有者删除
跨 Core 超时
重复请求
下载过期
```

## 53.7 审计证据

```text
事件哈希链
证据校验和
证据包生成
证据包篡改检测
权限隔离
保留期限
```

---

# 五十四、P5 到 P6 数据迁移

```text
1. 将现有 Role、Permission、Scope 注册为 Entitlement
2. 为现有高风险角色指定 Owner
3. 将平台 SUPER_ADMIN 拆分为细分管理员角色
4. 保留一个紧急 Break-glass 身份
5. 为现有直接授权创建 Grant 来源
6. 建立默认 SoD 策略
7. 建立首个特权访问审查 Campaign
8. 创建数据类别与处理活动目录
9. 为各 Core 注册隐私任务适配器
10. 最后启用自动到期和自动撤销
```

不要升级后立即撤销所有无法追溯来源的权限。

先标记为：

```text
LEGACY_UNGOVERNED
```

然后进入治理 Campaign。

---

# 五十五、P6 实施顺序

## P6.1：Entitlement 与 Grant

```text
Entitlement
Grant
权限来源
有效期
所有者
```

## P6.2：Access Package 与审批

```text
访问套餐
访问申请
多级审批
自动授予
自动到期
```

## P6.3：特权访问

```text
Eligible Role
Activation
Privileged Session
Break-glass
```

## P6.4：职责分离

```text
静态冲突
风险例外
补偿性控制
业务 Core 动态 SoD API
```

## P6.5：Access Review

```text
Campaign
Review Item
Decision
自动整改
孤儿身份
```

## P6.6：管理员分权

```text
平台角色拆分
代理 Session
支持人员最小权限
```

最小权限原则要求主体只获得完成任务所需的最少系统资源和授权。

## P6.7：审计与证据

```text
审计哈希
Evidence
Control
Framework Mapping
Assessment
Finding
```

## P6.8：隐私与数据生命周期

```text
数据目录
隐私请求
导出
删除
保留
Legal Hold
跨 Core Saga
```

---

# 五十六、P6 验收标准

## 访问治理

```text
用户可以申请访问套餐
高风险访问必须审批
Grant 拥有来源和有效期
临时访问自动到期
所有权限可以追溯来源
```

## 特权治理

```text
特权角色支持 Eligible/Active
激活要求强认证
特权 Session 短期有效
Break-glass 自动产生高危事件
```

## 职责分离

```text
系统可以检测静态冲突
业务 Core 可以执行动态 SoD
例外具有补偿控制和有效期
```

## 访问复核

```text
可以创建访问审查 Campaign
可以复核用户、角色、服务账号和应用
审查决定可以自动整改
未审查高风险访问可以自动暂停
```

## 管理员分权

```text
不再依赖日常 SUPER_ADMIN
安全、审计、支持、隐私角色相互隔离
代理访问保留真实操作者
```

## 审计与证据

```text
审计只追加
证据具有校验和
控制可以映射多个框架
可以生成可信证据包
```

## 隐私

```text
用户可以提交隐私请求
数据可以跨 Core 导出
删除请求支持部分完成
Legal Hold 可以阻止特定删除
保留理由可追踪
```

---

# 五十七、P6 最重要的注意点

## 1. 权限管理不等于身份治理

P2 解决：

```text
用户拥有什么权限
```

P6 解决：

```text
为什么拥有
谁批准
何时失效
是否冲突
是否仍需要
```

## 2. 不要保留一个日常万能超级管理员

Break-glass 可以存在，但必须只用于紧急情况。

## 3. 审查不是导出 Excel 后人工打勾

审查结果必须真正影响 Grant 和授权版本。

## 4. 合规映射不是合规认证

同一技术控制可以支持多个框架，但最终是否满足标准需要结合组织过程和外部评估。

## 5. 用户删除不等于删除全部历史事实

需要区分：

```text
删除
匿名化
保留
处理限制
Legal Hold
```

## 6. Workflow 只能编排，不能拥有治理事实

审批、Grant、审查和 Legal Hold 的最终状态仍属于 Identity。

---

# 五十八、P6 最终成果

P6 完成后：

```text
core-identity-backend

成为访问申请、审批、特权激活、
职责分离、访问审查和隐私生命周期的事实中心。
```

```text
core-identity-web

成为用户申请权限、批准访问、
参与复核和行使数据权利的治理门户。
```

```text
core-identity-admin-backend

成为平台治理、合规证据、隐私请求和
跨 Core 生命周期任务的编排中心。
```

```text
core-identity-admin-web

成为身份治理、访问复核、职责冲突、
审计证据和隐私运营控制台。
```

P6 最终建立的完整治理链路是：

```text
业务需要
→ 访问申请
→ 风险与冲突检查
→ 独立审批
→ 有限期 Grant
→ 特权激活
→ 持续监控
→ 周期复核
→ 自动到期或撤销
→ 审计证据
```

隐私生命周期则是：

```text
数据发现
→ 处理目的
→ 权限限制
→ 保留策略
→ 数据请求
→ 跨 Core 执行
→ 删除、匿名化或合法保留
→ 完成证据
```

到这一阶段，Core Identity 才真正从“企业身份平台”升级为：

> 能够证明每一项访问为什么存在，并在它不再合理时自动终止它。
