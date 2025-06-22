package com.workbridge.workbridge_app.user.repository;

import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.ProviderRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRequestRepository extends JpaRepository<ProviderRequest, Long> {
    boolean existsByUserAndApprovedFalse(ApplicationUser user);
}
