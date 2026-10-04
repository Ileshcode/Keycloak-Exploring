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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ilesh.resourceserver.data.RegistrationData;
import ru.ilesh.resourceserver.data.UserUpdateData;
import ru.ilesh.resourceserver.entity.UserEntity;
import ru.ilesh.resourceserver.repo.UserRepo;
import ru.ilesh.resourceserver.util.SecurityUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class KeycloakService {


    private final Keycloak adminClientKeycloak;
    private final UserRepo userRepo;
    @Value("${keycloak.realm}")
    private String realm;

    @Transactional
    public void createNewUser(RegistrationData registrationData) {
        RealmResource realmResource = adminClientKeycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserRepresentation userRepresentation = createUserRepresentation(registrationData);

        try (Response response = usersResource.create(userRepresentation)) {
            String userID = CreatedResponseUtil.getCreatedId(response);

            RoleRepresentation roleUser = realmResource.roles().get("ROLE_USER").toRepresentation();
            UserResource userResource = realmResource.users().get(userID);
            userResource.roles().realmLevel().add(List.of(roleUser));
            userResource.sendVerifyEmail();


            UserEntity createdUser = saveUserToDb(registrationData.getUsername(), userID);

            log.info("Created user: {}", createdUser);
        } catch (WebApplicationException e) {
            log.error("Error creating user: {}", e.getMessage());
            log.error(e.getResponse().readEntity(String.class));
        }
    }

    @Transactional
    public OAuth2AuthenticationToken updateUser(UserUpdateData userUpdateData) {
        RealmResource realmResource = adminClientKeycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        OidcUser oidcUser = SecurityUtil.getOidcUser();
        String uuid = oidcUser.getSubject();

        if (!userUpdateData.getUsername().equals(oidcUser.getPreferredUsername())) {
            List<UserRepresentation> usersWithExactUsername = usersResource.searchByUsername(userUpdateData.getUsername(), true);
            boolean existInDb = userRepo.existsByUsernameAndUuidNot(userUpdateData.getUsername(), uuid);

            if (!usersWithExactUsername.isEmpty() || existInDb) {
                throw new RuntimeException("User with username " + userUpdateData.getUsername() + " already exists");
            }
        }

        UserResource userResource = realmResource.users().get(uuid);

        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setUsername(userUpdateData.getUsername());
        userRepresentation.setFirstName(userUpdateData.getFirstname());
        userRepresentation.setLastName(userUpdateData.getLastname());

        try {
            userResource.update(userRepresentation);

            UserEntity updatedUser = updateUserInDb(userUpdateData.getUsername(), uuid);

            log.info("Updated user: {}", updatedUser);

            return createNewAuthToken(userUpdateData, oidcUser);

        } catch (WebApplicationException e) {
            log.error("Error updating user: {}", e.getMessage());
            throw new RuntimeException("Error while updating user");
        }


    }

    private OAuth2AuthenticationToken createNewAuthToken(UserUpdateData userUpdateData, OidcUser oldUser) {
        OidcIdToken oldToken = oldUser.getIdToken();

        Map<String, Object> claims = new HashMap<>(oldToken.getClaims());
        claims.put("given_name", userUpdateData.getFirstname());
        claims.put("family_name", userUpdateData.getLastname());
        claims.put("preferred_username", userUpdateData.getUsername());
        OidcIdToken newToken = new OidcIdToken(oldToken.getTokenValue(), oldToken.getIssuedAt(), oldToken.getExpiresAt(), claims);
        OidcUser newUser = new DefaultOidcUser(oldUser.getAuthorities(), newToken, oldUser.getUserInfo());

        return new OAuth2AuthenticationToken(newUser, newUser.getAuthorities(), "keycloak");
    }


    private UserEntity updateUserInDb(String username, String uuid) {
        UserEntity userEntity = userRepo.findByUuid(uuid);

        userEntity.setUsername(username);

        return userRepo.save(userEntity);
    }

    private UserEntity saveUserToDb(String username, String uuid) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setUuid(uuid);

        return userRepo.save(userEntity);
    }

    private UserRepresentation createUserRepresentation(RegistrationData registrationData) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(registrationData.getUsername());
        userRepresentation.setEmail(registrationData.getEmail());
        userRepresentation.setFirstName(registrationData.getFirstname());
        userRepresentation.setLastName(registrationData.getLastname());
        userRepresentation.setEmailVerified(false);
        userRepresentation.setEnabled(true);
        userRepresentation.setRequiredActions(List.of("VERIFY_EMAIL", "UPDATE_PASSWORD"));

        return userRepresentation;
    }
}
