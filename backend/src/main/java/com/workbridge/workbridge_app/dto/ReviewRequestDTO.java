package com.workbridge.workbridge_app.dto;

import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.Booking;

import lombok.Data;

@Data

public class ReviewRequestDTO {
    private Double rating;
    private String comment;
    private Long bookingId;
    private Long reviewedId;
    private Long reviewerId;
}
