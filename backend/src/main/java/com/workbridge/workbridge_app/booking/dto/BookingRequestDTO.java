package com.workbridge.workbridge_app.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingRequestDTO {
    private Long serviceId;   
    private LocalDateTime date; 
}
