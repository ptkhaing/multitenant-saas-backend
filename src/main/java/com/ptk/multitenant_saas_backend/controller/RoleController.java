package com.ptk.multitenant_saas_backend.controller;

import com.ptk.multitenant_saas_backend.model.Permission;
import com.ptk.multitenant_saas_backend.model.Role;
import com.ptk.multitenant_saas_backend.model.Tenant;
import com.ptk.multitenant_saas_backend.repository.PermissionRepository;
import com.ptk.multitenant_saas_backend.repository.RoleRepository;
import com.ptk.multitenant_saas_backend.repository.TenantRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PermissionRepository permissionRepository;

    public RoleController(
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            PermissionRepository permissionRepository
    ) {
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.permissionRepository = permissionRepository;
    }

    public record CreateRoleRequest(String name, String description, UUID tenantId) {}

    @PostMapping
    public Role createRole(@RequestBody CreateRoleRequest request) {
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Role role = new Role();
        role.setTenant(tenant);
        role.setName(request.name());
        role.setDescription(request.description());

        return roleRepository.save(role);
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    public Role addPermissionToRole(@PathVariable UUID roleId, @PathVariable UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }

    @GetMapping
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}