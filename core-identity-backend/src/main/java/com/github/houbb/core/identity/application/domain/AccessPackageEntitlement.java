package com.github.houbb.core.identity.application.domain;

/**
 * AccessPackageEntitlement — 套餐与权益的多对多关联。
 *
 * Table: identity_access_package_entitlement
 */
public class AccessPackageEntitlement {

    private String packageId;
    private String entitlementId;
    private long createdAt;

    public AccessPackageEntitlement() {
    }

    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public String getEntitlementId() { return entitlementId; }
    public void setEntitlementId(String entitlementId) { this.entitlementId = entitlementId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
