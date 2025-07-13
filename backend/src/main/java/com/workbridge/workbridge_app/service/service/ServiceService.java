package com.workbridge.workbridge_app.service.service;

import com.workbridge.workbridge_app.service.dto.ServiceFeedDTO;
import com.workbridge.workbridge_app.service.dto.ServiceRequestDTO;
import com.workbridge.workbridge_app.service.dto.ServiceResponseDTO;
import com.workbridge.workbridge_app.service.dto.UpdateServiceDTO;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.exception.ServiceNotFoundException;
import com.workbridge.workbridge_app.service.mapper.ServiceMapper;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.exception.UserNotServiceProviderException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Service layer for managing service-related operations in the application.
 * <p>
 * This class provides business logic for creating, updating, retrieving, and deleting services,
 * as well as fetching service feeds and verifying service providers. It acts as an intermediary
 * between the controller layer and the data access layer (repositories), ensuring that all
 * operations are performed with proper validation, exception handling, and mapping between
 * entities and DTOs.
 * </p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Create new services for verified providers</li>
 *   <li>Retrieve services by provider or provider ID with pagination</li>
 *   <li>Fetch a paginated feed of available services with provider info</li>
 *   <li>Update and delete services, ensuring only the owner can modify</li>
 *   <li>Map between service entities and DTOs</li>
 *   <li>Verify user roles and handle exceptions for not found or unauthorized access</li>
 * </ul>
 *
 * <p>All methods throw well-defined exceptions for error scenarios, and are fully documented.</p>
 *
 * @author Sidik
 * @since 2025-06-26
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceMapper serviceMapper;

    /**
     * Creates a new service for the given provider.
     *
     * @param serviceDTO the service request data
     * @param username the username of the provider
     * @return the created service as a response DTO
     * @throws UserNotFoundException if the provider is not found
     * @throws UserNotServiceProviderException if the user is not a service provider
     */
    public ServiceResponseDTO createService(ServiceRequestDTO serviceDTO, String username) {
        log.debug("Creating service for user='{}' with data={}", username, serviceDTO);
        ApplicationUser provider = getVerifiedProvider(username);
        Service service = serviceMapper.toEntity(serviceDTO, provider);
        Service saved = serviceRepository.save(service);
        log.info("Service created successfully. id={}, title={}, provider={}",
        saved.getId(),
        saved.getTitle(),
        username);
        return serviceMapper.toResponseDTO(saved);
    }

    /**
     * Retrieves a paginated list of services for a given provider username.
     *
     * @param username the provider's username
     * @param pageable the pagination information
     * @return a page of service response DTOs
     * @throws UserNotFoundException if the provider is not found
     * @throws UserNotServiceProviderException if the user is not a service provider
     */
    public Page<ServiceResponseDTO> getServicesByProvider(String username, Pageable pageable) {
        log.debug("Fetching services for provider username={}", username);
        ApplicationUser provider = getVerifiedProvider(username);
        Page<ServiceResponseDTO> result = serviceRepository.findByProviderId(provider.getId(), pageable)
                .map(serviceMapper::toResponseDTO);
        log.info("Found {} services for provider '{}'", result.getTotalElements(), username);
        return result;
    }

    /**
     * Retrieves a paginated list of services for a given provider ID.
     *
     * @param providerId the provider's user ID
     * @param pageable the pagination information
     * @return a page of service response DTOs
     * @throws UserNotFoundException if the provider is not found
     * @throws UserNotServiceProviderException if the user is not a service provider
     */
    public Page<ServiceResponseDTO> getServicesByProviderId(Long providerId, Pageable pageable) {
        log.debug("Fetching services for providerId={}", providerId);
        ApplicationUser provider = getVerifiedProviderById(providerId);
        Page<ServiceResponseDTO> result = serviceRepository.findByProviderId(provider.getId(), pageable)
                .map(serviceMapper::toResponseDTO);
        log.info("Found {} services for providerId={}", result.getTotalElements(), providerId);
        return result;
    }

    /**
     * Retrieves a paginated feed of available services, including provider info.
     *
     * @param pageable the pagination information
     * @return a page of service feed DTOs
     */
    public Page<ServiceFeedDTO> getServiceFeed(Pageable pageable) {
        log.info("Fetching service feed with pagination: {}", pageable);
        Page<ServiceFeedDTO> feed = serviceRepository.findServiceFeed(pageable)
                .map(projection -> new ServiceFeedDTO(
                    new ServiceResponseDTO(
                        projection.getServiceId(),
                        projection.getTitle(),
                        projection.getDescription(),
                        projection.getPrice(),
                        projection.getProviderId()
                    ),
                    projection.getProviderRating(),
                    projection.getProviderUsername(),
                    projection.getProviderEmail()
                ));
        log.info("Fetched {} items in service feed", feed.getTotalElements());
        return feed;
    }

    /**
     * Updates an existing service if the requesting user is the owner.
     *
     * @param serviceId the ID of the service to update
     * @param serviceDTO the update data
     * @param username the username of the provider
     * @return the updated service as a response DTO
     * @throws ServiceNotFoundException if the service is not found
     * @throws UserNotServiceProviderException if the user is not the owner
     */
    public ServiceResponseDTO updateService(
        Long serviceId,
        UpdateServiceDTO serviceDTO,
        String username) {
        log.debug("Updating serviceId={} by user={}", serviceId, username);
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.warn("Service not found: id={}", serviceId);
                    return new ServiceNotFoundException("Service not found");
                });

        if (!service.getProvider().getUsername().equals(username)) {
            log.warn("Unauthorized update attempt by '{}'. Owner is '{}'",
            username,
            service.getProvider().getUsername());
            throw new UserNotServiceProviderException("You are not the owner of this service");
        }

        serviceMapper.updateEntityFromDTO(serviceDTO, service);

        Service updated = serviceRepository.save(service);
        log.info("Service updated successfully: id={}, title={}", updated.getId(), updated.getTitle());

        return serviceMapper.toResponseDTO(service);
    }

    /**
     * Retrieves a service by its ID.
     *
     * @param serviceId the ID of the service
     * @return the service as a response DTO
     * @throws ServiceNotFoundException if the service is not found
     */
    public ServiceResponseDTO getServiceById(Long serviceId) {
        log.debug("Fetching service by id={}", serviceId);
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.warn("Service not found: id={}", serviceId);
                    return new ServiceNotFoundException("Service not found.");
                });
        log.info("Service fetched successfully: id={}, title={}", service.getId(), service.getTitle());

        return serviceMapper.toResponseDTO(service);
    }

    /**
     * Deletes a service by its ID.
     *
     * @param serviceId the ID of the service to delete
     * @throws ServiceNotFoundException if the service is not found
     */
    public void deleteService(Long serviceId) {
        log.debug("Deleting service with id={}", serviceId);
        if (!serviceRepository.existsById(serviceId)) {
            log.warn("Attempted to delete non-existent service: id={}", serviceId);
            throw new ServiceNotFoundException("Service not found.");
        }
        serviceRepository.deleteById(serviceId);
        log.info("Service deleted successfully: id={}", serviceId);
    }

    /**
     * Retrieves and verifies a user as a service provider by username.
     *
     * @param username the username to verify
     * @return the verified ApplicationUser
     * @throws UserNotFoundException if the user is not found
     * @throws UserNotServiceProviderException if the user is not a service provider
     */
    private ApplicationUser getVerifiedProvider(String username) {
        ApplicationUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: username={}", username);
                    return new UserNotFoundException("User not found");
                });

        if (!user.hasRole(UserRole.SERVICE_PROVIDER)) {
            log.warn("User is not a service provider: username={}", username);
            throw new UserNotServiceProviderException("User is not a service provider");
        }

        return user;
    }

    /**
     * Retrieves and verifies a user as a service provider by user ID.
     *
     * @param id the user ID to verify
     * @return the verified ApplicationUser
     * @throws UserNotFoundException if the user is not found
     * @throws UserNotServiceProviderException if the user is not a service provider
     */
    private ApplicationUser getVerifiedProviderById(Long id) {
        ApplicationUser user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: id={}", id);
                    return new UserNotFoundException("User not found");
                });

        if (!user.hasRole(UserRole.SERVICE_PROVIDER)) {
            log.warn("User is not a service provider: id={}", id);
            throw new UserNotServiceProviderException("User is not a service provider");
        }

        return user;
    }
}
