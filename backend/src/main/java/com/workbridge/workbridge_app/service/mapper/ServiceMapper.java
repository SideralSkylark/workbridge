package com.workbridge.workbridge_app.service.mapper;

import org.springframework.stereotype.Component;

import com.workbridge.workbridge_app.service.dto.ServiceRequestDTO;
import com.workbridge.workbridge_app.service.dto.ServiceResponseDTO;
import com.workbridge.workbridge_app.service.dto.UpdateServiceDTO;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;

@Component
public class ServiceMapper {

    public Service toEntity(ServiceRequestDTO dto, ApplicationUser provider) {
        Service entity = new Service();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setProvider(provider);
        return entity;
    }

    public ServiceResponseDTO toResponseDTO(Service entity) {
        ServiceResponseDTO dto = new ServiceResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setProviderId(entity.getProvider().getId());
        return dto;
    }

    public void updateEntityFromDTO(UpdateServiceDTO dto, Service entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
    }
}
