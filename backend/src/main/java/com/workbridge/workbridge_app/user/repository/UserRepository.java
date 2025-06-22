package com.workbridge.workbridge_app.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.entity.UserRole;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, Long> {

    Optional<ApplicationUser> findByUsername(String username);

    Optional<ApplicationUser> findByEmail(String email);

    Page<ApplicationUser> findDistinctByRoles_Role(UserRole role, Pageable pageable);
    List<ApplicationUser> findDistinctByRoles_Role(UserRole role);

    Page<ApplicationUser> findDistinctByRoles_RoleNot(UserRole role, Pageable pageable);
    List<ApplicationUser> findDistinctByRoles_RoleNot(UserRole role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM ApplicationUser u WHERE :role NOT IN (SELECT r.role FROM u.roles r)")
    Page<ApplicationUser> findAllExcludingRole(@Param("role") com.workbridge.workbridge_app.user.entity.UserRole role, Pageable pageable);

    @Query("SELECT u FROM ApplicationUser u WHERE :role NOT IN (SELECT r.role FROM u.roles r)")
    List<ApplicationUser> findAllExcludingRole(@Param("role") com.workbridge.workbridge_app.user.entity.UserRole role);

    default Page<ApplicationUser> findAllNonAdminUsers(Pageable pageable) {
        return findAllExcludingRole(com.workbridge.workbridge_app.user.entity.UserRole.ADMIN, pageable);
    }
    default List<ApplicationUser> findAllNonAdminUsers() {
        return findAllExcludingRole(com.workbridge.workbridge_app.user.entity.UserRole.ADMIN);
    }

    @Query("SELECT u FROM ApplicationUser u JOIN u.roles r WHERE r.role = :role")
    List<ApplicationUser> findAllByRole(@Param("role") com.workbridge.workbridge_app.user.entity.UserRole role);

    @Query("SELECT u FROM ApplicationUser u JOIN u.roles r WHERE r.role = :role")
    Page<ApplicationUser> findAllByRole(@Param("role") com.workbridge.workbridge_app.user.entity.UserRole role, Pageable pageable);
}