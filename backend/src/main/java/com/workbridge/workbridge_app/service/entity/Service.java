package com.workbridge.workbridge_app.service.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@SQLRestriction("deleted = false")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private Double price;

    @ManyToOne
    private ApplicationUser provider;

    @ElementCollection
    private List<LocalDateTime> availability;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    private Long deletedByUser;

    public boolean isProvider(ApplicationUser user) {
        return user != null && user.hasRole(UserRole.SERVICE_PROVIDER);
    }
}
