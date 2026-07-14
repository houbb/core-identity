package com.coreplatform.identity.repository;

import com.coreplatform.identity.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndDeletedFalse(String token);

    void deleteByUserIdAndDeletedFalse(Long userId);
}