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
public class ServiceSeeker extends User {
    
    @OneToMany(mappedBy = "seeker")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "seeker")
    private List<Review> reviews;

    @OneToMany(mappedBy = "seeker")
    private List<Payment> paymentHistory;
}
