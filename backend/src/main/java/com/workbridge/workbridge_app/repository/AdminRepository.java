package com.workbridge.workbridge_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long>{
    
}
