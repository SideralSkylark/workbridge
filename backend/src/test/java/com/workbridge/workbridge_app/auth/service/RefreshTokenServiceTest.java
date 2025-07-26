package com.workbridge.workbridge_app.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workbridge.workbridge_app.auth.entity.RefreshToken;
import com.workbridge.workbridge_app.auth.exception.InvalidTokenException;
import com.workbridge.workbridge_app.auth.repository.RefreshTokenRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private ApplicationUser user;

    @BeforeEach
    void setup() {
        user = ApplicationUser.builder().id(1L).email("test@domain.com").build();
        // Set token validity days manually
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);
        refreshTokenService.setTokenValidity(7);
    }

    @Test
    void createRefreshToken_ShouldPersistTokenWithCorrectAttributes() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        refreshTokenService.createRefreshToken(user, request);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertEquals("127.0.0.1", saved.getIp());
        assertEquals("JUnit", saved.getUserAgent());
        assertEquals(user, saved.getUser());
        assertFalse(saved.isRevoked());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsActive() {
        RefreshToken token = RefreshToken.builder()
            .revoked(false)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();

        when(refreshTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertTrue(refreshTokenService.isTokenValid("token123"));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsRevoked() {
        RefreshToken token = RefreshToken.builder()
            .revoked(true)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();

        when(refreshTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertFalse(refreshTokenService.isTokenValid("token123"));
    }

    @Test
    void getUserFromToken_ShouldReturnUser_WhenTokenIsValid() {
        RefreshToken token = RefreshToken.builder()
            .revoked(false)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .user(user)
            .build();

        when(refreshTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        assertEquals(user, refreshTokenService.getUserFromToken("token123"));
    }

    @Test
    void getUserFromToken_ShouldThrow_WhenTokenIsInvalid() {
        when(refreshTokenRepository.findByToken("invalid"))
            .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () ->
            refreshTokenService.getUserFromToken("invalid")
        );
    }

    @Test
    void rotateRefreshToken_ShouldRevokeOldAndCreateNewToken() {
        RefreshToken oldToken = RefreshToken.builder()
            .revoked(false)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .user(user)
            .build();

        when(refreshTokenRepository.findByToken("oldToken")).thenReturn(Optional.of(oldToken));
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String newToken = refreshTokenService.rotateRefreshToken("oldToken", request);

        assertNotNull(newToken);
        assertTrue(oldToken.isRevoked());
    }

    @Test
    void rotateRefreshToken_ShouldThrow_WhenTokenIsMissing() {
        assertThrows(InvalidTokenException.class, () ->
            refreshTokenService.rotateRefreshToken("", request)
        );
    }

    @Test
    void revokeAllTokensForUser_ShouldCallDeleteByUserId() {
        refreshTokenService.revokeAllTokensForUser(1L);
        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    void deleteByToken_ShouldCallRepositoryDeleteByToken() {
        refreshTokenService.deleteByToken("token123");
        verify(refreshTokenRepository).deleteByToken("token123");
    }
}

