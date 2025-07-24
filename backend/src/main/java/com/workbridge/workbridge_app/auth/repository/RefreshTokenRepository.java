package com.workbridge.workbridge_app.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
    void deleteByToken(String token);
    boolean existsByToken(String token);
}
