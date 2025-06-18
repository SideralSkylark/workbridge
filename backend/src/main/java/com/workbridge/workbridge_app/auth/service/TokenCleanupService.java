package com.workbridge.workbridge_app.auth.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.auth.repository.VerificationTokenRepository;

@Service
public class TokenCleanupService {

    private final VerificationTokenRepository tokenRepository;
    private final int ONE_HOUR = 3600000;

    public TokenCleanupService(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    @Scheduled(fixedRate = ONE_HOUR) 
    public void cleanExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        System.out.println("Expired tokens cleaned up.");
    }
}

