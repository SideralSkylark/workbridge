package com.workbridge.workbridge_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .toList();
    }

    public List<UserResponseDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
            .map(this::convertToDTO)
            .toList();
    }

    public Optional<ApplicationUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<ApplicationUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<ApplicationUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public ApplicationUser saveUser(ApplicationUser user) {
        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public boolean enableAccount(String email) {
        return updateAccountStatus(email, true);
    }

    @Transactional
    public boolean disableAccount(String email) {
        return updateAccountStatus(email, false);
    }

    private boolean updateAccountStatus(String email, boolean enable) {
        ApplicationUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("No user found with email: " + email));

        if (user.isEnabled() == enable) {
            return false;
        }

        user.setEnabled(enable);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    private UserResponseDTO convertToDTO(ApplicationUser user) {
        return new UserResponseDTO(
            user.getId(), user.getUsername(), user.getEmail(), user.getRole().toString(), user.isEnabled()
        );
    }
}
