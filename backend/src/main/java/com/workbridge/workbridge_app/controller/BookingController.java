// package com.workbridge.workbridge_app.controller;

// import java.util.List;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
// import com.workbridge.workbridge_app.exception.UserNotFoundException;
// import com.workbridge.workbridge_app.service.BookingService;
=======
import com.workbridge.workbridge_app.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.BookingService;
>>>>>>> 6f3ce38c808287948161f1ad78dce9fd25175c57

// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/v1/bookings")
// @RequiredArgsConstructor
// public class BookingController {
    
//     private final BookingService bookingService;

<<<<<<< HEAD
//     @PreAuthorize("hasrole('SERVICE_SEEKER')")
//     @GetMapping()
//     public ResponseEntity<?> getMyBookings() {
//         try {
//             String username = getAuthenticatedUsername();
//             List<BookingResponseDTO> bookings = bookingService.getUsersBookings(username);
//             return ResponseEntity.ok(bookings);
//         } catch (UserNotFoundException UserNotFoundException) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
//         } catch (Exception exception) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error ocured.");
//         }
//     }

//     @PreAuthorize("hasrole('SERVICE_SEEKER')")
//     @PostMapping("/book")
//     public ResponseEntity<?> bookService() {
        
//     }

//     @PreAuthorize("hasrole('SERVICE_SEEKER')")
//     @PostMapping("/book")
//     public ResponseEntity<?> updateBooking() {}

//     @PreAuthorize("hasrole('SERVICE_SEEKER')")
//     @PostMapping("/book")
//     public ResponseEntity<?> cancelBooking() {}
=======
    @PreAuthorize("hasrole('SERVICE_SEEKER')")
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

    @PreAuthorize("hasrole('SERVICE_SEEKER')")
    @PostMapping("/book")
    public ResponseEntity<?> bookService() {
        return ResponseEntity.ok(null);
    }

    @PreAuthorize("hasrole('SERVICE_SEEKER')")
    @PostMapping("/update")
    public ResponseEntity<?> updateBooking() { return ResponseEntity.ok(null);}

    @PreAuthorize("hasrole('SERVICE_SEEKER')")
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelBooking() { return ResponseEntity.ok(null);}
>>>>>>> 6f3ce38c808287948161f1ad78dce9fd25175c57

//     private String getAuthenticatedUsername() {
//         String username = SecurityContextHolder.getContext().getAuthentication().getName();
//         if (username == null || username.isBlank()) {
//             throw new IllegalStateException("Authenticated user not found.");
//         }
//         return username;
//     }
// }
