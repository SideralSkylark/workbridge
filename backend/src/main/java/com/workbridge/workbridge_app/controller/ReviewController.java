package com.workbridge.workbridge_app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workbridge.workbridge_app.dto.ReviewRequestDTO;
import com.workbridge.workbridge_app.dto.ReviewResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.exception.BookingNotFoundException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @GetMapping()
    public ResponseEntity<?> getReviewsByProvider(@RequestBody ReviewRequestDTO reviewRequestDTO) {
        try {
            List<ReviewResponseDTO> reviews = reviewService.getReviewsByProvider(reviewRequestDTO.getReviewedId());
            return ResponseEntity.ok(reviews);
        } catch (UserNotFoundException userNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Provider not found.");
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }

    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    @PostMapping("/review")
    public ResponseEntity<?> reviewServiceProvider(@RequestBody ReviewRequestDTO reviewRequestDTO) {
        try {
            ReviewResponseDTO result = reviewService.reviewProvider(reviewRequestDTO);
            return ResponseEntity.ok(result);
        } catch (BookingNotFoundException bookingNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("booking not found.");
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error ocured when atributing your review to the service provider.");
        }
    }
}
