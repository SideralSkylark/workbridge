package com.workbridge.workbridge_app.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class ServiceProvider extends User{
    
    private String companyName;

    @OneToMany(mappedBy = "provider")
    private List<Service> services;

    @OneToMany(mappedBy = "provider")
    private List<Review> ratings;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;
}
