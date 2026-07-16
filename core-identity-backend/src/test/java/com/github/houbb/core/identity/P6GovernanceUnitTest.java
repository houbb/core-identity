package com.github.houbb.core.identity;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P6 Governance Tests")
class P6GovernanceUnitTest {

    // ==================== Stubs ====================

    private StubAccessPackageRepository packageRepo;
    private StubAccessPackageEntitlementRepository pkgEntRepo;
    private StubAccessRequestRepository requestRepo;
    private StubApprovalInstanceRepository approvalInstRepo;
    private StubApprovalStepRepository approvalStepRepo;
    private StubApprovalDecisionRepository approvalDecRepo;
    private StubPrivilegedActivationRepository privActRepo;
    private StubSodPolicyRepository sodPolicyRepo;
    private StubSodDataRepository sodDataRepo;
    private StubAccessReviewDataRepository reviewDataRepo;
    private StubComplianceDataRepository complianceDataRepo;
    private StubPrivacyDataRepository privacyDataRepo;
    private StubPlatformOperatorRoleRepository operatorRoleRepo;

    // Services
    private AccessPackageService accessPackageService;
    private AccessRequestService accessRequestService;
    private ApprovalService approvalService;
    private PrivilegedAccessService privilegedAccessService;
    private SodService sodService;
    private AccessReviewService accessReviewService;
    private AdminRoleService adminRoleService;
    private ComplianceService complianceService;
    private PrivacyService privacyService;

    @BeforeEach
    void setUp() {
        packageRepo = new StubAccessPackageRepository();
        pkgEntRepo = new StubAccessPackageEntitlementRepository();
        requestRepo = new StubAccessRequestRepository();
        approvalInstRepo = new StubApprovalInstanceRepository();
        approvalStepRepo = new StubApprovalStepRepository();
        approvalDecRepo = new StubApprovalDecisionRepository();
        privActRepo = new StubPrivilegedActivationRepository();
        sodPolicyRepo = new StubSodPolicyRepository();
        sodDataRepo = new StubSodDataRepository();
        reviewDataRepo = new StubAccessReviewDataRepository();
        complianceDataRepo = new StubComplianceDataRepository();
        privacyDataRepo = new StubPrivacyDataRepository();
        operatorRoleRepo = new StubPlatformOperatorRoleRepository();

        accessPackageService = new AccessPackageServiceImpl(packageRepo, pkgEntRepo);
        accessRequestService = new AccessRequestServiceImpl(requestRepo, packageRepo);
        approvalService = new ApprovalService(approvalInstRepo, approvalStepRepo, approvalDecRepo);
        privilegedAccessService = new PrivilegedAccessServiceImpl(privActRepo);
        sodService = new SodService(sodPolicyRepo, sodDataRepo);
        accessReviewService = new AccessReviewService(reviewDataRepo);
        adminRoleService = new AdminRoleService(operatorRoleRepo);
        complianceService = new ComplianceService(complianceDataRepo);
        privacyService = new PrivacyService(privacyDataRepo);
    }

    // ==================== P6.1: Access Package ====================

    @Nested
    @DisplayName("AccessPackage Tests")
    class AccessPackageTests {

        @Test
        @DisplayName("创建访问套餐")
        void shouldCreateAccessPackage() {
            AccessPackage pkg = accessPackageService.createPackage(
                    "org-1", "财务查看者", "只读财务数据访问", "STANDARD", "LOW", "user-1",
                    0, 0, "AUTH_LEVEL_1", List.of("ent-1", "ent-2"));

            assertNotNull(pkg.getId());
            assertEquals("财务查看者", pkg.getName());
            assertEquals("org-1", pkg.getOrganizationId());
            assertEquals("ACTIVE", pkg.getStatus());
            assertEquals(1, pkg.getRequestable());
            assertEquals(2, pkgEntRepo.store.size());
        }

        @Test
        @DisplayName("创建套餐后禁止空名称")
        void shouldRejectEmptyName() {
            assertThrows(AccessPackageServiceImpl.ServiceException.class, () ->
                    accessPackageService.createPackage("org-1", "", "", "STANDARD", "LOW", null, 0, 0, null, null));
        }

