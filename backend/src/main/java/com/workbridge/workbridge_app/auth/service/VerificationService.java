package com.workbridge.workbridge_app.auth.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.auth.entity.VerificationToken;
import com.workbridge.workbridge_app.auth.exception.TokenExpiredException;
import com.workbridge.workbridge_app.auth.exception.TokenVerificationException;
import com.workbridge.workbridge_app.auth.repository.VerificationTokenRepository;
import com.workbridge.workbridge_app.common.service.EmailService;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;

import lombok.RequiredArgsConstructor;

/**
 * Service responsible for managing email verification tokens and codes.
 * This service handles the creation, verification, and management of email verification tokens
 * used in the user registration process. It works in conjunction with the EmailService
 * to send verification codes to users.
 */
@Service
@RequiredArgsConstructor
public class VerificationService {
    
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 10;
    
    /**
     * Creates a new verification token and sends it to the user's email.
     * This method:
     * 1. Generates a random verification code
     * 2. Creates a new verification token with expiration time
     * 3. Saves the token to the database
     * 4. Sends the verification code via email
     *
     * @param user The user to create and send the verification token for
     */
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
    
    /**
     * Verifies a user's email using the provided verification code.
     * This method:
     * 1. Retrieves the verification token for the email
     * 2. Validates the verification code
     * 3. Checks if the token has expired
     * 4. Marks the token as verified if all checks pass
     *
     * @param email The email address to verify
     * @param code The verification code to validate
     * @throws TokenVerificationException if verification code is invalid or not found
     * @throws TokenExpiredException if verification code has expired
     */
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
    
    /**
     * Deletes any existing verification token for the given email.
     * This is typically used when resending verification codes to ensure
     * only one active token exists per email.
     *
     * @param email The email address whose verification token should be deleted
     */
    @Transactional
    public void deleteExistingToken(String email) {
        verificationTokenRepository.deleteByEmail(email);
    }
    
    /**
     * Generates a random verification code of specified length.
     * The code is a numeric string padded with leading zeros if necessary.
     *
     * @return A string containing the generated verification code
     */
    private String generateVerificationCode() {
        return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", 
            new Random().nextInt((int) Math.pow(10, VERIFICATION_CODE_LENGTH)));
    }
} 