package com.coreplatform.identity.dto;

import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(max = 128, message = "显示名称不超过128个字符")
    private String displayName;

    @Size(max = 512, message = "头像URL不超过512个字符")
    private String avatar;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}