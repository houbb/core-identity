# Core Identity P4：账号安全与零信任基础

版本：P4
前置依赖：P0 工程边界、P1 Identity MVP、P2 组织与权限、P3 平台级身份服务
目标：抵抗账号接管、凭证填充、钓鱼、会话劫持和高权限误用，并为企业安全策略与身份治理建立基础。

---

# 一、P4 核心目标

P3 已经解决：

```text
用户、应用和服务如何获得身份
Token 如何签发
Scope 和 Permission 如何限制访问
```

P4 继续解决：

```text
当前登录是否真的可信？
用户使用了多强的认证方式？
登录环境是否异常？
高风险操作是否需要再次验证？
密码或认证器丢失后如何安全恢复？
账号被接管后如何快速止损？
组织能否强制成员采用更强认证？
管理员如何发现和处置安全事件？
```

P4 必须完成：

```text
TOTP MFA
WebAuthn / Passkey
硬件安全密钥
恢复码
认证器生命周期
认证强度等级
敏感操作再认证
登录风险评估
账号攻击防护
设备与会话安全
账号恢复
安全事件中心
组织安全策略
平台安全策略
管理员应急处置
```

NIST SP 800-63-4 将认证强度与会话管理分开考虑，并要求较高认证等级提供抗钓鱼认证选项；密码本身不具备抗钓鱼能力。P4 因此不能只依赖“密码 + 短信或邮箱验证码”，而应将 WebAuthn/Passkey 作为长期主方向。

---

# 二、P4 暂时不做什么

P4 不实现：

```text
企业外部 OIDC 身份提供商
SAML
SCIM
员工自动入离职
身份核验 KYC
完整设备管理平台 MDM
终端 EDR
企业访问审批
职责分离
定期访问审查
复杂行为机器学习模型
跨地域安全数据湖
多数据中心风险计算
```

这些分别属于 P5、P6 和 P7。

P4 的范围是：

> 对 Core Identity 自己发行和管理的账号、认证器、会话与凭证建立可信安全基础。

---

# 三、P4 的安全原则

## 3.1 永不单独信任密码

密码只能证明：

```text
请求者知道一个共享秘密
```

不能证明：

```text
请求者使用的是可信设备
请求没有来自钓鱼网站
密码没有从其他网站泄露
当前会话没有被窃取
```

因此高风险账号和高风险操作必须支持或要求更强认证。

---

## 3.2 零信任不是“每次操作都输入验证码”

P4 中的零信任表示：

```text
不因为来自内网就信任
不因为已经登录就永久信任
不因为设备曾经使用过就永久信任
不因为用户拥有 ADMIN 角色就跳过验证
```

每次访问仍根据：

```text
身份
组织
权限
认证强度
会话状态
设备上下文
操作风险
```

作出决定。

---

## 3.3 认证与授权必须分开

认证回答：

```text
你是否证明了自己是谁？
证明强度是多少？
```

授权回答：

```text
你是否有权执行这个操作？
```

最终访问条件：

```text
Permission 允许
AND
Scope 允许
AND
Token / Session 有效
AND
认证强度满足要求
AND
风险策略没有阻止
```

---

## 3.4 风险判断不能成为黑盒

每一次阻止或升级认证，都必须产生机器可读原因：

```text
NEW_DEVICE
IMPOSSIBLE_TRAVEL
IP_REPUTATION_HIGH_RISK
PASSWORD_SPRAY_SUSPECTED
SESSION_REAUTH_REQUIRED
AUTHENTICATOR_TOO_WEAK
```

管理员和用户应看到可理解的解释，而不是：

```text
系统认为你有风险
```

---

## 3.5 恢复流程不能弱于正常认证

如果账号要求 Passkey 或 MFA，却允许仅凭一封普通邮件立即清除全部认证器，那么 MFA 实际没有意义。

账号恢复必须根据账号风险和认证器强度采用分级流程。

---

# 四、四个子项目的职责

```text
core-identity/
├── core-identity-backend/
├── core-identity-web/
├── core-identity-admin-backend/
└── core-identity-admin-web/
```

---

## 4.1 core-identity-backend

P4 新增职责：

```text
Authenticator Registry
TOTP
WebAuthn / Passkey
Recovery Code
Authentication Challenge
Authentication Assurance
Step-up Authentication
Risk Assessment
Device Context
Login Protection
Session Security
Account Recovery
Security Policy
Security Event
Authenticator Notification
Emergency Revocation
```

只有 Identity Backend 可以：

```text
绑定认证器
验证认证器
计算认证强度
签发高强度认证上下文
修改会话认证等级
撤销认证器
执行账号恢复
```

---

## 4.2 core-identity-web

P4 新增用户侧能力：

```text
安全中心
MFA 设置
Passkey 设置
安全密钥管理
恢复码管理
设备与会话
登录活动
安全事件
账号恢复
再认证页面
组织安全策略提示
```

---

## 4.3 core-identity-admin-backend

P4 新增管理编排：

```text
风险账号聚合
异常登录查询
安全事件聚合
强制撤销会话
强制密码重置
强制重新注册 MFA
账号恢复审批
组织安全策略编排
认证器覆盖率统计
高危操作再认证
安全操作脱敏
```

它仍然不能：

```text
直接验证 TOTP
直接验证 WebAuthn
直接读取认证器密钥
直接更新认证器数据库
自行决定认证强度
```

---

## 4.4 core-identity-admin-web

P4 新增：

```text
安全总览
风险登录
高风险账号
认证器覆盖率
MFA 策略
Passkey 策略
账号恢复审批
会话处置
安全事件时间线
认证攻击趋势
平台安全配置
```

---

# 五、认证器统一模型

P4 不应为密码、TOTP、Passkey 和恢复码分别写四套完全独立的生命周期。

统一抽象：

```text
Authenticator
├── PASSWORD
├── TOTP
├── WEBAUTHN
├── RECOVERY_CODE_SET
└── EMAIL_RECOVERY
```

但要注意：

```text
EMAIL_RECOVERY
```

是恢复渠道，不应算作强 MFA 因子。NIST 指南不把邮件作为有效的带外认证器；OWASP 也建议恢复流程采用一次性、安全、限时的机制，而不是把安全问题或弱邮箱证明作为唯一恢复依据。

---

## 5.1 认证器状态

```text
PENDING
ACTIVE
SUSPENDED
COMPROMISED
REVOKED
```

### PENDING

注册流程尚未验证完成。

### ACTIVE

可以用于认证。

### SUSPENDED

暂时停用，可以由安全流程恢复。

### COMPROMISED

用户或管理员认为认证器可能泄露。

必须立即停止使用。

### REVOKED

永久撤销。

---

## 5.2 认证器保证级别

内部定义：

```text
AUTH_LEVEL_1
AUTH_LEVEL_2
AUTH_LEVEL_3
```

本项目不必声称取得任何外部合规认证，但可以借鉴认证保证等级的思想。

建议映射：

```text
AUTH_LEVEL_1
密码

AUTH_LEVEL_2
密码 + TOTP
密码 + 硬件 OTP
Passkey，用户验证能力满足策略

AUTH_LEVEL_3
受组织认可的硬件安全密钥
不可导出密钥
用户验证
管理员高安全策略
```

实际等级由策略与认证器属性共同决定，不只看认证器名称。

---

# 六、密码安全升级

P4 不删除密码登录，但要修正密码策略。

## 6.1 密码策略

建议：

```text
允许长密码
支持密码管理器
支持粘贴
不要求无意义的定期更换
不强制复杂的字符组合套路
检查常见密码和已泄露密码列表
密码泄露或风险事件后要求修改
```

NIST SP 800-63-4 明确建议使用常见、预期或已泄露密码阻止列表，同时不建议增加额外的字符组合规则。

---

## 6.2 密码泄露检查

定义端口：

```java
public interface CompromisedPasswordChecker {
    PasswordExposureResult check(char[] password);
}
```

默认实现可支持：

```text
本地常见密码列表
离线泄露密码摘要库
可选外部匿名查询 Provider
```

禁止：

```text
将完整密码发送给第三方 API
将密码写入日志
将密码保存到审计 metadata
```

---

## 6.3 密码重新哈希

用户成功登录后，如果发现：

