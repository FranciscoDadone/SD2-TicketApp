package com.TicketApp.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges

                        // 1. Bloqueo absoluto del endpoint interno (PATCH)
                        .pathMatchers("/internal/**").denyAll()

                        // 2. ROL SEGURIDAD: Solo el portero/control de acceso puede consumir los tickets
                        .pathMatchers(HttpMethod.POST, "/api/orders/validate/**").hasAuthority("ROLE_security")

                        // 3. RESTRICCIÓN ADMIN: ABM de eventos
                        .pathMatchers("/api/admin/**").hasAuthority("ROLE_admin")

                        // 4. CLIENTES COMUNES: Consultar eventos, ver sus órdenes o comprar
                        .pathMatchers("/api/orders/**").authenticated()
                        .pathMatchers("/api/events/**").authenticated()

                        // Cualquier otra ruta requiere estar logueado
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                ));

        return http.build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 1. Buscamos el bloque "resource_access"
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");

            // 2. Verificamos si existe y si adentro tiene a nuestro cliente "ticketapp-gateway"
            if (resourceAccess != null && resourceAccess.containsKey("ticketapp-gateway")) {
                Map<String, Object> client = (Map<String, Object>) resourceAccess.get("ticketapp-gateway");

                // 3. Extraemos la lista de roles
                Collection<String> roles = (Collection<String>) client.get("roles");
                if (roles != null) {
                    // 4. Les agregamos el prefijo "ROLE_" que espera Spring Security
                    return roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());
                }
            }
            // Si no tiene roles o no es el cliente correcto, no le damos ninguna autoridad
            return Collections.emptyList();
        });

        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}