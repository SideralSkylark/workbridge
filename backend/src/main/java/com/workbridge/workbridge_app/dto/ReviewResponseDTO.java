package com.workbridge.workbridge_app.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewResponseDTO {
    private Long id;
    private Double rating;
    private String comment;
    private UserResponseDTO reviewer;
    private UserResponseDTO reviewed;
    private LocalDateTime createdAt;
}
