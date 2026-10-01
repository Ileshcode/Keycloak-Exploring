package ru.ilesh.resourceserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.ilesh.resourceserver.security.BFFLogoutSuccessHandler;

import java.util.*;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {


    @Value("${cors.urls}")
    private String[] corsUrls;
    @Value("${csrf.domain}")
    private String csrfDomain;
    @Value("${auth.post-redir-url}")
    private String postAuthRedirectUrl;
    private final BFFLogoutSuccessHandler logoutSuccessHandler;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder customJwtDecoder) throws Exception {
        return http
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/registration").permitAll()
                        .anyRequest().authenticated()
                )
                .cors(source -> source.configurationSource(corsConfigurationSource()))
                .csrf((csrf) -> csrf
                        .spa()
                        .csrfTokenRepository(cookieCsrfTokenRepository())
                        .requireCsrfProtectionMatcher(request ->
                            request.getHeader("Authorization") == null &&
                                    !List.of("GET", "HEAD", "OPTIONS", "TRACE").contains(request.getMethod())
                        )
                )
                .oauth2Login(login -> login
                        .userInfoEndpoint(ui -> ui.oidcUserService(userService(customJwtDecoder)))
                        .successHandler(bffSuccessHandler())
                )
                .oauth2ResourceServer(config -> config
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler))
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new RequestHeaderRequestMatcher("Authorization")
                ))
                .build();
    }

    @Bean
    public AuthenticationSuccessHandler bffSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl(postAuthRedirectUrl);

        return successHandler;
    }

    @Bean
    public CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(customizer -> {
            customizer.domain(csrfDomain);
        });

        return repository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        Arrays.stream(corsUrls).forEach(configuration::addAllowedOrigin);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public JwtDecoder customJwtDecoder(@Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String issuer,
                                       @Value("${app.audience}") String audience) {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audValidator = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD,
                aud -> aud != null && aud.contains(audience)
        );

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audValidator));

        return jwtDecoder;

    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> userService(JwtDecoder customJwtDecoder) {
        OidcUserService delegate = new OidcUserService();

        return (userRequest -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            String accessTokenValue = userRequest.getAccessToken().getTokenValue();
            Jwt jwt = customJwtDecoder.decode(accessTokenValue);

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) {
                throw new RuntimeException("Realm access required");
            }
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) {
                throw new RuntimeException("Roles required");
            }

            Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        });
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(token -> {
            Map<String, Object> realmAccess = (Map<String, Object>) token.getClaim("realm_access");
            if (realmAccess == null) {
                throw new RuntimeException("Realm access required");
            }
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) {
                throw new RuntimeException("Roles required");
            }

            List<SimpleGrantedAuthority> realmRoles = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            List<GrantedAuthority> defaultRoles = new JwtGrantedAuthoritiesConverter().convert(token).stream().toList();

            return Stream.concat(defaultRoles.stream(), realmRoles.stream()).toList();
        });

        return jwtAuthenticationConverter;

    }
}
