package com.workbridge.workbridge_app.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.workbridge.workbridge_app.auth.dto.*;
import com.workbridge.workbridge_app.auth.entity.RefreshToken;
import com.workbridge.workbridge_app.auth.exception.InvalidTokenException;
import com.workbridge.workbridge_app.auth.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.auth.mapper.SessionMapper;
import com.workbridge.workbridge_app.auth.service.AuthenticationService;
import com.workbridge.workbridge_app.auth.service.RefreshTokenService;
import com.workbridge.workbridge_app.auth.service.VerificationService;
import com.workbridge.workbridge_app.auth.util.CookieUtil;
import com.workbridge.workbridge_app.auth.util.CookieUtil.TokenType;
import com.workbridge.workbridge_app.security.JwtService;
import com.workbridge.workbridge_app.user.entity.*;
import com.workbridge.workbridge_app.user.repository.UserRepository;
import com.workbridge.workbridge_app.user.repository.UserRoleRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class AuthenticationServiceTest {

    @InjectMocks private AuthenticationService authenticationService;
    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository roleRepository;
    @Mock private VerificationService verificationService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private CookieUtil cookieUtil;
    @Mock private SessionMapper sessionMapper;
    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest httpRequest;
    @Mock private HttpServletResponse httpResponse;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private final String username = "john";
    private final String email = "john@example.com";
    private final String password = "password";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private ApplicationUser buildUser(boolean enabled) {
        return ApplicationUser.builder()
            .id(1L)
            .email(email)
            .username(username)
            .enabled(enabled)
            .password("hashed")
            .roles(Set.of(new UserRoleEntity(1L, UserRole.SERVICE_SEEKER)))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    class RegisterTests {
        @Test
        void shouldRegisterUserSuccessfully() {
            RegisterRequestDTO request = new RegisterRequestDTO(username, email, password, List.of("SERVICE_SEEKER"), "INACTIVE");
            when(userRepository.existsByUsername(username)).thenReturn(false);
            when(userRepository.existsByEmail(email)).thenReturn(false);
            when(passwordEncoder.encode(password)).thenReturn("hashed");
            when(roleRepository.findByRole(UserRole.SERVICE_SEEKER)).thenReturn(Optional.of(new UserRoleEntity(1L, UserRole.SERVICE_SEEKER)));

            RegisterResponseDTO response = authenticationService.register(request);

            assertEquals(email, response.getEmail());
            verify(userRepository).save(any(ApplicationUser.class));
            verify(verificationService).createAndSendVerificationToken(any(ApplicationUser.class));
        }

        @Test
        void shouldThrowIfUsernameExists() {
            RegisterRequestDTO request = new RegisterRequestDTO(username, email, password, List.of("USER"), "INACTIVE");
            when(userRepository.existsByUsername(username)).thenReturn(true);
            assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(request));
        }
    }

    @Nested
    class VerifyTests {
        @Test
        void shouldEnableUserOnSuccessfulVerification() {
            ApplicationUser user = buildUser(false);
            EmailVerificationDTO dto = new EmailVerificationDTO(email, "123456");

            doNothing().when(verificationService).verifyToken(email, "123456");
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            AuthenticationResponseDTO response = authenticationService.verify(dto);

            assertTrue(user.isEnabled());
            assertEquals(username, response.getUsername());
            assertEquals(email, response.getEmail());
        }
    }

    @Nested
    class ResendVerificationCodeTests {
        @Test
        void shouldResendCodeIfUserIsNotVerified() {
            ApplicationUser user = buildUser(false);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            RegisterResponseDTO response = authenticationService.resendVerificationCode(email);

            assertEquals(email, response.getEmail());
            verify(verificationService).deleteExistingToken(email);
            verify(verificationService).createAndSendVerificationToken(user);
        }

        @Test
        void shouldNotResendIfUserIsVerified() {
            ApplicationUser user = buildUser(true);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            RegisterResponseDTO response = authenticationService.resendVerificationCode(email);

            assertEquals(email, response.getEmail());
            verify(verificationService, never()).createAndSendVerificationToken(any());
            verify(verificationService, never()).deleteExistingToken(any());
        }
    }

    @Nested
    class LoginTests {
        @Test
        void shouldLoginSuccessfully() {
            ApplicationUser user = buildUser(true);
            LoginRequestDTO dto = new LoginRequestDTO(email, password);

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, "hashed")).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("access-token");
            when(refreshTokenService.createRefreshToken(eq(user), any())).thenReturn(RefreshToken
                .builder()
                    .id(1L)
                    .token("refresh-token")
                    .user(user).
                    createdAt(LocalDateTime.now()).build());

            AuthenticationResponseDTO response = authenticationService.login(dto, httpRequest, httpResponse);

            assertEquals(email, response.getEmail());
            verify(cookieUtil).setTokenCookie(eq(httpResponse), any(), eq("access-token"), eq(TokenType.ACCESS));
            verify(cookieUtil).setTokenCookie(eq(httpResponse), any(), eq("refresh-token"), eq(TokenType.REFRESH));
        }
    }

    @Nested
    class RefreshTokenTests {
        @Test
        void shouldRefreshAccessToken() {
            ApplicationUser user = buildUser(true);

            when(cookieUtil.extractTokenFromCookie(httpRequest, "refresh_token")).thenReturn("old-token");
            when(refreshTokenService.isTokenValid("old-token")).thenReturn(true);
            when(refreshTokenService.getUserFromToken("old-token")).thenReturn(user);
            when(jwtService.generateToken(user)).thenReturn("new-access-token");
            when(refreshTokenService.createRefreshToken(eq(user), any())).thenReturn(RefreshToken
                .builder()
                    .id(2L)
                    .token("new-refresh-token")
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build());

            authenticationService.refreshAccessToken(httpRequest, httpResponse);

            verify(cookieUtil).setTokenCookie(eq(httpResponse), any(), eq("new-access-token"), eq(TokenType.ACCESS));
            verify(cookieUtil).setTokenCookie(eq(httpResponse), any(), eq("new-refresh-token"), eq(TokenType.REFRESH));
        }

        @Test
        void shouldThrowIfRefreshTokenInvalid() {
            when(cookieUtil.extractTokenFromCookie(httpRequest, "refresh_token")).thenReturn(null);
            assertThrows(InvalidTokenException.class, () -> authenticationService.refreshAccessToken(httpRequest, httpResponse));
        }
    }

    @Nested
    class LogoutTests {
        @Test
        void shouldClearCookiesAndRevokeToken() {
            when(cookieUtil.extractTokenFromCookie(httpRequest, "refresh_token")).thenReturn("token");

            authenticationService.logout(httpRequest, httpResponse);

            verify(refreshTokenService).deleteByToken("token");
            verify(cookieUtil, times(2)).clearCookie(eq(httpResponse), any());
        }
    }

    @Nested
    class ListSessionsTests {
        @Test
        void shouldReturnUserSessions() {
            ApplicationUser user = buildUser(true);
            RefreshToken token = RefreshToken
                .builder()
                    .id(1L)
                    .token("token")
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
            SessionDTO dto = new SessionDTO();

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(refreshTokenService.findAllByUserId(eq(1L), any())).thenReturn(new PageImpl<>(List.of(token)));
            when(sessionMapper.toSessionDTO(token)).thenReturn(dto);

            Page<SessionDTO> sessions = authenticationService.listSessions(username, Pageable.unpaged());

            assertEquals(1, sessions.getContent().size());
        }
    }
}
