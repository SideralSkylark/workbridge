package com.workbridge.workbridge_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.workbridge.workbridge_app.entity.ApplicationUser;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, Long> {

    Optional<ApplicationUser> findByUsername(String username);

    Optional<ApplicationUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}