```text
当前 Hash 参数低于最新安全策略
```

则透明重新 Hash。

流程：

```text
验证旧 Hash 成功
→ 生成新 Hash
→ 更新 Credential
→ 用户无感
```

---

# 七、TOTP MFA

## 7.1 定位

TOTP 是 P4 的基础 MFA 方式。

优点：

```text
部署简单
不依赖短信
大部分认证器应用支持
离线可用
```

限制：

```text
仍可能被实时钓鱼
依赖共享密钥
用户容易丢失设备
输入体验不如 Passkey
```

因此：

> TOTP 是可用的过渡型 MFA，但不是最终的抗钓鱼主方案。

---

## 7.2 TOTP 注册流程

```text
用户进入安全中心
    │
    ▼
点击“添加认证器应用”
    │
    ▼
要求当前密码或已有强认证器再认证
    │
    ▼
后端创建 PENDING TOTP Authenticator
    │
    ▼
生成随机 Secret
    │
    ▼
前端展示 QR Code 和手动密钥
    │
    ▼
用户输入当前 TOTP
    │
    ▼
后端验证
    │
    ▼
Authenticator → ACTIVE
    │
    ▼
生成恢复码
    │
    ▼
记录审计并发送安全通知
```

---

## 7.3 TOTP Secret 存储

TOTP 需要服务端验证共享 Secret，因此不能只保存 Hash。

必须：

```text
使用平台主密钥加密保存
数据库只保存密文
密钥版本单独记录
禁止出现在日志和响应
```

未来切换主密钥时必须支持重新加密。

---

## 7.4 TOTP 重放防护

记录：

```text
last_accepted_time_step
```

相同时间窗口的验证码不允许重复使用。

允许有限时间漂移：

```text
当前窗口
前一窗口
后一窗口
```

具体范围配置化。

不要为了兼容错误设备而接受过大的时间窗口。

---

## 7.5 TOTP UX

页面显示：

```text
1. 扫描二维码
2. 输入应用生成的 6 位验证码
3. 保存恢复码
```

必须提供：

```text
无法扫码？显示手动密钥
复制密钥
返回上一步
```

完成前不要把认证器标记为 ACTIVE。

---

# 八、WebAuthn 与 Passkey

WebAuthn 使用面向特定 Relying Party 的公钥凭证，认证私钥由认证器持有；这一模型避免服务端保存可用于模拟用户的共享认证秘密，并具备抗钓鱼能力。

## 8.1 P4 支持的 WebAuthn 场景

```text
平台 Passkey
同步 Passkey
设备绑定 Passkey
硬件安全密钥
密码后的第二因子
无密码登录
敏感操作确认
```

P4 建议分两步上线：

```text
P4.1
WebAuthn 作为第二因子和再认证方式

P4.2
允许 Passkey 作为首要登录方式
```

不要第一天就彻底删除密码登录和恢复机制。

---

## 8.2 WebAuthn 注册流程

```text
用户进入安全中心
    │
    ▼
点击“添加 Passkey”
    │
    ▼
执行再认证
    │
    ▼
POST /webauthn/registration/options
    │
    ▼
后端创建 Registration Challenge
    │
    ▼
浏览器调用 navigator.credentials.create()
    │
    ▼
认证器创建公私钥
    │
    ▼
POST /webauthn/registration/verify
    │
    ▼
后端验证 Challenge、Origin、RP ID 和签名
    │
    ▼
保存 Public Key Credential
    │
    ▼
写入安全事件和审计
```

---

## 8.3 WebAuthn 登录流程

```text
用户输入邮箱，或选择 Passkey 登录
    │
    ▼
请求 Authentication Options
    │
    ▼
浏览器调用 navigator.credentials.get()
    │
    ▼
认证器完成用户验证
    │
    ▼
后端验证：
    Challenge
    Origin
    RP ID
    Credential ID
    Signature
    Sign Count / Backup State
    │
    ▼
创建高强度 Session
```

---

## 8.4 WebAuthn 凭证类型

系统记录：

```text
PLATFORM
CROSS_PLATFORM
UNKNOWN
```

以及：

```text
discoverable
backup_eligible
backup_state
user_verification
attestation_format
aaguid
transports
```

不要把所有 WebAuthn 凭证统一显示成：

```text
安全密钥
```

用户看到的名称可以是：

```text
Windows Hello
iCloud 钥匙串
Android Passkey
YubiKey
USB 安全密钥
```

如果无法识别，则显示：

```text
Passkey
```

---

## 8.5 Attestation 策略

社区版默认建议：

```text
attestation = none
```

企业组织可以配置：

```text
允许任意认证器
只允许经过认证的认证器
只允许指定 AAGUID
必须使用硬件安全密钥
```

P4 只建立策略接口。

严格企业 Attestation 治理可以继续在 P6 完善。

---

## 8.6 Passkey UX

登录页主要操作可以调整为：

```text
使用 Passkey 登录
使用密码登录
```

浏览器支持条件式 UI 时，可以允许账号选择器直接显示 Passkey。

失败信息要区分：

```text
用户取消
浏览器不支持
认证器不可用
凭证不存在
验证失败
系统错误
```

用户主动取消不应显示红色安全警报。

---

# 九、恢复码

## 9.1 定位

恢复码用于：

```text
用户失去 TOTP 设备
Passkey 暂时不可用
无法完成正常 MFA
```

它是应急认证器，不是日常登录方式。

---

## 9.2 恢复码生成

建议生成：

```text
8～12 个一次性恢复码
```

数据库只保存 Hash。

每个恢复码：

```text
只能使用一次
使用后立即标记 USED
不能再次显示
```

---

## 9.3 恢复码 UX

生成后展示：

```text
下载
复制
打印
```

同时提示：

```text
请将恢复码保存在密码管理器或离线安全位置。
不要与密码存放在同一个不受保护的文本文件中。
```

用户必须确认：

```text
我已经保存恢复码
```

才能完成首次 MFA 启用。

---

## 9.4 重新生成

重新生成恢复码时：

```text
旧恢复码全部失效
生成新集合
记录安全事件
发送通知
```

需要强认证或敏感操作再认证。

---

# 十、认证强度上下文

每个 Session 增加：

```text
authentication_level
authentication_methods
authenticated_at
strong_auth_at
```

示例：

```json
{
  "authenticationLevel": "AUTH_LEVEL_2",
  "methods": [
    "PASSWORD",
    "TOTP"
  ],
  "authenticatedAt": "...",
  "strongAuthAt": "..."
}
```

WebAuthn Session：

```json
{
  "authenticationLevel": "AUTH_LEVEL_2",
  "methods": [
    "WEBAUTHN"
  ]
}
```

硬件安全策略满足更高要求时：

```text
AUTH_LEVEL_3
```

---

# 十一、敏感操作再认证

## 11.1 需要再认证的操作

建议包括：

```text
修改密码
修改主邮箱
关闭 MFA
删除最后一个 Passkey
重新生成恢复码
创建高权限 API Key
创建 Client Secret
创建 Service Account Credential
扩大 OAuth Client Scope
转移组织所有权
解散组织
修改组织安全策略
创建平台管理员
轮换 Signing Key
导出敏感审计数据
```

---

## 11.2 再认证不是重新登录

用户保持当前 Session。

系统创建：

```text
Step-up Challenge
```

完成后提升：

```text
strong_auth_at
authentication_level
```

在短时间内允许执行对应操作。

---

## 11.3 再认证令牌

完成强认证后，后端可以在 Session 中建立：

```text
StepUpGrant
```

属性：

```text
session_id
required_level
allowed_actions
issued_at
expires_at
consumed_at
```

高危操作建议一次性使用：

```text
转移组织所有权
关闭最后一个 MFA
生成新的 Client Secret
```

普通敏感操作可以在短时间窗口内复用。

---

## 11.4 前端交互

用户点击敏感操作：

```text
当前认证强度足够
    → 直接继续

认证强度不足
    → 打开安全验证弹窗
```

弹窗根据用户认证器显示：

```text
使用 Passkey
使用认证器应用
使用恢复码
重新输入密码
```

优先显示最强且最方便的方式。

---

# 十二、登录风险评估

P4 风险引擎必须先做规则化、可解释的版本。

