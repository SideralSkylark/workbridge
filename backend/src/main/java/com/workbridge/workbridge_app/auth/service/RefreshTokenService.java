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

/**
 * Service responsible for managing refresh tokens, including:
 * <ul>
 *   <li>Creating new refresh tokens during login or token rotation</li>
 *   <li>Validating token existence, expiration, and revocation state</li>
 *   <li>Rotating tokens securely</li>
 *   <li>Revoking or deleting tokens</li>
 *   <li>Querying token-related data (by token, ID, or user)</li>
 * </ul>
 *
 * <p>Refresh tokens are persisted in the database and associated with:
 * <ul>
 *   <li>User ID</li>
 *   <li>Client IP address</li>
 *   <li>User agent (browser/device info)</li>
 *   <li>Expiration date</li>
 *   <li>Revocation status</li>
 * </ul>
 *
 * <p>Default expiration is configurable via the property {@code auth.refresh-token-validity-days} (default = 7).</p>
 *
 * @author WorkBridge
 * @since 2025-06-22
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String ERR_TOKEN_NOT_FOUND = "Refresh token not found";
    private static final String ERR_TOKEN_EXPIRED_OR_REVOKED = "Refresh token expired or revoked";

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${auth.refresh-token-validity-days:7}")
    private int refreshTokenValidityDays;

    /**
     * Creates a new refresh token associated with a user and request metadata.
     *
     * <p>The token includes client IP and user agent, and is set to expire based on
     * the configured validity period.</p>
     *
     * @param user    The authenticated user
     * @param request The HTTP request (to extract IP and user-agent)
     * @return Persisted {@link RefreshToken} entity
     */
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
     * Validates whether the given refresh token is active (exists, not revoked, and not expired).
     *
     * @param token The token string
     * @return {@code true} if the token is valid and usable; {@code false} otherwise
     */
    public boolean isTokenValid(String token) {
        return refreshTokenRepository.findByToken(token)
            .map(this::isTokenUsable)
            .orElse(false);
    }

    /**
     * Retrieves the user associated with a valid refresh token.
     *
     * @param token The token string
     * @return The {@link ApplicationUser} who owns the token
     * @throws InvalidTokenException if the token is not found or is revoked/expired
     */
    public ApplicationUser getUserFromToken(String token) {
        RefreshToken refreshToken = getValidTokenOrThrow(token);
        return refreshToken.getUser();
    }

    /**
     * Rotates a refresh token by revoking the existing one and issuing a new one.
     *
     * <p>This is typically done after successful access token renewal.</p>
     *
     * @param oldToken The current token string to be revoked
     * @param request  The HTTP request for capturing new IP/user-agent
     * @return New refresh token string
     * @throws InvalidTokenException if the old token is missing or invalid
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

    /**
     * Revokes all refresh tokens associated with a user by deleting them.
     *
     * @param userId ID of the user
     */
    @Transactional
    public void revokeAllTokensForUser(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * Finds a refresh token by its token string.
     *
     * @param token The token string
     * @return Optional containing the token if found
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Finds a refresh token by its database ID.
     *
     * @param tokenId The token's ID
     * @return Optional containing the token if found
     */
    public Optional<RefreshToken> findByTokenId(Long tokenId) {
        return refreshTokenRepository.findById(tokenId);
    }

    /**
     * Retrieves a paginated list of refresh tokens associated with a user.
     *
     * @param userId   User's ID
     * @param pageable Pagination and sorting parameters
     * @return Page of {@link RefreshToken}
     */
    public Page<RefreshToken> findAllByUserId(long userId, Pageable pageable) {
        return refreshTokenRepository.findAllByUserId(pageable, userId);
    }

    /**
     * Deletes a refresh token by its token string.
     *
     * @param token Token string to delete
     */
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    /**
     * Method used to set life expectancy of a token, this method is used to setup unit tests
     *
     * @param validityInDays expects the number of days a token should remain active
     */
    public void setTokenValidity(int validityInDays) {
        this.refreshTokenValidityDays = validityInDays;
    }

    /**
     * Retrieves a valid token or throws an exception if it's missing, expired, or revoked.
     *
     * @param token The token string
     * @return Valid {@link RefreshToken}
     * @throws InvalidTokenException if invalid
     */
    private RefreshToken getValidTokenOrThrow(String token) {
        return refreshTokenRepository.findByToken(token)
            .filter(this::isTokenUsable)
            .orElseThrow(() -> new InvalidTokenException(
                isTokenExpired(token) ? ERR_TOKEN_EXPIRED_OR_REVOKED : ERR_TOKEN_NOT_FOUND
            ));
    }

    /**
     * Checks if a token is usable (not revoked and not expired).
     *
     * @param token RefreshToken to validate
     * @return {@code true} if usable
     */
    private boolean isTokenUsable(RefreshToken token) {
        return !token.isRevoked() && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    /**
     * Checks whether a token has expired.
     *
     * @param token Token string
     * @return {@code true} if expired; {@code false} otherwise
     */
    private boolean isTokenExpired(String token) {
        return refreshTokenRepository.findByToken(token)
            .map(t -> t.getExpiresAt().isBefore(LocalDateTime.now()))
            .orElse(false);
    }
}
