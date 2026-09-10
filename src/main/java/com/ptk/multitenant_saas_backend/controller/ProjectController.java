package com.ptk.multitenant_saas_backend.controller;

import com.ptk.multitenant_saas_backend.model.Project;
import com.ptk.multitenant_saas_backend.model.Tenant;
import com.ptk.multitenant_saas_backend.model.UserAccount;
import com.ptk.multitenant_saas_backend.repository.ProjectRepository;
import com.ptk.multitenant_saas_backend.repository.TenantRepository;
import com.ptk.multitenant_saas_backend.repository.UserAccountRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserAccountRepository userAccountRepository;
    private final TenantRepository tenantRepository;

    public ProjectController(
            ProjectRepository projectRepository,
            UserAccountRepository userAccountRepository,
            TenantRepository tenantRepository
    ) {
        this.projectRepository = projectRepository;
        this.userAccountRepository = userAccountRepository;
        this.tenantRepository = tenantRepository;
    }

    public record CreateProjectRequest(String name, String description, UUID tenantId) {}

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    public Project createProject(@RequestBody CreateProjectRequest request, Authentication authentication) {
        UserAccount creator = userAccountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Project project = new Project();
        project.setTenant(tenant);
        project.setCreatedBy(creator);
        project.setName(request.name());
        project.setDescription(request.description());

        return projectRepository.save(project);
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_DELETE')")
    public void deleteProject(@PathVariable UUID id) {
        projectRepository.deleteById(id);
    }
}