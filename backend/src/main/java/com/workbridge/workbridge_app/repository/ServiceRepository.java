package com.workbridge.workbridge_app.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.entity.Service;

public interface ServiceRepository extends JpaRepository<Service, Long>{
    
}