        @Test
        @DisplayName("查询组织下的所有套餐")
        void shouldListPackagesByOrganization() {
            accessPackageService.createPackage("org-1", "Pkg A", "", "STANDARD", "LOW", null, 0, 0, null, null);
            accessPackageService.createPackage("org-1", "Pkg B", "", "STANDARD", "LOW", null, 0, 0, null, null);

            List<AccessPackage> list = accessPackageService.listByOrganization("org-1");
            assertEquals(2, list.size());
        }

        @Test
        @DisplayName("更新套餐权益")
        void shouldSetEntitlements() {
            AccessPackage pkg = accessPackageService.createPackage("org-1", "Test", "", "STANDARD", "LOW", null, 0, 0, null, null);
            accessPackageService.setEntitlements(pkg.getId(), List.of("ent-a", "ent-b", "ent-c"));
            assertEquals(3, accessPackageService.getEntitlementIds(pkg.getId()).size());
        }

        @Test
        @DisplayName("删除套餐")
        void shouldDeletePackage() {
            AccessPackage pkg = accessPackageService.createPackage("org-1", "ToDelete", "", "STANDARD", "LOW", null, 0, 0, null, null);
            accessPackageService.deletePackage(pkg.getId());
            assertFalse(packageRepo.findById(pkg.getId()).isPresent());
        }
    }

    // ==================== P6.2: Access Request ====================

    @Nested
    @DisplayName("AccessRequest Tests")
    class AccessRequestTests {

        @Test
        @DisplayName("提交访问申请")
        void shouldSubmitAccessRequest() {
            // 先创建套餐
            accessPackageService.createPackage("org-1", "生产排障", "", "PRIVILEGED", "HIGH", "user-2",
                    0, 14400, "AUTH_LEVEL_3", List.of());

            AccessRequest req = accessRequestService.submit("user-1", "org-1",
                    packageRepo.store.values().iterator().next().getId(),
                    "月度结算故障排查", "INC-1024", 0, 0);

            assertNotNull(req.getId());
            assertEquals("SUBMITTED", req.getStatus());
            assertEquals("user-1", req.getRequesterUserId());
        }

        @Test
        @DisplayName("取消自己的申请")
        void shouldCancelOwnRequest() {
            accessPackageService.createPackage("org-1", "测试套餐", "", "STANDARD", "LOW", null, 0, 0, null, List.of());
            AccessRequest req = accessRequestService.submit("user-1", "org-1",
                    packageRepo.store.values().iterator().next().getId(), "测试", null, 0, 0);

            accessRequestService.cancel(req.getId(), "user-1");
            AccessRequest updated = accessRequestService.getById(req.getId());
            assertEquals("CANCELLED", updated.getStatus());
        }

        @Test
        @DisplayName("不能取消别人的申请")
        void shouldNotCancelOthersRequest() {
            accessPackageService.createPackage("org-1", "测试套餐", "", "STANDARD", "LOW", null, 0, 0, null, List.of());
            AccessRequest req = accessRequestService.submit("user-1", "org-1",
                    packageRepo.store.values().iterator().next().getId(), "测试", null, 0, 0);

            assertThrows(AccessRequestServiceImpl.ServiceException.class, () ->
                    accessRequestService.cancel(req.getId(), "user-2"));
        }
    }

    // ==================== P6.3: Privileged Access ====================

    @Nested
    @DisplayName("PrivilegedAccess Tests")
    class PrivilegedAccessTests {

        @Test
        @DisplayName("激活特权访问")
        void shouldActivatePrivilegedAccess() {
            PrivilegedActivation act = privilegedAccessService.activate(
                    "user-1", "org-1", "role-admin", "安全事件响应", "INC-2048",
                    "AUTH_LEVEL_3", 7200);

            assertNotNull(act.getId());
            assertEquals("ACTIVE", act.getStatus());
            assertEquals("user-1", act.getUserId());
            assertTrue(act.getExpiresAt() > act.getActivatedAt());
        }

