package com.workbridge.workbridge_app.unit.controller;

import com.workbridge.workbridge_app.controller.ServiceController;
import com.workbridge.workbridge_app.dto.ServiceDTO;
import com.workbridge.workbridge_app.service.ServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceControllerTest {

    @Mock
    private ServiceService serviceService;

    @InjectMocks
    private ServiceController serviceController;

    private ServiceDTO mockServiceDTO;

    @BeforeEach
    void setUp() {
        mockServiceDTO = new ServiceDTO();
        mockServiceDTO.setId(1L);
        mockServiceDTO.setTitle("Test Service");
        mockServiceDTO.setDescription("Service description");
        mockServiceDTO.setPrice(100.0);
        mockServiceDTO.setProviderId(1L);
    }

    private void mockAuthentication(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCreateService_Success() {
        String username = "providerUser";
        mockAuthentication(username);

        when(serviceService.createService(any(ServiceDTO.class), eq(username))).thenReturn(mockServiceDTO);

        ResponseEntity<ServiceDTO> response = serviceController.createService(mockServiceDTO);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(mockServiceDTO, response.getBody());
    }

    @Test
    void testGetServicesByProvider_Success() {
        String username = "providerUser";
        mockAuthentication(username);

        List<ServiceDTO> services = Collections.singletonList(mockServiceDTO);
        when(serviceService.getServicesByProvider(username)).thenReturn(services);

        ResponseEntity<List<ServiceDTO>> response = serviceController.getServicesByProvider();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockServiceDTO, response.getBody().get(0));
    }

    @Test
    void testGetServiceById_Success() {
        when(serviceService.getServiceById(1L)).thenReturn(mockServiceDTO);

        ResponseEntity<ServiceDTO> response = serviceController.getServiceById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(mockServiceDTO, response.getBody());
    }

    @Test
    void testUpdateService_Success() {
        String username = "providerUser";
        mockAuthentication(username);

        when(serviceService.updateService(eq(1L), any(ServiceDTO.class), eq(username))).thenReturn(mockServiceDTO);

        ResponseEntity<ServiceDTO> response = serviceController.updateService(1L, mockServiceDTO);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(mockServiceDTO, response.getBody());
    }

    @Test
    void testDeleteService_Success() {
        doNothing().when(serviceService).deleteService(1L);

        ResponseEntity<Void> response = serviceController.deleteService(1L);

        assertEquals(204, response.getStatusCode().value());
    }
}
