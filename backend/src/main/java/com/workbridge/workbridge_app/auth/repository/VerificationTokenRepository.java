package com.workbridge.workbridge_app.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.auth.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByEmail(String email);
    void deleteByEmail(String email);
    void deleteByExpiresAtBefore(LocalDateTime time);
}