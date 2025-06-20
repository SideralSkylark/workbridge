package com.workbridge.workbridge_app.booking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.workbridge.workbridge_app.auth.exception.UserNotAuthorizedException;
import com.workbridge.workbridge_app.booking.dto.BookingRequestDTO;
import com.workbridge.workbridge_app.booking.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.booking.dto.UpdateBookingRequestDTO;
import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.booking.entity.BookingStatus;
import com.workbridge.workbridge_app.booking.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.booking.repository.BookingRepository;
import com.workbridge.workbridge_app.review.repository.ReviewRepository;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.exception.ServiceListingNotFoundException;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewRepository reviewRepository;

    public List<BookingResponseDTO> getUsersBookings(String username) {
        ApplicationUser user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UserNotFoundException("User not found."));

        List<Booking> bookings = bookingRepository.findBySeeker_Id(user.getId());
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //TODO: write tests for this service
    public List<BookingResponseDTO> getBookingsByProviderId(Long providerId) {
        ApplicationUser provider = userRepository.findById(providerId)
            .orElseThrow(() -> new UserNotFoundException("Provider not found"));
    
        List<Booking> bookings = bookingRepository.findByService_Provider(provider);
    
        return bookings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponseDTO createBooking(String username, BookingRequestDTO request) {
        ApplicationUser user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UserNotFoundException("User not found."));
        
        Service service = serviceRepository.findById(request.getServiceId())
                            .orElseThrow(() -> new ServiceListingNotFoundException("Service not found."));

        Booking booking = new Booking();
        booking.setSeeker(user);
        booking.setService(service);
        booking.setStatus(BookingStatus.PENDING);
        booking.setDate(request.getDate());  
        
        Booking response = bookingRepository.save(booking);
        return convertToDTO(response);
    }

    @Transactional
    public BookingResponseDTO updateBooking(String username, UpdateBookingRequestDTO request) {
        ApplicationUser user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UserNotFoundException("User not found."));
        
        Booking booking = bookingRepository.findById(request.getBookingId())
                            .orElseThrow(() -> new BookingNotFoundException("Booking not found."));

        if (!booking.getSeeker().getUsername().equals(username)) {
            throw new UserNotAuthorizedException("You are not authorized to update this booking.");
        }

        booking.setDate(request.getDate());
        bookingRepository.save(booking);

        return convertToDTO(booking);
    }

    @Transactional
    public void cancelBooking(String username, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                            .orElseThrow(() -> new BookingNotFoundException("Booking not found."));

        if (!booking.getSeeker().getUsername().equals(username)) {
            throw new UserNotAuthorizedException("You can only cancel your bookings.");
        }

        reviewRepository.deleteByBooking_Id(bookingId);
        bookingRepository.delete(booking);
    }

    private BookingResponseDTO convertToDTO(Booking booking) {
        return new BookingResponseDTO(
            booking.getId(),
            booking.getService().getProvider().getId(),
            booking.getSeeker().getUsername(),
            booking.getService().getId(),
            booking.getService().getTitle(),
            booking.getService().getDescription(),
            booking.getService().getPrice(),
            booking.getService().getProvider().getUsername(),
            booking.getDate(),
            booking.getStatus().name()
        );
    }    
}
