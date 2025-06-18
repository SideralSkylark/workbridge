package com.workbridge.workbridge_app.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.service.entity.Service;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByProviderId(Long providerId);
}
