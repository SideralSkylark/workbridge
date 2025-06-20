package com.workbridge.workbridge_app.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workbridge.workbridge_app.auth.entity.VerificationToken;
import com.workbridge.workbridge_app.auth.exception.TokenExpiredException;
import com.workbridge.workbridge_app.auth.exception.TokenVerificationException;
import com.workbridge.workbridge_app.auth.repository.VerificationTokenRepository;
import com.workbridge.workbridge_app.common.service.EmailService;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing email verification tokens during user registration.
 * <p>
 * This includes:
 * <ul>
 *   <li>Generating and sending one-time codes</li>
 *   <li>Persisting and verifying token validity</li>
 *   <li>Deleting old or redundant tokens</li>
 * </ul>
 *
 * Works in conjunction with {@link EmailService} to deliver codes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 10;

    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    
    /**
     * Creates a new verification token for a user and sends the code by email.
     * <p>
     * If a token already exists for the user's email, it is deleted before generating a new one.
     *
     * @param user the {@link ApplicationUser} for whom the token should be created
     */
    @Transactional
    public void createAndSendVerificationToken(ApplicationUser user) {
        final String email = user.getEmail();

        log.debug("Creating new verification token for {}", email);

        // Ensure only one token per user
        verificationTokenRepository.deleteByEmail(email);
        log.debug("Deleted any existing verification token for {}", email);

        String code = generateVerificationCode();

        VerificationToken token = new VerificationToken(
            email,
            code,
            LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES)
        );

        verificationTokenRepository.save(token);
        emailService.sendVerificationCode(email, code);

        log.info("Verification code sent to {}", email);
    }
    
    /**
     * Verifies the email ownership by validating the given code.
     * <p>
     * If the code is correct and not expired, the token is marked as verified.
     *
     * @param email the user's email address
     * @param code the verification code provided by the user
     * @throws TokenVerificationException if the code is missing or incorrect
     * @throws TokenExpiredException if the token has expired
     */
    @Transactional
    public void verifyToken(String email, String code) {
        log.debug("Attempting to verify token for {}", email);

        VerificationToken token = verificationTokenRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Verification token not found for {}", email);
                return new TokenVerificationException("Verification code not found");
            });

        if (token.isVerified()) {
            log.info("Token already verified for {}", email);
            return; 
        }

        if (!token.getCode().equals(code)) {
            log.warn("Invalid verification code provided for {}", email);
            throw new TokenVerificationException("Invalid verification code");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Verification code for {} has expired", email);
            throw new TokenExpiredException("Verification code has expired");
        }

        token.setVerified(true);
        verificationTokenRepository.save(token);

        log.info("Email successfully verified: {}", email);
    }
    
    /**
     * Deletes any existing verification token for the specified email address.
     * <p>
     * Useful when resending verification codes or cleaning up.
     *
     * @param email the email address whose token should be deleted
     */
    @Transactional
    public void deleteExistingToken(String email) {
        log.debug("Deleting verification token for {}", email);
        verificationTokenRepository.deleteByEmail(email);
        log.info("Deleted verification token for {}", email);
    }
    
    /**
     * Generates a numeric verification code of fixed length.
     * <p>
     * The code is left-padded with zeroes if necessary.
     *
     * @return a 6-digit (default) string code
     */
    private String generateVerificationCode() {
        int max = (int) Math.pow(10, VERIFICATION_CODE_LENGTH);
        return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", SECURE_RANDOM.nextInt(max));
    }
} 