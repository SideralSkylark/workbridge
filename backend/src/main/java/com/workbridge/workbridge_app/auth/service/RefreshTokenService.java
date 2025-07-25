package com.workbridge.workbridge_app.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String ERR_TOKEN_NOT_FOUND = "Refresh token not found";
    private static final String ERR_TOKEN_EXPIRED_OR_REVOKED = "Refresh token expired or revoked";

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${auth.refresh-token-validity-days:7}")
    private int refreshTokenValidityDays;


    @Transactional
    public RefreshToken createRefreshToken(ApplicationUser user, HttpServletRequest request) {
        RefreshToken token = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .ip(request.getRemoteAddr())
            .userAgent(request.getHeader(USER_AGENT_HEADER))
            .expiresAt(LocalDateTime.now().plusDays(refreshTokenValidityDays))
            .revoked(false)
            .user(user)
            .build();

        return refreshTokenRepository.save(token);
    }

    /**
     * Checks if the refresh token is valid (exists, not expired, not revoked).
     */
    public boolean isTokenValid(String token) {
        return refreshTokenRepository.findByToken(token)
            .map(this::isTokenUsable)
            .orElse(false);
    }

    /**
     * Retrieves the user from a valid refresh token.
     * Throws exception if token is invalid.
     */
    public ApplicationUser getUserFromToken(String token) {
        RefreshToken refreshToken = getValidTokenOrThrow(token);
        return refreshToken.getUser();
    }

    /**
     * Rotates a refresh token: revoke the old one and create a new one.
     */
    @Transactional
    public String rotateRefreshToken(String oldToken, HttpServletRequest request) {
        if (oldToken == null || oldToken.isBlank()) {
            throw new InvalidTokenException("Missing refresh token.");
        }

        RefreshToken existing = getValidTokenOrThrow(oldToken);
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return createRefreshToken(existing.getUser(), request).getToken();
    }

    @Transactional
    public void revokeAllTokensForUser(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public Optional<RefreshToken> findByTokenId(Long tokenId) {
        return refreshTokenRepository.findById(tokenId);
    }

    public Page<RefreshToken> findAllByUserId(long userId, Pageable pageable) {
        return refreshTokenRepository.findAllByUserId(pageable, userId);
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    private RefreshToken getValidTokenOrThrow(String token) {
        return refreshTokenRepository.findByToken(token)
            .filter(this::isTokenUsable)
            .orElseThrow(() -> new InvalidTokenException(
                isTokenExpired(token) ? ERR_TOKEN_EXPIRED_OR_REVOKED : ERR_TOKEN_NOT_FOUND
            ));
    }

    private boolean isTokenUsable(RefreshToken token) {
        return !token.isRevoked() && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private boolean isTokenExpired(String token) {
        return refreshTokenRepository.findByToken(token)
            .map(t -> t.getExpiresAt().isBefore(LocalDateTime.now()))
            .orElse(false);
    }
}
