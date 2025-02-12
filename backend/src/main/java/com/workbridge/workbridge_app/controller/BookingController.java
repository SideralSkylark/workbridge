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

// import com.workbridge.workbridge_app.exception.UserNotFoundException;
// import com.workbridge.workbridge_app.service.BookingService;

// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/v1/bookings")
// @RequiredArgsConstructor
// public class BookingController {
    
//     private final BookingService bookingService;

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

//     private String getAuthenticatedUsername() {
//         String username = SecurityContextHolder.getContext().getAuthentication().getName();
//         if (username == null || username.isBlank()) {
//             throw new IllegalStateException("Authenticated user not found.");
//         }
//         return username;
//     }
// }
