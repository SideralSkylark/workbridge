package com.workbridge.workbridge_app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponseDTO {
    private Long id;
    private String title;
    private String description;
    private double price;
    private Long providerId; 
}
