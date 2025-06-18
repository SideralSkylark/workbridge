package com.workbridge.workbridge_app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.user.entity.UserRole;
import com.workbridge.workbridge_app.user.entity.UserRoleEntity;
import com.workbridge.workbridge_app.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final UserRoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (UserRole role : UserRole.values()) {
            roleRepository.findByRole(role).orElseGet(() -> {
                UserRoleEntity newRole = new UserRoleEntity();
                newRole.setRole(role);
                return roleRepository.save(newRole);
            });
        }
    }
}
