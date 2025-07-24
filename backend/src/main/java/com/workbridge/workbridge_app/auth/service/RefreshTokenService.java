package com.workbridge.workbridge_app.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.auth.entity.RefreshToken;
import com.workbridge.workbridge_app.auth.exception.InvalidTokenException;
import com.workbridge.workbridge_app.auth.repository.RefreshTokenRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    private final int refreshTokenValidityDays = 7;

    @Transactional
    public RefreshToken createRefreshToken(ApplicationUser user) {
        RefreshToken token = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().plusDays(refreshTokenValidityDays))
            .revoked(false)
            .user(user)
            .build();

        return refreshTokenRepository.save(token);
    }

    /**
     * Checks if the refresh token is valid (exists, not expired, not revoked).
     */
    public boolean validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
            .filter(t -> !t.isRevoked() && t.getExpiresAt().isAfter(LocalDateTime.now()))
            .isPresent();
    }

    /**
     * Retrieves the user from a valid refresh token.
     * Throws exception if token is invalid.
     */
    public ApplicationUser getUserFromToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }

        return refreshToken.getUser();
    }

    /**
     * Rotates a refresh token: revoke the old one and create a new one.
     */
    @Transactional
    public String rotateRefreshToken(String oldToken) {
        RefreshToken existing = refreshTokenRepository.findByToken(oldToken)
            .orElseThrow(() -> new InvalidTokenException("Old refresh token not found"));

        if (existing.isRevoked() || existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Old refresh token expired or revoked");
        }

        // Revoke old
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        // Create and return new token
        RefreshToken newToken = createRefreshToken(existing.getUser());
        return newToken.getToken();
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void revokeAllTokensForUser(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
