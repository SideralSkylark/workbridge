package com.workbridge.workbridge_app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.service.UserService;

@RestController
@RequestMapping("api/v1/Users")
public class UserController {
    
    private UserService userService;

    //Exemplo de um endpoint, podes tambem usar PostMapping Create, Delete, etc.
    @GetMapping
    public ResponseEntity<List<ApplicationUser>> getAllUsers() {
        try {
            List<ApplicationUser> users = userService.findAllUsers();
            if (users == null || users.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
