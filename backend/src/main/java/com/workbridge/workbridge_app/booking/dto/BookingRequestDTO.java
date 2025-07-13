package com.workbridge.workbridge_app.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDTO {

    @NotNull(message = "Service ID is required.")
    private Long serviceId;

    @NotNull(message = "Date is required.")
    @Future(message = "Booking date must be in the future.")
    private LocalDateTime date;
}
