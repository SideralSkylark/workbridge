package com.workbridge.workbridge_app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workbridge.workbridge_app.auth.exception.UserNotAuthorizedException;
import com.workbridge.workbridge_app.booking.dto.BookingRequestDTO;
import com.workbridge.workbridge_app.booking.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.booking.dto.UpdateBookingRequestDTO;
import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.booking.entity.BookingStatus;
import com.workbridge.workbridge_app.booking.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.booking.repository.BookingRepository;
import com.workbridge.workbridge_app.booking.service.BookingService;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.exception.ServiceListingNotFoundException;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private BookingService bookingService;

    private ApplicationUser testUser;
    private ApplicationUser testProvider;
    private Service testService;
    private Booking testBooking;
    private BookingRequestDTO bookingRequest;
    private UpdateBookingRequestDTO updateBookingRequest;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        // Create test date
        testDate = LocalDateTime.now().plusDays(7);

        // Create test user (seeker)
        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Create test provider
        testProvider = new ApplicationUser();
        testProvider.setId(2L);
        testProvider.setUsername("provider");
        testProvider.setEmail("provider@example.com");

        // Create test service
        testService = new Service();
        testService.setId(1L);
        testService.setTitle("Test Service");
        testService.setDescription("Test Description");
        testService.setPrice(100.0);
        testService.setProvider(testProvider);

        // Create test booking
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setSeeker(testUser);
        testBooking.setService(testService);
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setDate(testDate);

        // Create test DTOs
        bookingRequest = new BookingRequestDTO(1L, testDate);
        updateBookingRequest = new UpdateBookingRequestDTO(1L, testDate.plusDays(1));
    }

    @Test
    void getUsersBookings_WhenUserExists_ShouldReturnBookings() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findBySeeker_Id(1L)).thenReturn(Arrays.asList(testBooking));

        // Act
        List<BookingResponseDTO> result = bookingService.getUsersBookings("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBooking.getId(), result.get(0).getId());
        assertEquals(testUser.getUsername(), result.get(0).getSeekerName());
        verify(userRepository).findByUsername("testuser");
        verify(bookingRepository).findBySeeker_Id(1L);
    }

    @Test
    void getUsersBookings_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            bookingService.getUsersBookings("nonexistent")
        );
        verify(userRepository).findByUsername("nonexistent");
        verify(bookingRepository, never()).findBySeeker_Id(any());
    }

    @Test
    void getBookingsByProviderId_WhenProviderExists_ShouldReturnBookings() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));
        when(bookingRepository.findByService_Provider(testProvider)).thenReturn(Arrays.asList(testBooking));

        // Act
        List<BookingResponseDTO> result = bookingService.getBookingsByProviderId(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBooking.getId(), result.get(0).getId());
        assertEquals(testProvider.getUsername(), result.get(0).getProviderName());
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByService_Provider(testProvider);
    }

    @Test
    void getBookingsByProviderId_WhenProviderDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            bookingService.getBookingsByProviderId(999L)
        );
        verify(userRepository).findById(999L);
        verify(bookingRepository, never()).findByService_Provider(any());
    }

    @Test
    void createBooking_WhenValidRequest_ShouldCreateBooking() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // Act
        BookingResponseDTO result = bookingService.createBooking("testuser", bookingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testBooking.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getSeekerName());
        assertEquals(testService.getTitle(), result.getServiceTitle());
        assertEquals(BookingStatus.PENDING.name(), result.getStatus());
        verify(userRepository).findByUsername("testuser");
        verify(serviceRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            bookingService.createBooking("nonexistent", bookingRequest)
        );
        verify(userRepository).findByUsername("nonexistent");
        verify(serviceRepository, never()).findById(any());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenServiceDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceListingNotFoundException.class, () -> {
            bookingRequest = new BookingRequestDTO(999L, testDate);
            bookingService.createBooking("testuser", bookingRequest);
        });
        verify(userRepository).findByUsername("testuser");
        verify(serviceRepository).findById(999L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_WhenValidRequest_ShouldUpdateBooking() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // Act
        BookingResponseDTO result = bookingService.updateBooking("testuser", updateBookingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testBooking.getId(), result.getId());
        assertEquals(updateBookingRequest.getDate(), testBooking.getDate());
        verify(userRepository).findByUsername("testuser");
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(testBooking);
    }

    @Test
    void updateBooking_WhenBookingDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookingNotFoundException.class, () -> {
            updateBookingRequest = new UpdateBookingRequestDTO(999L, testDate);
            bookingService.updateBooking("testuser", updateBookingRequest);
        });
        verify(userRepository).findByUsername("testuser");
        verify(bookingRepository).findById(999L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_WhenUserNotAuthorized_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // Act & Assert
        assertThrows(UserNotAuthorizedException.class, () -> 
            bookingService.updateBooking("otheruser", updateBookingRequest)
        );
        verify(userRepository).findByUsername("otheruser");
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_WhenValidRequest_ShouldDeleteBooking() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // Act
        bookingService.cancelBooking("testuser", 1L);

        // Assert
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).delete(testBooking);
    }

    @Test
    void cancelBooking_WhenBookingDoesNotExist_ShouldThrowException() {
        // Arrange
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookingNotFoundException.class, () -> 
            bookingService.cancelBooking("testuser", 999L)
        );
        verify(bookingRepository).findById(999L);
        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    @Test
    void cancelBooking_WhenUserNotAuthorized_ShouldThrowException() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // Act & Assert
        assertThrows(UserNotAuthorizedException.class, () -> 
            bookingService.cancelBooking("otheruser", 1L)
        );
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).delete(any(Booking.class));
    }
} 