不要一开始引入复杂 AI 模型。

## 12.1 风险信号

```text
IP 地址
IP 网段变化
国家或地区变化
User-Agent
浏览器
操作系统
设备 Cookie
历史登录设备
登录时间
密码失败次数
邮箱枚举行为
账号尝试分布
访问速度
Token 使用异常
Refresh Token 重放
API Key 异常使用
组织敏感程度
用户权限等级
```

---

## 12.2 不应作为唯一阻止依据的信号

```text
IP 变化
国家变化
设备名称变化
浏览器版本变化
VPN
移动网络切换
```

这些信号误报率较高。

应组合判断，而不是：

```text
新 IP = 黑客
```

---

## 12.3 风险等级

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### LOW

```text
熟悉设备
常见位置
正常行为
```

决策：

```text
正常登录
```

### MEDIUM

```text
新设备
较大位置变化
普通账号异常时间登录
```

决策：

```text
要求 MFA
发送登录通知
```

### HIGH

```text
多次密码失败
疑似凭证填充
高权限账号新设备
异常 Token 行为
```

决策：

```text
要求抗钓鱼认证
不允许仅使用恢复码
限制敏感操作
```

### CRITICAL

```text
Refresh Token 重放
明确泄露凭证
短时间多国使用
平台管理员账号高危异常
```

决策：

```text
阻止登录
撤销相关会话
暂停凭证
产生安全事件
通知用户和管理员
```

---

## 12.4 风险决策

风险引擎输出：

```json
{
  "riskLevel": "HIGH",
  "decision": "STEP_UP_REQUIRED",
  "requiredAuthLevel": "AUTH_LEVEL_2",
  "reasons": [
    "NEW_DEVICE",
    "PRIVILEGED_ACCOUNT",
    "IP_REPUTATION_HIGH_RISK"
  ]
}
```

风险引擎不直接修改用户表。

它输出决策，由认证流程执行。

---

# 十三、攻击防护

OWASP 将凭证填充、密码喷洒与暴力破解视为不同但防御措施高度相关的攻击场景，并建议结合 MFA、限流、异常检测等控制，而不是只采用简单账户锁定。

## 13.1 多维限流

维度：

```text
IP
邮箱摘要
User ID
设备标识
Client ID
组织
接口
```

不要只按 IP 限流。

攻击者可能分布式访问，正常企业也可能共享出口 IP。

---

## 13.2 渐进式延迟

连续失败时增加：

```text
轻微延迟
验证码挑战
MFA 要求
临时阻止
```

避免固定阈值后长时间锁死账号。

---

## 13.3 账号锁定

P1 的简单锁定升级为：

```text
短期锁定
风险锁定
管理员锁定
凭证锁定
```

不要因为忘记密码请求而锁定账号，因为攻击者可以借此对已知邮箱实施拒绝服务。

---

## 13.4 CAPTCHA

只在以下情况触发：

```text
自动化行为明显
大量失败
异常请求速度
```

不要默认所有用户登录都显示 CAPTCHA。

CAPTCHA 是自动化成本控制，不是身份认证因子。

---

# 十四、设备上下文

## 14.1 Device 不是绝对可信身份

设备记录用于：

```text
展示登录设备
识别新设备
辅助风险评估
减少重复挑战
```

不能单独用于：

```text
永久跳过 MFA
授予权限
重置账号
```

---

## 14.2 设备标识

使用：

```text
第一方随机 Device Cookie
浏览器和系统摘要
首次见到时间
最近见到时间
```

不要尝试构建侵入式浏览器指纹。

不收集不必要的：

```text
字体列表
Canvas 指纹
硬件序列号
精确地理位置
```

---

## 14.3 记住此设备

“记住此设备”真正含义：

```text
在有限时间内降低重复 MFA 频率
```

不是：

```text
该设备永久可信
```

可信记录绑定：

```text
user_id
device_id
authenticator_id
created_at
expires_at
last_used_at
revoked_at
```

以下情况立即失效：

```text
用户修改密码
用户撤销全部会话
认证器被撤销
账号被标记高风险
组织策略变更
管理员强制失效
```

---

# 十五、会话安全升级

NIST 会话模型要求会话由认证事件建立，并且会话的保证等级不能高于触发它的认证事件；同时建议同时管理总体超时和非活动超时，并在必要时进行重新认证。

## 15.1 Session 新增字段

```text
authentication_level
authentication_methods_json
strong_auth_at
device_id
risk_level
reauth_required
security_version
last_risk_evaluated_at
```

---

## 15.2 会话安全版本

在 User 增加：

```text
security_version
```

以下操作递增：

```text
修改密码
完成账号恢复
撤销全部认证器
账号被禁用
检测到账号接管
管理员紧急锁定
```

Session 创建时记录：

```text
session.security_version
```

如果小于：

```text
user.security_version
```

Session 立即失效。

这比逐条更新大量 Session 更稳定。

---

## 15.3 会话再认证

会话同时具有：

```text
idle_expires_at
absolute_expires_at
reauth_required_at
```

达到再认证时间后：

```text
不一定彻底退出
但敏感操作和后续访问要求重新认证
```

对于即将超时的用户，应提前提示保存工作并提供清晰的重新认证入口。

---

## 15.4 会话绑定

P4 可以增加轻量绑定：

```text
Session Cookie
Device Cookie
CSRF Token
```

如果 Device Cookie 异常变化：

```text
提高风险等级
要求再认证
```

不要立即永久封禁用户。

---

## 15.5 Cookie 规则

继续使用：

```text
HttpOnly
Secure
SameSite
固定 Cookie 名称
最小 Path
合理 Domain
```

登录或认证强度提升后：

```text
轮换 Session ID
```

防止 Session Fixation。

---

# 十六、安全中心 UX

用户侧路由：

```text
/account/security
/account/security/passkeys
/account/security/authenticator-apps
/account/security/recovery-codes
/account/security/devices
/account/security/sessions
/account/security/activity
/account/security/recovery
```

---

## 16.1 安全总览

展示：

```text
账号安全等级
已配置认证器
Passkey 数量
TOTP 数量
恢复码状态
活跃会话
最近安全事件
组织安全要求
```

安全状态：

```text
基础
良好
强
需要处理
```

不要使用虚假的：

```text
你的账号安全评分是 87 分
```

除非评分有透明、稳定的计算标准。

建议使用可解释检查项：

```text
✓ 已设置 Passkey
✓ 已保存恢复码
△ 仍保留密码登录
✕ 最近出现高风险登录
```

---

## 16.2 添加认证器

主要入口：

```text
添加 Passkey
添加认证器应用
添加安全密钥
```

优先推荐：

```text
Passkey
```

但不能贬低或隐藏其他恢复方式。

---

## 16.3 删除认证器

删除前检查：

```text
是否仍有其他有效认证器
是否仍有恢复码
组织策略是否要求该类型认证器
是否为唯一抗钓鱼认证器
```

删除最后一个强认证器时：

```text
要求强再认证
显示风险
可能要求先添加替代认证器
```

---

# 十七、登录 UX

## 17.1 登录方式排序

存在 Passkey 时：

```text
使用 Passkey 登录
使用密码登录
```

密码验证后需要 MFA：

```text
使用 Passkey
认证器验证码
恢复码
```

不要默认优先恢复码。

---

## 17.2 MFA 挑战页面

显示：

```text
正在登录哪个账号
为什么需要额外验证
当前可用方式
是否为新设备
```

示例：

```text
这是一个新设备，因此需要额外验证。
```

而不是：

```text
触发风控规则 RISK-2093
```

---

## 17.3 尝试其他方式

提供：

```text
使用 Passkey
使用认证器应用
使用安全密钥
使用恢复码
无法使用这些方式
```

方式按照安全性与可用性排序。

---

## 17.4 登录通知

新设备或高风险登录成功后通知：

```text
登录时间
大致位置
浏览器与设备
IP 摘要
如果不是你该怎么办
```

操作：

```text
这是我
不是我
```

点击“不是我”后：

```text
撤销目标会话
提高账号风险
引导修改密码
检查认证器
显示其他活跃会话
```

---

# 十八、账号恢复

## 18.1 恢复场景

