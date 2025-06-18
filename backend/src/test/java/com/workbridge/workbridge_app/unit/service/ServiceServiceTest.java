package com.workbridge.workbridge_app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workbridge.workbridge_app.review.repository.ReviewRepository;
import com.workbridge.workbridge_app.service.dto.ServiceDTO;
import com.workbridge.workbridge_app.service.dto.ServiceFeedDTO;
import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.mapper.ServiceMapper;
import com.workbridge.workbridge_app.service.repository.ServiceRepository;
import com.workbridge.workbridge_app.service.service.ServiceService;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;
import com.workbridge.workbridge_app.user.entity.UserRoleEntity;
import com.workbridge.workbridge_app.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ServiceMapper serviceMapper;

    @InjectMocks
    private ServiceService serviceService;

    private ApplicationUser testProvider;
    private UserRoleEntity providerRole;
    private Service testService;
    private ServiceDTO serviceDTO;

    @BeforeEach
    void setUp() {
        // Create provider role
        providerRole = new UserRoleEntity();
        providerRole.setRole(UserRole.SERVICE_PROVIDER);

        // Create test provider
        testProvider = new ApplicationUser();
        testProvider.setId(1L);
        testProvider.setUsername("provider");
        testProvider.setEmail("provider@example.com");
        testProvider.setRoles(Set.of(providerRole));

        // Create test service
        testService = new Service();
        testService.setId(1L);
        testService.setTitle("Test Service");
        testService.setDescription("Test Description");
        testService.setPrice(100.0);
        testService.setProvider(testProvider);

        // Create test DTO
        serviceDTO = new ServiceDTO();
        serviceDTO.setTitle("Test Service");
        serviceDTO.setDescription("Test Description");
        serviceDTO.setPrice(100.0);
    }

    @Test
    void createService_WhenValidRequest_ShouldCreateService() {
        // Arrange
        when(userRepository.findByUsername("provider")).thenReturn(Optional.of(testProvider));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);

        // Act
        ServiceDTO result = serviceService.createService(serviceDTO, "provider");

        // Assert
        assertNotNull(result);
        assertEquals(testService.getId(), result.getId());
        assertEquals(testService.getTitle(), result.getTitle());
        assertEquals(testService.getDescription(), result.getDescription());
        assertEquals(testService.getPrice(), result.getPrice());
        verify(userRepository).findByUsername("provider");
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void createService_WhenUserNotProvider_ShouldThrowException() {
        // Arrange
        ApplicationUser nonProvider = new ApplicationUser();
        nonProvider.setUsername("nonprovider");
        when(userRepository.findByUsername("nonprovider")).thenReturn(Optional.of(nonProvider));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            serviceService.createService(serviceDTO, "nonprovider")
        );
        verify(userRepository).findByUsername("nonprovider");
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void getServicesByProvider_WhenValidProvider_ShouldReturnServices() {
        // Arrange
        when(userRepository.findByUsername("provider")).thenReturn(Optional.of(testProvider));
        when(serviceRepository.findByProviderId(1L)).thenReturn(Arrays.asList(testService));

        // Act
        List<ServiceDTO> result = serviceService.getServicesByProvider("provider");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testService.getId(), result.get(0).getId());
        assertEquals(testService.getTitle(), result.get(0).getTitle());
        assertEquals(testService.getDescription(), result.get(0).getDescription());
        assertEquals(testService.getPrice(), result.get(0).getPrice());
        assertEquals(testProvider.getId(), result.get(0).getProviderId());
        verify(userRepository).findByUsername("provider");
        verify(serviceRepository).findByProviderId(1L);
    }

    @Test
    void getServicesByProvider_WhenUserNotProvider_ShouldThrowException() {
        // Arrange
        ApplicationUser nonProvider = new ApplicationUser();
        nonProvider.setUsername("nonprovider");
        when(userRepository.findByUsername("nonprovider")).thenReturn(Optional.of(nonProvider));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            serviceService.getServicesByProvider("nonprovider")
        );
        verify(userRepository).findByUsername("nonprovider");
        verify(serviceRepository, never()).findByProviderId(any());
    }

    @Test
    void getServiceFeed_ShouldReturnSortedServices() {
        // Arrange
        Service service1 = new Service();
        service1.setId(1L);
        service1.setTitle("Service 1");
        service1.setDescription("Description 1");
        service1.setPrice(100.0);
        service1.setProvider(testProvider);

        Service service2 = new Service();
        service2.setId(2L);
        service2.setTitle("Service 2");
        service2.setDescription("Description 2");
        service2.setPrice(200.0);
        service2.setProvider(testProvider);

        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setId(1L);
        serviceDTO1.setTitle("Service 1");
        serviceDTO1.setDescription("Description 1");
        serviceDTO1.setPrice(100.0);
        serviceDTO1.setProviderId(1L);

        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setId(2L);
        serviceDTO2.setTitle("Service 2");
        serviceDTO2.setDescription("Description 2");
        serviceDTO2.setPrice(200.0);
        serviceDTO2.setProviderId(1L);

        when(serviceRepository.findAll()).thenReturn(Arrays.asList(service1, service2));
        when(serviceMapper.toDTO(service1)).thenReturn(serviceDTO1);
        when(serviceMapper.toDTO(service2)).thenReturn(serviceDTO2);
        when(reviewRepository.findAverageRatingByProviderId(1L)).thenReturn(4.5);

        // Act
        List<ServiceFeedDTO> result = serviceService.getServiceFeed();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Service 1", result.get(0).getService().getTitle());
        assertEquals("Service 2", result.get(1).getService().getTitle());
        assertEquals(4.5, result.get(0).getProviderRating());
        assertEquals(testProvider.getUsername(), result.get(0).getProviderUsername());
        assertEquals(testProvider.getEmail(), result.get(0).getProviderEmail());
    }

    @Test
    void updateService_WhenValidRequest_ShouldUpdateService() {
        // Arrange
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);

        // Act
        ServiceDTO result = serviceService.updateService(1L, serviceDTO, "provider");

        // Assert
        assertNotNull(result);
        assertEquals(testService.getId(), result.getId());
        assertEquals(serviceDTO.getTitle(), testService.getTitle());
        assertEquals(serviceDTO.getDescription(), testService.getDescription());
        assertEquals(serviceDTO.getPrice(), testService.getPrice());
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(testService);
    }

    @Test
    void updateService_WhenServiceNotFound_ShouldThrowException() {
        // Arrange
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            serviceService.updateService(999L, serviceDTO, "provider")
        );
        verify(serviceRepository).findById(999L);
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void updateService_WhenUserNotOwner_ShouldThrowException() {
        // Arrange
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            serviceService.updateService(1L, serviceDTO, "otheruser")
        );
        verify(serviceRepository).findById(1L);
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void getServiceById_WhenServiceExists_ShouldReturnService() {
        // Arrange
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));

        // Act
        ServiceDTO result = serviceService.getServiceById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testService.getId(), result.getId());
        assertEquals(testService.getTitle(), result.getTitle());
        assertEquals(testService.getDescription(), result.getDescription());
        assertEquals(testService.getPrice(), result.getPrice());
        assertEquals(testProvider.getId(), result.getProviderId());
        verify(serviceRepository).findById(1L);
    }

    @Test
    void getServiceById_WhenServiceDoesNotExist_ShouldThrowException() {
        // Arrange
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            serviceService.getServiceById(999L)
        );
        verify(serviceRepository).findById(999L);
    }

    @Test
    void deleteService_ShouldDeleteService() {
        // Act
        serviceService.deleteService(1L);

        // Assert
        verify(serviceRepository).deleteById(1L);
    }
} 