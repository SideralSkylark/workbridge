package com.workbridge.workbridge_app.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.workbridge.workbridge_app.common.dto.InvoiceDTO;
import com.workbridge.workbridge_app.common.service.EmailService;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController()
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @PostMapping("/send-email-with-pdf")
    public ResponseEntity<String> sendInvoiceEmailWithPdf(
            @RequestPart("invoiceData") InvoiceDTO invoiceData,
            @RequestPart("pdf") MultipartFile pdfFile
    ) {
        try {
            ApplicationUser user = userRepository.findByUsername(invoiceData.getClientName())
                                    .orElseThrow(() -> new UserNotFoundException("Usuario nao existe."));

            String toEmail = user.getEmail();
            String subject = "Fatura WorkBridge";
            String message = "Olá " + invoiceData.getClientName() + ",\n\nSegue em anexo a sua fatura.";

            emailService.sendEmailWithAttachment(toEmail, subject, message, pdfFile);
            return ResponseEntity.ok("Email enviado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao enviar o email: " + e.getMessage());
        }
    }

}
