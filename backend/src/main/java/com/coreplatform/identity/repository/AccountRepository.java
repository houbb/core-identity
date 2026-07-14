package com.coreplatform.identity.repository;

import com.coreplatform.identity.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserIdAndDeletedFalse(Long userId);

    Optional<Account> findByAccountTypeAndIdentifierAndDeletedFalse(
            Account.AccountType accountType, String identifier);

    Optional<Account> findByUserIdAndAccountTypeAndDeletedFalse(
            Long userId, Account.AccountType accountType);

    boolean existsByAccountTypeAndIdentifierAndDeletedFalse(
            Account.AccountType accountType, String identifier);
}