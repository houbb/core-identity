package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.LoginAttempt;

import java.util.List;

/**
 * Repository for identity_login_attempt.
 */
public interface LoginAttemptRepository {

    void save(LoginAttempt attempt);

    int countRecentFailuresByUser(String userId, long sinceTimestamp);

    int countRecentAttemptsByIp(String ipAddress, long sinceTimestamp);

    List<LoginAttempt> findByUserId(String userId, int limit);
}