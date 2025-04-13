package com.workbridge.workbridge_app.controller;

import com.workbridge.workbridge_app.dto.ServiceDTO;
import com.workbridge.workbridge_app.dto.ServiceFeedDTO;
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
    public ResponseEntity<List<ServiceDTO>> getMyServices() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(serviceService.getServicesByProvider(username));
    }

    // TODO: implement new method in service controller and new tests
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceDTO>> getServicesByProvider(@PathVariable Long providerId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(serviceService.getServicesByProvider(username));
    }

    @GetMapping("/feed")
    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    public ResponseEntity<List<ServiceFeedDTO>> getServiceFeed() {
        return ResponseEntity.ok(serviceService.getServiceFeed());
    }

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PutMapping("/{serviceId}")
    public ResponseEntity<ServiceDTO> updateService(
            @PathVariable Long serviceId,
            @RequestBody ServiceDTO serviceDTO) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(serviceService.updateService(serviceId, serviceDTO, username));
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceService.getServiceById(serviceId));
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        serviceService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }
}