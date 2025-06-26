package com.workbridge.workbridge_app.booking.mapper;

import org.springframework.stereotype.Component;

import com.workbridge.workbridge_app.booking.dto.BookingRequestDTO;
import com.workbridge.workbridge_app.booking.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.service.entity.Service;

@Component
public class BookingMapper {

    public BookingResponseDTO toDTO(Booking booking) {
        return new BookingResponseDTO(
            booking.getId(),
            booking.getService().getProvider().getId(),
            booking.getSeeker().getUsername(),
            booking.getService().getId(),
            booking.getService().getTitle(),
            booking.getService().getDescription(),
            booking.getService().getPrice(),
            booking.getService().getProvider().getUsername(),
            booking.getDate(),
            booking.getStatus().name()
        );
    }

    public Booking toEntity(BookingRequestDTO dto, ApplicationUser seeker, Service service) {
        Booking booking = new Booking();
        booking.setSeeker(seeker);
        booking.setService(service);
        booking.setDate(dto.getDate());
        return booking;
    }
}
