package com.workbridge.workbridge_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
    
}
