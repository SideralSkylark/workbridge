package com.workbridge.workbridge_app.dto;

import lombok.Data;
import java.util.List;

@Data
public class RegisterRequestDTO {
    private String username;
    private String email;
    private String password;
    private List<String> roles;
    private String status;
}
