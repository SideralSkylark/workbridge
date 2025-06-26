package com.workbridge.workbridge_app.review.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class ReviewRequestDTO {

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5")
    private Double rating;

    @NotBlank(message = "Comment cannot be blank")
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Reviewed user ID is required")
    private Long reviewedId;

    @NotNull(message = "Reviewer user ID is required")
    private Long reviewerId;
}
