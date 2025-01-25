package com.workbridge.workbridge_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.entity.ServiceSeeker;

public interface ServiceSeekerRepository extends JpaRepository<ServiceSeeker, Long>{
    
}
