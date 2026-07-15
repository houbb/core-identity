package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Scope;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_scope.
 */
public interface ScopeRepository {

    void save(Scope scope);

    Optional<Scope> findById(String id);

    Optional<Scope> findByCode(String scopeCode);

    List<Scope> findByService(String sourceService);

    List<Scope> findByIds(List<String> ids);

    List<Scope> findAllAssignable();

    List<Scope> findAll();

    void update(Scope scope);

    void updateStatus(String id, String status, long now, long version);
}