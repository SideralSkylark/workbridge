package com.workbridge.workbridge_app.service.controller;

import com.workbridge.workbridge_app.common.response.ApiResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.security.SecurityUtil;
import com.workbridge.workbridge_app.service.dto.ServiceFeedDTO;
import com.workbridge.workbridge_app.service.dto.ServiceRequestDTO;
import com.workbridge.workbridge_app.service.dto.ServiceResponseDTO;
import com.workbridge.workbridge_app.service.dto.UpdateServiceDTO;
import com.workbridge.workbridge_app.service.service.ServiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceResponseDTO>> createService(
        @Valid @RequestBody ServiceRequestDTO serviceDTO) {
        return ResponseFactory.ok(
            serviceService.createService(serviceDTO, SecurityUtil.getAuthenticatedUsername()),
            "Service created successfully."
        );
    }

    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<ServiceResponseDTO>>> getMyServices(
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseFactory.ok(
            serviceService.getServicesByProvider(SecurityUtil.getAuthenticatedUsername(), pageable),
            "Fetched services successfully."
        );
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<Page<ServiceResponseDTO>>> getServicesByProvider(
        @PathVariable Long providerId,
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseFactory.ok(
            serviceService.getServicesByProvider(SecurityUtil.getAuthenticatedUsername(), pageable),
            "Fetched services by provider successfully."
        );
    }

    @GetMapping("/feed")
    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    public ResponseEntity<ApiResponse<Page<ServiceFeedDTO>>> getServiceFeed(
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseFactory.ok(
            serviceService.getServiceFeed(pageable),
            "Fetched service feed successfully."
        );
    }

    
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PutMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceResponseDTO>> updateService(
            @PathVariable Long serviceId,
            @Valid @RequestBody UpdateServiceDTO serviceDTO) {
                return ResponseFactory.ok(
                    serviceService.updateService(
                        serviceId, 
                        serviceDTO, 
                        SecurityUtil.getAuthenticatedUsername()),
                    "Service updated successfully."
                );
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceResponseDTO>> getServiceById(@PathVariable Long serviceId) {
        return ResponseFactory.ok(
            serviceService.getServiceById(serviceId),
            "Service retrieved successfully."
        );
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        serviceService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }
}