package com.ptk.multitenant_saas_backend.controller;

import com.ptk.multitenant_saas_backend.model.Permission;
import com.ptk.multitenant_saas_backend.repository.PermissionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    public PermissionController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @PostMapping
    public Permission createPermission(@RequestBody Permission permission) {
        return permissionRepository.save(permission);
    }

    @GetMapping
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
}