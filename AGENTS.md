# 称呼

每次和我沟通，叫我【帅哥】

# 统一约束

## 首先要确保真实理解

请先不要修改代码。

请检查你对这个任务的理解是否完整:

我真正想解决的问题是什么?

哪些需求是明确的?

哪些地方可能有歧义?

如果直接开写，最可能误解哪里?

最后给出你的执行计划。

## 拓展性

Java 各种类型实现时尽量考虑拓展性，采用策略模式。

## 风格一致

尽量和以前的代码风格一致

## 最少改动

最少改动原则，尽可能不要破坏以前的实现。

如果需要破坏，需要和用户确认。

## 实现建议

深刻理解原始项目+用户目的之后，给出 plan 才实行

## 测试回归

保证修改的功能的正确性。添加独立对应的的 junit5 单元测试。断言方式

确保功能各个常见场景正确（至少包含正反例、边界测试）、验证通过后方可验收。

## java parser 扫描流程

如果你需要关心 java 代码的扫描流程，可以参考 SCAN-FLOW.md 文件中的介绍。




# Unknowns Management

## 强制触发规则 (Hard Trigger)

**当用户提出以下任一类型的任务时，你必须立即调用 `unknowns-discovery` Skill（通过 Skill 工具），然后才能开始实现：**

- 新功能开发 / 新模块创建
- 架构设计 / 数据模型设计
- 数据库表设计或变更
- 认证、授权、安全相关代码
- 跨模块 / 跨系统改动
- 多文件、大范围改动
- 不可逆操作（如数据库迁移、删除数据）
- 用户需求中有主观描述词（"简单""好看""智能""自然"）

**这是硬性要求，不是建议。调用方式：`Skill("unknowns-discovery", "standard")`**

**只有以下情况可以跳过：**
- 单行修复（typo、注释修正）
- 单文件简单 bug fix（已有明确根因）
- 纯代码解释类问题

---

Do not treat the initial request, specification, or implementation plan as a complete description of reality.

Before implementing any non-trivial change, identify the important unknowns that could alter the architecture, data model, user-visible behavior, security, compatibility, or scope of the work.

Distinguish between:

* **Known knowns**: facts confirmed by the user, codebase, tests, or documentation.
* **Known unknowns**: unresolved decisions or missing information already visible.
* **Unknown knowns**: implicit product, design, or domain expectations that have not yet been made explicit.
* **Unknown unknowns**: overlooked constraints, dependencies, edge cases, failure modes, or alternative problem definitions.

Follow these rules:

1. Do not silently convert uncertainty into an assumption.
2. Verify codebase facts by inspecting the relevant code, tests, schema, history, and adjacent modules.
3. Prioritize unknowns that are high-impact, difficult to reverse, or expensive to discover later.
4. For reversible local decisions, choose the most conservative option and record the assumption.
5. For irreversible or cross-system decisions, surface the issue before committing to an implementation direction.
6. During implementation, record material discoveries, deviations, assumptions, and unresolved risks.
7. After implementation, explain what changed, what remains uncertain, and what evidence verifies the result.
8. Convert recurring discoveries into tests, documentation, conventions, or reusable project knowledge.

For substantial features, architecture changes, ambiguous product work, migrations, or cross-module changes, run the **Unknowns Discovery** skill before implementation.
