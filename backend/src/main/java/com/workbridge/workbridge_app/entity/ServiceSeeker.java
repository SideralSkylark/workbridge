package com.workbridge.workbridge_app.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("SERVICE_SEEKER")
public class ServiceSeeker extends ApplicationUser {

    @OneToMany(mappedBy = "seeker")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "seeker")
    private List<Review> reviews;

    @OneToMany(mappedBy = "seeker")
    private List<Payment> paymentHistory;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + getRole().name()));
    }
}
