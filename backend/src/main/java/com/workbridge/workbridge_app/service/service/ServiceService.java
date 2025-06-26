package com.workbridge.workbridge_app.service.service;

import com.workbridge.workbridge_app.service.dto.ServiceFeedDTO;
import com.workbridge.workbridge_app.service.dto.ServiceRequestDTO;
import com.workbridge.workbridge_app.service.dto.ServiceResponseDTO;
import com.workbridge.workbridge_app.service.dto.UpdateServiceDTO;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.exception.ServiceNotFoundException;
import com.workbridge.workbridge_app.service.mapper.ServiceMapper;
import com.workbridge.workbridge_app.service.projection.ServiceFeedProjection;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.exception.UserNotServiceProviderException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceMapper serviceMapper;

    public ServiceResponseDTO createService(ServiceRequestDTO serviceDTO, String username) {
        ApplicationUser provider = getVerifiedProvider(username);
        Service service = serviceMapper.toEntity(serviceDTO, provider);
        Service saved = serviceRepository.save(service);
        return serviceMapper.toResponseDTO(saved);
    }

    public Page<ServiceResponseDTO> getServicesByProvider(String username, Pageable pageable) {
        ApplicationUser provider = getVerifiedProvider(username);
        return serviceRepository.findByProviderId(provider.getId(), pageable)
                .map(serviceMapper::toResponseDTO);
    }

    public Page<ServiceResponseDTO> getServicesByProviderId(Long providerId, Pageable pageable) {
        return serviceRepository.findByProviderId(providerId, pageable)
                .map(serviceMapper::toResponseDTO);
    }

    public Page<ServiceFeedDTO> getServiceFeed(Pageable pageable) {
        return serviceRepository.findServiceFeed(pageable)
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
    }
    
    public ServiceResponseDTO updateService(
        Long serviceId, 
        UpdateServiceDTO serviceDTO, 
        String username) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found"));

        if (!service.getProvider().getUsername().equals(username)) {
            throw new UserNotServiceProviderException("You are not the owner of this service");
        }   

        serviceMapper.updateEntityFromDTO(serviceDTO, service);

        serviceRepository.save(service);

        return serviceMapper.toResponseDTO(service);
    }

    public ServiceResponseDTO getServiceById(Long serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found."));

        return serviceMapper.toResponseDTO(service);
    }

    public void deleteService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            throw new ServiceNotFoundException("Service not found.");
        }
        serviceRepository.deleteById(serviceId);
    }

    private ApplicationUser getVerifiedProvider(String username) {
        ApplicationUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.hasRole(UserRole.SERVICE_PROVIDER)) {
            throw new UserNotServiceProviderException("User is not a service provider");
        }

        return user;
    }
}
