package com.workbridge.workbridge_app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workbridge.workbridge_app.dto.ProviderRequestDTO;
import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.ProviderRequest;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.entity.UserRoleEntity;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.repository.ProviderRequestRepository;
import com.workbridge.workbridge_app.repository.UserRepository;
import com.workbridge.workbridge_app.repository.UserRoleRepository;
import com.workbridge.workbridge_app.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private ProviderRequestRepository providerRequestRepository;

    @InjectMocks
    private UserService userService;

    private ApplicationUser testUser;
    private ApplicationUser testAdmin;
    private UserRoleEntity providerRole;
    private UserRoleEntity seekerRole;
    private UserRoleEntity adminRole;
    private ProviderRequest testProviderRequest;

    @BeforeEach
    void setUp() {
        // Create roles
        providerRole = new UserRoleEntity();
        providerRole.setRole(UserRole.SERVICE_PROVIDER);

        seekerRole = new UserRoleEntity();
        seekerRole.setRole(UserRole.SERVICE_SEEKER);

        adminRole = new UserRoleEntity();
        adminRole.setRole(UserRole.ADMIN);

        // Create test user
        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoles(Set.of(providerRole));
        testUser.setEnabled(true);

        // Create test admin
        testAdmin = new ApplicationUser();
        testAdmin.setId(2L);
        testAdmin.setUsername("admin");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setRoles(Set.of(adminRole));
        testAdmin.setEnabled(true);

        // Create test provider request
        testProviderRequest = new ProviderRequest();
        testProviderRequest.setId(1L);
        testProviderRequest.setUser(testUser);
        testProviderRequest.setRequestedOn(LocalDateTime.now());
        testProviderRequest.setApproved(false);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, testAdmin));

        // Act
        List<UserResponseDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        assertEquals(testAdmin.getUsername(), result.get(1).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void getAllNonAdminUsers_ShouldReturnOnlyNonAdminUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, testAdmin));

        // Act
        List<UserResponseDTO> result = userService.getAllNonAdminUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void getUsersByRole_ShouldReturnUsersWithSpecificRole() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, testAdmin));

        // Act
        List<UserResponseDTO> result = userService.getUsersByRole(UserRole.SERVICE_PROVIDER);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void getAllProviderRequestNotApproved_ShouldReturnPendingRequests() {
        // Arrange
        when(providerRequestRepository.findAll()).thenReturn(Arrays.asList(testProviderRequest));

        // Act
        List<ProviderRequestDTO> result = userService.getAllProviderRequestNotApproved();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        assertEquals(testUser.getEmail(), result.get(0).getEmail());
        assertFalse(result.get(0).isApproved());
        verify(providerRequestRepository).findAll();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<ApplicationUser> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<ApplicationUser> result = userService.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<ApplicationUser> result = userService.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void saveUser_ShouldReturnSavedUser() {
        // Arrange
        when(userRepository.save(any(ApplicationUser.class))).thenReturn(testUser);

        // Act
        ApplicationUser result = userService.saveUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void isServiceProvider_WhenUserIsProvider_ShouldReturnTrue() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.isServiceProvider("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void isServiceProvider_WhenUserIsNotProvider_ShouldReturnFalse() {
        // Arrange
        ApplicationUser nonProvider = new ApplicationUser();
        nonProvider.setUsername("nonprovider");
        nonProvider.setRoles(Set.of(seekerRole));
        when(userRepository.findByUsername("nonprovider")).thenReturn(Optional.of(nonProvider));

        // Act
        boolean result = userService.isServiceProvider("nonprovider");

        // Assert
        assertFalse(result);
        verify(userRepository).findByUsername("nonprovider");
    }

    @Test
    void isServiceProvider_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            userService.isServiceProvider("nonexistent")
        );
        verify(userRepository).findByUsername("nonexistent");
    }
} 