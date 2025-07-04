package com.workbridge.workbridge_app.user.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@SQLRestriction("deleted = false")
public class ApplicationUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean enabled = false;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
         inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<UserRoleEntity> roles = new HashSet<>();

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false;

    private Instant deletedAt;
    private Long deletedByUserId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role.getRole().name())
                .toList();
    }

    public void addRole(UserRoleEntity role) {
        roles.add(role);
    }

    public boolean hasRole(UserRole role) {
        return roles.stream().anyMatch(r -> r.getRole() == role);
    }

    public boolean lacksRole(UserRole role) {
        return roles.stream().noneMatch(r -> r.getRole() == role);
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    public boolean isServiceSeeker() {
        return hasRole(UserRole.SERVICE_SEEKER);
    }

    public boolean isServiceProvider() {
        return hasRole(UserRole.SERVICE_PROVIDER);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
