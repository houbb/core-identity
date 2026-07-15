package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.RefreshToken;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(RefreshToken token);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    Optional<RefreshToken> findById(String id);
    void update(RefreshToken token);
    List<RefreshToken> findByFamilyId(String familyId);
    void deleteExpired(long before);
}
