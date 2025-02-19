 package com.workbridge.workbridge_app.controller;

 import java.util.List;

 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workbridge.workbridge_app.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.exception.ServiceListingNotFoundException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
 import com.workbridge.workbridge_app.service.BookingService;
import com.workbridge.workbridge_app.dto.BookingRequestDTO;
import com.workbridge.workbridge_app.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.dto.UpdateBookingRequestDTO;

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error ocured.");
        }
    }

    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PostMapping("/book")
    public ResponseEntity<?> bookService(@RequestBody BookingRequestDTO bookingRequestDTO) {
        try {
            String username = getAuthenticatedUsername();
            BookingResponseDTO booking = bookingService.createBooking(username, bookingRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (ServiceListingNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found.");
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error ocured.");
        }
    }

    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PostMapping("/update")
    public ResponseEntity<?> updateBooking(@RequestBody UpdateBookingRequestDTO updateBookingRequestDTO ) { 
        try {
            String username = getAuthenticatedUsername();
            BookingResponseDTO updatedBooking = bookingService.updateBooking(username, updateBookingRequestDTO);
            return ResponseEntity.ok(updatedBooking);
        } catch (ServiceListingNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found.");
        } catch(BookingNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelBooking(@RequestParam Long bookingId) {
        try {
            String username = getAuthenticatedUsername();
            bookingService.cancelBooking(username, bookingId);
            return ResponseEntity.ok("Booking canceled successfully.");
        } catch (BookingNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
     }

    private String getAuthenticatedUsername() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
         if (username == null || username.isBlank()) {
             throw new IllegalStateException("Authenticated user not found.");
         }
         return username;
     }
}
