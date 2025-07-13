package com.workbridge.workbridge_app.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {
    private Long id;
    private Long providerId;
    private String seekerName;
    private Long serviceId;
    private String serviceTitle;
    private String serviceDescription;
    private Double price;
    private String providerName;
    private LocalDateTime date;
    private String status;
}
