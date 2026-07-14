package com.coreplatform.identity.service;

import com.coreplatform.identity.entity.Account;
import com.coreplatform.identity.entity.Credential;
import com.coreplatform.identity.entity.RefreshToken;
import com.coreplatform.identity.entity.User;
import com.coreplatform.common.exception.BusinessException;
import com.coreplatform.identity.exception.IdentityErrorCode;
import com.coreplatform.identity.repository.AccountRepository;
import com.coreplatform.identity.repository.CredentialRepository;
import com.coreplatform.identity.repository.RefreshTokenRepository;
import com.coreplatform.identity.security.JwtTokenProvider;
import com.coreplatform.identity.util.TokenGenerator;
import com.coreplatform.identity.vo.LoginResponse;
import com.coreplatform.identity.vo.TokenResponse;
import com.coreplatform.identity.vo.UserVO;
import com.coreplatform.identity.util.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final AccountRepository accountRepository;
    private final CredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenGenerator tokenGenerator;

    public AuthService(UserService userService,
                       AccountRepository accountRepository,
                       CredentialRepository credentialRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       TokenGenerator tokenGenerator) {
        this.userService = userService;
        this.accountRepository = accountRepository;
        this.credentialRepository = credentialRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenGenerator = tokenGenerator;
    }

    @Transactional
    public LoginResponse register(String username, String password, String email, String displayName) {
        User user = userService.register(username, password, email, displayName);
        return generateLoginResponse(user);
    }

    @Transactional
    public LoginResponse login(String username, String password) {
        User user = userService.findUserForAuth(username);

        // Check user status
        if (user.getStatus() == User.UserStatus.DISABLED) {
            throw new BusinessException(IdentityErrorCode.AUTH_ACCOUNT_DISABLED);
        }
        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new BusinessException(IdentityErrorCode.AUTH_ACCOUNT_LOCKED);
        }

        // Find EMAIL account
        Account account = accountRepository.findByUserIdAndAccountTypeAndDeletedFalse(
                        user.getId(), Account.AccountType.EMAIL)
                .orElseThrow(() -> new BusinessException(IdentityErrorCode.AUTH_INVALID_CREDENTIALS));

        // Find PASSWORD credential
        Credential credential = credentialRepository
                .findByAccountIdAndCredentialTypeAndStatusAndDeletedFalse(
                        account.getId(),
                        Credential.CredentialType.PASSWORD,
                        Credential.CredentialStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(IdentityErrorCode.AUTH_INVALID_CREDENTIALS));

        // Verify password
        if (!passwordEncoder.matches(password, credential.getCredentialValue())) {
            throw new BusinessException(IdentityErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        return generateLoginResponse(user);
    }

    @Transactional
    public TokenResponse refreshAccessToken(String refreshTokenValue) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndDeletedFalse(refreshTokenValue)
                .orElseThrow(() -> new BusinessException(IdentityErrorCode.AUTH_REFRESH_TOKEN_EXPIRED));

        if (!storedToken.isValid()) {
            if (storedToken.getRevoked()) {
                throw new BusinessException(IdentityErrorCode.AUTH_REFRESH_TOKEN_REVOKED);
            }
            throw new BusinessException(IdentityErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        // Token rotation: revoke old token, issue new one
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = userService.findUserForAuth(
                userService.getById(storedToken.getUserId()).getUsername());

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        RefreshToken newRefreshToken = createRefreshToken(user.getId());

        return new TokenResponse(newAccessToken, newRefreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpiration() / 1000);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserIdAndDeletedFalse(userId);
        log.info("User logged out: userId={}", userId);
    }

    private LoginResponse generateLoginResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        RefreshToken refreshToken = createRefreshToken(user.getId());
        UserVO userVO = UserMapper.toVO(user);

        return new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getAccessTokenExpiration() / 1000,
                userVO
        );
    }

    private RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(tokenGenerator.generateRefreshToken());
        refreshToken.setExpireTime(LocalDateTime.now().plusSeconds(
                jwtTokenProvider.getAccessTokenExpiration() / 1000 * 4)); // ~4x access token
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }
}