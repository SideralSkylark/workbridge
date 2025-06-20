package com.workbridge.workbridge_app.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UpdateBookingRequestDTO {
    private Long bookingId;      
    private LocalDateTime date;  
}