```text
忘记密码
丢失 TOTP 设备
Passkey 全部不可用
恢复码丢失
邮箱仍可用
邮箱也不可用
账号疑似被接管
```

不能用一个流程处理全部情况。

---

## 18.2 恢复等级

### Recovery Level 1

用户仍有：

```text
有效 Passkey
有效 TOTP
恢复码
```

通过现有认证器自助恢复。

### Recovery Level 2

用户失去强认证器，但仍有：

```text
已验证邮箱
已知密码
熟悉设备
```

进入延迟恢复：

```text
邮箱确认
风险评估
安全等待期
通知全部现有渠道
```

### Recovery Level 3

用户失去：

```text
邮箱
密码
认证器
恢复码
```

不能自动恢复。

进入：

```text
人工审核
企业管理员担保
身份核验
或拒绝恢复
```

P4 只建立审批骨架，不建设复杂 KYC。

---

## 18.3 恢复冷静期

高风险恢复可以进入：

```text
PENDING_RECOVERY
```

冷静期内：

```text
通知原邮箱
通知活跃设备
允许取消恢复
冻结敏感操作
不立即清除认证器
```

冷静期结束后才完成恢复。

---

## 18.4 恢复完成

必须：

```text
递增 security_version
撤销全部 Session
撤销 Refresh Token Family
撤销高风险 API Key，可配置
撤销旧恢复码
暂停或撤销旧 MFA
要求重新设置密码
要求重新注册 MFA
记录安全事件
发送多渠道通知
```

---

## 18.5 禁止安全问题作为唯一恢复方式

不使用：

```text
你母亲的姓名是什么？
你的第一所学校是什么？
```

作为唯一恢复凭证。OWASP 明确指出安全问题答案经常容易猜测或被获取，不应单独作为密码重置或身份恢复依据。

---

# 十九、组织安全策略

P2 组织管理员可以管理成员。

P4 增加组织级安全要求。

## 19.1 策略项目

```text
是否要求 MFA
是否要求抗钓鱼认证
允许的认证器类型
是否允许 TOTP
是否允许同步 Passkey
是否要求硬件安全密钥
管理员是否必须更强认证
受信设备有效期
会话最大时长
空闲超时
敏感操作再认证时长
账号恢复方式
```

---

## 19.2 策略应用对象

```text
ALL_MEMBERS
PRIVILEGED_MEMBERS
SPECIFIC_ROLES
```

P4 不实现复杂条件表达式。

---

## 19.3 策略状态

```text
DRAFT
ENFORCING
ACTIVE
SUSPENDED
```

建议支持宽限期：

```text
策略发布
→ 14 天配置期
→ 正式强制
```

不要管理员保存后立即把整个组织成员锁在门外。

---

## 19.4 不符合策略的成员

状态：

```text
COMPLIANT
GRACE_PERIOD
NON_COMPLIANT
EXEMPTED
```

处理：

```text
GRACE_PERIOD
允许登录，但持续提示配置

NON_COMPLIANT
只能访问安全设置和帮助页面

EXEMPTED
管理员明确豁免，必须有原因和过期时间
```

永久豁免应避免。

---

# 二十、组织安全策略 UX

路由：

```text
/organizations/:organizationId/security
/organizations/:organizationId/security/policies
/organizations/:organizationId/security/compliance
```

策略向导：

```text
1. 选择保护对象
2. 选择认证要求
3. 设置宽限期
4. 预览受影响成员
5. 发布策略
```

发布前展示：

```text
受影响成员：128
已经符合：92
需要配置 MFA：31
可能失去访问：5
```

不能只显示抽象配置项。

---

# 二十一、平台管理员安全策略

平台策略控制：

```text
平台管理员必须使用 Passkey 或安全密钥
平台管理员不能使用仅密码登录
高危管理操作必须 AUTH_LEVEL_3
管理 Session 更短
管理端禁止长期受信设备
平台管理员恢复必须双人审核
```

P4 可以先实现：

```text
平台管理员强制 AUTH_LEVEL_2
Signing Key、角色和安全策略操作要求强再认证
```

更严格的双人审批放到 P6。

---

# 二十二、安全事件模型

## 22.1 事件类型

```text
LOGIN_NEW_DEVICE
LOGIN_HIGH_RISK
LOGIN_BLOCKED
PASSWORD_SPRAY_DETECTED
CREDENTIAL_STUFFING_SUSPECTED
ACCOUNT_TEMPORARILY_LOCKED

MFA_ENABLED
MFA_DISABLED
AUTHENTICATOR_ADDED
AUTHENTICATOR_REMOVED
AUTHENTICATOR_COMPROMISED
RECOVERY_CODES_REGENERATED

REFRESH_TOKEN_REUSE_DETECTED
API_KEY_SUSPICIOUS_USAGE
CLIENT_SECRET_SUSPICIOUS_USAGE
SESSION_HIJACK_SUSPECTED

ACCOUNT_RECOVERY_STARTED
ACCOUNT_RECOVERY_CANCELLED
ACCOUNT_RECOVERY_COMPLETED

ORGANIZATION_POLICY_VIOLATION
PRIVILEGED_ACCOUNT_WEAK_AUTH
```

---

## 22.2 严重级别

```text
INFO
LOW
MEDIUM
HIGH
CRITICAL
```

---

## 22.3 生命周期

```text
OPEN
INVESTIGATING
RESOLVED
FALSE_POSITIVE
```

P4 可以让用户处理自己的部分事件：

```text
确认是本人
报告不是本人
```

平台管理员处理平台级事件。

---

# 二十三、Admin Security Console

新增路由：

```text
/admin/security
/admin/security/events
/admin/security/events/:eventId
/admin/security/risky-users
/admin/security/login-attempts
/admin/security/authenticators
/admin/security/recoveries
/admin/security/policies
/admin/security/sessions
```

---

## 23.1 安全总览

展示：

```text
高风险登录
被阻止登录
MFA 覆盖率
Passkey 覆盖率
平台管理员强认证覆盖率
待处理恢复
疑似凭证填充
Refresh Token 重放
异常 API Key
```

不能只展示累计总数。

应展示：

```text
最近 24 小时
最近 7 天
趋势
需要处理的事件
```

---

## 23.2 风险用户详情

展示：

```text
用户基本信息
组织
平台权限
认证器
当前 Session
最近登录
风险事件
账号恢复
Token 和 API Key 状态
```

管理员操作：

```text
撤销全部会话
要求重新登录
要求修改密码
要求重新注册 MFA
暂停认证器
禁用账号
启动账号恢复
标记误报
```

每个操作必须有影响说明。

---

## 23.3 紧急锁定

紧急锁定操作：

```text
User → LOCKED_SECURITY
security_version + 1
全部 Session 失效
Refresh Token Family 失效
高风险 API Key 暂停
OAuth Grant 暂停，可配置
产生 CRITICAL 事件
通知用户
```

不同于普通登录失败产生的短期锁定。

---

# 二十四、Public API

## 24.1 认证器

```text
GET    /api/v1/identity/me/authenticators
DELETE /api/v1/identity/me/authenticators/{authenticatorId}
POST   /api/v1/identity/me/authenticators/{authenticatorId}/rename
POST   /api/v1/identity/me/authenticators/{authenticatorId}/report-compromised
```

---

## 24.2 TOTP

```text
POST /api/v1/identity/me/authenticators/totp/enroll
POST /api/v1/identity/me/authenticators/totp/confirm
POST /api/v1/identity/me/authenticators/totp/cancel
```

---

## 24.3 WebAuthn

```text
POST /api/v1/identity/webauthn/registration/options
POST /api/v1/identity/webauthn/registration/verify

POST /api/v1/identity/webauthn/authentication/options
POST /api/v1/identity/webauthn/authentication/verify
```

---

## 24.4 恢复码

```text
POST /api/v1/identity/me/recovery-codes
GET  /api/v1/identity/me/recovery-codes/status
```

不能提供：

```text
GET /recovery-codes/full
```

---

## 24.5 再认证

```text
POST /api/v1/identity/reauthentication/challenges
POST /api/v1/identity/reauthentication/challenges/{challengeId}/verify
GET  /api/v1/identity/reauthentication/status
```

---

## 24.6 安全活动

