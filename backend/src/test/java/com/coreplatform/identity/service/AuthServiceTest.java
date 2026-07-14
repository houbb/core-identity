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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CredentialRepository credentialRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenGenerator tokenGenerator;

    private AuthService authService;

    private User testUser;
    private Account testAccount;
    private Credential testCredential;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, accountRepository, credentialRepository,
                refreshTokenRepository, passwordEncoder, jwtTokenProvider, tokenGenerator);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testAccount = new Account();
        testAccount.setId(100L);
        testAccount.setUser(testUser);
        testAccount.setAccountType(Account.AccountType.EMAIL);

        testCredential = new Credential();
        testCredential.setId(200L);
        testCredential.setAccount(testAccount);
        testCredential.setCredentialType(Credential.CredentialType.PASSWORD);
        testCredential.setCredentialValue("$2a$encoded");
        testCredential.setStatus(Credential.CredentialStatus.ACTIVE);
    }

    @Test
    void shouldLoginSuccessfully() {
        when(userService.findUserForAuth("testuser")).thenReturn(testUser);
        when(accountRepository.findByUserIdAndAccountTypeAndDeletedFalse(1L, Account.AccountType.EMAIL))
                .thenReturn(Optional.of(testAccount));
        when(credentialRepository.findByAccountIdAndCredentialTypeAndStatusAndDeletedFalse(
                100L, Credential.CredentialType.PASSWORD, Credential.CredentialStatus.ACTIVE))
                .thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches("password123", "$2a$encoded")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(1L, "testuser")).thenReturn("access-token-mock");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
        when(tokenGenerator.generateRefreshToken()).thenReturn("refresh-token-mock");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = authService.login("testuser", "password123");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-mock");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void shouldRejectWrongPassword() {
        when(userService.findUserForAuth("testuser")).thenReturn(testUser);
        when(accountRepository.findByUserIdAndAccountTypeAndDeletedFalse(1L, Account.AccountType.EMAIL))
                .thenReturn(Optional.of(testAccount));
        when(credentialRepository.findByAccountIdAndCredentialTypeAndStatusAndDeletedFalse(
                100L, Credential.CredentialType.PASSWORD, Credential.CredentialStatus.ACTIVE))
                .thenReturn(Optional.of(testCredential));
        when(passwordEncoder.matches("wrongpass", "$2a$encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("testuser", "wrongpass"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(IdentityErrorCode.AUTH_INVALID_CREDENTIALS.getCode());
    }

    @Test
    void shouldRejectDisabledUser() {
        testUser.setStatus(User.UserStatus.DISABLED);
        when(userService.findUserForAuth("testuser")).thenReturn(testUser);

        assertThatThrownBy(() -> authService.login("testuser", "pass"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(IdentityErrorCode.AUTH_ACCOUNT_DISABLED.getCode());
    }

    @Test
    void shouldRejectLockedUser() {
        testUser.setStatus(User.UserStatus.LOCKED);
        when(userService.findUserForAuth("testuser")).thenReturn(testUser);

        assertThatThrownBy(() -> authService.login("testuser", "pass"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(IdentityErrorCode.AUTH_ACCOUNT_LOCKED.getCode());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setId(1L);
        storedToken.setUserId(1L);
        storedToken.setToken("old-refresh");
        storedToken.setExpireTime(LocalDateTime.now().plusDays(1));
        storedToken.setRevoked(false);

        when(refreshTokenRepository.findByTokenAndDeletedFalse("old-refresh"))
                .thenReturn(Optional.of(storedToken));
        when(userService.getById(1L)).thenReturn(
                new com.coreplatform.identity.vo.UserVO() {{
                    setUsername("testuser");
                }});
        when(userService.findUserForAuth("testuser")).thenReturn(testUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(1L, "testuser")).thenReturn("new-access-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
        when(tokenGenerator.generateRefreshToken()).thenReturn("new-refresh-token");

        var response = authService.refreshAccessToken("old-refresh");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(storedToken.getRevoked()).isTrue();
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setId(1L);
        storedToken.setUserId(1L);
        storedToken.setToken("expired-refresh");
        storedToken.setExpireTime(LocalDateTime.now().minusDays(1));
        storedToken.setRevoked(false);

        when(refreshTokenRepository.findByTokenAndDeletedFalse("expired-refresh"))
                .thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refreshAccessToken("expired-refresh"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(IdentityErrorCode.AUTH_REFRESH_TOKEN_EXPIRED.getCode());
    }

    @Test
    void shouldRejectRevokedRefreshToken() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setId(1L);
        storedToken.setUserId(1L);
        storedToken.setToken("revoked-refresh");
        storedToken.setExpireTime(LocalDateTime.now().plusDays(1));
        storedToken.setRevoked(true);

        when(refreshTokenRepository.findByTokenAndDeletedFalse("revoked-refresh"))
                .thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refreshAccessToken("revoked-refresh"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(IdentityErrorCode.AUTH_REFRESH_TOKEN_REVOKED.getCode());
    }

    @Test
    void shouldLogoutSuccessfully() {
        doNothing().when(refreshTokenRepository).deleteByUserIdAndDeletedFalse(1L);

        authService.logout(1L);

        verify(refreshTokenRepository).deleteByUserIdAndDeletedFalse(1L);
    }

    @Test
    void shouldRegisterAndLogin() {
        when(userService.register("newuser", "pass123", "new@test.com", "New User"))
                .thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(1L, "testuser")).thenReturn("access-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
        when(tokenGenerator.generateRefreshToken()).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = authService.register("newuser", "pass123", "new@test.com", "New User");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }
}