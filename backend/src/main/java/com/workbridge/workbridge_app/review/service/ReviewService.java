package com.workbridge.workbridge_app.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.booking.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.booking.repository.BookingRepository;
import com.workbridge.workbridge_app.review.dto.ReviewRequestDTO;
import com.workbridge.workbridge_app.review.dto.ReviewResponseDTO;
import com.workbridge.workbridge_app.review.entity.Review;
import com.workbridge.workbridge_app.review.mapper.ReviewMapper;
import com.workbridge.workbridge_app.review.repository.ReviewRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing review operations for service providers.
 * <p>
 * This service provides business logic for:
 * <ul>
 *   <li>Retrieving paginated reviews for a provider</li>
 *   <li>Submitting a review for a provider</li>
 *   <li>Checking if a booking has already been reviewed</li>
 * </ul>
 *
 * <p>All methods throw domain-specific exceptions for missing users or bookings.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   reviewService.getReviewsByProvider(providerId, pageable);
 *   reviewService.reviewProvider(reviewRequestDTO);
 *   reviewService.hasUserReviewedBooking(bookingId);
 * </pre>
 *
 * @author Workbridge Team
 * @since 2025-06-25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    /**
     * Retrieves paginated reviews for a given provider.
     *
     * @param providerId the ID of the reviewed provider
     * @param pageable   pagination options (page, size, sort)
     * @return a page of {@link ReviewResponseDTO} objects
     * @throws UserNotFoundException if the provider is not found
     */
    public Page<ReviewResponseDTO> getReviewsByProvider(Long providerId, Pageable pageable) {
        log.debug("Fetching reviews for providerId={}", providerId);
        ApplicationUser provider = findUserOrThrow(providerId, "Provider not found");
        Page<Review> reviewsPage = reviewRepository.findByReviewed_Id(provider.getId(), pageable);
        log.info("Retrieved {} reviews for providerId={}", reviewsPage.getTotalElements(), providerId);
        return reviewsPage.map(ReviewMapper::toDTO);
    }

    /**
     * Allows a user to submit a review for a service provider.
     *
     * @param reviewRequestDTO review data (rating, comment, bookingId, reviewedId, reviewerId)
     * @return the saved review as a {@link ReviewResponseDTO}
     * @throws UserNotFoundException if the reviewer or reviewed user is not found
     * @throws BookingNotFoundException if the booking is not found
     */
    @Transactional
    public ReviewResponseDTO reviewProvider(ReviewRequestDTO reviewRequestDTO) {
        log.debug("Attempting to submit review: reviewerId={}, reviewedId={}, bookingId={}",
                reviewRequestDTO.getReviewerId(),
                reviewRequestDTO.getReviewedId(),
                reviewRequestDTO.getBookingId());
        ApplicationUser reviewer = findUserOrThrow(reviewRequestDTO.getReviewerId(), "Reviewer not found");
        ApplicationUser reviewed = findUserOrThrow(reviewRequestDTO.getReviewedId(), "Reviewed not found");
        Booking booking = bookingRepository.findById(reviewRequestDTO.getBookingId())
                                           .orElseThrow(() -> {
                    log.warn("Booking not found: id={}", reviewRequestDTO.getBookingId());
                    return new BookingNotFoundException("Booking not found");
                                           });
        Review review = ReviewMapper.toEntity(reviewer, reviewed, booking, reviewRequestDTO);
        Review savedReview = reviewRepository.save(review);

        log.info("Review submitted successfully: reviewId={} bookingId={} reviewerId={} reviewedId={}",
                savedReview.getId(), booking.getId(), reviewer.getId(), reviewed.getId());

        return ReviewMapper.toDTO(savedReview);
    }

    /**
     * Checks if a booking already has a review.
     *
     * @param bookingId the ID of the booking to check
     * @return true if a review exists for the booking, false otherwise
     * @throws BookingNotFoundException if the booking is not found
     */
    public boolean hasUserReviewedBooking(Long bookingId) {
        log.debug("Checking if booking has review: bookingId={}", bookingId);

        bookingRepository.findById(bookingId)
                         .orElseThrow(() -> {
                    log.warn("Booking not found while checking for review: bookingId={}", bookingId);
                    return new BookingNotFoundException("Booking not found");
                         });
        boolean exists = reviewRepository.existsByBooking_Id(bookingId);
        log.info("Review exists for bookingId={}: {}", bookingId, exists);
        return exists;
    }

    /**
     * Utility to get user or throw exception.
     *
     * @param userId the user ID to look up
     * @param notFoundMessage the error message if not found
     * @return the found {@link ApplicationUser}
     * @throws UserNotFoundException if the user is not found
     */
    private ApplicationUser findUserOrThrow(Long userId, String notFoundMessage) {
        return userRepository.findById(userId)
                             .orElseThrow(() -> {
                    log.warn("{}: userId={}", notFoundMessage, userId);
                    return new UserNotFoundException(notFoundMessage);
                             });
    }
}
