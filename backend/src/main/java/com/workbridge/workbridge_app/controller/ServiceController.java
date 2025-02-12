package com.workbridge.workbridge_app.controller;

import com.workbridge.workbridge_app.dto.ServiceDTO;
import com.workbridge.workbridge_app.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PostMapping
    public ResponseEntity<ServiceDTO> createService(@RequestBody ServiceDTO serviceDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(serviceService.createService(serviceDTO, username));
    }

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @GetMapping("/me")
    public ResponseEntity<List<ServiceDTO>> getServicesByProvider() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(serviceService.getServicesByProvider(username));
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        serviceService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }
}
