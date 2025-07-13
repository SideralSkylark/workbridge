package com.workbridge.workbridge_app.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import com.workbridge.workbridge_app.service.dto.*;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.exception.ServiceNotFoundException;
import com.workbridge.workbridge_app.service.mapper.ServiceMapper;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.service.service.ServiceService;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;
import com.workbridge.workbridge_app.user.entity.UserRoleEntity;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.exception.UserNotServiceProviderException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

class ServiceServiceTest {

    private static final Long SERVICE_ID = 1L;
    private static final Long PROVIDER_ID = 100L;
    private static final String PROVIDER_USERNAME = "provider_user";
    private static final String SERVICE_TITLE = "Test Service";

    @Mock private ServiceRepository serviceRepository;
    @Mock private UserRepository userRepository;
    @Mock private ServiceMapper serviceMapper;

    @InjectMocks private ServiceService serviceService;

    private ApplicationUser provider;
    private UserRoleEntity userRoleEntity;
    private Service service;
    private ServiceRequestDTO requestDTO;
    private ServiceResponseDTO responseDTO;
    private UpdateServiceDTO updateDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        provider = new ApplicationUser();
        userRoleEntity = new UserRoleEntity();
        userRoleEntity.setRole(UserRole.SERVICE_PROVIDER);
        provider.setId(PROVIDER_ID);
        provider.setUsername(PROVIDER_USERNAME);
        provider.setRoles(Collections.singleton(userRoleEntity));

        service = new Service();
        service.setId(SERVICE_ID);
        service.setTitle(SERVICE_TITLE);
        service.setProvider(provider);

        requestDTO = new ServiceRequestDTO("New Service", "Description", 150.0);
        updateDTO = new UpdateServiceDTO("Updated Title", "Updated Desc", 200.0);
        responseDTO = new ServiceResponseDTO(SERVICE_ID, SERVICE_TITLE, "Desc", 100.0, PROVIDER_ID);
    }

    @Test
    void createService_shouldCreateSuccessfully() {
        when(userRepository.findByUsername(PROVIDER_USERNAME)).thenReturn(Optional.of(provider));
        when(serviceMapper.toEntity(requestDTO, provider)).thenReturn(service);
        when(serviceRepository.save(service)).thenReturn(service);
        when(serviceMapper.toResponseDTO(service)).thenReturn(responseDTO);

        ServiceResponseDTO result = serviceService.createService(requestDTO, PROVIDER_USERNAME);

        assertThat(result).isEqualTo(responseDTO);
        verify(serviceRepository).save(service);
    }

    @Test
    void createService_shouldThrowIfUserNotFound() {
        when(userRepository.findByUsername(PROVIDER_USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceService.createService(requestDTO, PROVIDER_USERNAME))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found");
    }

    @Test
    void createService_shouldThrowIfUserNotProvider() {
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setRole(UserRole.SERVICE_SEEKER);
        provider.setRoles(Collections.singleton(userRoleEntity));
        when(userRepository.findByUsername(PROVIDER_USERNAME)).thenReturn(Optional.of(provider));

        assertThatThrownBy(() -> serviceService.createService(requestDTO, PROVIDER_USERNAME))
            .isInstanceOf(UserNotServiceProviderException.class)
            .hasMessage("User is not a service provider");
    }

    @Test
    void getServicesByProvider_shouldReturnPage() {
        Page<Service> page = new PageImpl<>(Collections.singletonList(service));
        Page<ServiceResponseDTO> expected = new PageImpl<>(Collections.singletonList(responseDTO));
        Pageable pageable = PageRequest.of(0, 5);

        when(userRepository.findByUsername(PROVIDER_USERNAME)).thenReturn(Optional.of(provider));
        when(serviceRepository.findByProviderId(PROVIDER_ID, pageable)).thenReturn(page);
        when(serviceMapper.toResponseDTO(service)).thenReturn(responseDTO);

        Page<ServiceResponseDTO> result = serviceService.getServicesByProvider(PROVIDER_USERNAME, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(responseDTO);
    }

    @Test
    void updateService_shouldUpdateSuccessfully() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        when(serviceRepository.save(service)).thenReturn(service);
        when(serviceMapper.toResponseDTO(service)).thenReturn(responseDTO);

        ServiceResponseDTO result = serviceService.updateService(SERVICE_ID, updateDTO, PROVIDER_USERNAME);

        verify(serviceMapper).updateEntityFromDTO(updateDTO, service);
        verify(serviceRepository).save(service);
        assertThat(result).isEqualTo(responseDTO);
    }

    @Test
    void updateService_shouldThrowIfServiceNotFound() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceService.updateService(SERVICE_ID, updateDTO, PROVIDER_USERNAME))
            .isInstanceOf(ServiceNotFoundException.class)
            .hasMessage("Service not found");
    }

    @Test
    void updateService_shouldThrowIfUserNotOwner() {
        ApplicationUser other = new ApplicationUser();
        other.setUsername("other_user");
        service.setProvider(other);

        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));

        assertThatThrownBy(() -> serviceService.updateService(SERVICE_ID, updateDTO, PROVIDER_USERNAME))
            .isInstanceOf(UserNotServiceProviderException.class)
            .hasMessage("You are not the owner of this service");
    }

    @Test
    void getServiceById_shouldReturnDTO() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        when(serviceMapper.toResponseDTO(service)).thenReturn(responseDTO);

        ServiceResponseDTO result = serviceService.getServiceById(SERVICE_ID);
        assertThat(result).isEqualTo(responseDTO);
    }

    @Test
    void getServiceById_shouldThrowIfNotFound() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceService.getServiceById(SERVICE_ID))
            .isInstanceOf(ServiceNotFoundException.class)
            .hasMessage("Service not found.");
    }

    @Test
    void deleteService_shouldDeleteSuccessfully() {
        when(serviceRepository.existsById(SERVICE_ID)).thenReturn(true);

        serviceService.deleteService(SERVICE_ID);
        verify(serviceRepository).deleteById(SERVICE_ID);
    }

    @Test
    void deleteService_shouldThrowIfNotExists() {
        when(serviceRepository.existsById(SERVICE_ID)).thenReturn(false);

        assertThatThrownBy(() -> serviceService.deleteService(SERVICE_ID))
            .isInstanceOf(ServiceNotFoundException.class)
            .hasMessage("Service not found.");
    }
}
