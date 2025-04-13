package com.workbridge.workbridge_app.mapper;

import com.workbridge.workbridge_app.dto.ServiceDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceMapper {

    public ServiceDTO toDTO(Service service) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setTitle(service.getTitle());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setProviderId(service.getProvider() != null ? service.getProvider().getId() : null);
        return dto;
    }

    public Service toEntity(ServiceDTO dto, ApplicationUser provider) {
        Service service = new Service();
        service.setId(dto.getId());
        service.setTitle(dto.getTitle());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());
        service.setProvider(provider); // assumes provider is fetched beforehand
        return service;
    }
}