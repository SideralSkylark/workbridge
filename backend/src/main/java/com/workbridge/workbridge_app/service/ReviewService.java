package com.workbridge.workbridge_app.service;

import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.dto.ReviewRequestDTO;
import com.workbridge.workbridge_app.repository.ReviewRepository;

@Service
public class ReviewService {
    
    private ReviewRepository repository;
    //TODO: implement the logic to allow reviewing a service provider using the reviewRequestDTO
    public boolean reviewProvider(ReviewRequestDTO reviewRequestDTO) {
        return true;
    }
}
