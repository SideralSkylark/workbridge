package com.workbridge.workbridge_app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.workbridge.workbridge_app.entity.User;
import com.workbridge.workbridge_app.repository.UserRepository;

@Service
public class UserService {
    
    private UserRepository userRepository;

    public List<User> findAllUsers() { return userRepository.findAll(); }
}
