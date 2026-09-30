package ru.ilesh.resourceserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.ilesh.resourceserver.security.BFFLogoutSuccessHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {


    @Value("${cors.urls}")
    private String[] corsUrls;
    @Value("${csrf.domain}")
    private String csrfDomain;
    @Value("${auth.post-redir-url}")
    private String postAuthRedirectUrl;
    private final BFFLogoutSuccessHandler logoutSuccessHandler;


    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, JwtDecoder customJwtDecoder) throws Exception {
        return http
                .securityMatcher("/api/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(conf -> conf
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                                .decoder(customJwtDecoder)))
                .build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                )
                .oauth2Login(login -> login
                        .successHandler(bffSuccessHandler())
                )
                .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler))
                .build();
    }

    @Bean
    public AuthenticationSuccessHandler bffSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl(postAuthRedirectUrl);

        return successHandler;
    }

    @Bean
    public CookieCsrfTokenRepository cookieCsrfTokenRepository () {
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
    public JwtDecoder customJwtDecoder (@Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String issuer,
                                        @Value("${app.audience}") String audience) {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audValidator = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD,
                aud -> aud !=null && aud.contains(audience)
        );

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audValidator));

        return jwtDecoder;

    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(token -> {
            Map<String, Object> realmAccess = (Map<String, Object>) token.getClaim("realm_access");
            if(realmAccess == null) {throw new RuntimeException("Realm access required");}
            List<String> roles = (List<String>) realmAccess.get("roles");
            if(roles == null) {throw new RuntimeException("Roles required");}

            List<SimpleGrantedAuthority> realmRoles = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            List<GrantedAuthority> defaultRoles = new JwtGrantedAuthoritiesConverter().convert(token).stream().toList();

            return Stream.concat(defaultRoles.stream(), realmRoles.stream()).toList();
        });

        return jwtAuthenticationConverter;

    }
}
