package com.workbridge.workbridge_app.dto;

import com.workbridge.workbridge_app.entity.ApplicationUser;

import lombok.Data;

@Data

public class ReviewRequestDTO {
    private Double rating;
    private String comment;
    private ApplicationUser reviewed;
}