```text
GET  /api/v1/identity/me/security-events
GET  /api/v1/identity/me/login-activity
POST /api/v1/identity/me/security-events/{eventId}/confirm
POST /api/v1/identity/me/security-events/{eventId}/report
```

---

## 24.7 账号恢复

```text
POST /api/v1/identity/account-recoveries
GET  /api/v1/identity/account-recoveries/{recoveryId}
POST /api/v1/identity/account-recoveries/{recoveryId}/verify
POST /api/v1/identity/account-recoveries/{recoveryId}/cancel
POST /api/v1/identity/account-recoveries/{recoveryId}/complete
```

---

# 二十五、组织安全策略 API

```text
GET /api/v1/identity/organizations/{organizationId}/security-policy
PUT /api/v1/identity/organizations/{organizationId}/security-policy

POST /api/v1/identity/organizations/{organizationId}/security-policy/publish
POST /api/v1/identity/organizations/{organizationId}/security-policy/suspend

GET /api/v1/identity/organizations/{organizationId}/security-compliance
GET /api/v1/identity/organizations/{organizationId}/security-compliance/{membershipId}

POST /api/v1/identity/organizations/{organizationId}/security-exemptions
DELETE /api/v1/identity/organizations/{organizationId}/security-exemptions/{exemptionId}
```

---

# 二十六、Admin API

```text
GET /admin-api/v1/identity/security/overview
GET /admin-api/v1/identity/security/events
GET /admin-api/v1/identity/security/events/{eventId}

GET /admin-api/v1/identity/security/risky-users
GET /admin-api/v1/identity/security/users/{userId}

POST /admin-api/v1/identity/security/users/{userId}/lock
POST /admin-api/v1/identity/security/users/{userId}/unlock
POST /admin-api/v1/identity/security/users/{userId}/revoke-sessions
POST /admin-api/v1/identity/security/users/{userId}/require-password-reset
POST /admin-api/v1/identity/security/users/{userId}/require-mfa-reset

GET  /admin-api/v1/identity/security/recoveries
POST /admin-api/v1/identity/security/recoveries/{recoveryId}/approve
POST /admin-api/v1/identity/security/recoveries/{recoveryId}/reject

GET /admin-api/v1/identity/security/authenticator-metrics
GET /admin-api/v1/identity/security/login-metrics
```

---

# 二十七、Internal API

供其他 Core 查询认证上下文：

```text
POST /internal/v1/identity/authentication-context/introspect
```

响应：

```json
{
  "subjectId": "user-id",
  "sessionId": "session-id",
  "authenticationLevel": "AUTH_LEVEL_2",
  "authenticationMethods": [
    "PASSWORD",
    "TOTP"
  ],
  "strongAuthAt": "...",
  "riskLevel": "LOW",
  "reauthenticationRequired": false
}
```

其他 Core 发起敏感操作要求：

```text
POST /internal/v1/identity/step-up/requirements/evaluate
```

请求：

```json
{
  "subjectId": "user-id",
  "sessionId": "session-id",
  "organizationId": "organization-id",
  "action": "billing.payment_method.update",
  "requiredLevel": "AUTH_LEVEL_2"
}
```

---

# 二十八、P4 数据表总览

P4 新增：

```text
identity_authenticator
identity_totp_authenticator
identity_webauthn_credential
identity_recovery_code_set
identity_recovery_code

identity_authentication_challenge
identity_step_up_grant

identity_device
identity_trusted_device

identity_risk_assessment
identity_risk_signal
identity_security_event

identity_security_policy
identity_security_policy_target
identity_security_exemption

identity_account_recovery
identity_account_recovery_evidence

identity_login_throttle
```

扩展：

```text
identity_user
identity_session
identity_credential
identity_platform_operator
```

---

# 二十九、identity_authenticator

| 字段                        | 类型           | 说明                     |
| ------------------------- | ------------ | ---------------------- |
| id                        | VARCHAR(36)  | 认证器 ID                 |
| user_id                   | VARCHAR(36)  | 用户                     |
| authenticator_type        | VARCHAR(30)  | PASSWORD/TOTP/WEBAUTHN |
| name                      | VARCHAR(150) | 用户定义名称                 |
| status                    | VARCHAR(30)  | 状态                     |
| assurance_level           | VARCHAR(30)  | 认证强度                   |
| phishing_resistant        | INTEGER      | 是否抗钓鱼                  |
| user_verification_capable | INTEGER      | 是否支持用户验证               |
| enrolled_at               | BIGINT       | 注册时间                   |
| last_used_at              | BIGINT       | 最近使用                   |
| compromised_at            | BIGINT       | 报告泄露时间                 |
| revoked_at                | BIGINT       | 撤销时间                   |
| created_at                | BIGINT       | 创建时间                   |
| updated_at                | BIGINT       | 更新时间                   |
| version                   | BIGINT       | 乐观锁                    |

索引：

```text
idx_identity_authenticator_user_status
idx_identity_authenticator_user_type
idx_identity_authenticator_last_used
```

密码凭证仍可保留在：

```text
identity_credential
```

但应建立逻辑上的 Authenticator 映射。

---

# 三十、identity_totp_authenticator

| 字段                     | 类型          | 说明            |
| ---------------------- | ----------- | ------------- |
| authenticator_id       | VARCHAR(36) | 主键            |
| encrypted_secret       | TEXT        | 加密 Secret     |
| encryption_key_version | VARCHAR(50) | 密钥版本          |
| algorithm              | VARCHAR(20) | SHA1/SHA256 等 |
| digits                 | INTEGER     | 位数            |
| period_seconds         | INTEGER     | 周期            |
| last_accepted_step     | BIGINT      | 防重放           |
| confirmed_at           | BIGINT      | 完成注册时间        |

不要将 Secret 放入通用 metadata JSON。

---

# 三十一、identity_webauthn_credential

| 字段                 | 类型           | 说明                      |
| ------------------ | ------------ | ----------------------- |
| authenticator_id   | VARCHAR(36)  | 认证器                     |
| credential_id      | TEXT         | WebAuthn Credential ID  |
| public_key         | TEXT         | 公钥                      |
| user_handle        | VARCHAR(255) | User Handle             |
| sign_count         | BIGINT       | 签名计数                    |
| aaguid             | VARCHAR(100) | AAGUID                  |
| transports_json    | TEXT         | 传输方式                    |
| attachment         | VARCHAR(30)  | PLATFORM/CROSS_PLATFORM |
| discoverable       | INTEGER      | 是否可发现                   |
| backup_eligible    | INTEGER      | 是否可备份                   |
| backup_state       | INTEGER      | 当前备份状态                  |
| attestation_format | VARCHAR(50)  | Attestation 格式          |
| created_origin     | VARCHAR(500) | 注册 Origin               |
| rp_id              | VARCHAR(255) | RP ID                   |
| created_at         | BIGINT       | 创建时间                    |
| last_used_at       | BIGINT       | 最近使用                    |

约束：

```text
UNIQUE(credential_id)
```

---

# 三十二、恢复码表

## identity_recovery_code_set

| 字段              | 类型          |
| --------------- | ----------- |
| id              | VARCHAR(36) |
| user_id         | VARCHAR(36) |
| status          | VARCHAR(20) |
| total_count     | INTEGER     |
| remaining_count | INTEGER     |
| generated_at    | BIGINT      |
| revoked_at      | BIGINT      |
| version         | BIGINT      |

## identity_recovery_code

| 字段          | 类型           |
| ----------- | ------------ |
| id          | VARCHAR(36)  |
| code_set_id | VARCHAR(36)  |
| code_hash   | VARCHAR(255) |
| status      | VARCHAR(20)  |
| used_at     | BIGINT       |
| created_at  | BIGINT       |

恢复码明文不入库。

---

# 三十三、identity_authentication_challenge

