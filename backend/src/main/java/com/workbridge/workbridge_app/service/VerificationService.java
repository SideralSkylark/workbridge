package com.workbridge.workbridge_app.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.VerificationToken;
import com.workbridge.workbridge_app.exception.TokenExpiredException;
import com.workbridge.workbridge_app.exception.TokenVerificationException;
import com.workbridge.workbridge_app.repository.VerificationTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {
    
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 10;
    
    @Transactional
    public void createAndSendVerificationToken(ApplicationUser user) {
        String code = generateVerificationCode();
        VerificationToken verificationToken = new VerificationToken(
            user.getEmail(),
            code,
            LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES)
        );
        
        verificationTokenRepository.save(verificationToken);
        emailService.sendVerificationCode(user.getEmail(), code);
    }
    
    @Transactional
    public void verifyToken(String email, String code) {
        VerificationToken token = verificationTokenRepository.findByEmail(email)
                .orElseThrow(() -> new TokenVerificationException("Verification code not found"));
                
        if (!token.getCode().equals(code)) {
            throw new TokenVerificationException("Invalid verification code");
        }
        
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification code has expired");
        }
        
        token.setVerified(true);
        verificationTokenRepository.save(token);
    }
    
    @Transactional
    public void deleteExistingToken(String email) {
        verificationTokenRepository.deleteByEmail(email);
    }
    
    private String generateVerificationCode() {
        return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", 
            new Random().nextInt((int) Math.pow(10, VERIFICATION_CODE_LENGTH)));
    }
} 