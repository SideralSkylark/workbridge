package com.workbridge.workbridge_app.service;

import com.workbridge.workbridge_app.dto.ServiceDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.Service;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.repository.ServiceRepository;
import com.workbridge.workbridge_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceService {
    
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

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

    serviceRepository.save(service);
    serviceDTO.setId(service.getId());
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

    public void deleteService(Long serviceId) {
        serviceRepository.deleteById(serviceId);
    }
}
