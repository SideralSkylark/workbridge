package com.workbridge.workbridge_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.dto.ProviderRequestDTO;
import com.workbridge.workbridge_app.dto.UserResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.ProviderRequest;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.entity.UserRoleEntity;
import com.workbridge.workbridge_app.exception.ProviderRequestNotFoundException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.repository.ProviderRequestRepository;
import com.workbridge.workbridge_app.repository.UserRepository;
import com.workbridge.workbridge_app.repository.UserRoleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ProviderRequestRepository providerRequestRepository;

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .toList();
    }

    //TODO: implement tests for this service
    public List<UserResponseDTO> getAllNonAdminUsers() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream().noneMatch(role -> role.getRole() == UserRole.ADMIN))
            .map(this::convertToDTO)
            .toList();
    }    

    public List<UserResponseDTO> getUsersByRole(UserRole role) {
        return userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream().anyMatch(r -> r.getRole() == role))
            .map(this::convertToDTO)
            .toList();
    }

    @Transactional
    public List<ProviderRequestDTO> getAllProviderRequestNotApproved() {
        return providerRequestRepository.findAll().stream()
            .filter(providerRequest -> !providerRequest.isApproved())  
            .map(this::convertToProviderRequestDTO)
            .collect(Collectors.toList());
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

    public ApplicationUser updateUser(String username, UserResponseDTO userResponseDTO) {
        ApplicationUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setUsername(userResponseDTO.getUsername());
        user.setEmail(userResponseDTO.getEmail());
        user.setEnabled(userResponseDTO.isEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public void deleteByUsername(String username) {
        ApplicationUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
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

    public void requestToBecomeProvider(String username) {
        ApplicationUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("No user found with email: " + username));
        
        if (user.hasRole(UserRole.SERVICE_PROVIDER)) {
            throw new IllegalStateException("User is already a service provider.");
        }

        ProviderRequest providerRequest = new ProviderRequest(user);
        providerRequestRepository.save(providerRequest);

        //TODO: notify admin
    }

    @Transactional
    public void approveProviderRequest(Long requestId) {
        ProviderRequest providerRequest = providerRequestRepository.findById(requestId)
            .orElseThrow(() -> new ProviderRequestNotFoundException("No provider request found with id: " + requestId));

        if (providerRequest.isApproved()) {
            throw new IllegalStateException("Request already approved.");
        }

        providerRequest.setApproved(true);
        providerRequest.setApprovedOn(LocalDateTime.now());

        ApplicationUser user = providerRequest.getUser();

        // ✅ Fetch managed, existing role
        UserRoleEntity serviceProviderRole = userRoleRepository.findByRole(UserRole.SERVICE_PROVIDER)
            .orElseThrow(() -> new IllegalStateException("SERVICE_PROVIDER role not found in DB"));

        user.addRole(serviceProviderRole);
        userRepository.save(user);
        //TODO: notify user
        providerRequestRepository.save(providerRequest);
    }

    public boolean hasPendingProviderRequest(String username) {
        ApplicationUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("No user found with email: " + username));
        return providerRequestRepository.existsByUserAndApprovedFalse(user);
    }
    
    public boolean isServiceProvider(String username) {
        ApplicationUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("No user found with email: " + username));
        return user.hasRole(UserRole.SERVICE_PROVIDER);
    }
    

    public UserResponseDTO convertToDTO(ApplicationUser user) {
        Set<String> roleNames = user.getRoles().stream()
            .map(role -> role.getRole().name())
            .collect(Collectors.toSet());

        return new UserResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            roleNames,
            user.isEnabled()
        );
    }

    public ProviderRequestDTO convertToProviderRequestDTO(ProviderRequest providerRequest) {
        ApplicationUser user = providerRequest.getUser();
        return new ProviderRequestDTO(
            providerRequest.getId(),
            user.getUsername(),
            user.getEmail(),
            providerRequest.getRequestedOn(),
            providerRequest.isApproved(),
            providerRequest.getApprovedOn()
        );
    }
}
