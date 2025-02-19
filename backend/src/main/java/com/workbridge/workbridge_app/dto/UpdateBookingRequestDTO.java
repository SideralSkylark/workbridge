package com.workbridge.workbridge_app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateBookingRequestDTO {
    private Long bookingId;      
    private LocalDateTime date;  
}
