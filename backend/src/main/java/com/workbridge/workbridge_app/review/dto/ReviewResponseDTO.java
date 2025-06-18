package com.workbridge.workbridge_app.review.dto;

import java.time.LocalDateTime;

import com.workbridge.workbridge_app.user.entity.ApplicationUser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private Double rating;
    private String comment;
    private ApplicationUser reviewer;
    private ApplicationUser reviewed;
    private LocalDateTime createdAt;
}
