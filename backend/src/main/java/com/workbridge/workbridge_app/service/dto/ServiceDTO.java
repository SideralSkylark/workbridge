package com.workbridge.workbridge_app.service.dto;

import lombok.Data;

@Data
public class ServiceDTO {
    private Long id;
    private String title;
    private String description;
    private double price;
    private Long providerId; 
}
