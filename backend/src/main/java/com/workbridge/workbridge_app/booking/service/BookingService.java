package com.workbridge.workbridge_app.booking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.workbridge.workbridge_app.auth.exception.UserNotAuthorizedException;
import com.workbridge.workbridge_app.booking.dto.BookingRequestDTO;
import com.workbridge.workbridge_app.booking.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.booking.dto.UpdateBookingRequestDTO;
import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.booking.entity.BookingStatus;
import com.workbridge.workbridge_app.booking.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.booking.mapper.BookingMapper;
import com.workbridge.workbridge_app.booking.repository.BookingRepository;
import com.workbridge.workbridge_app.review.repository.ReviewRepository;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.exception.ServiceNotFoundException;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for managing booking operations in the application.
 * <p>
 * This class provides business logic for creating, updating, retrieving, and canceling bookings.
 * It ensures that all booking operations are performed with proper validation, authorization,
 * and mapping between entities and DTOs. It also handles the removal of related reviews when a booking is canceled.
 * </p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Retrieve bookings for seekers and providers with pagination</li>
 *   <li>Create new bookings for services</li>
 *   <li>Update and cancel bookings, ensuring only the seeker can modify</li>
 *   <li>Map between booking entities and DTOs</li>
 *   <li>Validate user authorization and handle not found scenarios</li>
 * </ul>
 *
 * <p>All methods throw well-defined exceptions for error scenarios, and are fully documented.</p>
 *
 * @author Sidik
 * @since 2025-06-26
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Retrieves a paginated list of bookings for the given service seeker.
     *
     * @param username the seeker's username
     * @param pageable pagination information
     * @return a page of booking response DTOs
     * @throws UserNotFoundException if the user is not found
     */
    public Page<BookingResponseDTO> getUsersBookings(String username, Pageable pageable) {
        log.debug("Fetching bookings for seeker: {}", username);
        ApplicationUser user = findUser(username);
        Page<BookingResponseDTO> bookings =  bookingRepository.findBySeeker_Id(user.getId(), pageable)
                .map(bookingMapper::toDTO);
        log.info("Retrieved {} bookings for seeker: {}", bookings.getTotalElements(), username);
        return bookings;
    }

    /**
     * Retrieves a paginated list of bookings for the given service provider.
     *
     * @param username the provider's username
     * @param pageable pagination information
     * @return a page of booking response DTOs
     * @throws UserNotFoundException if the user is not found
     */
    public Page<BookingResponseDTO> getBookingsByProviderId(String username, Pageable pageable) {
        log.debug("Fetching bookings for provider: {}", username);
        ApplicationUser provider = findUser(username);
        Page<BookingResponseDTO> bookings =  bookingRepository.findByService_Provider(provider, pageable)
                .map(bookingMapper::toDTO);
        log.info("Retrieved {} bookings for provider: {}", bookings.getTotalElements(), username);
        return bookings;
    }

    /**
     * Creates a new booking for the given service and seeker.
     *
     * @param username the seeker's username
     * @param request the booking request data
     * @return the created booking as a response DTO
     * @throws UserNotFoundException if the seeker is not found
     * @throws ServiceNotFoundException if the service is not found
     */
    @Transactional
    public BookingResponseDTO createBooking(String username, BookingRequestDTO request) {
        log.debug("Creating booking for user={} serviceId={}", username, request.getServiceId());
        ApplicationUser seeker = findUser(username);
        Service service = findServiceById(request.getServiceId());

        Booking booking = bookingMapper.toEntity(request, seeker, service);
        booking.setStatus(BookingStatus.PENDING);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking created: id={} seeker={} service={}", savedBooking.getId(), username, service.getId());
        return bookingMapper.toDTO(savedBooking);
    }

    /**
     * Updates an existing booking if the requesting user is the seeker.
     *
     * @param username the seeker's username
     * @param bookingId the ID of the booking to update
     * @param request the update data
     * @return the updated booking as a response DTO
     * @throws BookingNotFoundException if the booking is not found
     * @throws UserNotAuthorizedException if the user is not the seeker
     */
    @Transactional
    public BookingResponseDTO updateBooking(String username, Long bookingId, UpdateBookingRequestDTO request) {
        log.debug("Updating booking id={} by user={}", bookingId, username);
        Booking booking = findBookingById(bookingId);
        validateUserIsBookingSeeker(username, booking);

        booking.setDate(request.getDate());
        Booking saved = bookingRepository.save(booking);

        log.info("Booking updated: id={} by user={}", bookingId, username);
        return bookingMapper.toDTO(saved);
    }

    /**
     * Cancels a booking if the requesting user is the seeker. Also deletes related reviews.
     *
     * @param username the seeker's username
     * @param bookingId the ID of the booking to cancel
     * @throws BookingNotFoundException if the booking is not found
     * @throws UserNotAuthorizedException if the user is not the seeker
     */
    @Transactional
    public void cancelBooking(String username, Long bookingId) {
        log.debug("Cancelling booking id={} by user={}", bookingId, username);
        Booking booking = findBookingById(bookingId);
        validateUserIsBookingSeeker(username, booking);

        reviewRepository.deleteByBooking_Id(bookingId);
        bookingRepository.delete(booking);

        log.info("Booking cancelled and reviews deleted: id={} by user={}", bookingId, username);
    }

    /**
     * Finds a user by username or throws if not found.
     *
     * @param username the username to search for
     * @return the found ApplicationUser
     * @throws UserNotFoundException if the user is not found
     */
    private ApplicationUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UserNotFoundException("User not found.");
                });
    }

    /**
     * Finds a service by ID or throws if not found.
     *
     * @param serviceId the ID of the service
     * @return the found Service
     * @throws ServiceNotFoundException if the service is not found
     */
    private Service findServiceById(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.warn("Service not found: id={}", serviceId);
                    return new ServiceNotFoundException("Service not found.");
                });
    }

    /**
     * Finds a booking by ID or throws if not found.
     *
     * @param bookingId the ID of the booking
     * @return the found Booking
     * @throws BookingNotFoundException if the booking is not found
     */
    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking not found: id={}", bookingId);
                    return new BookingNotFoundException("Booking not found.");
                });
    }

    /**
     * Validates that the given username matches the booking's seeker.
     *
     * @param username the username to validate
     * @param booking the booking to check
     * @throws UserNotAuthorizedException if the user is not the seeker
     */
    private void validateUserIsBookingSeeker(String username, Booking booking) {
        if (!booking.getSeeker().getUsername().equals(username)) {
            log.warn("Unauthorized booking modification attempt: user={} bookingId={}", username, booking.getId());
            throw new UserNotAuthorizedException("You are not authorized to modify this booking.");
        }
    }
}
