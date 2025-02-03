package com.workbridge.workbridge_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.entity.UserRoleEntity;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
    Optional<UserRoleEntity> findByRole(UserRole role);
}
