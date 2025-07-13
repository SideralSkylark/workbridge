package com.workbridge.workbridge_app.review;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.booking.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.booking.repository.BookingRepository;
import com.workbridge.workbridge_app.review.dto.ReviewRequestDTO;
import com.workbridge.workbridge_app.review.dto.ReviewResponseDTO;
import com.workbridge.workbridge_app.review.entity.Review;
import com.workbridge.workbridge_app.review.repository.ReviewRepository;
import com.workbridge.workbridge_app.review.service.ReviewService;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

class ReviewServiceTest {

    private static final Long REVIEWER_ID = 1L;
    private static final Long REVIEWED_ID = 2L;
    private static final Long BOOKING_ID = 10L;
    private static final Long REVIEW_ID = 99L;
    private static final double RATING = 5;
    private static final String COMMENT = "Great!";
    private static final int PAGE_SIZE = 10;
    private static final int PAGE_NUMBER = 0;
    private static final Long UNKNOWN_USER_ID = 999L;

    @Mock private ReviewRepository reviewRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ReviewService reviewService;

    private ApplicationUser reviewer;
    private ApplicationUser reviewed;
    private Booking booking;
    private Review review;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        reviewer = new ApplicationUser();
        reviewer.setId(REVIEWER_ID);

        reviewed = new ApplicationUser();
        reviewed.setId(REVIEWED_ID);

        booking = new Booking();
        booking.setId(BOOKING_ID);

        review = new Review();
        review.setId(REVIEW_ID);
        review.setReviewer(reviewer);
        review.setReviewed(reviewed);
        review.setBooking(booking);
        review.setRating(RATING);
        review.setComment(COMMENT);
    }

    @Test
    void getReviewsByProvider_shouldReturnPageOfReviews() {
        PageRequest pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        when(userRepository.findById(REVIEWED_ID)).thenReturn(Optional.of(reviewed));
        when(reviewRepository.findByReviewed_Id(REVIEWED_ID, pageable))
            .thenReturn(new PageImpl<>(Collections.singletonList(review)));

        Page<ReviewResponseDTO> result = reviewService.getReviewsByProvider(REVIEWED_ID, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findById(REVIEWED_ID);
        verify(reviewRepository).findByReviewed_Id(REVIEWED_ID, pageable);
    }

    @Test
    void getReviewsByProvider_shouldThrowIfUserNotFound() {
        when(userRepository.findById(UNKNOWN_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewsByProvider(UNKNOWN_USER_ID, PageRequest.of(PAGE_NUMBER, PAGE_SIZE)))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("Provider not found");
    }

    @Test
    void reviewProvider_shouldSaveReviewSuccessfully() {
        ReviewRequestDTO dto = new ReviewRequestDTO(RATING, COMMENT, BOOKING_ID, REVIEWED_ID, REVIEWER_ID);

        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(reviewer));
        when(userRepository.findById(REVIEWED_ID)).thenReturn(Optional.of(reviewed));
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDTO result = reviewService.reviewProvider(dto);

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(RATING);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void reviewProvider_shouldThrowIfReviewerNotFound() {
        ReviewRequestDTO dto = new ReviewRequestDTO(RATING, COMMENT, BOOKING_ID, REVIEWED_ID, REVIEWER_ID);
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.reviewProvider(dto))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("Reviewer not found");
    }

    @Test
    void reviewProvider_shouldThrowIfReviewedNotFound() {
        ReviewRequestDTO dto = new ReviewRequestDTO(RATING, COMMENT, BOOKING_ID, REVIEWED_ID, REVIEWER_ID);

        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(reviewer));
        when(userRepository.findById(REVIEWED_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.reviewProvider(dto))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("Reviewed not found");
    }

    @Test
    void reviewProvider_shouldThrowIfBookingNotFound() {
        ReviewRequestDTO dto = new ReviewRequestDTO(RATING, COMMENT, BOOKING_ID, REVIEWED_ID, REVIEWER_ID);

        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(reviewer));
        when(userRepository.findById(REVIEWED_ID)).thenReturn(Optional.of(reviewed));
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.reviewProvider(dto))
            .isInstanceOf(BookingNotFoundException.class)
            .hasMessage("Booking not found");
    }

    @Test
    void hasUserReviewedBooking_shouldReturnTrueIfExists() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBooking_Id(BOOKING_ID)).thenReturn(true);

        boolean result = reviewService.hasUserReviewedBooking(BOOKING_ID);
        assertThat(result).isTrue();
    }

    @Test
    void hasUserReviewedBooking_shouldReturnFalseIfNotExists() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBooking_Id(BOOKING_ID)).thenReturn(false);

        boolean result = reviewService.hasUserReviewedBooking(BOOKING_ID);
        assertThat(result).isFalse();
    }

    @Test
    void hasUserReviewedBooking_shouldThrowIfBookingNotFound() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.hasUserReviewedBooking(BOOKING_ID))
            .isInstanceOf(BookingNotFoundException.class)
            .hasMessage("Booking not found");
    }
}
