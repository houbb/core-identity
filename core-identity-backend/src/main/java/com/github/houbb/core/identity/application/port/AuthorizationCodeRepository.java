package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AuthorizationCode;
import java.util.Optional;

public interface AuthorizationCodeRepository {
    void save(AuthorizationCode code);
    Optional<AuthorizationCode> findByCodeHash(String codeHash);
    void markUsed(String id, long now);
    void deleteExpired(long before);
}
