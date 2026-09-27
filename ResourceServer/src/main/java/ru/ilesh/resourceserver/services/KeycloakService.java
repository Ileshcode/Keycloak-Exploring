package ru.ilesh.resourceserver.services;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ilesh.resourceserver.data.RegistrationData;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {


    private final Keycloak adminClientKeycloak;
    @Value("${keycloak.realm}")
    private String realm;

    public void createNewUser (RegistrationData registrationData) {
        RealmResource realmResource = adminClientKeycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(registrationData.getUsername());
        userRepresentation.setEmail(registrationData.getEmail());
        userRepresentation.setFirstName(registrationData.getFirstname());
        userRepresentation.setLastName(registrationData.getLastname());
        userRepresentation.setEmailVerified(false);
        userRepresentation.setEnabled(true);
        userRepresentation.setRequiredActions(List.of("VERIFY_EMAIL", "UPDATE_PASSWORD"));

        try (Response response = usersResource.create(userRepresentation)) {
            String userID = CreatedResponseUtil.getCreatedId(response);

            RoleRepresentation roleUser = realmResource.roles().get("ROLE_USER").toRepresentation();
            UserResource userResource = realmResource.users().get(userID);
            userResource.roles().realmLevel().add(List.of(roleUser));
            userResource.sendVerifyEmail();

            log.info("Created user: {}", userID);
        } catch (WebApplicationException e) {
            log.error("Error creating user: {}", e.getMessage());
            log.error(e.getResponse().readEntity(String.class));
        }
    }
}
