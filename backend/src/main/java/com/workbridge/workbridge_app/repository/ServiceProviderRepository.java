package com.workbridge.workbridge_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.entity.ServiceProvider;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long>{
    
}
