package com.workbridge.workbridge_app.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateServiceDTO {
    
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Double price;
}
