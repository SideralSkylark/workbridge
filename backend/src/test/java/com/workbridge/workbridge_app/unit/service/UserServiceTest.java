package com.workbridge.workbridge_app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.entity.UserRoleEntity;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.repository.UserRepository;
import com.workbridge.workbridge_app.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService; 

    private ApplicationUser mockUser;

    static class TestUser extends ApplicationUser {
        @Override
        public List<? extends GrantedAuthority> getAuthorities() {
            return List.of();
        }
    }

    @BeforeEach
    void setUp() {
        mockUser = new TestUser();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setEmail("test@example.com");
        mockUser.setRoles(Set.of(new UserRoleEntity(UserRole.SERVICE_SEEKER)));
        mockUser.setEnabled(false);
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        List<UserResponseDTO> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("testUser", users.get(0).getUsername());
    }

    @Test
    void testFindById_UserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        Optional<ApplicationUser> user = userService.findById(1L);

        assertTrue(user.isPresent());
        assertEquals("testUser", user.get().getUsername());
    }

    @Test
    void testFindById_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<ApplicationUser> user = userService.findById(2L);

        assertFalse(user.isPresent());
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        ApplicationUser savedUser = userService.saveUser(mockUser);

        assertNotNull(savedUser);
        assertEquals("testUser", savedUser.getUsername());
    }

    @Test
    void testDeleteUserById() {
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUserById(1L));

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEnableAccount_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(ApplicationUser.class))).thenReturn(mockUser);

        boolean result = userService.enableAccount("test@example.com");

        assertTrue(result);
        assertTrue(mockUser.isEnabled());
    }

    @Test
    void testEnableAccount_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.enableAccount("notfound@example.com"));
    }

    @Test
    void testDisableAccount_Success() {
        mockUser.setEnabled(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(ApplicationUser.class))).thenReturn(mockUser);

        boolean result = userService.disableAccount("test@example.com");

        assertTrue(result);
        assertFalse(mockUser.isEnabled());
    }
}
