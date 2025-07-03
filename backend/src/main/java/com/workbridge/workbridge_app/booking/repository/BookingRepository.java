package com.workbridge.workbridge_app.booking.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.workbridge.workbridge_app.booking.entity.Booking;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;

public interface BookingRepository extends JpaRepository<Booking, Long>{
    Page<Booking> findBySeeker_Id(Long seekerId, Pageable pageable);
    List<Booking> findBySeeker_Id(Long seekerId);

    Page<Booking> findByService_Provider(ApplicationUser provider, Pageable pageable);
    List<Booking> findByService_Provider(ApplicationUser provider);

    @Modifying
    @Query("""
        UPDATE Booking b
        SET b.deleted = true,
            b.deletedAt = CURRENT_TIMESTAMP,
            b.deletedByUser = :deleterId
        WHERE b.seeker.id = :userId
        AND b.deleted = false
    """)
    void softDeleteBySeeker(@Param("userId") Long userId, @Param("deleterId") Long deleterId);
}
