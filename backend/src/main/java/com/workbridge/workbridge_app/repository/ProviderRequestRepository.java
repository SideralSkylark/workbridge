package com.workbridge.workbridge_app.repository;

import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.ProviderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRequestRepository extends JpaRepository<ProviderRequest, Long> {
    boolean existsByUserAndApprovedFalse(ApplicationUser user);
}