        @Test
        @DisplayName("禁止超长特权激活")
        void shouldRejectOverlongActivation() {
            assertThrows(PrivilegedAccessServiceImpl.ServiceException.class, () ->
                    privilegedAccessService.activate("user-1", "org-1", "role-admin",
                            "理由", null, "AUTH_LEVEL_1", 99999));
        }

        @Test
        @DisplayName("禁止空理由的激活")
        void shouldRejectEmptyReason() {
            assertThrows(PrivilegedAccessServiceImpl.ServiceException.class, () ->
                    privilegedAccessService.activate("user-1", "org-1", "role-admin",
                            "", null, "AUTH_LEVEL_1", 3600));
        }

        @Test
        @DisplayName("提前结束特权")
        void shouldEndPrivilegedAccess() {
            PrivilegedActivation act = privilegedAccessService.activate(
                    "user-1", "org-1", "role-admin", "测试", null, "AUTH_LEVEL_2", 3600);
            privilegedAccessService.end(act.getId(), "user-1");

            PrivilegedActivation ended = privilegedAccessService.getById(act.getId());
            assertEquals("ENDED", ended.getStatus());
        }
    }

    // ==================== P6.4: SoD ====================

    @Nested
    @DisplayName("SoD Tests")
    class SodTests {

        @Test
        @DisplayName("创建 SoD 策略")
        void shouldCreateSodPolicy() {
            SodPolicy policy = sodService.createPolicy("org-1", "财务冲突策略", "DENY", "user-1");
            assertNotNull(policy.getId());
            assertEquals("ACTIVE", policy.getStatus());
        }

        @Test
        @DisplayName("添加策略项")
        void shouldAddPolicyItem() {
            SodPolicy policy = sodService.createPolicy("org-1", "测试策略", "DENY", "user-1");
            sodService.addPolicyItem(policy.getId(), "ent-billing-create", "ent-billing-approve", "HIGH");
            assertEquals(1, sodDataRepo.policyItems.size());
        }
    }

    // ==================== P6.5: Access Review ====================

    @Nested
    @DisplayName("AccessReview Tests")
    class AccessReviewTests {

        @Test
        @DisplayName("创建审查活动")
        void shouldCreateCampaign() {
            Map<String, Object> result = accessReviewService.createCampaign(
                    "org-1", "Q3 访问审查", "USER_ACCESS", null, null, 0, 0, "user-1");
            assertEquals("Q3 访问审查", result.get("name"));
            assertNotNull(result.get("id"));
        }

        @Test
        @DisplayName("启动审查并生成审查项")
        void shouldLaunchCampaign() {
            Map<String, Object> result = accessReviewService.createCampaign(
                    "org-1", "Q3 审查", "USER_ACCESS", null, null, 0, 0, "user-1");
            accessReviewService.launchCampaign((String) result.get("id"));
            assertEquals("ACTIVE", reviewDataRepo.campaigns.get(result.get("id")));
        }
    }

    // ==================== P6.6: Admin Role ====================

    @Nested
    @DisplayName("AdminRole Tests")
    class AdminRoleTests {

        @Test
        @DisplayName("分配管理员角色")
        void shouldAssignAdminRole() {
            adminRoleService.assignRole("operator-1", AdminRoleService.VALID_ROLES.get(0), "super-admin");
            assertTrue(adminRoleService.hasRole("operator-1", AdminRoleService.VALID_ROLES.get(0)));
        }

        @Test
        @DisplayName("拒绝无效角色")
        void shouldRejectInvalidRole() {
            assertThrows(AdminRoleService.ServiceException.class, () ->
                    adminRoleService.assignRole("operator-1", "INVALID_ROLE", "admin"));
        }

        @Test
        @DisplayName("撤销管理员角色")
        void shouldRevokeAdminRole() {
            adminRoleService.assignRole("operator-1", "PLATFORM_AUDIT_ADMIN", "admin");
            adminRoleService.revokeRole("operator-1", "PLATFORM_AUDIT_ADMIN");
            assertFalse(adminRoleService.hasRole("operator-1", "PLATFORM_AUDIT_ADMIN"));
        }
    }

