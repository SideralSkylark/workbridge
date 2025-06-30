package com.workbridge.workbridge_app.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.workbridge.workbridge_app.common.response.ApiResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.review.dto.ReviewRequestDTO;
import com.workbridge.workbridge_app.review.dto.ReviewResponseDTO;
import com.workbridge.workbridge_app.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing reviews of service providers.
 * <p>
 * This controller exposes endpoints for:
 * <ul>
 *   <li>Retrieving paginated reviews for a specific provider</li>
 *   <li>Submitting a review for a provider (by a service seeker)</li>
 *   <li>Checking if a user has already reviewed a booking</li>
 * </ul>
 *
 * <p>All endpoints return standardized API responses using {@link ApiResponse} and {@link ResponseFactory}.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   GET    /api/v1/reviews/provider/{providerId}         // Paginated reviews for a provider
 *   POST   /api/v1/reviews                               // Submit a review for a provider
 *   GET    /api/v1/reviews/booking/{bookingId}/reviewed  // Check if a review exists for a booking
 * </pre>
 *
 * <p>Role-based access control is enforced for review submission.</p>
 *
 * @author Workbridge Team
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Retrieves a paginated list of reviews for a specific provider.
     *
     * @param providerId The ID of the provider whose reviews are being fetched
     * @param pageable   Pagination and sorting information (default: page=0, size=20, sort by id ASC)
     * @return 200 OK with a page of {@link ReviewResponseDTO} objects and a success message
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<PagedModel<ReviewResponseDTO>>> getReviewsByProvider(
        @PathVariable Long providerId,
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByProvider(providerId, pageable);
        return ResponseFactory.ok(
            new PagedModel<>(reviews),
            "Reviews retrieved successfully"
        );
    }

    /**
     * Submits a review for a service provider.
     * <p>
     * Only users with the SERVICE_SEEKER role can submit reviews.
     *
     * @param reviewRequestDTO The review data (rating, comment, bookingId, reviewedId, reviewerId)
     * @return 200 OK with the created {@link ReviewResponseDTO} and a success message
     */
    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> reviewServiceProvider(
        @Valid @RequestBody ReviewRequestDTO reviewRequestDTO) {
        return ResponseFactory.ok(
            reviewService.reviewProvider(reviewRequestDTO),
            "Review submitted successfully"
        );
    }

    /**
     * Checks if a user has already reviewed a booking.
     *
     * @param bookingId The ID of the booking to check
     * @return 200 OK with true if a review exists, false otherwise, and a status message
     */
    @GetMapping("/booking/{bookingId}/reviewed")
    public ResponseEntity<ApiResponse<Boolean>> hasUserReviewedBooking(@PathVariable Long bookingId) {
        return ResponseFactory.ok(
            reviewService.hasUserReviewedBooking(bookingId),
            "Review status checked successfully"
        );
    }
}
