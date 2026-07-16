package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.ApprovalDecision;
import com.github.houbb.core.identity.application.domain.ApprovalInstance;
import com.github.houbb.core.identity.application.domain.ApprovalStep;
import com.github.houbb.core.identity.application.port.ApprovalDecisionRepository;
import com.github.houbb.core.identity.application.port.ApprovalInstanceRepository;
import com.github.houbb.core.identity.application.port.ApprovalStepRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Approval 管理服务 — 创建审批流程、处理审批决定。
 */
public class ApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalService.class);

    private final ApprovalInstanceRepository instanceRepo;
    private final ApprovalStepRepository stepRepo;
    private final ApprovalDecisionRepository decisionRepo;

    public ApprovalService(ApprovalInstanceRepository instanceRepo,
                           ApprovalStepRepository stepRepo,
                           ApprovalDecisionRepository decisionRepo) {
        this.instanceRepo = instanceRepo;
        this.stepRepo = stepRepo;
        this.decisionRepo = decisionRepo;
    }

    /**
     * 创建审批流程。
     *
     * @param requestType 请求类型（ACCESS_REQUEST / PRIVILEGED_ACTIVATION）
     * @param requestId   请求 ID
     * @param steps       审批步骤定义（每个步骤包含模式、审批人类型、审批人引用）
     * @param dueDurationMs 每步的审批超时（毫秒，从创建时算起）
     */
    @Transactional
    public ApprovalInstance createApproval(String requestType, String requestId,
                                           List<ApprovalStepDef> steps, long dueDurationMs) {
        long now = System.currentTimeMillis();

        ApprovalInstance instance = new ApprovalInstance();
        instance.setId(UUID.randomUUID().toString());
        instance.setRequestType(requestType);
        instance.setRequestId(requestId);
        instance.setStatus("PENDING");
        instance.setCurrentStep(0);
        instance.setCreatedAt(now);
        instance.setUpdatedAt(now);
        instance.setVersion(1);
        instanceRepo.save(instance);

        for (int i = 0; i < steps.size(); i++) {
            ApprovalStepDef def = steps.get(i);
            ApprovalStep step = new ApprovalStep();
            step.setId(UUID.randomUUID().toString());
            step.setApprovalInstanceId(instance.getId());
            step.setStepOrder(i);
            step.setApprovalMode(def.mode() != null ? def.mode() : "SINGLE");
            step.setRequiredApprovals(def.requiredApprovals() > 0 ? def.requiredApprovals() : 1);
            step.setApproverType(def.approverType() != null ? def.approverType() : "DIRECT_MANAGER");
            step.setApproverReference(def.approverReference());
            step.setStatus("PENDING");
            step.setDueAt(now + dueDurationMs);
            step.setCreatedAt(now);
            stepRepo.save(step);
        }

        log.info("Created approval instance: id={}, requestType={}, requestId={}, steps={}",
                instance.getId(), requestType, requestId, steps.size());
        return instance;
    }

    /**
     * 审批人做出决定。
     *
     * @return 返回当前审批实例的最新状态
     */
    @Transactional
    public ApprovalStatus decide(String approvalStepId, String approverUserId,
                                  String decision, String reason) {
        long now = System.currentTimeMillis();

        // 验证步骤存在且处于 PENDING
        ApprovalStep step = stepRepo.findById(approvalStepId)
                .orElseThrow(() -> new ServiceException("IDENTITY_STEP_NOT_FOUND",
                        "审批步骤 " + approvalStepId + " 不存在"));
        if (!"PENDING".equals(step.getStatus())) {
            throw new ServiceException("IDENTITY_STEP_ALREADY_DECIDED",
                    "该审批步骤已处理，当前状态: " + step.getStatus());
        }

        // 防止自己批准自己（由调用方在决定前检查）
        // 记录决定
        ApprovalDecision dec = new ApprovalDecision();
        dec.setId(UUID.randomUUID().toString());
        dec.setApprovalStepId(approvalStepId);
        dec.setApproverUserId(approverUserId);
        dec.setDecision(decision);
        dec.setReason(reason);
        dec.setDecidedAt(now);
        dec.setRequestId(String.valueOf(UUID.randomUUID()));
        decisionRepo.save(dec);

        // 检查该步骤是否满足通过条件
        ApprovalInstance instance = instanceRepo.findById(step.getApprovalInstanceId())
                .orElseThrow(() -> new ServiceException("IDENTITY_INSTANCE_NOT_FOUND",
                        "审批实例不存在"));

        boolean stepApproved = evaluateStep(step, decision);
        String instanceStatus = null;

        if ("REJECTED".equals(decision) || "RETURNED".equals(decision)) {
            // 一票否决
            stepRepo.updateStatus(approvalStepId, "REJECTED", now);
            instanceRepo.updateStatus(instance.getId(), "REJECTED", now, now, instance.getVersion());
            instanceStatus = "REJECTED";
            log.info("Approval rejected: instance={}, step={}, decision={}", instance.getId(), approvalStepId, decision);
        } else if (stepApproved) {
            // 当前步骤通过，检查下一步
            stepRepo.updateStatus(approvalStepId, "APPROVED", now);
            List<ApprovalStep> allSteps = stepRepo.findByApprovalInstanceId(instance.getId());

            boolean allApproved = allSteps.stream()
                    .allMatch(s -> s.getId().equals(approvalStepId) || "APPROVED".equals(s.getStatus()));

            if (allApproved) {
                // 全部步骤通过
                instanceRepo.updateStatus(instance.getId(), "APPROVED", now, now, instance.getVersion());
                instanceStatus = "APPROVED";
                log.info("Approval fully approved: instance={}", instance.getId());
            } else {
                // 激活下一步
                int nextOrder = step.getStepOrder() + 1;
                instance.setCurrentStep(nextOrder);
                instance.setUpdatedAt(now);
                // 激活 pending 状态的下一步
                List<ApprovalStep> nextSteps = stepRepo.findByApprovalInstanceIdAndStatus(instance.getId(), "PENDING");
                for (ApprovalStep ns : nextSteps) {
                    if (ns.getStepOrder() == nextOrder) {
                        ns.setStatus("ACTIVE");
                        stepRepo.update(ns);
                    }
                }
                instanceRepo.update(instance);
                instanceStatus = "IN_PROGRESS";
            }
        } else {
            instanceStatus = "PENDING";
        }

        return new ApprovalStatus(instance.getId(), instanceStatus,
                step.getStepOrder(), decision, "APPROVED".equals(instanceStatus));
    }

    /**
     * 查询审批实例详情（含步骤和决定）。
     */
    public ApprovalDetail getApprovalDetail(String instanceId) {
        ApprovalInstance instance = instanceRepo.findById(instanceId)
                .orElseThrow(() -> new ServiceException("IDENTITY_INSTANCE_NOT_FOUND",
                        "审批实例 " + instanceId + " 不存在"));

        List<ApprovalStep> steps = stepRepo.findByApprovalInstanceId(instanceId);
        List<ApprovalDetail.StepDetail> stepDetails = new java.util.ArrayList<>();
        for (ApprovalStep step : steps) {
            List<ApprovalDecision> decisions = decisionRepo.findByStepId(step.getId());
            stepDetails.add(new ApprovalDetail.StepDetail(step, decisions));
        }

        return new ApprovalDetail(instance, stepDetails);
    }

    /**
     * 查询用户待审批项。
     */
    public List<ApprovalStep> listPendingApprovals(String approverUserId) {
        // 根据审批人类型/引用来查找待审批步骤
        // 简化实现：直接查所有 PENDING/ACTIVE 状态的步骤
        return List.of(); // 实际实现需要更复杂的查询
    }

    /**
     * 评估当前步骤是否通过。
     */
    private boolean evaluateStep(ApprovalStep step, String latestDecision) {
        int approvedCount = decisionRepo.countByStepIdAndDecision(step.getId(), "APPROVED") + 1; // +1 for current
        return switch (step.getApprovalMode()) {
            case "SINGLE" -> true; // 一人批准即通过
            case "ALL" -> {
                // 需要所有指定审批人批准 - 简化处理
                yield approvedCount >= step.getRequiredApprovals();
            }
            case "ANY_N" -> approvedCount >= step.getRequiredApprovals();
            default -> approvedCount >= step.getRequiredApprovals();
        };
    }

    /**
     * 审批步骤定义。
     */
    public record ApprovalStepDef(String mode, int requiredApprovals,
                                   String approverType, String approverReference) {
    }

    /**
     * 审批状态。
     */
    public record ApprovalStatus(String instanceId, String status, int currentStep,
                                  String latestDecision, boolean isFullyApproved) {
    }

    /**
     * 审批详情（含步骤和决定）。
     */
    public static class ApprovalDetail {
        private final ApprovalInstance instance;
        private final List<StepDetail> steps;

        public ApprovalDetail(ApprovalInstance instance, List<StepDetail> steps) {
            this.instance = instance;
            this.steps = steps;
        }

        public ApprovalInstance getInstance() { return instance; }
        public List<StepDetail> getSteps() { return steps; }

        public static class StepDetail {
            private final ApprovalStep step;
            private final List<ApprovalDecision> decisions;

            public StepDetail(ApprovalStep step, List<ApprovalDecision> decisions) {
                this.step = step;
                this.decisions = decisions;
            }

            public ApprovalStep getStep() { return step; }
            public List<ApprovalDecision> getDecisions() { return decisions; }
        }
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;

        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}
