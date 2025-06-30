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
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing service listings and provider operations.
 * <p>
 * This controller exposes endpoints for:
 * <ul>
 *   <li>Creating, updating, and deleting services (for providers)</li>
 *   <li>Retrieving a provider's own services</li>
 *   <li>Retrieving services by provider ID</li>
 *   <li>Fetching the service feed (for seekers)</li>
 *   <li>Getting service details by ID</li>
 * </ul>
 *
 * <p>All endpoints return standardized API responses using {@link ApiResponse} and {@link ResponseFactory}.</p>
 *
 * <p>Role-based access control is enforced for all mutating and sensitive endpoints.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   POST   /api/v1/services                    // Create a new service (provider)
 *   GET    /api/v1/services/provider/me        // Get current provider's services
 *   GET    /api/v1/services/provider/{id}      // Get services by provider ID
 *   GET    /api/v1/services/feed               // Get service feed (seeker)
 *   PUT    /api/v1/services/{serviceId}        // Update a service (provider)
 *   GET    /api/v1/services/{serviceId}        // Get service details by ID
 *   DELETE /api/v1/services/{serviceId}        // Delete a service (provider)
 * </pre>
 *
 * @author Workbridge Team
 * @since 2025-06-26
 */
@RestController
@RequestMapping("api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    /**
     * Creates a new service for the authenticated provider.
     *
     * @param serviceDTO The service creation data
     * @return 200 OK with the created {@link ServiceResponseDTO} and a success message
     */
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceResponseDTO>> createService(
        @Valid @RequestBody ServiceRequestDTO serviceDTO) {
        return ResponseFactory.ok(
            serviceService.createService(serviceDTO, SecurityUtil.getAuthenticatedUsername()),
            "Service created successfully."
        );
    }

    /**
     * Retrieves a paginated list of services for the authenticated provider.
     *
     * @param pageable Pagination and sorting information
     * @return 200 OK with a page of {@link ServiceResponseDTO} objects and a success message
     */
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @GetMapping("/provider/me")
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

    /**
     * Retrieves a paginated list of services for a specific provider by ID.
     *
     * @param providerId The ID of the provider
     * @param pageable   Pagination and sorting information
     * @return 200 OK with a page of {@link ServiceResponseDTO} objects and a success message
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<Page<ServiceResponseDTO>>> getServicesByProvider(
        @PathVariable Long providerId,
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseFactory.ok(
            serviceService.getServicesByProviderId(providerId, pageable),
            "Fetched services by provider successfully."
        );
    }

    /**
     * Retrieves the service feed for seekers (paginated).
     *
     * @param pageable Pagination and sorting information
     * @return 200 OK with a page of {@link ServiceFeedDTO} objects and a success message
     */
    @GetMapping("/feed")
    @PreAuthorize("hasRole('SERVICE_SEEKER')")
    public ResponseEntity<ApiResponse<PagedModel<ServiceFeedDTO>>> getServiceFeed(
        @PageableDefault(
            page = 0,
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseFactory.ok(
            new PagedModel<>(serviceService.getServiceFeed(pageable)),
            "Fetched service feed successfully."
        );
    }

    /**
     * Updates a service for the authenticated provider.
     *
     * @param serviceId  The ID of the service to update
     * @param serviceDTO The update data
     * @return 200 OK with the updated {@link ServiceResponseDTO} and a success message
     */
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

    /**
     * Retrieves a service by its ID (for providers and seekers).
     *
     * @param serviceId The ID of the service
     * @return 200 OK with the {@link ServiceResponseDTO} and a success message
     */
    @PreAuthorize("hasAnyRole('SERVICE_PROVIDER', 'SERVICE_SEEKER')")
    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceResponseDTO>> getServiceById(@PathVariable Long serviceId) {
        return ResponseFactory.ok(
            serviceService.getServiceById(serviceId),
            "Service retrieved successfully."
        );
    }

    /**
     * Deletes a service by its ID (for providers).
     *
     * @param serviceId The ID of the service to delete
     * @return 204 No Content if deletion was successful
     */
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        serviceService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }
}
