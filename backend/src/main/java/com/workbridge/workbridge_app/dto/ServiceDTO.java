package com.workbridge.workbridge_app.dto;

import lombok.Data;

@Data
public class ServiceDTO {
    private Long id;
    private String title;
    private String description;
    private double price;
    private Long providerId; // ID do ServiceProvider dono do serviço
}
