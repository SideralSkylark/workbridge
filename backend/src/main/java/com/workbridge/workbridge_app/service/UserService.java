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

    /**
     * Retrieves all users from the database
     * @return List of UserResponseDTO objects
     */
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .toList();
    }

    /**
     * Retrieves all non-admin users from the database
     * @return List of UserResponseDTO objects
     */
    public List<UserResponseDTO> getAllNonAdminUsers() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream().noneMatch(role -> role.getRole() == UserRole.ADMIN))
            .map(this::convertToDTO)
            .toList();
    }    

    /**
     * Retrieves users by a specific role
     * @param role The role to filter by
     * @return List of UserResponseDTO objects
     */
    public List<UserResponseDTO> getUsersByRole(UserRole role) {
        return userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream().anyMatch(r -> r.getRole() == role))
            .map(this::convertToDTO)
            .toList();
    }

    /**
     * Retrieves all pending provider requests
     * @return List of ProviderRequestDTO objects
     */
    @Transactional
    public List<ProviderRequestDTO> getAllProviderRequestNotApproved() {
        return providerRequestRepository.findAll().stream()
            .filter(providerRequest -> !providerRequest.isApproved())  
            .map(this::convertToProviderRequestDTO)
            .collect(Collectors.toList());
    }       

    /**
     * Finds a user by ID
     * @param id The user ID
     * @return Optional containing the user if found
     */
    public Optional<ApplicationUser> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by username
     * @param username The username
     * @return Optional containing the user if found
     */
    public Optional<ApplicationUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user by email
     * @param email The email
     * @return Optional containing the user if found
     */
    public Optional<ApplicationUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Saves a user to the database
     * @param user The user to save
     * @return The saved user
     */
    public ApplicationUser saveUser(ApplicationUser user) {
        return userRepository.save(user);
    }

    /**
     * Updates a user's information
     * @param username The username of the user to update
     * @param userResponseDTO The updated user information
     * @return The updated user
     * @throws UserNotFoundException if the user is not found
     */
    @Transactional
    public ApplicationUser updateUser(String username, UserResponseDTO userResponseDTO) {
        ApplicationUser user = getUserByUsername(username);

        user.setUsername(userResponseDTO.getUsername());
        user.setEmail(userResponseDTO.getEmail());
        user.setEnabled(userResponseDTO.isEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Deletes a user by ID
     * @param id The user ID
     */
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Deletes a user by username
     * @param username The username
     * @throws UserNotFoundException if the user is not found
     */
    @Transactional
    public void deleteByUsername(String username) {
        ApplicationUser user = getUserByUsername(username);
        userRepository.delete(user);
    }

    /**
     * Enables a user account
     * @param email The user's email
     * @return true if the account was enabled, false if it was already enabled
     * @throws UserNotFoundException if the user is not found
     */
    @Transactional
    public boolean enableAccount(String email) {
        return updateAccountStatus(email, true);
    }

    /**
     * Disables a user account
     * @param email The user's email
     * @return true if the account was disabled, false if it was already disabled
     * @throws UserNotFoundException if the user is not found
     */
    @Transactional
    public boolean disableAccount(String email) {
        return updateAccountStatus(email, false);
    }

    /**
     * Updates a user's account status
     * @param email The user's email
     * @param enable Whether to enable or disable the account
     * @return true if the status was changed, false if it was already in the desired state
     * @throws UserNotFoundException if the user is not found
     */
    private boolean updateAccountStatus(String email, boolean enable) {
        ApplicationUser user = getUserByEmail(email);

        if (user.isEnabled() == enable) {
            return false;
        }

        user.setEnabled(enable);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    /**
     * Submits a request to become a service provider
     * @param username The username of the user requesting to become a provider
     * @throws UserNotFoundException if the user is not found
     * @throws IllegalStateException if the user is already a service provider
     */
    @Transactional
    public void requestToBecomeProvider(String username) {
        ApplicationUser user = getUserByUsername(username);
        
        if (user.hasRole(UserRole.SERVICE_PROVIDER)) {
            throw new IllegalStateException("User is already a service provider.");
        }

        ProviderRequest providerRequest = new ProviderRequest(user);
        providerRequestRepository.save(providerRequest);

        //TODO: notify admin
    }

    /**
     * Approves a provider request
     * @param requestId The ID of the request to approve
     * @throws ProviderRequestNotFoundException if the request is not found
     * @throws IllegalStateException if the request is already approved or the SERVICE_PROVIDER role is not found
     */
    @Transactional
    public void approveProviderRequest(Long requestId) {
        ProviderRequest providerRequest = getProviderRequestById(requestId);

        if (providerRequest.isApproved()) {
            throw new IllegalStateException("Request already approved.");
        }

        providerRequest.setApproved(true);
        providerRequest.setApprovedOn(LocalDateTime.now());

        ApplicationUser user = providerRequest.getUser();

        // Fetch managed, existing role
        UserRoleEntity serviceProviderRole = userRoleRepository.findByRole(UserRole.SERVICE_PROVIDER)
            .orElseThrow(() -> new IllegalStateException("SERVICE_PROVIDER role not found in DB"));

        user.addRole(serviceProviderRole);
        userRepository.save(user);
        //TODO: notify user
        providerRequestRepository.save(providerRequest);
    }

    /**
     * Checks if a user has a pending provider request
     * @param username The username
     * @return true if the user has a pending request, false otherwise
     * @throws UserNotFoundException if the user is not found
     */
    public boolean hasPendingProviderRequest(String username) {
        ApplicationUser user = getUserByUsername(username);
        return providerRequestRepository.existsByUserAndApprovedFalse(user);
    }
    
    /**
     * Checks if a user is a service provider
     * @param username The username
     * @return true if the user is a service provider, false otherwise
     * @throws UserNotFoundException if the user is not found
     */
    public boolean isServiceProvider(String username) {
        ApplicationUser user = getUserByUsername(username);
        return user.hasRole(UserRole.SERVICE_PROVIDER);
    }
    
    /**
     * Converts an ApplicationUser to a UserResponseDTO
     * @param user The user to convert
     * @return The converted DTO
     */
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

    /**
     * Converts a ProviderRequest to a ProviderRequestDTO
     * @param providerRequest The request to convert
     * @return The converted DTO
     */
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

    /**
     * Helper method to get a user by username or throw an exception
     * @param username The username
     * @return The user
     * @throws UserNotFoundException if the user is not found
     */
    private ApplicationUser getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("No user found with username: " + username));
    }

    /**
     * Helper method to get a user by email or throw an exception
     * @param email The email
     * @return The user
     * @throws UserNotFoundException if the user is not found
     */
    private ApplicationUser getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("No user found with email: " + email));
    }

    /**
     * Helper method to get a provider request by ID or throw an exception
     * @param requestId The request ID
     * @return The provider request
     * @throws ProviderRequestNotFoundException if the request is not found
     */
    private ProviderRequest getProviderRequestById(Long requestId) {
        return providerRequestRepository.findById(requestId)
            .orElseThrow(() -> new ProviderRequestNotFoundException("No provider request found with id: " + requestId));
    }
}
