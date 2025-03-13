package com.workbridge.workbridge_app.dto;

import lombok.Data;

@Data

public class ReviewRequestDTO {
    private Double rating;
    private String comment;
    private Long bookingId;
    private Long reviewedId;
    private Long reviewerId;
}
