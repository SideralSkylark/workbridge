package com.workbridge.workbridge_app.user.repository;

import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.ProviderRequest;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRequestRepository extends JpaRepository<ProviderRequest, Long> {
    boolean existsByUserAndApprovedFalse(ApplicationUser user);

    @Query("SELECT pr FROM ProviderRequest pr WHERE pr.approved = false")
    List<ProviderRequest> findAllPending();

    @Query("SELECT pr FROM ProviderRequest pr WHERE pr.approved = false")
    Page<ProviderRequest> findAllPending(Pageable pageable);

    boolean existsByUser_UsernameAndApprovedFalse(String username);

    default List<ProviderRequest> findByApprovedFalse() {
        return findAllPending();
    }

    default Page<ProviderRequest> findByApprovedFalse(Pageable pageable) {
        return findAllPending(pageable);
    }
}
