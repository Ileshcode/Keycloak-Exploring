package ru.ilesh.resourceserver.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import ru.ilesh.resourceserver.data.RegistrationData;
import ru.ilesh.resourceserver.data.UserProfileData;
import ru.ilesh.resourceserver.data.UserUpdateData;
import ru.ilesh.resourceserver.services.KeycloakService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@Slf4j
@RequiredArgsConstructor
public class ResourceController {

    private final KeycloakService keycloakService;
    private final SecurityContextRepository securityContextRepository;


    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser(@RequestBody RegistrationData registrationData) {
        keycloakService.createNewUser(registrationData);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateData userUpdateData, HttpServletRequest request, HttpServletResponse response) {

       var newAuth =  keycloakService.updateUser(userUpdateData);

        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(newAuth);
        SecurityContextHolder.setContext(ctx);
        securityContextRepository.saveContext(ctx, request, response);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String status() {
        return "OK";
    }

    @GetMapping("/admin/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminStatus() {
        return "OK";
    }

    @GetMapping("/api/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String apiStatus() {
        return "OK";
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<UserProfileData> me(@AuthenticationPrincipal OidcUser user) {
        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setEmail(user.getEmail());
        userProfileData.setFirstname(user.getGivenName());
        userProfileData.setLastname(user.getFamilyName());
        userProfileData.setUsername(user.getPreferredUsername());
        userProfileData.setRoles(user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority
                ).collect(Collectors.toList()));

        return ResponseEntity.ok().body(userProfileData);
    }

}
