package com.ptk.multitenant_saas_backend.repository;

import com.ptk.multitenant_saas_backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findByTenantId(UUID tenantId);
}