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

import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.booking.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.booking.repository.BookingRepository;
import com.workbridge.workbridge_app.review.dto.ReviewRequestDTO;
import com.workbridge.workbridge_app.review.dto.ReviewResponseDTO;
import com.workbridge.workbridge_app.review.entity.Review;
import com.workbridge.workbridge_app.review.repository.ReviewRepository;
import com.workbridge.workbridge_app.review.service.ReviewService;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private ApplicationUser testUser;
    private ApplicationUser testProvider;
    private Service testService;
    private Booking testBooking;
    private Review testReview;
    private ReviewRequestDTO reviewRequest;

    @BeforeEach
    void setUp() {
        // Create test user
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
        testBooking.setDate(LocalDateTime.now());

        // Create test review
        testReview = new Review();
        testReview.setId(1L);
        testReview.setRating(5.0);
        testReview.setComment("Great service!");
        testReview.setBooking(testBooking);
        testReview.setReviewer(testUser);
        testReview.setReviewed(testProvider);
        testReview.setCreatedAt(LocalDateTime.now());

        // Create test review request
        reviewRequest = new ReviewRequestDTO();
        reviewRequest.setBookingId(1L);
        reviewRequest.setRating(5.0);
        reviewRequest.setComment("Great service!");
        reviewRequest.setReviewerId(1L);
        reviewRequest.setReviewedId(2L);
    }

    @Test
    void reviewProvider_WhenValidRequest_ShouldCreateReview() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // Act
        ReviewResponseDTO result = reviewService.reviewProvider(reviewRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testReview.getRating(), result.getRating());
        assertEquals(testReview.getComment(), result.getComment());
        assertEquals(testUser, result.getReviewer());
        assertEquals(testProvider, result.getReviewed());
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void reviewProvider_WhenReviewerNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        reviewRequest.setReviewerId(999L);
        assertThrows(UserNotFoundException.class, () -> 
            reviewService.reviewProvider(reviewRequest)
        );
        verify(userRepository).findById(999L);
        verify(bookingRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void reviewProvider_WhenReviewedNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        reviewRequest.setReviewedId(999L);
        assertThrows(UserNotFoundException.class, () -> 
            reviewService.reviewProvider(reviewRequest)
        );
        verify(userRepository).findById(1L);
        verify(userRepository).findById(999L);
        verify(bookingRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void reviewProvider_WhenBookingNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        reviewRequest.setBookingId(999L);
        assertThrows(BookingNotFoundException.class, () -> 
            reviewService.reviewProvider(reviewRequest)
        );
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(bookingRepository).findById(999L);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getReviewsByProvider_WhenProviderExists_ShouldReturnReviews() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(testProvider));
        when(reviewRepository.findByReviewed_Id(2L)).thenReturn(Arrays.asList(testReview));

        // Act
        List<ReviewResponseDTO> result = reviewService.getReviewsByProvider(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testReview.getRating(), result.get(0).getRating());
        assertEquals(testReview.getComment(), result.get(0).getComment());
        assertEquals(testUser, result.get(0).getReviewer());
        assertEquals(testProvider, result.get(0).getReviewed());
        verify(userRepository).findById(2L);
        verify(reviewRepository).findByReviewed_Id(2L);
    }

    @Test
    void getReviewsByProvider_WhenProviderNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            reviewService.getReviewsByProvider(999L)
        );
        verify(userRepository).findById(999L);
        verify(reviewRepository, never()).findByReviewed_Id(any());
    }
} 