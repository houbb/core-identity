package com.coreplatform.identity.service;

import com.coreplatform.identity.entity.User;
import com.coreplatform.identity.exception.BusinessException;
import com.coreplatform.identity.exception.ErrorCode;
import com.coreplatform.identity.repository.AccountRepository;
import com.coreplatform.identity.repository.CredentialRepository;
import com.coreplatform.identity.repository.UserRepository;
import com.coreplatform.identity.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CredentialRepository credentialRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, accountRepository,
                credentialRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByUsernameAndDeletedFalse("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(credentialRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");

        User user = userService.register("testuser", "password123",
                "test@example.com", "Test User");

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any());
        verify(credentialRepository).save(any());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        when(userRepository.existsByUsernameAndDeletedFalse("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("existinguser", "pass", null, null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS.getCode());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByUsernameAndDeletedFalse("newuser")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedFalse("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("newuser", "pass", "taken@test.com", null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS.getCode());
    }

    @Test
    void shouldGetUserById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setDisplayName("Test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserVO vo = userService.getById(1L);

        assertThat(vo).isNotNull();
        assertThat(vo.getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());
    }
}