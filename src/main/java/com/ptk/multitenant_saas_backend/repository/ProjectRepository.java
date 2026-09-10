package com.ptk.multitenant_saas_backend.repository;

import com.ptk.multitenant_saas_backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByTenantId(UUID tenantId);
}