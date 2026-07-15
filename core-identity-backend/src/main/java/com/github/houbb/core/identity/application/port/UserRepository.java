package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.User;

import java.util.Optional;

/**
 * Repository for identity_user.
 */
public interface UserRepository {

    void save(User user);

    Optional<User> findById(String id);

    void update(User user);

    void updateStatus(String id, String status, long version);
}