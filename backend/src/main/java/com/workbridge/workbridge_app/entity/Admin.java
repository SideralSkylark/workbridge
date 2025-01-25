package com.workbridge.workbridge_app.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Admin extends User{
    
    @OneToMany
    private List<ServiceProvider> serviceProviders;

    @OneToMany
    private List<ServiceSeeker> serviceSeekers;
}
