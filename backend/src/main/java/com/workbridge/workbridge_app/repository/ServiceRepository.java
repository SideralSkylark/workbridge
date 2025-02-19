package com.workbridge.workbridge_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.workbridge.workbridge_app.entity.Service;
import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByProviderId(Long providerId);
}
