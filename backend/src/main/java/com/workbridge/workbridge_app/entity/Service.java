package com.workbridge.workbridge_app.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
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

    public boolean isProvider(ApplicationUser user) {
        return user != null && user.hasRole(UserRole.SERVICE_PROVIDER);
    }
}
