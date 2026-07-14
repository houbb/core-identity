package com.coreplatform.identity.util;

import com.coreplatform.identity.entity.User;
import com.coreplatform.identity.vo.UserVO;

public class UserMapper {

    private UserMapper() {
    }

    public static UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setDisplayName(user.getDisplayName());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus().name());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}