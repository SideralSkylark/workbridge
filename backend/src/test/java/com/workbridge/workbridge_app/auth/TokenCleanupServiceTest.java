package com.workbridge.workbridge_app.auth;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import com.workbridge.workbridge_app.auth.repository.VerificationTokenRepository;
import com.workbridge.workbridge_app.auth.service.TokenCleanupService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class TokenCleanupServiceTest {

    @Mock private VerificationTokenRepository tokenRepository;
    private TokenCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cleanupService = new TokenCleanupService(tokenRepository);
    }

    @Test
    void shouldDeleteExpiredTokensSuccessfully() {
        cleanupService.cleanExpiredVerificationTokens();
        verify(tokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}
