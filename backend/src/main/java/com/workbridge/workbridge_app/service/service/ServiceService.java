package com.workbridge.workbridge_app.service.service;

import com.workbridge.workbridge_app.review.repository.ReviewRepository;
import com.workbridge.workbridge_app.service.dto.ServiceDTO;
import com.workbridge.workbridge_app.service.dto.ServiceFeedDTO;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.mapper.ServiceMapper;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;
import com.workbridge.workbridge_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ServiceMapper serviceMapper;

    public ServiceDTO createService(ServiceDTO serviceDTO, String username) {
        // Obtém o usuário autenticado
        ApplicationUser provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verifica se o usuário tem a role de PROVIDER
        if (!provider.hasRole(UserRole.SERVICE_PROVIDER)) {
            throw new RuntimeException("User is not a service provider");
        }

        // Criar e salvar o serviço
        Service service = new Service();
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setPrice(serviceDTO.getPrice());
        service.setProvider(provider);
        Service savedService = serviceRepository.save(service);
        
        serviceDTO.setId(savedService.getId());
        return serviceDTO;
    }

    public List<ServiceDTO> getServicesByProvider(String username) {
        // Obtém o usuário autenticado
        ApplicationUser provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verifica se o usuário tem a role de PROVIDER
        if (!provider.hasRole(UserRole.SERVICE_PROVIDER)) {
            throw new RuntimeException("User is not a service provider");
        }

        // Busca os serviços criados por esse usuário
        return serviceRepository.findByProviderId(provider.getId()).stream().map(service -> {
            ServiceDTO dto = new ServiceDTO();
            dto.setId(service.getId());
            dto.setTitle(service.getTitle());
            dto.setDescription(service.getDescription());
            dto.setPrice(service.getPrice());
            dto.setProviderId(service.getProvider().getId());
            return dto;
        }).collect(Collectors.toList());
    }

    //TODO: implement tests for the new method
    public List<ServiceFeedDTO> getServiceFeed() {
        return serviceRepository.findAll().stream()
                .map(service -> {
                    Double avgRating = reviewRepository.findAverageRatingByProviderId(service.getProvider().getId());
                    String name = service.getProvider().getUsername();
                    String email = service.getProvider().getEmail();
                    return new ServiceFeedDTO(
                        serviceMapper.toDTO(service), 
                        avgRating != null ? avgRating : 0.0,
                        name,
                        email);
                })
                .sorted(Comparator.comparingDouble(ServiceFeedDTO::getProviderRating).reversed())
                .collect(Collectors.toList());
    }


    public ServiceDTO updateService(Long serviceId, ServiceDTO serviceDTO, String username) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Verifica se o usuário autenticado é o provedor dono do serviço
        if (!service.getProvider().getUsername().equals(username)) {
            throw new RuntimeException("You are not the owner of this service");
        }

        // Atualiza os dados do serviço
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setPrice(serviceDTO.getPrice());

        serviceRepository.save(service);

        // Retorna o DTO atualizado
        return mapToDTO(service);
    }

    public ServiceDTO getServiceById(Long serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        return mapToDTO(service);
    }

    public void deleteService(Long serviceId) {
        serviceRepository.deleteById(serviceId);
    }

    private ServiceDTO mapToDTO(Service service) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setTitle(service.getTitle());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setProviderId(service.getProvider().getId());
        return dto;
    }

}
