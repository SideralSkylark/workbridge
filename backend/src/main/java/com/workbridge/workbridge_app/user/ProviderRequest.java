package com.workbridge.workbridge_app.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.workbridge.workbridge_app.user.entity.ApplicationUser;

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
}