    // ==================== P6.7: Compliance ====================

    @Nested
    @DisplayName("Compliance Tests")
    class ComplianceTests {

        @Test
        @DisplayName("创建合规控制")
        void shouldCreateControl() {
            Map<String, Object> control = complianceService.createControl(
                    "CTRL-IAM-001", "强认证要求", "平台管理员必须使用强认证",
                    "PREVENTIVE", "user-1", "MONTHLY");
            assertEquals("CTRL-IAM-001", control.get("controlCode"));
        }

        @Test
        @DisplayName("创建 Finding")
        void shouldCreateFinding() {
            complianceService.createControl("CTRL-001", "测试", "测试", "DETECTIVE", "user-1", null);
            Map<String, Object> finding = complianceService.createFinding(
                    complianceDataRepo.controls.values().iterator().next().get("id").toString(),
                    "未启用 MFA", "管理员账户缺少 MFA", "HIGH", "user-1", System.currentTimeMillis() + 86400000);
            assertEquals("HIGH", finding.get("severity"));
        }

        @Test
        @DisplayName("记录审计证据")
        void shouldRecordEvidence() {
            complianceService.createControl("CTRL-002", "测试", "测试", "DETECTIVE", "user-1", null);
            Map<String, Object> evidence = complianceService.recordEvidence(
                    complianceDataRepo.controls.values().iterator().next().get("id").toString(),
                    "CONFIGURATION", "core-identity", "ref-001",
                    "/storage/evidence/001.json", "sha256:abc123", "user-1");
            assertEquals("VALID", evidence.get("status"));
        }
    }

    // ==================== P6.8: Privacy ====================

    @Nested
    @DisplayName("Privacy Tests")
    class PrivacyTests {

        @Test
        @DisplayName("提交隐私请求")
        void shouldSubmitPrivacyRequest() {
            Map<String, Object> result = privacyService.submitPrivacyRequest(
                    "user-1", "org-1", "ERASURE", "GDPR");
            assertEquals("SUBMITTED", result.get("status"));
            assertNotNull(result.get("dueAt"));
        }

        @Test
        @DisplayName("创建保留策略")
        void shouldCreateRetentionPolicy() {
            Map<String, Object> result = privacyService.createRetentionPolicy(
                    "org-1", "AUDIT", "ACCOUNT_CLOSURE", 7776000, "DELETE", "CN", 80);
            assertEquals("AUDIT", result.get("dataCategory"));
        }

        @Test
        @DisplayName("创建 Legal Hold")
        void shouldCreateLegalHold() {
            Map<String, Object> result = privacyService.createLegalHold(
                    "org-1", "CASE-2024-001", "诉讼保留",
                    "涉及用户数据诉讼", "user-law", System.currentTimeMillis() + 90L * 86400000);
            assertEquals("ACTIVE", result.get("status"));
        }

        @Test
        @DisplayName("释放 Legal Hold")
        void shouldReleaseLegalHold() {
            Map<String, Object> hold = privacyService.createLegalHold(
                    "org-1", "CASE-002", "临时保留", "调查", "user-1", 0);
            privacyService.releaseLegalHold((String) hold.get("id"), "user-1");
            assertEquals("RELEASED", privacyDataRepo.legalHolds.get(hold.get("id")));
        }

        @Test
        @DisplayName("创建处理活动记录")
        void shouldCreateProcessingActivity() {
            Map<String, Object> result = privacyService.createProcessingActivity(
                    "org-1", "ACT-USER-AUTH", "用户认证", "身份验证和授权", "Core Identity");
            assertEquals("ACT-USER-AUTH", result.get("activityCode"));
        }
    }

    // ==================== Approval Tests ====================

    @Nested
    @DisplayName("Approval Tests")
    class ApprovalTests {

