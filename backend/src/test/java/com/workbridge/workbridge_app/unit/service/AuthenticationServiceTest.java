package com.workbridge.workbridge_app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.workbridge.workbridge_app.dto.AuthenticationResponseDTO;
import com.workbridge.workbridge_app.dto.EmailVerificationDTO;
import com.workbridge.workbridge_app.dto.LoginRequestDTO;
import com.workbridge.workbridge_app.dto.RegisterRequestDTO;
import com.workbridge.workbridge_app.dto.RegisterResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.entity.UserRoleEntity;
import com.workbridge.workbridge_app.entity.VerificationToken;
import com.workbridge.workbridge_app.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.repository.UserRepository;
import com.workbridge.workbridge_app.repository.UserRoleRepository;
import com.workbridge.workbridge_app.repository.VerificationTokenRepository;
import com.workbridge.workbridge_app.security.JwtService;
import com.workbridge.workbridge_app.service.AuthenticationService;
import com.workbridge.workbridge_app.service.EmailService;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository roleRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private ApplicationUser testUser;
    private UserRoleEntity seekerRole;
    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private EmailVerificationDTO verificationRequest;

    @BeforeEach
    void setUp() {
        // Create test role
        seekerRole = new UserRoleEntity();
        seekerRole.setRole(UserRole.SERVICE_SEEKER);

        // Create test user
        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(false);
        testUser.setRoles(Set.of(seekerRole));

        // Create test DTOs
        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRoles(Arrays.asList("SERVICE_SEEKER"));

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        verificationRequest = new EmailVerificationDTO();
        verificationRequest.setEmail("test@example.com");
        verificationRequest.setCode("123456");
    }

    @Test
    void register_WhenUsernameAndEmailAvailable_ShouldCreateUser() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByRole(any())).thenReturn(Optional.of(seekerRole));
        when(userRepository.save(any(ApplicationUser.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Act
        RegisterResponseDTO response = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository).save(any(ApplicationUser.class));
        verify(emailService).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void register_WhenUsernameExists_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> 
            authenticationService.register(registerRequest)
        );
        verify(userRepository, never()).save(any(ApplicationUser.class));
    }

    @Test
    void verify_WhenValidCode_ShouldEnableUserAndReturnToken() {
        // Arrange
        VerificationToken token = new VerificationToken(
            testUser.getEmail(),
            "123456",
            LocalDateTime.now().plusMinutes(10)
        );
        when(verificationTokenRepository.findByEmail(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(ApplicationUser.class))).thenReturn("jwt.token.here");

        // Act
        AuthenticationResponseDTO response = authenticationService.verify(verificationRequest);

        // Assert
        assertNotNull(response);
        assertTrue(testUser.isEnabled());
        assertEquals("jwt.token.here", response.getToken());
        verify(userRepository).save(testUser);
        verify(verificationTokenRepository).save(token);
    }

    @Test
    void verify_WhenInvalidCode_ShouldThrowException() {
        // Arrange
        VerificationToken token = new VerificationToken(
            testUser.getEmail(),
            "654321", // Different code
            LocalDateTime.now().plusMinutes(10)
        );
        when(verificationTokenRepository.findByEmail(anyString())).thenReturn(Optional.of(token));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authenticationService.verify(verificationRequest)
        );
        verify(userRepository, never()).save(any(ApplicationUser.class));
    }

    @Test
    void login_WhenValidCredentials_ShouldReturnToken() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(ApplicationUser.class))).thenReturn("jwt.token.here");

        // Act
        AuthenticationResponseDTO response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals(testUser.getUsername(), response.getUsername());
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void login_WhenInvalidCredentials_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            authenticationService.login(loginRequest)
        );
        verify(jwtService, never()).generateToken(any(ApplicationUser.class));
    }

    @Test
    void resendVerificationCode_WhenUserNotEnabled_ShouldSendNewCode() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(new VerificationToken());

        // Act
        RegisterResponseDTO response = authenticationService.resendVerificationCode(testUser.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(verificationTokenRepository).deleteByEmail(testUser.getEmail());
        verify(emailService).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void resendVerificationCode_WhenUserAlreadyEnabled_ShouldReturnEmail() {
        // Arrange
        testUser.setEnabled(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        RegisterResponseDTO response = authenticationService.resendVerificationCode(testUser.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(verificationTokenRepository, never()).deleteByEmail(anyString());
        verify(emailService, never()).sendVerificationCode(anyString(), anyString());
    }
} 