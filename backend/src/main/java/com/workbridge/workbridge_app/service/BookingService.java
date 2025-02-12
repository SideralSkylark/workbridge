package com.workbridge.workbridge_app.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.dto.BookingResponseDTO;
import com.workbridge.workbridge_app.repository.BookingRepository;

@Service
public class BookingService {
    
    private BookingRepository bookingRepository;

    public List<BookingResponseDTO> getUsersBookings(String username) {
        List<BookingResponseDTO> list = new ArrayList<>();
        return list;
    }
}
