package com.workbridge.workbridge_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>{
    
}
