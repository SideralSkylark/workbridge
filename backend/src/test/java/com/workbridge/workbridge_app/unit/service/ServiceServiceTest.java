package com.workbridge.workbridge_app.unit.service;

import com.workbridge.workbridge_app.dto.ServiceDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.Service;
import com.workbridge.workbridge_app.entity.UserRole;
import com.workbridge.workbridge_app.entity.UserRoleEntity;
import com.workbridge.workbridge_app.repository.ServiceRepository;
import com.workbridge.workbridge_app.repository.UserRepository;
import com.workbridge.workbridge_app.service.ServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ServiceService serviceService;

    private ApplicationUser provider;
    private Service mockService;
    private ServiceDTO mockServiceDTO;

    @BeforeEach
    void setUp() {
        provider = new ApplicationUser();
        provider.setId(1L);
        provider.setUsername("providerUser");
        provider.setRoles(Collections.singleton(new UserRoleEntity(UserRole.SERVICE_PROVIDER)));

        mockService = new Service();
        mockService.setId(1L);
        mockService.setTitle("Test Service");
        mockService.setDescription("Service description");
        mockService.setPrice(100.0);
        mockService.setProvider(provider);

        mockServiceDTO = new ServiceDTO();
        mockServiceDTO.setId(1L);
        mockServiceDTO.setTitle("Test Service");
        mockServiceDTO.setDescription("Service description");
        mockServiceDTO.setPrice(100.0);
        mockServiceDTO.setProviderId(provider.getId());
    }

    @Test
    void testCreateService_Success() {
        when(userRepository.findByUsername("providerUser")).thenReturn(Optional.of(provider));
        when(serviceRepository.save(any(Service.class))).thenAnswer(invocation -> {
            Service savedService = invocation.getArgument(0);
            savedService.setId(1L);
            return savedService;
        });

        ServiceDTO result = serviceService.createService(mockServiceDTO, "providerUser");

        assertNotNull(result);
        assertEquals(mockServiceDTO.getTitle(), result.getTitle());
        assertEquals(mockServiceDTO.getDescription(), result.getDescription());
        verify(serviceRepository, times(1)).save(any(Service.class));
    }

    @Test
    void testCreateService_UserNotFound() {
        when(userRepository.findByUsername("providerUser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> serviceService.createService(mockServiceDTO, "providerUser"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testCreateService_UserNotProvider() {
        provider.setRoles(Collections.emptySet()); // Remove a role de SERVICE_PROVIDER
        when(userRepository.findByUsername("providerUser")).thenReturn(Optional.of(provider));

        Exception exception = assertThrows(RuntimeException.class,
                () -> serviceService.createService(mockServiceDTO, "providerUser"));

        assertEquals("User is not a service provider", exception.getMessage());
    }

    @Test
    void testGetServicesByProvider_Success() {
        when(userRepository.findByUsername("providerUser")).thenReturn(Optional.of(provider));
        when(serviceRepository.findByProviderId(provider.getId())).thenReturn(List.of(mockService));

        List<ServiceDTO> services = serviceService.getServicesByProvider("providerUser");

        assertFalse(services.isEmpty());
        assertEquals(1, services.size());
        assertEquals(mockService.getTitle(), services.get(0).getTitle());
    }

    @Test
    void testGetServicesByProvider_UserNotFound() {
        when(userRepository.findByUsername("providerUser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> serviceService.getServicesByProvider("providerUser"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetServiceById_Success() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(mockService));

        ServiceDTO result = serviceService.getServiceById(1L);

        assertNotNull(result);
        assertEquals(mockServiceDTO.getTitle(), result.getTitle());
    }

    @Test
    void testGetServiceById_NotFound() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> serviceService.getServiceById(1L));

        assertEquals("Service not found", exception.getMessage());
    }

    @Test
    void testUpdateService_Success() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(mockService));
        when(serviceRepository.save(any(Service.class))).thenReturn(mockService);

        ServiceDTO updatedDTO = new ServiceDTO();
        updatedDTO.setTitle("Updated Service");
        updatedDTO.setDescription("Updated Description");
        updatedDTO.setPrice(200.0);

        ServiceDTO result = serviceService.updateService(1L, updatedDTO, "providerUser");

        assertNotNull(result);
        assertEquals("Updated Service", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(200.0, result.getPrice());
    }

    @Test
    void testUpdateService_NotFound() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> serviceService.updateService(1L, mockServiceDTO, "providerUser"));

        assertEquals("Service not found", exception.getMessage());
    }

    @Test
    void testUpdateService_NotOwner() {
        ApplicationUser anotherUser = new ApplicationUser();
        anotherUser.setId(2L);
        anotherUser.setUsername("otherUser");

        mockService.setProvider(anotherUser);

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(mockService));

        Exception exception = assertThrows(RuntimeException.class,
                () -> serviceService.updateService(1L, mockServiceDTO, "providerUser"));

        assertEquals("You are not the owner of this service", exception.getMessage());
    }

    @Test
    void testDeleteService_Success() {
        doNothing().when(serviceRepository).deleteById(1L);

        assertDoesNotThrow(() -> serviceService.deleteService(1L));
        verify(serviceRepository, times(1)).deleteById(1L);
    }
}
