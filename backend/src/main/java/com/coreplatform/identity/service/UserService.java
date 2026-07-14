package com.coreplatform.identity.service;

import com.coreplatform.identity.entity.Account;
import com.coreplatform.identity.entity.Credential;
import com.coreplatform.identity.entity.User;
import com.coreplatform.common.exception.BusinessException;
import com.coreplatform.identity.exception.IdentityErrorCode;
import com.coreplatform.identity.repository.AccountRepository;
import com.coreplatform.identity.repository.CredentialRepository;
import com.coreplatform.identity.repository.UserRepository;
import com.coreplatform.identity.vo.UserVO;
import com.coreplatform.identity.util.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       AccountRepository accountRepository,
                       CredentialRepository credentialRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String password, String email, String displayName) {
        if (userRepository.existsByUsernameAndDeletedFalse(username)) {
            throw new BusinessException(IdentityErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (email != null && !email.isBlank() && userRepository.existsByEmailAndDeletedFalse(email)) {
            throw new BusinessException(IdentityErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(displayName != null ? displayName : username);
        user.setStatus(User.UserStatus.ACTIVE);
        user = userRepository.save(user);

        // Create EMAIL account
        String accountIdentifier = email != null && !email.isBlank() ? email : username;
        Account account = new Account();
        account.setUser(user);
        account.setAccountType(Account.AccountType.EMAIL);
        account.setIdentifier(accountIdentifier);
        account.setIsPrimary(true);
        account.setStatus(Account.AccountStatus.ACTIVE);
        account = accountRepository.save(account);

        // Create PASSWORD credential
        Credential credential = new Credential();
        credential.setAccount(account);
        credential.setCredentialType(Credential.CredentialType.PASSWORD);
        credential.setCredentialValue(passwordEncoder.encode(password));
        credential.setStatus(Credential.CredentialStatus.ACTIVE);
        credentialRepository.save(credential);

        return user;
    }

    public UserVO getById(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.getDeleted())
                .orElseThrow(() -> new BusinessException(IdentityErrorCode.USER_NOT_FOUND));
        return UserMapper.toVO(user);
    }

    public UserVO getByUsername(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(IdentityErrorCode.USER_NOT_FOUND));
        return UserMapper.toVO(user);
    }

    @Transactional
    public UserVO update(Long userId, String displayName, String avatar) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.getDeleted())
                .orElseThrow(() -> new BusinessException(IdentityErrorCode.USER_NOT_FOUND));

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        user = userRepository.save(user);
        return UserMapper.toVO(user);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Account account = accountRepository.findByUserIdAndAccountTypeAndDeletedFalse(
                        userId, Account.AccountType.EMAIL)
                .orElseThrow(() -> new BusinessException(IdentityErrorCode.USER_ACCOUNT_NOT_FOUND));

        Credential credential = credentialRepository
                .findByAccountIdAndCredentialTypeAndStatusAndDeletedFalse(
                        account.getId(),
                        Credential.CredentialType.PASSWORD,
                        Credential.CredentialStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(IdentityErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(oldPassword, credential.getCredentialValue())) {
            throw new BusinessException(IdentityErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        credential.setCredentialValue(passwordEncoder.encode(newPassword));
        credentialRepository.save(credential);
    }

    public User findUserForAuth(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(IdentityErrorCode.AUTH_INVALID_CREDENTIALS));
    }
}