package com.workbridge.workbridge_app.auth.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.auth.repository.VerificationTokenRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenCleanupService {
    private final int ONE_HOUR_MS = 3600000; // 1 hour in milliseconds
    // private final int SEVEN_DAYS_MS = 1;

    private final VerificationTokenRepository tokenRepository;

    public TokenCleanupService(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Periodically deletes expired verification tokens from the database.
     * Runs every hour by default.
     */
    @Transactional
    @Scheduled(fixedRate = ONE_HOUR_MS)
    public void cleanExpiredVerificationTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Expired verification tokens have been cleaned up.");
    }

    // Future enhancement:
    // @Transactional
    // @Scheduled(fixedRate = Duration.ofDays(7).toMillis())
    // public void cleanExpiredRefreshTokens() {
    //     // TODO: Implement deletion logic for expired refresh tokens
    // }
}

