package nl.teamrocket.core.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

/**
 * Security config for the Core Monolith.
 * Validates RS256 JWTs issued by the Identity service.
 * Fetches public key from Identity's JWKS endpoint at startup and caches it.
 * No per-request call to Identity — fully local verification.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class CoreSecurityConfig {
    private final CorrelationFilter correlationFilter;

    @Value("${app.identity.jwks-uri}")
    private String jwksUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(correlationFilter, SecurityContextHolderFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/users/*/notification-prefs").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthConverter())
                        )
                )
                .build();
    }

    /** For dev/testing, allow all requests without JWT validation. */
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterBefore(correlationFilter, SecurityContextHolderFilter.class)
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()
//                )
//                .build();
//    }

    /** Fetches RSA public key from Identity service JWKS endpoint (cached 6h by Spring). */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
    }

    /** Dev mode: skip JWT validation to allow running without Identity service. */
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        // TODO: restore JWKS fetch when Identity service is running
//        // return NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
//        return token -> {
//            throw new org.springframework.security.oauth2.jwt.BadJwtException("JWT validation disabled in dev mode");
//        };
//    }

    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        var authConverter = new JwtGrantedAuthoritiesConverter();
        authConverter.setAuthoritiesClaimName("roles");
        authConverter.setAuthorityPrefix("ROLE_");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authConverter);
        converter.setPrincipalClaimName("sub");
        return converter;
    }
}
