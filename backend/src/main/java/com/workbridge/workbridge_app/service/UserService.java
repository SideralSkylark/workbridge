package com.workbridge.workbridge_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Find all users
    public List<ApplicationUser> findAllUsers() {
        return userRepository.findAll();
    }

    // Find user by ID
    public Optional<ApplicationUser> findById(Long id) {
        return userRepository.findById(id);
    }

    // Find user by username
    public Optional<ApplicationUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Find user by email
    public Optional<ApplicationUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Save user
    public ApplicationUser saveUser(ApplicationUser user) {
        return userRepository.save(user);
    }

    // Delete user by ID
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}