| 字段                   | 类型           | 说明                               |
| -------------------- | ------------ | -------------------------------- |
| id                   | VARCHAR(36)  | Challenge ID                     |
| user_id              | VARCHAR(36)  | 用户，可空                            |
| session_id           | VARCHAR(36)  | Session，可空                       |
| challenge_type       | VARCHAR(40)  | 登录、注册、再认证                        |
| required_level       | VARCHAR(30)  | 要求等级                             |
| allowed_methods_json | TEXT         | 允许方式                             |
| challenge_hash       | VARCHAR(255) | Challenge 摘要                     |
| context_json         | TEXT         | 上下文                              |
| status               | VARCHAR(20)  | PENDING/SUCCEEDED/FAILED/EXPIRED |
| attempt_count        | INTEGER      | 尝试次数                             |
| expires_at           | BIGINT       | 过期时间                             |
| completed_at         | BIGINT       | 完成时间                             |
| created_at           | BIGINT       | 创建时间                             |
| version              | BIGINT       | 乐观锁                              |

WebAuthn 原始 Challenge 不能长期保存明文。

---

# 三十四、identity_step_up_grant

| 字段                   | 类型          |
| -------------------- | ----------- |
| id                   | VARCHAR(36) |
| user_id              | VARCHAR(36) |
| session_id           | VARCHAR(36) |
| authentication_level | VARCHAR(30) |
| allowed_actions_json | TEXT        |
| status               | VARCHAR(20) |
| issued_at            | BIGINT      |
| expires_at           | BIGINT      |
| consumed_at          | BIGINT      |
| created_at           | BIGINT      |
| version              | BIGINT      |

高危 Grant 一次使用后：

```text
CONSUMED
```

---

# 三十五、identity_device

| 字段                 | 类型           |
| ------------------ | ------------ |
| id                 | VARCHAR(36)  |
| user_id            | VARCHAR(36)  |
| device_cookie_hash | VARCHAR(255) |
| display_name       | VARCHAR(150) |
| browser            | VARCHAR(100) |
| operating_system   | VARCHAR(100) |
| first_seen_at      | BIGINT       |
| last_seen_at       | BIGINT       |
| last_ip            | VARCHAR(64)  |
| status             | VARCHAR(20)  |
| created_at         | BIGINT       |
| updated_at         | BIGINT       |
| version            | BIGINT       |

不保存精确 GPS。

---

# 三十六、identity_trusted_device

| 字段                          | 类型          |
| --------------------------- | ----------- |
| id                          | VARCHAR(36) |
| user_id                     | VARCHAR(36) |
| device_id                   | VARCHAR(36) |
| trusted_by_authenticator_id | VARCHAR(36) |
| status                      | VARCHAR(20) |
| trusted_at                  | BIGINT      |
| expires_at                  | BIGINT      |
| last_used_at                | BIGINT      |
| revoked_at                  | BIGINT      |
| version                     | BIGINT      |

“可信”只是减少 MFA 频率，不表示绕过权限。

---

# 三十七、identity_risk_assessment

| 字段                  | 类型           |
| ------------------- | ------------ |
| id                  | VARCHAR(36)  |
| user_id             | VARCHAR(36)  |
| session_id          | VARCHAR(36)  |
| operation           | VARCHAR(100) |
| risk_level          | VARCHAR(20)  |
| decision            | VARCHAR(40)  |
| required_auth_level | VARCHAR(30)  |
| score               | INTEGER      |
| reasons_json        | TEXT         |
| model_version       | VARCHAR(50)  |
| request_id          | VARCHAR(64)  |
| created_at          | BIGINT       |

`score` 只用于内部排序。

真实决策必须由：

```text
risk_level
decision
reasons
```

表达。

---

# 三十八、identity_risk_signal

| 字段                | 类型           |
| ----------------- | ------------ |
| id                | VARCHAR(36)  |
| assessment_id     | VARCHAR(36)  |
| signal_type       | VARCHAR(80)  |
| signal_value_hash | VARCHAR(255) |
| weight            | INTEGER      |
| result            | VARCHAR(30)  |
| metadata_json     | TEXT         |
| created_at        | BIGINT       |

敏感值应摘要化或脱敏。

---

# 三十九、identity_security_event

| 字段                 | 类型            |
| ------------------ | ------------- |
| id                 | VARCHAR(36)   |
| user_id            | VARCHAR(36)   |
| organization_id    | VARCHAR(36)   |
| event_type         | VARCHAR(100)  |
| severity           | VARCHAR(20)   |
| status             | VARCHAR(30)   |
| source             | VARCHAR(50)   |
| risk_assessment_id | VARCHAR(36)   |
| title              | VARCHAR(200)  |
| description        | VARCHAR(1000) |
| metadata_json      | TEXT          |
| detected_at        | BIGINT        |
| resolved_at        | BIGINT        |
| resolved_by        | VARCHAR(36)   |
| resolution         | VARCHAR(500)  |
| created_at         | BIGINT        |
| updated_at         | BIGINT        |
| version            | BIGINT        |

---

# 四十、identity_security_policy

| 字段                               | 类型           |
| -------------------------------- | ------------ |
| id                               | VARCHAR(36)  |
| organization_id                  | VARCHAR(36)  |
| name                             | VARCHAR(150) |
| status                           | VARCHAR(30)  |
| minimum_auth_level               | VARCHAR(30)  |
| phishing_resistant_required      | INTEGER      |
| allowed_authenticator_types_json | TEXT         |
| privileged_roles_only            | INTEGER      |
| trusted_device_days              | INTEGER      |
| session_idle_seconds             | INTEGER      |
| session_absolute_seconds         | INTEGER      |
| reauth_seconds                   | INTEGER      |
| grace_period_ends_at             | BIGINT       |
| created_by                       | VARCHAR(36)  |
| published_at                     | BIGINT       |
| created_at                       | BIGINT       |
| updated_at                       | BIGINT       |
| version                          | BIGINT       |

P4 可先限制每个组织一个主策略。

---

# 四十一、identity_security_exemption

| 字段            | 类型           |
| ------------- | ------------ |
| id            | VARCHAR(36)  |
| policy_id     | VARCHAR(36)  |
| membership_id | VARCHAR(36)  |
| reason        | VARCHAR(500) |
| status        | VARCHAR(20)  |
| expires_at    | BIGINT       |
| granted_by    | VARCHAR(36)  |
| created_at    | BIGINT       |
| revoked_at    | BIGINT       |

豁免必须有过期时间。

---

# 四十二、identity_account_recovery

| 字段                      | 类型          |
| ----------------------- | ----------- |
| id                      | VARCHAR(36) |
| user_id                 | VARCHAR(36) |
| recovery_type           | VARCHAR(40) |
| status                  | VARCHAR(30) |
| risk_level              | VARCHAR(20) |
| required_evidence_level | VARCHAR(30) |
| initiated_ip            | VARCHAR(64) |
| initiated_device_id     | VARCHAR(36) |
| cooling_off_until       | BIGINT      |
| approved_by             | VARCHAR(36) |
| rejected_by             | VARCHAR(36) |
| completed_at            | BIGINT      |
| cancelled_at            | BIGINT      |
| created_at              | BIGINT      |
| updated_at              | BIGINT      |
| version                 | BIGINT      |

状态：

```text
PENDING_VERIFICATION
COOLING_OFF
PENDING_REVIEW
APPROVED
REJECTED
CANCELLED
COMPLETED
```

---

# 四十三、identity_login_throttle

| 字段                | 类型           |
| ----------------- | ------------ |
| id                | VARCHAR(36)  |
| dimension_type    | VARCHAR(30)  |
| dimension_hash    | VARCHAR(255) |
| failure_count     | INTEGER      |
| blocked_until     | BIGINT       |
| last_failure_at   | BIGINT       |
| window_started_at | BIGINT       |
| updated_at        | BIGINT       |
| version           | BIGINT       |

维度：

```text
USER
EMAIL_HASH
IP
DEVICE
CLIENT
```

高频临时计数可以同时放内存，数据库用于持久风险状态。

---

# 四十四、现有表扩展

## identity_user

增加：

```text
security_version
security_status
risk_level
mfa_enrolled
phishing_resistant_enrolled
recovery_state
last_security_review_at
```

不要把所有认证器信息直接塞进 User。

---

## identity_session

增加：

```text
device_id
authentication_level
authentication_methods_json
strong_auth_at
risk_level
reauth_required_at
security_version
last_risk_evaluated_at
```

---

## identity_credential

增加：

```text
hash_policy_version
last_rehashed_at
compromised_detected_at
```

---

# 四十五、事务设计

## 45.1 启用 TOTP

同一事务：

