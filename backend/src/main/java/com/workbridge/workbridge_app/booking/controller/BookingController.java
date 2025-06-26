/**
 * REST controller for managing service bookings.
 * <p>
 * This controller provides endpoints for service seekers and service providers to interact with bookings.
 * Service seekers can create, update, view, and cancel their bookings, while service providers can view
 * bookings for their provided services. All endpoints return standardized API responses using the
 * {@link ResponseFactory}.
 * </p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Fetch paginated bookings for the authenticated service seeker or provider</li>
 *   <li>Create a new booking for a service</li>
 *   <li>Update an existing booking (by the seeker)</li>
 *   <li>Cancel a booking (by the seeker)</li>
 *   <li>All endpoints are secured by role-based authorization</li>
 * </ul>
 *
 * <p>All methods are fully documented and use {@link SecurityUtil} to extract the authenticated username.</p>
 *
 * @author Sidik
 * @since 2025-06-26
 */
package com.workbridge.workbridge_app.booking.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.workbridge.workbridge_app.security.SecurityUtil;

import jakarta.validation.Valid;

import com.workbridge.workbridge_app.booking.dto.BookingRequestDTO;
import com.workbridge.workbridge_app.booking.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.booking.dto.UpdateBookingRequestDTO;
import com.workbridge.workbridge_app.booking.service.BookingService;
import com.workbridge.workbridge_app.common.response.ApiResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;

import lombok.RequiredArgsConstructor;

 @RestController
 @RequestMapping("/api/v1/bookings")
 @RequiredArgsConstructor
 public class BookingController {
    
    private final BookingService bookingService;

    /**
     * Retrieves a paginated list of bookings for the authenticated service seeker.
     *
     * @param pageable pagination information
     * @return a standardized API response containing a page of booking response DTOs
     */
    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @GetMapping("/seeker")
    public ResponseEntity<ApiResponse<Page<BookingResponseDTO>>> getMyBookings(
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseFactory.ok(
            bookingService.getUsersBookings(SecurityUtil.getAuthenticatedUsername(), pageable),
            "Fetched bookings successfully."
        );
    }

    /**
     * Retrieves a paginated list of bookings for the authenticated service provider.
     *
     * @param pageable pagination information
     * @return a standardized API response containing a page of booking response DTOs
     */
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<ApiResponse<Page<BookingResponseDTO>>> getMyBookedServices(
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseFactory.ok(
            bookingService.getBookingsByProviderId(SecurityUtil.getAuthenticatedUsername(), pageable),//pass username instead
            "Fetched bookings successfully."
        );
    }

    /**
     * Creates a new booking for the authenticated service seeker.
     *
     * @param bookingRequestDTO the booking request data
     * @return a standardized API response containing the created booking response DTO
     */
    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDTO>> bookService(
        @Valid @RequestBody BookingRequestDTO bookingRequestDTO) {
        return ResponseFactory.created(
            bookingService.createBooking(SecurityUtil.getAuthenticatedUsername(), bookingRequestDTO),
            "Booking created successfully."
        );
    }

    /**
     * Updates an existing booking for the authenticated service seeker.
     *
     * @param bookingId the ID of the booking to update
     * @param updateBookingRequestDTO the update data
     * @return a standardized API response containing the updated booking response DTO
     */
    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PatchMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> updateBooking(
        @PathVariable Long bookingId,
        @Valid @RequestBody UpdateBookingRequestDTO updateBookingRequestDTO) { 
        return ResponseFactory.ok(
            bookingService.updateBooking(
                SecurityUtil.getAuthenticatedUsername(),
                bookingId, 
                updateBookingRequestDTO),
            "Booking updated successfully."
        );
    }

    /**
     * Cancels a booking for the authenticated service seeker.
     *
     * @param bookingId the ID of the booking to cancel
     * @return a standardized API response indicating success
     */
    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(SecurityUtil.getAuthenticatedUsername(), bookingId);
        return ResponseFactory.ok("Booking cancelled successfully.");
     }
}
