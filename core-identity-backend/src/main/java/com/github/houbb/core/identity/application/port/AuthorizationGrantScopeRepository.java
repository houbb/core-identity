package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AuthorizationGrantScope;

import java.util.List;

/**
 * Repository for identity_authorization_grant_scope.
 */
public interface AuthorizationGrantScopeRepository {

    void save(AuthorizationGrantScope grantScope);

    void saveAll(List<AuthorizationGrantScope> grantScopes);

    List<AuthorizationGrantScope> findByGrantId(String grantId);

    List<AuthorizationGrantScope> findByGrantIds(List<String> grantIds);

    void deleteByGrantId(String grantId);

    void deleteByGrantIdAndScopeId(String grantId, String scopeId);
}