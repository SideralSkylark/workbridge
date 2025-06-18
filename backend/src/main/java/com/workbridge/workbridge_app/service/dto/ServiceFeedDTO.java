package com.workbridge.workbridge_app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceFeedDTO {
    private ServiceDTO service;
    private Double providerRating;
    private String providerUsername;
    private String providerEmail;
}