```text
锁定 PENDING Authenticator
验证验证码
Authenticator → ACTIVE
保存 last_accepted_step
生成恢复码集合
增加 User security_version，可选
写 Audit
写 Security Event
写 Outbox
```

---

## 45.2 注册 Passkey

同一事务：

```text
锁定 Challenge
验证 Challenge
验证 Origin 和 RP ID
验证签名与用户验证
创建 Authenticator
创建 WebAuthnCredential
Challenge → SUCCEEDED
写 Audit
写 Security Event
```

---

## 45.3 删除认证器

同一事务：

```text
强再认证
锁定 Authenticator
检查组织策略
检查剩余认证器
Authenticator → REVOKED
撤销关联 Trusted Device
必要时 security_version + 1
写 Audit
写 Security Event
```

---

## 45.4 高风险登录成功

同一事务：

```text
完成强认证
创建或升级 Session
保存 Risk Assessment
更新 Device
写 Login Attempt
写 Audit
写 Security Event
写登录通知 Outbox
```

---

## 45.5 账号恢复完成

同一事务：

```text
锁定 Recovery
验证状态和冷静期
User security_version + 1
撤销所有 Session
撤销 Refresh Token Family
撤销恢复码
暂停旧认证器
要求修改密码
Recovery → COMPLETED
写 Audit
写 CRITICAL Security Event
写通知 Outbox
```

---

# 四十六、P4 事件

```text
identity.authenticator.enrolled
identity.authenticator.revoked
identity.authenticator.compromised
identity.mfa.enabled
identity.mfa.disabled

identity.passkey.registered
identity.passkey.used
identity.recovery_codes.generated
identity.recovery_code.used

identity.login.risk_detected
identity.login.step_up_required
identity.login.blocked
identity.login.new_device

identity.session.authentication_upgraded
identity.session.security_revoked

identity.account_recovery.started
identity.account_recovery.approved
identity.account_recovery.rejected
identity.account_recovery.completed

identity.security_policy.published
identity.security_policy.enforced
identity.security_policy.violation_detected

identity.security_event.created
identity.security_event.resolved
```

不要把每一次普通 TOTP 验证都广播给所有 Core。

---

# 四十七、与 core-api-gateway 的交互

Gateway 负责：

```text
基础 IP 限流
请求频率统计
异常 Client 行为
Token 和 API Key 提取
设备上下文透传
Request ID
```

Gateway 可以传递：

```text
X-Forwarded-For
User-Agent
Client ID
Token JTI
```

Identity 自己必须计算最终风险。

Gateway 不得自行决定：

```text
用户已经通过 MFA
当前认证等级为 AUTH_LEVEL_2
```

除非信息来自经过验证的 Identity Token 或内部可信上下文。

---

# 四十八、与 core-notification 的交互

P4 通知包括：

```text
新设备登录
高风险登录
密码修改
MFA 启用
MFA 关闭
Passkey 添加
认证器删除
恢复码重新生成
账号恢复开始
账号恢复完成
会话紧急撤销
安全策略即将强制
```

高风险安全通知应优先发送，不能与营销通知共用退订逻辑。

用户不能关闭：

```text
密码已修改
MFA 已关闭
账号恢复开始
安全凭证新增
```

这类关键安全通知。

---

# 四十九、与 core-billing 的交互

Billing 可以定义敏感操作：

```text
添加支付方式
修改支付方式
发起退款
修改结算主体
查看完整账单敏感信息
```

调用前检查：

```text
required_auth_level = AUTH_LEVEL_2
```

大额或高风险操作可要求：

```text
AUTH_LEVEL_3
```

Identity 不决定金额风险。

Billing 提交操作上下文，Identity 判断认证强度是否满足。

---

# 五十、与 core-storage 的交互

Storage 敏感操作：

```text
删除大量文件
导出全部数据
生成长期公开链接
修改组织存储策略
访问特别敏感文件
```

Storage 可以要求 Step-up。

文件敏感等级由 Storage 管理。

认证强度由 Identity 管理。

---

# 五十一、与 core-ai-gateway 的交互

高风险 AI 操作：

```text
新增 Provider Key
查看或轮换 Provider Key
允许 Agent 使用高权限 Tool
提高预算
启用外部数据发送
```

要求强再认证。

AI Gateway 可以将异常调用模式发送给 Identity：

```text
API Key 突然跨地区使用
Service Account 调用量异常
高权限 Agent 调用异常
```

Identity 将其作为风险信号，而不是让 AI 模型直接封禁账号。

---

# 五十二、与 core-workflow 的交互

Workflow 可以处理：

```text
组织 MFA 宽限期提醒
账号恢复冷静期结束
安全事件通知
长期未使用认证器提醒
可信设备过期
安全策略违规升级
```

但核心认证状态必须由 Identity 修改。

Workflow 不能直接：

```text
清除 MFA
恢复账号
修改认证强度
```

---

# 五十三、与 core-marketplace 的交互

Marketplace 应用请求高风险 Scope 时：

```text
用户必须完成强再认证
授权页明确显示风险
组织策略可以禁止
```

插件不能提供任意 Java 类来替换核心认证。

未来允许扩展：

```text
RiskSignalProvider
AuthenticatorMetadataProvider
SecurityNotificationSink
```

扩展只能提供信号或 Provider。

最终决策仍由 Identity Security Policy 完成。

---

# 五十四、审计事件

P4 必须审计：

```text
AUTHENTICATOR_ENROLLED
AUTHENTICATOR_REVOKED
AUTHENTICATOR_COMPROMISED
TOTP_ENABLED
PASSKEY_REGISTERED
RECOVERY_CODES_GENERATED
RECOVERY_CODE_USED

STEP_UP_STARTED
STEP_UP_COMPLETED
STEP_UP_FAILED

HIGH_RISK_LOGIN_DETECTED
LOGIN_BLOCKED
NEW_DEVICE_LOGIN
SESSION_SECURITY_REVOKED

SECURITY_POLICY_CREATED
SECURITY_POLICY_PUBLISHED
SECURITY_EXEMPTION_GRANTED

ACCOUNT_RECOVERY_STARTED
ACCOUNT_RECOVERY_APPROVED
ACCOUNT_RECOVERY_REJECTED
ACCOUNT_RECOVERY_COMPLETED

EMERGENCY_ACCOUNT_LOCKED
EMERGENCY_ACCOUNT_UNLOCKED
```

审计中不能保存：

```text
TOTP Secret
验证码
恢复码
WebAuthn Challenge
私钥
完整 Device Cookie
```

---

# 五十五、错误码

```text
IDENTITY_AUTHENTICATOR_NOT_FOUND
IDENTITY_AUTHENTICATOR_NOT_ACTIVE
IDENTITY_AUTHENTICATOR_ALREADY_EXISTS
IDENTITY_AUTHENTICATOR_REQUIRED
IDENTITY_AUTHENTICATOR_POLICY_VIOLATION

IDENTITY_TOTP_INVALID
IDENTITY_TOTP_REPLAYED
IDENTITY_TOTP_ENROLLMENT_EXPIRED

IDENTITY_WEBAUTHN_CHALLENGE_INVALID
IDENTITY_WEBAUTHN_ORIGIN_INVALID
IDENTITY_WEBAUTHN_RP_ID_INVALID
IDENTITY_WEBAUTHN_CREDENTIAL_UNKNOWN
IDENTITY_WEBAUTHN_SIGNATURE_INVALID
IDENTITY_WEBAUTHN_USER_VERIFICATION_REQUIRED

IDENTITY_RECOVERY_CODE_INVALID
IDENTITY_RECOVERY_CODE_USED
IDENTITY_RECOVERY_CODES_EXHAUSTED

IDENTITY_STEP_UP_REQUIRED
IDENTITY_STEP_UP_FAILED
IDENTITY_AUTHENTICATION_LEVEL_INSUFFICIENT

IDENTITY_LOGIN_RISK_HIGH
IDENTITY_LOGIN_BLOCKED
IDENTITY_DEVICE_NOT_TRUSTED
IDENTITY_SESSION_REAUTH_REQUIRED

IDENTITY_SECURITY_POLICY_VIOLATION
IDENTITY_MFA_REQUIRED
IDENTITY_PHISHING_RESISTANT_AUTH_REQUIRED

IDENTITY_ACCOUNT_RECOVERY_PENDING
IDENTITY_ACCOUNT_RECOVERY_COOLING_OFF
IDENTITY_ACCOUNT_RECOVERY_REJECTED
IDENTITY_ACCOUNT_RECOVERY_EVIDENCE_INSUFFICIENT
```

