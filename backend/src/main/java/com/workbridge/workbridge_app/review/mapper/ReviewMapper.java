package com.workbridge.workbridge_app.review.mapper;

import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.review.dto.ReviewRequestDTO;
import com.workbridge.workbridge_app.review.dto.ReviewResponseDTO;
import com.workbridge.workbridge_app.review.entity.Review;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;

public class ReviewMapper {
    private ReviewMapper(){}

    public static ReviewResponseDTO toDTO(Review review) {
        return new ReviewResponseDTO(
            review.getId(),
            review.getRating(),
            review.getComment(),
            review.getReviewer(),
            review.getReviewed(),
            review.getCreatedAt()
        );
    }

    public static Review toEntity(
        ApplicationUser reviewer,
        ApplicationUser reviewed,
        Booking booking,
        ReviewRequestDTO requestDTO) {
            Review review = new Review();
            review.setRating(requestDTO.getRating());
            review.setComment(requestDTO.getComment());
            review.setBooking(booking);
            review.setReviewer(reviewer);
            review.setReviewed(reviewed);
            return review;
    }
}
