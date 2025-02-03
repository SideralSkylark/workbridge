package com.workbridge.workbridge_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workbridge.workbridge_app.dto.ReviewRequestDTO;
import com.workbridge.workbridge_app.dto.ReviewResponseDTO;
import com.workbridge.workbridge_app.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    //TODO:implement get reviewsByProvider
    @GetMapping()
    public ResponseEntity<ReviewResponseDTO> getReviewsByProvider(@RequestParam ReviewRequestDTO reviewRequestDTO) {
        ReviewResponseDTO r = new ReviewResponseDTO();
        return ResponseEntity.ok(r);
    }

    @PreAuthorize("hasrole('ROLE_SERVICE_SEEKER')")
    @PostMapping("/review")
    public ResponseEntity<?> reviewServiceProvider(@RequestParam ReviewRequestDTO reviewRequestDTO) {
        try {
            boolean result = reviewService.reviewProvider(reviewRequestDTO);
            if (result) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Review atributed to the user successfully.");
            } 
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error ocured when atributing your review to the service provider.")
        }
    }

    //TODO:implement pagination on an endpoint to get reviews by a  list of providers
    
}
