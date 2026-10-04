package ru.ilesh.resourceserver.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KcActionResolver implements OAuth2AuthorizationRequestResolver {


    private final DefaultOAuth2AuthorizationRequestResolver delegate;


    @Autowired
    public KcActionResolver(ClientRegistrationRepository registrationRepository) {

       delegate =  new DefaultOAuth2AuthorizationRequestResolver(registrationRepository, "/oauth2/authorization");
    }

    @Override
    public @Nullable OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return customize(delegate.resolve(request), request);
    }

    @Override
    public @Nullable OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return customize(delegate.resolve(request, clientRegistrationId), request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
        if(authorizationRequest == null) {return null;}
        String kcAction = request.getParameter("kc_action");
        if(kcAction == null) {return authorizationRequest;}
        Map<String, Object> extra = new HashMap<>(authorizationRequest.getAdditionalParameters());
        extra.put("kc_action", kcAction);
        return OAuth2AuthorizationRequest.from(authorizationRequest).additionalParameters(extra).build();
    }
}
