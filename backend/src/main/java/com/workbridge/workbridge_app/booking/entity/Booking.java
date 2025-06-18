package com.workbridge.workbridge_app.booking.entity;

import java.time.LocalDateTime;

import com.workbridge.workbridge_app.payment.entity.Payment;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Service service;

    @ManyToOne
    private ApplicationUser seeker; 

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @OneToOne
    private Payment payment;

    public boolean isSeeker(ApplicationUser user) {
        return user != null && user.hasRole(UserRole.SERVICE_SEEKER);
    }
}