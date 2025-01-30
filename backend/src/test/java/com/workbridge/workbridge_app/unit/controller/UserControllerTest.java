package com.workbridge.workbridge_app.unit.controller;

import com.workbridge.workbridge_app.controller.UserController;
import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.ServiceSeeker;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class) // Anotação para inicializar o Mockito
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;


//     @Test
// void testFindByUsername() {
//     // Teste para garantir que findByUsername está funcionando corretamente
//     String username = "usuarioTeste";
//     ServiceSeeker user = new ServiceSeeker();
//     user.setId(1L);
//     user.setUsername("usuarioTeste");
//     user.setEmail("usuario@teste.com");
//     user.setRole(UserRole.SERVICE_SEEKER);
//     user.setEnabled(true);

//     when(userService.findByUsername(username)).thenReturn(Optional.of(user));

//     Optional<ApplicationUser> foundUser = userService.findByUsername(username);
//     assertTrue(foundUser.isPresent());
//     assertEquals(user.getUsername(), foundUser.get().getUsername());
// }


// @Test
// void testConvertToDTO() {
//     // Teste para garantir que convertToDTO está funcionando corretamente
//     ServiceSeeker user = new ServiceSeeker();
//     user.setId(1L);
//     user.setUsername("usuarioTeste");
//     user.setEmail("usuario@teste.com");
//     user.setRole(UserRole.SERVICE_SEEKER);
//     user.setEnabled(true);

//     UserResponseDTO userResponseDTO = new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(),
//             user.getRole().toString(), user.isEnabled());

//     when(userService.convertToDTO(user)).thenReturn(userResponseDTO);

//     UserResponseDTO dto = userService.convertToDTO(user);
//     assertNotNull(dto);
//     assertEquals(user.getUsername(), dto.getUsername());
// }

@Test
void testGetUserDetails() {
    // Configuração do mock de autenticação
    String username = "usuarioTeste";

    // Criando uma instância concreta de ApplicationUser (por exemplo, ServiceSeeker)
    ServiceSeeker user = new ServiceSeeker();
    user.setId(1L);
    user.setUsername("usuarioTeste");
    user.setEmail("usuario@teste.com");
    user.setRole(UserRole.SERVICE_SEEKER);
    user.setEnabled(true);

    // Mocking SecurityContextHolder
    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.getName()).thenReturn(username);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Verifique se a autenticação foi configurada corretamente
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());

    // Mocking o serviço para retornar um Optional<ApplicationUser>
    when(userService.findByUsername(username)).thenAnswer(invocation -> {
        System.out.println("Calling findByUsername for username: " + username); // Log de depuração
        Optional<ApplicationUser> userOptional = Optional.of(user);
        if (userOptional.isPresent()) {
            System.out.println("User found: " + userOptional.get().getUsername()); // Log de depuração
        } else {
            System.out.println("User not found"); // Log de depuração
        }
        return userOptional;
    });

    // Mocking a conversão do usuário para UserResponseDTO
    UserResponseDTO userResponseDTO = new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(),
            user.getRole().toString(), user.isEnabled());
    when(userService.convertToDTO(user)).thenReturn(userResponseDTO);

    // Chamando o endpoint
    System.out.println("Calling getUserDetails endpoint..."); // Log de depuração
    ResponseEntity<UserResponseDTO> response = userController.getUserDetails();

    // Verificando a resposta
    assertNotNull(response); // Verifique se o response não é null
    System.out.println("Response received: " + response); // Log de depuração
    assertEquals(200, response.getStatusCodeValue()); // Verifique se o status é 200 OK

    assertNotNull(response.getBody()); // Verifique se o body não é null
    System.out.println("Response body: " + response.getBody()); // Log de depuração
    assertEquals(user.getId(), response.getBody().getId());
    assertEquals(user.getUsername(), response.getBody().getUsername());
    assertEquals(user.getEmail(), response.getBody().getEmail());
    assertEquals(user.getRole().toString(), response.getBody().getRole());
    assertEquals(user.isEnabled(), response.getBody().isEnabled());
}

    

    @Test
    void testUpdateUserDetails() {
        // Configuração do mock de autenticação
        String username = "usuarioTeste";

        // Criando uma instância concreta de ApplicationUser (por exemplo,
        // ServiceSeeker)
        ServiceSeeker user = new ServiceSeeker();
        user.setId(1L);
        user.setUsername("usuarioTeste");
        user.setEmail("usuario@teste.com");
        user.setRole(UserRole.SERVICE_SEEKER);
        user.setEnabled(true);

        // Criando o UserResponseDTO com os dados atualizados
        UserResponseDTO userUpdateDTO = new UserResponseDTO(1L, "novoUsername", "novoEmail@teste.com", "SERVICE_SEEKER",
                true);

        // Mocking SecurityContextHolder
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mocking o serviço para retornar o usuário
        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // Mocking o serviço para salvar o usuário atualizado
        when(userService.saveUser(any(ApplicationUser.class))).thenReturn(user);

        // Mocking o serviço para converter o usuário para DTO
        UserResponseDTO updatedUserResponseDTO = new UserResponseDTO(1L, "novoUsername", "novoEmail@teste.com",
                "SERVICE_SEEKER", true);
        when(userService.convertToDTO(user)).thenReturn(updatedUserResponseDTO);

        // Chamando o endpoint
        ResponseEntity<UserResponseDTO> response = userController.updateUserDetails(userUpdateDTO);

        // Verificando a resposta
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updatedUserResponseDTO, response.getBody());

        // Verificando que o método saveUser foi chamado
        verify(userService, times(1)).saveUser(any(ApplicationUser.class));
    }

    @Test
    void testDeleteUser() {
        // Configuração do mock de autenticação
        String username = "usuarioTeste";

        // Criando uma instância concreta de ApplicationUser (por exemplo,
        // ServiceSeeker)
        ServiceSeeker user = new ServiceSeeker();
        user.setId(1L);
        user.setUsername("usuarioTeste");
        user.setEmail("usuario@teste.com");
        user.setRole(UserRole.SERVICE_SEEKER);
        user.setEnabled(true);

        // Mocking SecurityContextHolder
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mocking o serviço para retornar o usuário
        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // Mocking o serviço para deletar o usuário
        doNothing().when(userService).deleteUserById(user.getId());

        // Chamando o endpoint
        ResponseEntity<Void> response = userController.deleteUser();

        // Verificando a resposta
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue()); // Código 204 (No Content) indicando que a exclusão foi
                                                          // bem-sucedida

        // Verificando se o método deleteUserById foi chamado
        verify(userService, times(1)).deleteUserById(user.getId());
    }
}