        @Test
        @DisplayName("创建审批流程")
        void shouldCreateApproval() {
            ApprovalInstance instance = approvalService.createApproval("ACCESS_REQUEST", "req-001",
                    List.of(new ApprovalService.ApprovalStepDef("SINGLE", 1, "DIRECT_MANAGER", null)),
                    86400000);
            assertNotNull(instance.getId());
            assertEquals("PENDING", instance.getStatus());
        }

        @Test
        @DisplayName("审批通过")
        void shouldApprove() {
            ApprovalInstance instance = approvalService.createApproval("ACCESS_REQUEST", "req-002",
                    List.of(new ApprovalService.ApprovalStepDef("SINGLE", 1, "DIRECT_MANAGER", null)),
                    86400000);

            ApprovalStep step = approvalStepRepo.stepsByInstance.get(instance.getId()).get(0);
            ApprovalService.ApprovalStatus status = approvalService.decide(step.getId(),
                    "approver-1", "APPROVED", "批准访问");
            assertEquals("APPROVED", status.status());
            assertTrue(status.isFullyApproved());
        }

        @Test
        @DisplayName("审批拒绝")
        void shouldReject() {
            ApprovalInstance instance = approvalService.createApproval("ACCESS_REQUEST", "req-003",
                    List.of(new ApprovalService.ApprovalStepDef("SINGLE", 1, "DIRECT_MANAGER", null)),
                    86400000);

            ApprovalStep step = approvalStepRepo.stepsByInstance.get(instance.getId()).get(0);
            ApprovalService.ApprovalStatus status = approvalService.decide(step.getId(),
                    "approver-1", "REJECTED", "理由不足");
            assertEquals("REJECTED", status.status());
        }
    }

    // ==================== Stub Implementations ====================

    static class StubAccessPackageRepository implements AccessPackageRepository {
        final Map<String, AccessPackage> store = new LinkedHashMap<>();
        @Override public void save(AccessPackage p) { store.put(p.getId(), p); }
        @Override public Optional<AccessPackage> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<AccessPackage> findByOrgAndCode(String o, String c) { return Optional.empty(); }
        @Override public List<AccessPackage> findByOrgId(String o) { return store.values().stream().filter(p -> o.equals(p.getOrganizationId())).toList(); }
        @Override public List<AccessPackage> findByOrgIdAndType(String o, String t) { return List.of(); }
        @Override public List<AccessPackage> findRequestableByOrg(String o) { return List.of(); }
        @Override public void update(AccessPackage p) { store.put(p.getId(), p); }
        @Override public void updateStatus(String id, String s, long n, long v) { store.get(id).setStatus(s); }
        @Override public void deleteById(String id, long v) { store.remove(id); }
    }

    static class StubAccessPackageEntitlementRepository implements AccessPackageEntitlementRepository {
        final List<AccessPackageEntitlement> store = new ArrayList<>();
        @Override public void save(AccessPackageEntitlement m) { store.add(m); }
        @Override public void saveBatch(List<AccessPackageEntitlement> ms) { store.addAll(ms); }
        @Override public List<String> findEntitlementIdsByPackageId(String pid) { return store.stream().filter(m -> pid.equals(m.getPackageId())).map(AccessPackageEntitlement::getEntitlementId).toList(); }
        @Override public List<String> findPackageIdsByEntitlementId(String eid) { return List.of(); }
        @Override public void deleteAllByPackageId(String pid) { store.removeIf(m -> pid.equals(m.getPackageId())); }
        @Override public void deleteByPackageAndEntitlement(String pid, String eid) { store.removeIf(m -> pid.equals(m.getPackageId()) && eid.equals(m.getEntitlementId())); }
    }

