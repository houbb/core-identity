package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.RefreshTokenFamily;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenFamilyRepository {
    void save(RefreshTokenFamily family);
    Optional<RefreshTokenFamily> findById(String id);
    void update(RefreshTokenFamily family);
    List<RefreshTokenFamily> findByUserId(String userId);
    List<RefreshTokenFamily> findByGrantId(String grantId);
}
