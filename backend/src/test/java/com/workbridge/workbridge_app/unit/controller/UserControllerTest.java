package com.workbridge.workbridge_app.unit.controller;

import com.workbridge.workbridge_app.controller.UserController;
import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.ServiceSeeker;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private void mockAuthentication(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private ServiceSeeker createTestUser() {
        ServiceSeeker user = new ServiceSeeker();
        user.setId(1L);
        user.setUsername("usuarioTeste");
        user.setEmail("usuario@teste.com");
        user.setRole(UserRole.SERVICE_SEEKER);
        user.setEnabled(true);
        return user;
    }

    @Test
    void testGetUserDetails_Success() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        ServiceSeeker user = createTestUser();
        UserResponseDTO userResponseDTO = new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole().toString(), user.isEnabled());

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(userService.convertToDTO(user)).thenReturn(userResponseDTO);

        ResponseEntity<?> response = userController.getUserDetails();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userResponseDTO, response.getBody());
    }

    @Test
    void testGetUserDetails_UserNotFound() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        when(userService.findByUsername(username)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserDetails();

        assertEquals(404, response.getStatusCode().value());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testUpdateUserDetails_Success() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        ServiceSeeker user = createTestUser();
        UserResponseDTO userUpdateDTO = new UserResponseDTO(user.getId(), "novoUsername", "novoEmail@teste.com", "SERVICE_SEEKER", true);
        when(userService.updateUser(username, userUpdateDTO)).thenReturn(user);
        when(userService.convertToDTO(user)).thenReturn(userUpdateDTO);

        ResponseEntity<?> response = userController.updateUserDetails(userUpdateDTO);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(userUpdateDTO, response.getBody());
    }

    @Test
    void testUpdateUserDetails_UserNotFound() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        UserResponseDTO userUpdateDTO = new UserResponseDTO(1L, "novoUsername", "novoEmail@teste.com", "SERVICE_SEEKER", true);
        when(userService.updateUser(username, userUpdateDTO)).thenThrow(new UserNotFoundException("User not found"));

        ResponseEntity<?> response = userController.updateUserDetails(userUpdateDTO);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testDeleteUser_Success() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        doNothing().when(userService).deleteByUsername(username);

        ResponseEntity<?> response = userController.deleteUser();

        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void testDeleteUser_UserNotFound() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        doThrow(new UserNotFoundException("User not found")).when(userService).deleteByUsername(username);

        ResponseEntity<?> response = userController.deleteUser();

        assertEquals(404, response.getStatusCode().value());
        assertEquals("User not found", response.getBody());
    }
}