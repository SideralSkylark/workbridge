package com.workbridge.workbridge_app.review.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.workbridge.workbridge_app.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long>{
    Page<Review> findByReviewed_Id(Long id, Pageable pageable);
    List<Review> findByReviewed_Id(Long id);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewed.id = :providerId")
    Double findAverageRatingByProviderId(@Param("providerId") Long providerId);
    
    boolean existsByBooking_Id(Long bookingId);
    void deleteByBooking_Id(Long bookingId);
}
