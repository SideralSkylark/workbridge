package com.workbridge.workbridge_app.review.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;

import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@SQLRestriction("deleted = false")
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

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    private Long deletedByUser;


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
