package com.ptk.multitenant_saas_backend.controller;

import com.ptk.multitenant_saas_backend.model.Tenant;
import com.ptk.multitenant_saas_backend.model.UserAccount;
import com.ptk.multitenant_saas_backend.repository.TenantRepository;
import com.ptk.multitenant_saas_backend.repository.UserAccountRepository;
import com.ptk.multitenant_saas_backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.ptk.multitenant_saas_backend.model.Role;
import com.ptk.multitenant_saas_backend.repository.RoleRepository;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository userAccountRepository;
private final TenantRepository tenantRepository;
private final PasswordEncoder passwordEncoder;
private final AuthenticationManager authenticationManager;
private final JwtUtil jwtUtil;
private final RoleRepository roleRepository;

public AuthController(
        UserAccountRepository userAccountRepository,
        TenantRepository tenantRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtUtil jwtUtil,
        RoleRepository roleRepository
) {
    this.userAccountRepository = userAccountRepository;
    this.tenantRepository = tenantRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.roleRepository = roleRepository;
}

    public record SignupRequest(String email, String password, String firstName, String lastName, UUID tenantId) {}
    public record LoginRequest(String email, String password) {}

    @PostMapping("/signup")
    public UserAccount signup(@RequestBody SignupRequest request) {
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        UserAccount user = new UserAccount();
        user.setTenant(tenant);
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);

        return userAccountRepository.save(user);
    }
@PostMapping("/login")
public Map<String, String> login(@RequestBody LoginRequest request) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    String token = jwtUtil.generateToken(request.email());
    return Map.of("token", token);
}
    @PostMapping("/users/{userId}/roles/{roleId}")
public UserAccount assignRoleToUser(@PathVariable UUID userId, @PathVariable UUID roleId) {
    UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    Role role = roleRepository.findByTenantId(user.getTenant().getId()).stream()
            .filter(r -> r.getId().equals(roleId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Role not found"));

    user.getRoles().add(role);
    return userAccountRepository.save(user);
}
}