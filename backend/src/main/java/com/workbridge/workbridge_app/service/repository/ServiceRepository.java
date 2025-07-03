package com.workbridge.workbridge_app.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.workbridge.workbridge_app.service.entity.Service;
import com.workbridge.workbridge_app.service.projection.ServiceFeedProjection;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    Page<Service> findByProviderId(Long providerId, Pageable pageable);
    List<Service> findByProviderId(Long providerId);
    @Query("""
        SELECT
            s.id AS serviceId,
            s.title AS title,
            s.description AS description,
            s.price AS price,
            p.id AS providerId,
            p.username AS providerUsername,
            p.email AS providerEmail,
            COALESCE(AVG(r.rating), 0.0) AS providerRating
        FROM Service s
        JOIN s.provider p
        LEFT JOIN Review r ON r.reviewed.id = p.id
        GROUP BY s.id, p.id, p.username, p.email
    """)
    Page<ServiceFeedProjection> findServiceFeed(Pageable pageable);

    @Modifying
    @Query("""
        UPDATE Service s
        SET s.deleted = true,
            s.deletedAt = CURRENT_TIMESTAMP,
            s.deletedByUser = :deleterId
        WHERE s.provider.id = :userId
        AND s.deleted = false
    """)
    void softDeleteByProvider(@Param("userId") Long userId, @Param("deleterId") Long deleterId);
}
