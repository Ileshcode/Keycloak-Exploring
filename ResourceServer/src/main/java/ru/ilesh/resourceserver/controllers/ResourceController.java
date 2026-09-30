package ru.ilesh.resourceserver.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import ru.ilesh.resourceserver.data.RegistrationData;
import ru.ilesh.resourceserver.data.UserProfileData;
import ru.ilesh.resourceserver.services.KeycloakService;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@Slf4j
@RequiredArgsConstructor
public class ResourceController {

    private final KeycloakService keycloakService;


    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser(@RequestBody RegistrationData registrationData) {
        keycloakService.createNewUser(registrationData);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String status() {
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