WebAuthn 的底层错误不要原样返回给用户。

---

# 五十六、测试设计

## 56.1 TOTP

```text
正常注册
错误验证码
过期注册
时间漂移
同窗口重放
Secret 加密
删除认证器
唯一认证器删除保护
```

---

## 56.2 WebAuthn

```text
正常注册
正常认证
Challenge 不匹配
Origin 不匹配
RP ID 不匹配
签名错误
Credential 不存在
用户取消
重复 Credential
Discoverable Credential
备份状态变化
安全密钥拔出
```

WebAuthn 协议验证应优先使用经过维护的成熟实现库。

---

## 56.3 恢复码

```text
正常使用
重复使用
错误恢复码
重新生成后旧码失效
全部耗尽
数据库无明文
```

---

## 56.4 Step-up

```text
认证等级足够
认证等级不足
Grant 过期
一次性 Grant 重复使用
操作不匹配
Session 不匹配
组织不匹配
```

---

## 56.5 风险评估

```text
熟悉设备
新设备
新 IP
高权限用户
密码喷洒
凭证填充
Refresh Token 重放
误报处理
多信号组合
风险原因完整
```

---

## 56.6 会话安全

```text
security_version 变化后旧 Session 失效
认证器撤销后相关 Session 失效
强认证时间过期
空闲超时
绝对超时
Session ID 轮换
Device Cookie 变化
```

---

## 56.7 账号恢复

```text
恢复码自助恢复
邮箱延迟恢复
冷静期取消
管理员批准
管理员拒绝
恢复完成撤销全部 Session
恢复完成撤销 Refresh Token
恢复并发执行
```

---

## 56.8 组织策略

```text
要求 MFA
要求 Passkey
管理员角色强制更高认证
宽限期
成员不符合策略
临时豁免
豁免过期
策略变更
```

---

## 56.9 越权测试

```text
用户删除他人认证器
组织管理员查看成员 TOTP Secret
平台管理员查看完整恢复码
Admin Backend 直接验证 TOTP
伪造 authentication_level
伪造 strong_auth_at
普通用户修改组织安全策略
```

---

# 五十七、P3 到 P4 数据迁移

迁移步骤：

```text
1. 创建 Authenticator 统一表
2. 将现有 Password Credential 映射为 PASSWORD Authenticator
3. 为 User 初始化 security_version
4. 为 Session 初始化 AUTH_LEVEL_1
5. 创建 Security Event 和 Risk Assessment 表
6. 创建默认平台安全策略
7. 平台管理员进入 MFA 宽限期
8. 启用 TOTP
9. 启用 WebAuthn
10. 宽限期结束后强制平台管理员 MFA
```

不能在升级瞬间要求所有用户必须 Passkey。

---

# 五十八、P4 实施顺序

## P4.1：认证器统一模型

完成：

```text
Authenticator
Authentication Level
Authentication Challenge
Session 认证上下文
再认证基础
```

理由：

先统一模型，再增加具体认证方式。

---

## P4.2：TOTP 与恢复码

完成：

```text
TOTP 注册
TOTP 登录
恢复码
安全中心
MFA 登录流程
```

理由：

快速建立基础 MFA 闭环。

---

## P4.3：WebAuthn 与 Passkey

完成：

```text
Passkey 注册
Passkey 登录
安全密钥
再认证
Credential 管理
```

理由：

建立长期抗钓鱼认证方向。

---

## P4.4：会话与设备安全

完成：

```text
Device
Trusted Device
security_version
Session 再认证
会话风险状态
```

理由：

认证完成后仍需持续保护会话。

---

## P4.5：风险引擎

完成：

```text
风险信号
规则评估
风险等级
Step-up 决策
登录攻击防护
安全事件
```

理由：

先拥有强认证器，风险引擎才能要求用户升级认证。

---

## P4.6：账号恢复

完成：

```text
分级恢复
冷静期
恢复审批
恢复后全局撤销
```

理由：

强认证上线后，恢复流程必须同步完善。

---

## P4.7：组织安全策略

完成：

```text
MFA 策略
Passkey 策略
管理员强认证
宽限期
合规状态
豁免
```

理由：

最后让组织强制成员执行安全要求。

---

## P4.8：安全控制台与跨 Core 集成

完成：

```text
风险账号
安全事件
Billing Step-up
Storage Step-up
AI Gateway 安全信号
Gateway 限流
Workflow 安全自动化
```

---

# 五十九、P4 验收标准

## 认证器

```text
用户可以配置 TOTP
用户可以配置 Passkey
用户可以配置硬件安全密钥
用户可以生成恢复码
认证器可以安全撤销
数据库不保存明文恢复码
```

## 登录

```text
支持密码 + TOTP
支持 Passkey 登录
支持新设备挑战
支持高风险 Step-up
支持攻击限流
支持风险登录阻止
```

## 会话

```text
Session 记录认证强度
敏感操作可以要求再认证
账号安全版本变化后旧 Session 失效
用户可以查看和撤销设备与会话
```

## 恢复

```text
用户可以使用恢复码恢复
高风险恢复具有冷静期
恢复完成后旧 Session 和 Token 失效
恢复过程具有完整通知和审计
```

## 组织

```text
组织可以要求成员启用 MFA
组织可以要求抗钓鱼认证
策略支持宽限期
管理员可以查看成员合规状态
豁免具有原因和过期时间
```

## 平台管理

```text
平台管理员必须使用强认证
安全控制台可以发现风险账号
管理员可以紧急撤销账号会话
管理员不能查看认证器秘密
安全处置全部审计
```

---

# 六十、P4 最重要的注意点

## 1. 不要把邮箱验证码算作强 MFA

邮箱通常依赖已有邮箱 Session，本身可能与当前账号共享风险。

它适合：

```text
通知
验证邮箱所有权
有限恢复流程
```

不应作为企业高安全认证的主要第二因子。

---

## 2. 不要把 TOTP 当成终点

TOTP 能显著改善密码单因子风险，但仍可能被实时钓鱼。

长期应优先：

```text
Passkey
WebAuthn
硬件安全密钥
```

---

## 3. 不要让“受信设备”永久跳过安全验证

受信设备必须：

```text
过期
可撤销
受安全策略影响
账号风险变化后失效
```

---

## 4. 不要只靠账号锁定防攻击

简单锁定容易被攻击者用于拒绝服务。

必须组合：

```text
限流
渐进延迟
MFA
风险评估
设备识别
异常检测
```

---

## 5. 不要让风险引擎直接修改权限

风险引擎输出：

```text
允许
要求 Step-up
阻止
```

它不能静默删除用户角色或改变组织权限。

---

## 6. 不要让恢复流程成为最弱后门

认证器越强，恢复流程越需要分级、延迟、通知和审计。

---

## 7. 不要自行实现 WebAuthn 密码学

Identity 负责：

```text
策略
生命周期
数据模型
交互
审计
```

协议验证和编码细节应使用成熟、持续维护的实现。

---

# 六十一、P4 最终成果

P4 完成后：

```text
core-identity-backend

成为认证器、认证强度、登录风险、
会话安全、账号恢复与安全策略的事实中心。
```

```text
core-identity-web

成为用户配置 Passkey、MFA、恢复方式、
设备、会话和安全活动的统一安全中心。
```

```text
core-identity-admin-backend

成为风险账号、安全事件、账号恢复和
紧急安全处置的管理编排层。
```

```text
core-identity-admin-web

成为平台风险登录、认证器覆盖率、
安全事件与账号处置的安全运营控制台。
```

P4 最终形成的完整安全链路是：

```text
识别主体
→ 评估风险
→ 选择认证器
→ 建立认证强度
→ 创建安全会话
→ 敏感操作再认证
→ 持续检测异常
→ 快速撤销和恢复
```

到这一阶段，Core Identity 才从“可以签发身份”进一步升级为：

> 能够判断这份身份在当前时刻是否仍然值得信任。
