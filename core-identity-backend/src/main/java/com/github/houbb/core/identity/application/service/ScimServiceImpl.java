package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * SCIM 2.0 Service implementation — Core Identity as SCIM Service Provider.
 *
 * P5 CRITICAL RULES:
 * - SCIM deactivation suspends Membership + ExternalIdentity, does NOT delete global User
 * - SCIM-deactivated members cannot be re-activated by JIT
 * - Group-role mapping never grants OWNER/SUPER_ADMIN
 */
public class ScimServiceImpl implements ScimService {

    private static final Logger log = LoggerFactory.getLogger(ScimServiceImpl.class);

    private final ScimResourceRepository resourceRepo;
    private final ScimGroupRepository groupRepo;
    private final ScimGroupMemberRepository memberRepo;
    private final ScimGroupRoleMappingRepository mappingRepo;
    private final ExternalIdentityRepository extIdRepo;
    private final MembershipRepository membershipRepo;
    private final MembershipRoleRepository membershipRoleRepo;
    private final OrganizationRepository orgRepo;
    private final RoleRepository roleRepo;
    private final SessionRepository sessionRepo;
    private final ProvisioningJobRepository jobRepo;
    private final ProvisioningLogRepository logRepo;

    public ScimServiceImpl(ScimResourceRepository resourceRepo, ScimGroupRepository groupRepo,
                           ScimGroupMemberRepository memberRepo, ScimGroupRoleMappingRepository mappingRepo,
                           ExternalIdentityRepository extIdRepo, MembershipRepository membershipRepo,
                           MembershipRoleRepository membershipRoleRepo, OrganizationRepository orgRepo,
                           RoleRepository roleRepo, SessionRepository sessionRepo,
                           ProvisioningJobRepository jobRepo, ProvisioningLogRepository logRepo) {
        this.resourceRepo = resourceRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.mappingRepo = mappingRepo;
        this.extIdRepo = extIdRepo;
        this.membershipRepo = membershipRepo;
        this.membershipRoleRepo = membershipRoleRepo;
        this.orgRepo = orgRepo;
        this.roleRepo = roleRepo;
        this.sessionRepo = sessionRepo;
        this.jobRepo = jobRepo;
        this.logRepo = logRepo;
    }

    @Override
    @Transactional
    public String createScimUser(String connectionId, String externalId, String userName,
                                  String displayName, String email, boolean active, long now) {
        // Check duplicate
        resourceRepo.findByConnectionIdAndResourceTypeAndExternalId(connectionId, "User", externalId)
                .ifPresent(existing -> { throw new ServiceException("IDENTITY_SCIM_RESOURCE_CONFLICT",
                        "SCIM user already exists: " + externalId); });

        // Create SCIM resource mapping (user creation handled by ExternalIdentityService/JIT)
        String resourceId = UUID.randomUUID().toString();
        ScimResource resource = new ScimResource();
        resource.setId(resourceId);
        resource.setConnectionId(connectionId);
        resource.setResourceType("User");
        resource.setExternalId(externalId);
        resource.setUserName(userName);
        resource.setActive(active ? 1 : 0);
        resource.setResourceVersion(1);
        resource.setLastSyncedAt(now);
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);
        resource.setVersion(1);
        resourceRepo.save(resource);

        writeProvisioningLog(null, "User", externalId, "CREATE", "SUCCESS", null, null, now);

