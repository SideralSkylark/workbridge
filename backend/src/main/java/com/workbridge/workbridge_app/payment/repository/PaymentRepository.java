package com.workbridge.workbridge_app.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>{
    
}