    static class StubAccessRequestRepository implements AccessRequestRepository {
        final Map<String, AccessRequest> store = new LinkedHashMap<>();
        @Override public void save(AccessRequest r) { store.put(r.getId(), r); }
        @Override public Optional<AccessRequest> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<AccessRequest> findByRequesterId(String uid) { return store.values().stream().filter(r -> uid.equals(r.getRequesterUserId())).toList(); }
        @Override public List<AccessRequest> findByOrganizationId(String oid) { return List.of(); }
        @Override public List<AccessRequest> findByStatus(String s) { return List.of(); }
        @Override public List<AccessRequest> findByOrgAndStatus(String o, String s) { return List.of(); }
        @Override public List<AccessRequest> findPendingByOrg(String o) { return List.of(); }
        @Override public void update(AccessRequest r) { store.put(r.getId(), r); }
        @Override public void updateStatus(String id, String s, long c, long n, long v) { store.get(id).setStatus(s); }
    }

    static class StubApprovalInstanceRepository implements ApprovalInstanceRepository {
        final Map<String, ApprovalInstance> store = new LinkedHashMap<>();
        @Override public void save(ApprovalInstance i) { store.put(i.getId(), i); }
        @Override public Optional<ApprovalInstance> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<ApprovalInstance> findByRequest(String t, String id) { return Optional.empty(); }
        @Override public void update(ApprovalInstance i) { store.put(i.getId(), i); }
        @Override public void updateStatus(String id, String s, long c, long n, long v) { store.get(id).setStatus(s); store.get(id).setCompletedAt(c); }
    }

    static class StubApprovalStepRepository implements ApprovalStepRepository {
        final Map<String, List<ApprovalStep>> stepsByInstance = new LinkedHashMap<>();
        final Map<String, ApprovalStep> store = new LinkedHashMap<>();
        @Override public void save(ApprovalStep s) {
            store.put(s.getId(), s);
            stepsByInstance.computeIfAbsent(s.getApprovalInstanceId(), k -> new ArrayList<>()).add(s);
        }
        @Override public void saveBatch(List<ApprovalStep> ss) { ss.forEach(this::save); }
        @Override public Optional<ApprovalStep> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<ApprovalStep> findByApprovalInstanceId(String iid) { return stepsByInstance.getOrDefault(iid, List.of()); }
        @Override public List<ApprovalStep> findByApprovalInstanceIdAndStatus(String iid, String s) { return findByApprovalInstanceId(iid).stream().filter(st -> s.equals(st.getStatus())).toList(); }
        @Override public void update(ApprovalStep s) { store.put(s.getId(), s); }
        @Override public void updateStatus(String id, String s, long n) { store.get(id).setStatus(s); }
        @Override public void deleteByApprovalInstanceId(String iid) { stepsByInstance.remove(iid); }
    }

    static class StubApprovalDecisionRepository implements ApprovalDecisionRepository {
        final Map<String, List<ApprovalDecision>> decisionsByStep = new LinkedHashMap<>();
        @Override public void save(ApprovalDecision d) { decisionsByStep.computeIfAbsent(d.getApprovalStepId(), k -> new ArrayList<>()).add(d); }
        @Override public Optional<ApprovalDecision> findById(String id) { return Optional.empty(); }
        @Override public List<ApprovalDecision> findByStepId(String sid) { return decisionsByStep.getOrDefault(sid, List.of()); }
        @Override public List<ApprovalDecision> findByApproverId(String uid) { return List.of(); }
        @Override public int countByStepIdAndDecision(String sid, String d) { return (int) decisionsByStep.getOrDefault(sid, List.of()).stream().filter(x -> d.equals(x.getDecision())).count(); }
    }

    static class StubPrivilegedActivationRepository implements PrivilegedActivationRepository {
        final Map<String, PrivilegedActivation> store = new LinkedHashMap<>();
        @Override public void save(PrivilegedActivation a) { store.put(a.getId(), a); }
        @Override public Optional<PrivilegedActivation> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<PrivilegedActivation> findByUserId(String uid) { return store.values().stream().filter(a -> uid.equals(a.getUserId())).toList(); }
        @Override public List<PrivilegedActivation> findActiveByUserId(String uid) { return store.values().stream().filter(a -> uid.equals(a.getUserId()) && "ACTIVE".equals(a.getStatus())).toList(); }
        @Override public Optional<PrivilegedActivation> findActiveByUserIdAndRole(String uid, String rid) { return store.values().stream().filter(a -> uid.equals(a.getUserId()) && rid.equals(a.getRoleId()) && "ACTIVE".equals(a.getStatus())).findFirst(); }
        @Override public List<PrivilegedActivation> findExpiringActivations(long t) { return List.of(); }
        @Override public void update(PrivilegedActivation a) { store.put(a.getId(), a); }
        @Override public void end(String id, long e, long n, long v) { store.get(id).setStatus("ENDED"); store.get(id).setEndedAt(e); }
    }

