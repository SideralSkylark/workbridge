package com.workbridge.workbridge_app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.workbridge.workbridge_app.exception.InvalidCredentialsException;
import com.workbridge.workbridge_app.exception.UserAlreadyExistsException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.repository.UserRepository;
import com.workbridge.workbridge_app.repository.UserRoleRepository;
import com.workbridge.workbridge_app.security.JwtService;
import com.workbridge.workbridge_app.service.AuthenticationService;
import com.workbridge.workbridge_app.service.VerificationService;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private VerificationService verificationService;

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
        verify(verificationService).createAndSendVerificationToken(any(ApplicationUser.class));
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
        verify(verificationService, never()).createAndSendVerificationToken(any(ApplicationUser.class));
    }

    @Test
    void verify_WhenValidCode_ShouldEnableUserAndReturnToken() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(ApplicationUser.class))).thenReturn("jwt.token.here");

        // Act
        AuthenticationResponseDTO response = authenticationService.verify(verificationRequest);

        // Assert
        assertNotNull(response);
        assertTrue(testUser.isEnabled());
        assertEquals("jwt.token.here", response.getToken());
        verify(userRepository).save(testUser);
        verify(verificationService).verifyToken(verificationRequest.getEmail(), verificationRequest.getCode());
    }

    @Test
    void verify_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            authenticationService.verify(verificationRequest)
        );
        verify(userRepository, never()).save(any(ApplicationUser.class));
    }

    @Test
    void login_WhenValidCredentials_ShouldReturnToken() {
        // Arrange
        testUser.setEnabled(true);
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
        assertThrows(InvalidCredentialsException.class, () -> 
            authenticationService.login(loginRequest)
        );
        verify(jwtService, never()).generateToken(any(ApplicationUser.class));
    }

    @Test
    void login_WhenUserNotEnabled_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

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

        // Act
        RegisterResponseDTO response = authenticationService.resendVerificationCode(testUser.getEmail());

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(verificationService).deleteExistingToken(testUser.getEmail());
        verify(verificationService).createAndSendVerificationToken(testUser);
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
        verify(verificationService, never()).deleteExistingToken(anyString());
        verify(verificationService, never()).createAndSendVerificationToken(any(ApplicationUser.class));
    }
} 