        log.info("SCIM user created: extId={}, userName={}", externalId, userName);
        return resourceId;
    }

    @Override
    @Transactional
    public void updateScimUser(String connectionId, String scimUserId, String displayName,
                                String email, boolean active, long now) {
        ScimResource resource = resourceRepo.findById(scimUserId)
                .orElseThrow(() -> new ServiceException("IDENTITY_SCIM_RESOURCE_NOT_FOUND",
                        "SCIM user not found: " + scimUserId));

        resource.setActive(active ? 1 : 0);
        resource.setResourceVersion(resource.getResourceVersion() + 1);
        resource.setLastSyncedAt(now);
        resource.setUpdatedAt(now);
        resourceRepo.update(resource);

        // Update local membership if mapped
        if (resource.getLocalResourceId() != null) {
            membershipRepo.findByOrgAndUser(
                    orgRepo.findById(extIdRepo.findById(resource.getLocalResourceId())
                            .map(ExternalIdentity::getOrganizationId).orElse("")).orElse(null).getId(),
                    resource.getLocalResourceId()
            ).ifPresent(m -> {
                if (!active) {
                    m.setStatus("SUSPENDED");
                    m.setDeprovisionedAt(now);
                    m.setUpdatedAt(now);
                    membershipRepo.update(m);
                }
            });
        }

        writeProvisioningLog(null, "User", resource.getExternalId(), "UPDATE", "SUCCESS", null, null, now);
    }

    @Override
    @Transactional
    public void deactivateScimUser(String connectionId, String scimUserId, long now) {
        ScimResource resource = resourceRepo.findById(scimUserId)
                .orElseThrow(() -> new ServiceException("IDENTITY_SCIM_RESOURCE_NOT_FOUND",
                        "SCIM user not found"));

        // Suspend membership (not delete user!)
        if (resource.getLocalResourceId() != null) {
            resourceRepo.findByConnectionIdAndResourceTypeAndLocalResourceId(connectionId, "User", resource.getLocalResourceId())
                    .ifPresent(sr -> {
                        // Find and suspend external identity
                        extIdRepo.findByConnectionIdAndExternalSubject(connectionId, sr.getExternalId())
                                .ifPresent(ei -> {
                                    extIdRepo.updateStatus(ei.getId(), "SUSPENDED", now, ei.getVersion());
                                    // Revoke sessions
                                    List.of(sessionRepo.findByUserIdAndStatus(ei.getUserId(), "ACTIVE"))
                                            .forEach(sessions -> sessions.forEach(s ->
                                                    sessionRepo.revokeByUserId(ei.getUserId(), "SCIM_DEPROVISION", now)));
                                });
                    });
        }

        resource.setActive(0);
        resource.setResourceVersion(resource.getResourceVersion() + 1);
        resource.setLastSyncedAt(now);
        resource.setUpdatedAt(now);
        resourceRepo.update(resource);

        writeProvisioningLog(null, "User", resource.getExternalId(), "DEACTIVATE", "SUCCESS", null, null, now);
        log.info("SCIM user deactivated: {}", resource.getExternalId());
    }

    @Override
    @Transactional
    public String createScimGroup(String connectionId, String externalId, String displayName, long now) {
        groupRepo.findByConnectionIdAndExternalId(connectionId, externalId).ifPresent(existing -> {
            throw new ServiceException("IDENTITY_SCIM_RESOURCE_CONFLICT", "SCIM group already exists: " + externalId);
        });

        String groupId = UUID.randomUUID().toString();
        ScimGroup group = new ScimGroup();
        group.setId(groupId);
        group.setConnectionId(connectionId);
        group.setExternalId(externalId);
        group.setDisplayName(displayName);
        group.setStatus("ACTIVE");
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        group.setVersion(1);
        groupRepo.save(group);

        writeProvisioningLog(null, "Group", externalId, "CREATE", "SUCCESS", null, null, now);
        return groupId;
    }

    @Override
    @Transactional
    public void updateScimGroup(String connectionId, String scimGroupId, String displayName, long now) {
        ScimGroup group = groupRepo.findById(scimGroupId)
                .orElseThrow(() -> new ServiceException("IDENTITY_SCIM_RESOURCE_NOT_FOUND", "SCIM group not found"));
        group.setDisplayName(displayName);
        group.setUpdatedAt(now);
        groupRepo.update(group);
    }

    @Override
    @Transactional
    public void deleteScimGroup(String connectionId, String scimGroupId, long now) {
        ScimGroup group = groupRepo.findById(scimGroupId)
                .orElseThrow(() -> new ServiceException("IDENTITY_SCIM_RESOURCE_NOT_FOUND", "SCIM group not found"));
        groupRepo.updateStatus(scimGroupId, "DELETED", now, group.getVersion());
        memberRepo.deleteByGroupId(scimGroupId);
        writeProvisioningLog(null, "Group", group.getExternalId(), "DELETE", "SUCCESS", null, null, now);
    }

    @Override
    @Transactional
    public void addScimGroupMember(String connectionId, String scimGroupId, String externalIdentityId, long now) {
        ScimGroupMember member = new ScimGroupMember();
        member.setGroupId(scimGroupId);
        member.setExternalIdentityId(externalIdentityId);
        member.setCreatedAt(now);
        memberRepo.save(member);

        // Apply group-role mappings
        applyGroupRoleMappings(connectionId, scimGroupId, now);
    }

    @Override
    @Transactional
    public void removeScimGroupMember(String connectionId, String scimGroupId, String externalIdentityId, long now) {
        memberRepo.deleteByGroupIdAndExternalIdentityId(scimGroupId, externalIdentityId);
        // Re-apply group-role mappings (removal may revoke roles)
        applyGroupRoleMappings(connectionId, scimGroupId, now);
    }

    @Override
    @Transactional
    public void createGroupRoleMapping(String connectionId, String scimGroupId, String roleId,
                                        String mappingMode, String createdBy, long now) {
        // Protect critical roles
        roleRepo.findById(roleId).ifPresent(role -> {
            if ("OWNER".equals(role.getRoleKey()) || "SUPER_ADMIN".equals(role.getRoleKey())) {
                throw new ServiceException("IDENTITY_SCIM_PROVISIONING_FAILED",
                        "Cannot map external group to protected role: " + role.getRoleKey());
            }
        });

        ScimGroupRoleMapping mapping = new ScimGroupRoleMapping();
        mapping.setId(UUID.randomUUID().toString());
        mapping.setGroupId(scimGroupId);
        mapping.setRoleId(roleId);
        mapping.setMappingMode(mappingMode);
        mapping.setStatus("ACTIVE");
        mapping.setCreatedBy(createdBy);
        mapping.setCreatedAt(now);
        mapping.setUpdatedAt(now);
        mapping.setVersion(1);
        mappingRepo.save(mapping);

        log.info("SCIM group-role mapping created: group={}, role={}, mode={}", scimGroupId, roleId, mappingMode);
    }

    @Override
    @Transactional
    public void applyGroupRoleMappings(String connectionId, String scimGroupId, long now) {
        List<ScimGroupRoleMapping> mappings = mappingRepo.findByGroupId(scimGroupId);
        List<ScimGroupMember> members = memberRepo.findByGroupId(scimGroupId);

        for (ScimGroupMember member : members) {
            extIdRepo.findById(member.getExternalIdentityId()).ifPresent(ei -> {
                for (ScimGroupRoleMapping mapping : mappings) {
                    if (!"ACTIVE".equals(mapping.getStatus())) continue;

                    if ("ADD_ONLY".equals(mapping.getMappingMode())) {
                        // Add role if not already present
                        membershipRepo.findByOrgAndUser(ei.getOrganizationId(), ei.getUserId())
                                .ifPresent(m -> {
                                    membershipRoleRepo.findByMembershipId(m.getId()).stream()
                                            .filter(mr -> mr.getRoleId().equals(mapping.getRoleId()))
                                            .findAny()
                                            .orElseGet(() -> {
                                                MembershipRole mr = new MembershipRole();
                                                mr.setMembershipId(m.getId());
                                                mr.setRoleId(mapping.getRoleId());
                                                membershipRoleRepo.save(mr);
                                                return mr;
                                            });
                                });
                    }
                    // AUTHORITATIVE mode handled separately
                }
            });
        }
    }

    private void writeProvisioningLog(String jobId, String resourceType, String externalId,
                                       String operation, String result, String errorCode,
                                       String errorMessage, long now) {
        try {
            ProvisioningLog l = new ProvisioningLog();
            l.setId(UUID.randomUUID().toString());
            l.setJobId(jobId);
            l.setResourceType(resourceType);
            l.setExternalId(externalId);
            l.setOperation(operation);
            l.setResult(result);
            l.setErrorCode(errorCode);
            l.setErrorMessage(errorMessage);
            l.setOccurredAt(now);
            l.setCreatedAt(now);
            logRepo.save(l);
        } catch (Exception e) {
            log.warn("Failed to write provisioning log: {}", e.getMessage());
        }
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;
        public ServiceException(String errorCode, String message) { super(message); this.errorCode = errorCode; }
        public String getErrorCode() { return errorCode; }
    }
}