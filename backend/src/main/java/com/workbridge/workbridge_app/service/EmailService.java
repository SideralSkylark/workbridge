package com.workbridge.workbridge_app.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("WorkBridge Email Verification");
            message.setText("Your verification code is: " + code + ". It expires in 10 minutes.\n\n"
                             + "If you did not request this verification, please ignore this email.");
            message.setFrom("noreply.workbridge@gmail.com"); // Specify the 'from' address to match the sending Gmail account.
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
