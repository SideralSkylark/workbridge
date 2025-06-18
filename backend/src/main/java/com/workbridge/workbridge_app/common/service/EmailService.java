package com.workbridge.workbridge_app.common.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import jakarta.mail.MessagingException;

import jakarta.mail.internet.MimeMessage;
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

    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply.workbridge@gmail.com");
    
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void sendEmailWithAttachment(String toEmail, String subject, String body, MultipartFile attachment) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
    
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body);
        helper.setFrom("noreply.workbridge@gmail.com");
    
        helper.addAttachment("fatura.pdf", new ByteArrayResource(attachment.getBytes()));
    
        mailSender.send(message);
    }    
}
