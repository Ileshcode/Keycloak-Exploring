package ru.ilesh.resourceserver.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BFFLogoutSuccessHandler extends OidcClientInitiatedLogoutSuccessHandler {


    public BFFLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository,
                                   @Value("${auth.post-redir-url}") String postRedirectUrl
                                   ) {
        super(clientRegistrationRepository);
        setPostLogoutRedirectUri(postRedirectUrl);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) throws IOException, ServletException {
        String url = super.determineTargetUrl(request, response, authentication);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.getWriter().write(url);
    }
}
