 package com.workbridge.workbridge_app.booking.controller;

 import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workbridge.workbridge_app.security.SecurityUtil;
import com.workbridge.workbridge_app.service.exception.ServiceNotFoundException;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.booking.dto.BookingRequestDTO;
import com.workbridge.workbridge_app.booking.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.booking.dto.UpdateBookingRequestDTO;
import com.workbridge.workbridge_app.booking.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.booking.service.BookingService;

import lombok.RequiredArgsConstructor;

 @RestController
 @RequestMapping("/api/v1/bookings")
 @RequiredArgsConstructor
 public class BookingController {
    
    private final BookingService bookingService;

    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @GetMapping("/me")
    public ResponseEntity<?> getMyBookings() {
        try {
            String username = getAuthenticatedUsername();
            List<BookingResponseDTO> bookings = bookingService.getUsersBookings(username);
            return ResponseEntity.ok(bookings);
        } catch (UserNotFoundException UserNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error ocured.", "message", exception.getMessage()));
        }
    }

    // TODO: write teste for this endpoint
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<?> getMyBookedServices(@RequestParam Long providerId) {
        try {
            List<BookingResponseDTO> bookings = bookingService.getBookingsByProviderId(providerId);
            return ResponseEntity.ok(bookings);
        } catch (UserNotFoundException UserNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error ocured.", "message", exception.getMessage()));
        }
    }

    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PostMapping("/book")
    public ResponseEntity<?> bookService(@RequestBody BookingRequestDTO bookingRequestDTO) {
        try {
            String username = getAuthenticatedUsername();
            BookingResponseDTO booking = bookingService.createBooking(username, bookingRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (ServiceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found.");
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error ocured.", "message", exception.getMessage()));
        }
    }

    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PatchMapping("/update")
    public ResponseEntity<?> updateBooking(@RequestBody UpdateBookingRequestDTO updateBookingRequestDTO) { 
        try {
            String username = getAuthenticatedUsername();
            BookingResponseDTO updatedBooking = bookingService.updateBooking(username, updateBookingRequestDTO);
            return ResponseEntity.ok(updatedBooking);
        } catch (ServiceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found.");
        } catch(BookingNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error ocured.", "message", exception.getMessage()));
        }
    }

    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelBooking(@RequestParam Long bookingId) {
        try {
            String username = getAuthenticatedUsername();
            bookingService.cancelBooking(username, bookingId);
            return ResponseEntity.ok(Map.of("message", "Booking canceled successfully."));
        } catch (BookingNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Booking not found."));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error ocured.", "message", exception.getMessage()));
        }
     }
     
    private String getAuthenticatedUsername() {
        String username = SecurityUtil.getAuthenticatedUsername();
         if (username == null || username.isBlank()) {
             throw new UserNotFoundException("Authenticated user not found.");
         }
         return username;
     }
}
