package ru.ilesh.resourceserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
public class SecurityConfig {


    @Value("${cors.urls}")
    private String[] corsUrls;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/registration").permitAll()
                        .anyRequest().authenticated()
                )
                .cors(source -> source.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(conf -> conf.jwt(jwtConfigurer -> {
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

                        return  Stream.concat(defaultRoles.stream(), realmRoles.stream()).toList();
                    });

                    jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter);
                }))
                .build();
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
}
