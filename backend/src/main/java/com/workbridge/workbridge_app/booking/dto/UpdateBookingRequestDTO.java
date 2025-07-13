package com.workbridge.workbridge_app.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingRequestDTO {
    @NotNull(message = "Date is required.")
    @Future(message = "Booking date must be in the future.")
    private LocalDateTime date;
}
