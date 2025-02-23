package com.workbridge.workbridge_app.unit.controller;

import com.workbridge.workbridge_app.controller.BookingController;
import com.workbridge.workbridge_app.dto.BookingRequestDTO;
import com.workbridge.workbridge_app.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.dto.UpdateBookingRequestDTO;
import com.workbridge.workbridge_app.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.exception.ServiceListingNotFoundException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private void mockAuthentication(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetMyBookings_Success() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        List<BookingResponseDTO> bookings = List.of(new BookingResponseDTO(1L, "Seeker", "Service", 100.0, "Provider", LocalDateTime.now(), "CONFIRMED"));
        when(bookingService.getUsersBookings(username)).thenReturn(bookings);

        ResponseEntity<?> response = bookingController.getMyBookings();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetMyBookings_UserNotFound() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        when(bookingService.getUsersBookings(username)).thenThrow(new UserNotFoundException("User not found"));

        ResponseEntity<?> response = bookingController.getMyBookings();

        assertEquals(404, response.getStatusCode().value());
        assertEquals("User not found.", response.getBody());
    }

    @Test
    void testBookService_Success() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        BookingRequestDTO request = new BookingRequestDTO(1L, LocalDateTime.now());
        BookingResponseDTO responseDTO = new BookingResponseDTO(1L, "Seeker", "Service", 100.0, "Provider", LocalDateTime.now(), "PENDING");
        when(bookingService.createBooking(username, request)).thenReturn(responseDTO);

        ResponseEntity<?> response = bookingController.bookService(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(responseDTO, response.getBody());
    }

    @Test
    void testBookService_ServiceNotFound() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        BookingRequestDTO request = new BookingRequestDTO(1L, LocalDateTime.now());
        when(bookingService.createBooking(username, request)).thenThrow(new ServiceListingNotFoundException("Service not found"));

        ResponseEntity<?> response = bookingController.bookService(request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Service not found.", response.getBody());
    }

    @Test
    void testUpdateBooking_Success() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        UpdateBookingRequestDTO updateRequest = new UpdateBookingRequestDTO(1L, LocalDateTime.now());
        BookingResponseDTO updatedBooking = new BookingResponseDTO(1L, "Seeker", "Service", 100.0, "Provider", LocalDateTime.now(), "UPDATED");
        when(bookingService.updateBooking(username, updateRequest)).thenReturn(updatedBooking);

        ResponseEntity<?> response = bookingController.updateBooking(updateRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(updatedBooking, response.getBody());
    }

    @Test
    void testUpdateBooking_NotFound() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        UpdateBookingRequestDTO updateRequest = new UpdateBookingRequestDTO(1L, LocalDateTime.now());
        when(bookingService.updateBooking(username, updateRequest)).thenThrow(new BookingNotFoundException("Booking not found"));

        ResponseEntity<?> response = bookingController.updateBooking(updateRequest);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Booking not found.", response.getBody());
    }

    @Test
    void testCancelBooking_Success() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        doNothing().when(bookingService).cancelBooking(username, 1L);

        ResponseEntity<?> response = bookingController.cancelBooking(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(Map.of("message", "Booking canceled successfully."), response.getBody());
    }

    @Test
    void testCancelBooking_NotFound() {
        String username = "usuarioTeste";
        mockAuthentication(username);
        doThrow(new BookingNotFoundException("Booking not found")).when(bookingService).cancelBooking(username, 1L);

        ResponseEntity<?> response = bookingController.cancelBooking(1L);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Booking not found.", response.getBody());
    }
}