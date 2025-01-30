package com.workbridge.workbridge_app.controller;

import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Endpoint para obter detalhes do usuário logado
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUserDetails() {
        try {
            // Obtém o username do usuário autenticado
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // Busca o ApplicationUser com base no username
            ApplicationUser user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Converte ApplicationUser para UserResponseDTO
            UserResponseDTO userResponseDTO = userService.convertToDTO(user);
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint para atualizar os dados do usuário logado
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateUserDetails(@RequestBody UserResponseDTO userResponseDTO) {
        try {
            // Obtém o username do usuário autenticado
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            // Atualiza os dados do usuário com base no username e no DTO
            ApplicationUser user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Atualiza os campos do ApplicationUser
            user.setUsername(userResponseDTO.getUsername());
            user.setEmail(userResponseDTO.getEmail());
            user.setEnabled(userResponseDTO.isEnabled());

            // Salva a atualização
            userService.saveUser(user);

            // Converte o ApplicationUser atualizado para UserResponseDTO
            UserResponseDTO updatedUserResponseDTO = userService.convertToDTO(user);
            return ResponseEntity.ok(updatedUserResponseDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint para excluir o usuário logado
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser() {
        try {
            // Obtém o username do usuário autenticado
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            // Busca o ApplicationUser com base no username
            ApplicationUser user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Exclui o usuário
            userService.deleteUserById(user.getId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
