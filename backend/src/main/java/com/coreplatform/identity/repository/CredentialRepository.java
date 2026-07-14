package com.coreplatform.identity.repository;

import com.coreplatform.identity.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    Optional<Credential> findByAccountIdAndCredentialTypeAndStatusAndDeletedFalse(
            Long accountId, Credential.CredentialType credentialType, Credential.CredentialStatus status);

    Optional<Credential> findByAccountIdAndCredentialTypeAndDeletedFalse(
            Long accountId, Credential.CredentialType credentialType);
}