    static class StubSodPolicyRepository implements SodPolicyRepository {
        final Map<String, SodPolicy> store = new LinkedHashMap<>();
        @Override public void save(SodPolicy p) { store.put(p.getId(), p); }
        @Override public Optional<SodPolicy> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<SodPolicy> findByOrgId(String oid) { return store.values().stream().filter(p -> oid.equals(p.getOrganizationId())).toList(); }
        @Override public List<SodPolicy> findActiveByOrgId(String oid) { return findByOrgId(oid).stream().filter(p -> "ACTIVE".equals(p.getStatus())).toList(); }
        @Override public void update(SodPolicy p) { store.put(p.getId(), p); }
        @Override public void updateStatus(String id, String s, long n, long v) { store.get(id).setStatus(s); }
    }

    static class StubSodDataRepository implements SodDataRepository {
        final List<Map<String, Object>> policyItems = new ArrayList<>();
        final List<Map<String, Object>> conflicts = new ArrayList<>();
        @Override public void insertPolicyItem(String id, String pid, String l, String r, String rl) { policyItems.add(Map.of("id", id, "policy_id", pid, "left_entitlement_id", l, "right_entitlement_id", r, "risk_level", rl, "enforcement_mode", "DENY")); }
        @Override public List<Map<String, Object>> findPolicyItemsByStatus(String s) { return policyItems; }
        @Override public void insertConflict(String id, String pid, String sid, String lg, String rg, long d) { conflicts.add(Map.of("id", id, "policy_id", pid, "subject_id", sid)); }
        @Override public List<Map<String, Object>> findOpenConflictsByPolicyAndSubject(String sid, String pid) { return List.of(); }
        @Override public List<Map<String, Object>> findConflictsByOrg(String oid) { return List.of(); }
        @Override public void updateConflictStatus(String id, String s, String r, long n) {}
        @Override public void insertException(String id, String cid, String r, String cc, String ab, long vf, long ex) {}
        @Override public List<Map<String, Object>> findActiveGrantsBySubject(String sid) { return List.of(); }
    }

    static class StubAccessReviewDataRepository implements AccessReviewDataRepository {
        final Map<String, String> campaigns = new LinkedHashMap<>();
        final List<Map<String, Object>> items = new ArrayList<>();
        @Override public void insertCampaign(String id, String oid, String n, String ct, String sj, String rj, long sa, long d, String cb, long now) { campaigns.put(id, "DRAFT"); }
        @Override public void updateCampaignStatus(String id, String s, long n) { campaigns.put(id, s); }
        @Override public List<Map<String, Object>> findCampaignsByOrg(String oid) { return List.of(); }
        @Override public Map<String, Object> findCampaignById(String id) { return Map.of("id", id, "organization_id", "org-1"); }
        @Override public void insertItem(String id, String cid, String st, String si, String eid, String gid, String rl, long n) { items.add(Map.of("id", id, "grant_id", gid, "campaign_id", cid)); }
        @Override public void updateItemStatus(String id, String s, long n) {}
        @Override public List<Map<String, Object>> findItemsByCampaign(String cid) { return items; }
        @Override public List<Map<String, Object>> findItemsByCampaignWithDecisions(String cid) { return items; }
        @Override public void insertDecision(String id, String ri, String rui, String d, String r, Long ne, long n) {}
        @Override public List<Map<String, Object>> findActiveGrantsByOrg(String oid) { return List.of(); }
        @Override public void revokeGrant(String gid, String rb, long ra, String r, long n) {}
    }

