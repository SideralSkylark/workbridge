package com.workbridge.workbridge_app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingRequestDTO {
    private Long serviceId;   
    private LocalDateTime date; 
}
