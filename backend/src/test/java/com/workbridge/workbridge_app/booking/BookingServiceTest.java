package com.workbridge.workbridge_app.booking;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import com.workbridge.workbridge_app.booking.dto.*;
import com.workbridge.workbridge_app.booking.entity.*;
import com.workbridge.workbridge_app.booking.exception.*;
import com.workbridge.workbridge_app.booking.mapper.BookingMapper;
import com.workbridge.workbridge_app.booking.repository.BookingRepository;
import com.workbridge.workbridge_app.booking.service.BookingService;
import com.workbridge.workbridge_app.review.repository.ReviewRepository;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.exception.ServiceNotFoundException;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.auth.exception.UserNotAuthorizedException;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

public class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private UserRepository userRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private ReviewRepository reviewRepository;

    @InjectMocks private BookingService bookingService;

    private ApplicationUser seeker;
    private Service service;
    private Booking booking;
    private BookingRequestDTO bookingRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        seeker = new ApplicationUser();
        seeker.setId(1L);
        seeker.setUsername("user1");

        service = new Service();
        service.setId(2L);

        booking = new Booking();
        booking.setId(3L);
        booking.setSeeker(seeker);
        booking.setService(service);

        bookingRequest = new BookingRequestDTO();
        bookingRequest.setServiceId(2L);
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(seeker));
        when(serviceRepository.findById(2L)).thenReturn(Optional.of(service));
        when(bookingMapper.toEntity(bookingRequest, seeker, service)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        BookingResponseDTO dto = new BookingResponseDTO();
        when(bookingMapper.toDTO(booking)).thenReturn(dto);

        BookingResponseDTO result = bookingService.createBooking("user1", bookingRequest);

        assertNotNull(result);
        verify(bookingRepository).save(booking);
    }

    @Test
    void shouldThrowIfUserNotFoundWhenCreatingBooking() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> bookingService.createBooking("user1", bookingRequest));
    }

    @Test
    void shouldThrowIfServiceNotFoundWhenCreatingBooking() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(seeker));
        when(serviceRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ServiceNotFoundException.class, () -> bookingService.createBooking("user1", bookingRequest));
    }

    @Test
    void shouldUpdateBookingSuccessfully() {
        UpdateBookingRequestDTO updateRequest = new UpdateBookingRequestDTO();
        updateRequest.setDate(LocalDateTime.now().plusDays(1));

        booking.setDate(LocalDateTime.now());

        when(bookingRepository.findById(3L)).thenReturn(Optional.of(booking));
        BookingResponseDTO dto = new BookingResponseDTO();
        when(bookingMapper.toDTO(booking)).thenReturn(dto);
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDTO result = bookingService.updateBooking("user1", 3L, updateRequest);
        assertNotNull(result);
        verify(bookingRepository).save(booking);
    }

    @Test
    void shouldThrowWhenUpdatingBookingNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> bookingService.updateBooking("user1", 999L, new UpdateBookingRequestDTO()));
    }

    @Test
    void shouldThrowWhenUnauthorizedUserTriesToUpdateBooking() {
        booking.setSeeker(new ApplicationUser());
        booking.getSeeker().setUsername("otherUser");
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(booking));
        assertThrows(UserNotAuthorizedException.class, () -> bookingService.updateBooking("user1", 3L, new UpdateBookingRequestDTO()));
    }

    @Test
    void shouldCancelBookingSuccessfully() {
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(booking));

        booking.setSeeker(seeker);
        booking.setService(service);

        bookingService.cancelBooking("user1", 3L);

        verify(reviewRepository).deleteByBooking_Id(3L);
        verify(bookingRepository).delete(booking);
    }

    @Test
    void shouldReturnBookingsForSeeker() {
        Page<Booking> bookings = new PageImpl<>(java.util.List.of(booking));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(seeker));
        when(bookingRepository.findBySeeker_Id(1L, Pageable.unpaged())).thenReturn(bookings);
        when(bookingMapper.toDTO(booking)).thenReturn(new BookingResponseDTO());

        Page<BookingResponseDTO> result = bookingService.getUsersBookings("user1", Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldReturnBookingsForProvider() {
        Page<Booking> bookings = new PageImpl<>(java.util.List.of(booking));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(seeker));
        when(bookingRepository.findByService_Provider(seeker, Pageable.unpaged())).thenReturn(bookings);
        when(bookingMapper.toDTO(booking)).thenReturn(new BookingResponseDTO());

        Page<BookingResponseDTO> result = bookingService.getBookingsByProviderId("user1", Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }
}
