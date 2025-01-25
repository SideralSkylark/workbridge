package com.workbridge.workbridge_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long>{
    
}