    static class StubComplianceDataRepository implements ComplianceDataRepository {
        final Map<String, Map<String, Object>> controls = new LinkedHashMap<>();
        final List<Map<String, Object>> findings = new ArrayList<>();
        @Override public void insertControl(String id, String cc, String n, String d, String ct, String ou, String f, long now) { controls.put(id, Map.of("id", id, "control_code", cc)); }
        @Override public List<Map<String, Object>> findAllControls() { return List.of(); }
        @Override public void updateControlStatus(String id, String s, long n) {}
        @Override public void insertFramework(String id, String fc, String n, String v, String p, long now) {}
        @Override public void upsertControlMapping(String cid, String fid, String rc) {}
        @Override public void insertFinding(String id, String cid, String t, String d, String s, String ou, long da, long n) { findings.add(Map.of("id", id, "severity", s)); }
        @Override public List<Map<String, Object>> findOpenFindingsWithControls() { return List.of(); }
        @Override public void updateFindingResolved(String id, long n) {}
        @Override public void insertEvidence(String id, String cid, String et, String ss, String sr, String cl, String cs, long ca, String cb) {}
        @Override public List<Map<String, Object>> findEvidenceByControl(String cid) { return List.of(); }
        @Override public void insertAssessment(String id, String cid, String ab, long ad, String r, String fs) {}
        @Override public List<Map<String, Object>> findAssessmentsByControl(String cid) { return List.of(); }
    }

    static class StubPrivacyDataRepository implements PrivacyDataRepository {
        final Map<String, String> legalHolds = new LinkedHashMap<>();
        @Override public void insertRequest(String id, String uid, String oid, String rt, String j, long sa, long da, long n) {}
        @Override public void updateRequestStatus(String id, String s, String vl, long n) {}
        @Override public void updateRequestCompleted(String id, String s, long n) {}
        @Override public void updateRequestRejected(String id, String r, long n) {}
        @Override public List<Map<String, Object>> findRequestsByStatus(String s) { return List.of(); }
        @Override public Map<String, Object> findRequestById(String id) { return Map.of(); }
        @Override public List<Map<String, Object>> findTasksByRequestId(String rid) { return List.of(); }
        @Override public void insertTask(String id, String pid, String ts, String tt, long n) {}
        @Override public void insertRetentionPolicy(String id, String oid, String dc, String tt, long rs, String ea, String j, int p, long n) {}
        @Override public List<Map<String, Object>> findRetentionPoliciesByOrg(String oid) { return List.of(); }
        @Override public void insertLegalHold(String id, String oid, String cr, String n, String r, long ea, long ra, String cb, long now) { legalHolds.put(id, "ACTIVE"); }
        @Override public void insertLegalHoldScope(String id, String lhid, String st, String sr, String dc) {}
        @Override public void releaseLegalHold(String id, long n) { legalHolds.put(id, "RELEASED"); }
        @Override public List<Map<String, Object>> findLegalHoldsByOrg(String oid) { return List.of(); }
        @Override public int countActiveLegalHoldByScope(String sr, String dc) { return 0; }
        @Override public void insertProcessingActivity(String id, String oid, String ac, String n, String p, String c, long now) {}
        @Override public List<Map<String, Object>> findProcessingActivitiesByOrg(String oid) { return List.of(); }
    }

    static class StubPlatformOperatorRoleRepository implements PlatformOperatorRoleRepository {
        final Map<String, Set<String>> rolesByOperator = new LinkedHashMap<>();
        @Override public void save(String id, String oid, String rc, String gb, long ga, long ca) { rolesByOperator.computeIfAbsent(oid, k -> new LinkedHashSet<>()).add(rc); }
        @Override public void delete(String oid, String rc) { rolesByOperator.getOrDefault(oid, Set.of()).remove(rc); }
        @Override public List<String> findRoleCodesByOperatorId(String oid) { return List.copyOf(rolesByOperator.getOrDefault(oid, Set.of())); }
        @Override public int countByOperatorAndRole(String oid, String rc) { return rolesByOperator.getOrDefault(oid, Set.of()).contains(rc) ? 1 : 0; }
    }
}
