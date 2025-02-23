package com.workbridge.workbridge_app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double rating;

    private String comment;

    @OneToOne
    private Booking booking;

    @ManyToOne
    private ApplicationUser reviewer; 

    @ManyToOne
    private ApplicationUser reviewed; 
    
    private LocalDateTime createdAt;


    public boolean isReviewer(ApplicationUser user) {
        return user != null && user.hasRole(UserRole.SERVICE_SEEKER);
    }

    public boolean isReviewed(ApplicationUser user) {
        return user != null && user.hasRole(UserRole.SERVICE_PROVIDER);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}