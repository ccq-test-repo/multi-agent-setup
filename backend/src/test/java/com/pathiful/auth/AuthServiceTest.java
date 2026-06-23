package com.pathiful.auth;

import com.pathiful.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final TokenService tokenService = new TokenService();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, tokenService);
    }

    // -----------------------------------------------------------------------
    // Registrierung
    // -----------------------------------------------------------------------

    @Test
    void shouldRegisterSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("securePass123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getToken()).isNotBlank();

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("test@example.com") &&
                u.getPasswordHash().startsWith("$2a$") && // BCrypt hash
                u.getRole() == User.Role.USER
        ));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("securePass123");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bereits registriert");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNormalizeEmailToLowercase() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("Test@Example.COM");
        request.setPassword("securePass123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        AuthResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    // -----------------------------------------------------------------------
    // Login
    // -----------------------------------------------------------------------

    @Test
    void shouldLoginSuccessfully() {
        String email = "user@example.com";
        String password = "myPassword123";
        String hash = passwordEncoder.encode(password);

        User user = new User(email, hash, User.Role.USER);
        user.setId(10L);

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    void shouldRejectWrongPassword() {
        String hash = passwordEncoder.encode("correctPassword");

        User user = new User("user@example.com", hash, User.Role.USER);
        user.setId(10L);

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ungültige Anmeldedaten");
    }

    @Test
    void shouldRejectNonExistentEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("somePassword123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ungültige Anmeldedaten");
    }

    // -----------------------------------------------------------------------
    // Logout
    // -----------------------------------------------------------------------

    @Test
    void shouldLogoutSuccessfully() {
        // Register first to get a token
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("logout@example.com");
        reg.setPassword("password123");

        when(userRepository.existsByEmail("logout@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(5L);
            return u;
        });

        AuthResponse response = authService.register(reg);
        String token = response.getToken();

        // Token is valid before logout
        assertThat(tokenService.validateToken(token)).isPresent();

        // Logout
        authService.logout(token);

        // Token is now invalid
        assertThat(tokenService.validateToken(token)).isEmpty();
    }

    @Test
    void shouldHandleLogoutWithNullToken() {
        // Should not throw
        authService.logout(null);
    }

    // -----------------------------------------------------------------------
    // ADMIN-MFA
    // -----------------------------------------------------------------------

    @Test
    void shouldSetupMfaForAdmin() {
        User admin = new User("admin@example.com", "hash", User.Role.ADMIN);
        admin.setId(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        MfaSetupResponse response = authService.setupMfa(99L);

        assertThat(response).isNotNull();
        assertThat(response.getSecret()).isNotBlank();
        assertThat(response.getUri()).contains("otpauth://totp/Pathiful:admin@example.com");
        assertThat(admin.isMfaEnabled()).isTrue();
    }

    @Test
    void shouldRejectMfaSetupForUser() {
        User user = new User("user@example.com", "hash", User.Role.USER);
        user.setId(50L);

        when(userRepository.findById(50L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.setupMfa(50L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nur für ADMIN");
    }

    @Test
    void shouldVerifyMfaCorrectCode() {
        User admin = new User("admin@example.com", "hash", User.Role.ADMIN);
        admin.setId(99L);
        admin.setMfaEnabled(true);

        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        boolean result = authService.verifyMfa(99L, "123456");
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectMfaWrongCode() {
        User admin = new User("admin@example.com", "hash", User.Role.ADMIN);
        admin.setId(99L);
        admin.setMfaEnabled(true);

        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        boolean result = authService.verifyMfa(99L, "999999");
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectMfaWhenNotEnabled() {
        User admin = new User("admin@example.com", "hash", User.Role.ADMIN);
        admin.setId(99L);
        admin.setMfaEnabled(false); // Not enabled

        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> authService.verifyMfa(99L, "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nicht aktiviert");
    }
}
