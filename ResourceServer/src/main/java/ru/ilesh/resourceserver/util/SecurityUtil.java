package ru.ilesh.resourceserver.util;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class SecurityUtil {


    public static String getSub () {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

       if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
           return oidcUser.getSubject();
       } else {
           throw new RuntimeException("Authentication object is null");
       }
    }

    public static OidcUser getOidcUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return oidcUser;
        } else {
            throw new RuntimeException("Authentication object is null");
        }
    }
}
