package com.workbridge.workbridge_app.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
public class ProviderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser user; 

    private LocalDateTime requestedOn; 
    private boolean approved; 

    private LocalDateTime approvedOn;

    public ProviderRequest(ApplicationUser user) {
        this.user = user;
        this.requestedOn = LocalDateTime.now();
        this.approved = false;
    }

    public void markApproved(LocalDateTime approvedOn) {
        this.approved = true;
        this.approvedOn = approvedOn;
    }
}