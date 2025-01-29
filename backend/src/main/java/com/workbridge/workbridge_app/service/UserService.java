package com.workbridge.workbridge_app.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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

    public List<ApplicationUser> findAllUsers() {
        return userRepository.findAll();
    }

    public List<ApplicationUser> findAllUsersByRole(UserRole role) {
        //TODO: implement a filter that returns a collection based on the role parameter
        List<ApplicationUser> output = new ArrayList<>();
        return output;
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
        Optional<ApplicationUser> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();

            if (user.isEnabled()) {
                return false;
            }
            user.setEnabled(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }

        throw new UserNotFoundException("No user found with email: " + email);
    }

    @Transactional
    public boolean disableAccount(String email) {
        Optional<ApplicationUser> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();

            if (!user.isEnabled()) {
                return false;
            }
            user.setEnabled(false);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }
        throw new UserNotFoundException("No user found with email: " + email);
    }

    //TODO: public method that returns custom userResponse object, insted of entity
    //TODO: public method that returns a collection of custom userResponse object, insted of entity
}