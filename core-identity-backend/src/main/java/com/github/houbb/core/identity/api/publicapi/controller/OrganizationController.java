package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.Organization;
import com.github.houbb.core.identity.application.domain.Membership;
import com.github.houbb.core.identity.application.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Public API for organization and member management.
 */
@RestController
@RequestMapping("/api/v1/identity")
public class OrganizationController {

    private final OrganizationService orgService;
    private final MembershipService membershipService;
    private final InvitationService invitationService;
    private final AuthorizationService authorizationService;

    public OrganizationController(OrganizationService orgService,
                                   MembershipService membershipService,
                                   InvitationService invitationService,
                                   AuthorizationService authorizationService) {
        this.orgService = orgService;
        this.membershipService = membershipService;
        this.invitationService = invitationService;
        this.authorizationService = authorizationService;
    }

    // === My Organizations ===

    @GetMapping("/me/organizations")
    public ResponseEntity<Map<String, Object>> getMyOrganizations(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) return ResponseEntity.ok(Map.of("organizations", List.of()));
        List<Organization> orgs = orgService.getMyOrganizations(userId);
        List<Map<String, Object>> result = orgs.stream().map(this::toOrgMap).toList();
        return ResponseEntity.ok(Map.of("organizations", result, "total", result.size()));
    }

    @PostMapping("/me/current-organization")
    public ResponseEntity<Map<String, Object>> setCurrentOrganization(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody Map<String, String> body) {
        if (userId == null) return ResponseEntity.ok(Map.of("message", "OK"));
        String orgId = body.get("organizationId");
        orgService.switchOrganization(userId, orgId);
        return ResponseEntity.ok(Map.of("message", "OK", "organizationId", orgId));
    }

    // === Organizations ===

    @PostMapping("/organizations")
    public ResponseEntity<Map<String, Object>> createOrganization(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody Map<String, String> body) {
        if (userId == null) throw new AuthException("IDENTITY_SESSION_INVALID", "Not authenticated");
        String name = body.get("name");
        String description = body.getOrDefault("description", "");
        Organization org = orgService.createTeamOrg(name, description, userId);
        return ResponseEntity.status(201).body(toOrgMap(org));
    }

    @GetMapping("/organizations/{organizationId}")
    public ResponseEntity<Map<String, Object>> getOrganization(
            @PathVariable String organizationId) {
        Organization org = orgService.getOrganization(organizationId);
        return ResponseEntity.ok(toOrgMap(org));
    }

    @PatchMapping("/organizations/{organizationId}")
    public ResponseEntity<Map<String, Object>> updateOrganization(
            @PathVariable String organizationId,
            @RequestBody Map<String, String> body) {
        Organization org = orgService.updateOrganization(organizationId, body.get("name"), body.get("description"));
        return ResponseEntity.ok(toOrgMap(org));
    }

    @PostMapping("/organizations/{organizationId}/transfer-ownership")
    public ResponseEntity<Map<String, Object>> transferOwnership(
            @PathVariable String organizationId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody Map<String, String> body) {
        if (userId == null) throw new AuthException("IDENTITY_SESSION_INVALID", "Not authenticated");
        String newOwnerUserId = body.get("newOwnerUserId");
        String password = body.get("password");
        orgService.transferOwnership(organizationId, userId, newOwnerUserId, password);
        return ResponseEntity.ok(Map.of("message", "所有权已转移"));
    }

    @PostMapping("/organizations/{organizationId}/request-deletion")
    public ResponseEntity<Map<String, Object>> requestDeletion(
            @PathVariable String organizationId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody Map<String, String> body) {
        if (userId == null) throw new AuthException("IDENTITY_SESSION_INVALID", "Not authenticated");
        orgService.requestDeletion(organizationId, userId, body.get("password"));
        return ResponseEntity.ok(Map.of("message", "组织解散申请已提交"));
    }

    @PostMapping("/organizations/{organizationId}/cancel-deletion")
    public ResponseEntity<Map<String, Object>> cancelDeletion(
            @PathVariable String organizationId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) throw new AuthException("IDENTITY_SESSION_INVALID", "Not authenticated");
        orgService.cancelDeletion(organizationId, userId);
        return ResponseEntity.ok(Map.of("message", "组织解散已取消"));
    }

    // === Permission Snapshot ===

    @GetMapping("/me/organizations/{organizationId}/permissions")
    public ResponseEntity<Map<String, Object>> getPermissions(
            @PathVariable String organizationId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) throw new AuthException("IDENTITY_SESSION_INVALID", "Not authenticated");
        var snapshot = authorizationService.getPermissionSnapshot(userId, organizationId);
        return ResponseEntity.ok(Map.of(
                "organizationId", snapshot.organizationId(),
                "membershipId", snapshot.membershipId(),
                "roles", snapshot.roleNames(),
                "permissions", snapshot.permissionCodes(),
                "permissionVersion", snapshot.permissionVersion()
        ));
    }

    // === Internal Auth Check ===

    @PostMapping("/internal/v1/identity/authorization/check")
    public ResponseEntity<Map<String, Object>> checkAuthorization(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        String orgId = (String) body.get("organizationId");
        String permission = (String) body.get("permission");

        AuthorizationService.AuthorizationResult result = authorizationService.check(userId, orgId, permission);
        return ResponseEntity.ok(Map.of(
                "allowed", result == AuthorizationService.AuthorizationResult.ALLOW,
                "decision", result.name(),
                "permission", permission
        ));
    }

    private Map<String, Object> toOrgMap(Organization org) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", org.getId());
        map.put("organizationType", org.getOrganizationType());
        map.put("name", org.getName());
        map.put("slug", org.getSlug());
        map.put("description", org.getDescription());
        map.put("status", org.getStatus());
        map.put("ownerUserId", org.getOwnerUserId());
        map.put("authorizationVersion", org.getAuthorizationVersion());
        map.put("createdAt", org.getCreatedAt());
        map.put("updatedAt", org.getUpdatedAt());
        return map;
    }

    public static class AuthException extends RuntimeException {
        private final String errorCode;
        public AuthException(String errorCode, String message) { super(message); this.errorCode = errorCode; }
        public String getErrorCode() { return errorCode; }
    }
}