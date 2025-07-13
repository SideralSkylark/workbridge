package com.workbridge.workbridge_app.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import com.workbridge.workbridge_app.auth.entity.VerificationToken;
import com.workbridge.workbridge_app.auth.exception.TokenExpiredException;
import com.workbridge.workbridge_app.auth.exception.TokenVerificationException;
import com.workbridge.workbridge_app.auth.repository.VerificationTokenRepository;
import com.workbridge.workbridge_app.auth.service.VerificationService;
import com.workbridge.workbridge_app.common.service.EmailService;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

public class VerificationServiceTest {

    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @InjectMocks private VerificationService verificationService;

    private final String email = "user@example.com";
    private final String code = "123456";

    @Captor private ArgumentCaptor<VerificationToken> tokenCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private ApplicationUser buildUser() {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);
        return user;
    }

    @Test
    void shouldCreateAndSendVerificationTokenSuccessfully() {
        ApplicationUser user = buildUser();

        verificationService.createAndSendVerificationToken(user);

        verify(tokenRepository).deleteByEmail(email);
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendVerificationCode(eq(email), anyString());

        VerificationToken savedToken = tokenCaptor.getValue();
        assertEquals(email, savedToken.getEmail());
        assertFalse(savedToken.isVerified());
        assertNotNull(savedToken.getCode());
    }

    @Test
    void shouldVerifyValidTokenSuccessfully() {
        VerificationToken token = new VerificationToken(email, code, LocalDateTime.now().plusMinutes(5));
        when(tokenRepository.findByEmail(email)).thenReturn(Optional.of(token));

        verificationService.verifyToken(email, code);

        assertTrue(token.isVerified());
        verify(tokenRepository).save(token);
    }

    @Test
    void shouldSkipVerificationIfTokenAlreadyVerified() {
        VerificationToken token = new VerificationToken(email, code, LocalDateTime.now().plusMinutes(5));
        token.setVerified(true);

        when(tokenRepository.findByEmail(email)).thenReturn(Optional.of(token));

        // Should not throw or update anything
        verificationService.verifyToken(email, code);

        verify(tokenRepository, never()).save(any());
    }

    @Test
    void shouldThrowIfTokenNotFound() {
        when(tokenRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(TokenVerificationException.class,
            () -> verificationService.verifyToken(email, code));
    }

    @Test
    void shouldThrowIfCodeIsInvalid() {
        VerificationToken token = new VerificationToken(email, "999999", LocalDateTime.now().plusMinutes(5));
        when(tokenRepository.findByEmail(email)).thenReturn(Optional.of(token));

        assertThrows(TokenVerificationException.class,
            () -> verificationService.verifyToken(email, code));
    }

    @Test
    void shouldThrowIfTokenExpired() {
        VerificationToken token = new VerificationToken(email, code, LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByEmail(email)).thenReturn(Optional.of(token));

        assertThrows(TokenExpiredException.class,
            () -> verificationService.verifyToken(email, code));
    }

    @Test
    void shouldDeleteTokenByEmail() {
        verificationService.deleteExistingToken(email);
        verify(tokenRepository).deleteByEmail(email);
    }
}
