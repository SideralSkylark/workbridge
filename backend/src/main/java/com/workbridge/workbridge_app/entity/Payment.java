package com.workbridge.workbridge_app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @ManyToOne
    private Service service;

    @ManyToOne
    private ApplicationUser payer; 

    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public boolean isPayer(ApplicationUser user) {
        return user != null && user.hasRole(UserRole.SERVICE_SEEKER);
    }
}