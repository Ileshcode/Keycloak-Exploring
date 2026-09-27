package ru.ilesh.resourceserver.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ilesh.resourceserver.data.RegistrationData;
import ru.ilesh.resourceserver.services.KeycloakService;

@RestController
@RequestMapping("/")
@Slf4j
@RequiredArgsConstructor
public class ResourceController {

    private final KeycloakService keycloakService;

    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser(@RequestBody RegistrationData registrationData) {
        keycloakService.createNewUser(registrationData);
        return  ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String status() {
        return "OK";
    }
}
