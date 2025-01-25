package com.workbridge.workbridge_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long>{
    
}
