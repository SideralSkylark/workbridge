package com.workbridge.workbridge_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.workbridge.workbridge_app.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long>{
    List<Review> findByReviewed_Id(Long id);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewed.id = :providerId")
    Double findAverageRatingByProviderId(@Param("providerId") Long providerId);
    
    boolean existsByBooking_Id(Long bookingId);
    void deleteByBooking_Id(Long bookingId);
}
