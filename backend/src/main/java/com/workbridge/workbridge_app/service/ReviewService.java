package com.workbridge.workbridge_app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.dto.ReviewRequestDTO;
import com.workbridge.workbridge_app.dto.ReviewResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.Booking;
import com.workbridge.workbridge_app.entity.Review;
import com.workbridge.workbridge_app.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.repository.BookingRepository;
import com.workbridge.workbridge_app.repository.ReviewRepository;
import com.workbridge.workbridge_app.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    
    
    public List<ReviewResponseDTO> getReviewsByProvider(Long providerId) {
        ApplicationUser provider = userRepository.findById(providerId)
                                    .orElseThrow(() -> new UserNotFoundException("Provider not found."));

        List<Review> reviews = reviewRepository.findByReviewed_Id(provider.getId());
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ReviewResponseDTO reviewProvider(ReviewRequestDTO reviewRequestDTO) {
        ApplicationUser reviewer = userRepository.findById(reviewRequestDTO.getReviewerId())
                                    .orElseThrow(() -> new UserNotFoundException("Reviewer not found."));

        ApplicationUser reviewed = userRepository.findById(reviewRequestDTO.getReviewedId())
                                    .orElseThrow(() -> new UserNotFoundException("Reviewed not found."));
        
        Booking booking = bookingRepository.findById(reviewRequestDTO.getBookingId())
                            .orElseThrow(() -> new BookingNotFoundException("Booking not found."));

        Review review = new Review();
        review.setBooking(booking);
        review.setReviewed(reviewed);
        review.setReviewer(reviewer);
        review.setComment(reviewRequestDTO.getComment());
        review.setRating(reviewRequestDTO.getRating());

        reviewRepository.save(review);
        return convertToDTO(review);
    }

    /**
     * Checks if a review exists for a specific booking
     * @param bookingId The ID of the booking to check
     * @return true if a review exists for the booking, false otherwise
     */
    public boolean hasUserReviewedBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                            .orElseThrow(() -> new BookingNotFoundException("Booking not found."));
        
        return reviewRepository.existsByBooking_Id(bookingId);
    }

    private ReviewResponseDTO convertToDTO(Review review) {
        return new ReviewResponseDTO(
            review.getId(),
            review.getRating(),
            review.getComment(),
            review.getReviewer(),
            review.getReviewed(),
            review.getCreatedAt()
        );
    }
}
