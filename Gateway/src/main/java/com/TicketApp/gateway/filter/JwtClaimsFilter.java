package com.TicketApp.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtClaimsFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .map(auth -> (JwtAuthenticationToken) auth)
                .map(jwtAuth -> jwtAuth.getToken())
                .flatMap(jwt -> {
                    // Extraer claims del JWT
                    String email = jwt.getClaimAsString("email");
                    System.out.println("🔍 JwtClaimsFilter - Email extraído: " + email); // Log para debug
                    String name = jwt.getClaimAsString("name");
                    String preferredUsername = jwt.getClaimAsString("preferred_username");
                    String subject = jwt.getSubject();
                    
                    // Log para debug (puedes removerlo en producción)
                    System.out.println("🔍 JwtClaimsFilter - Email extraído: " + email);
                    
                    // Construir la request mutada con los headers adicionales
                    ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
                    
                    if (email != null && !email.isEmpty()) {
                        requestBuilder.header("X-User-Email", email);
                    }
                    if (name != null && !name.isEmpty()) {
                        requestBuilder.header("X-User-Name", name);
                    }
                    if (preferredUsername != null && !preferredUsername.isEmpty()) {
                        requestBuilder.header("X-User-Username", preferredUsername);
                    }
                    if (subject != null && !subject.isEmpty()) {
                        requestBuilder.header("X-User-Subject", subject);
                    }
                    
                    ServerHttpRequest request = requestBuilder.build();
                    return chain.filter(exchange.mutate().request(request).build());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Si no hay contexto de seguridad, continuar sin agregar headers
                    System.out.println("⚠️ JwtClaimsFilter - No se encontró contexto de seguridad JWT");
                    return chain.filter(exchange);
                }));
    }

    @Override
    public int getOrder() {
        // Ejecutar después de la autenticación JWT de Spring Security pero antes del routing
        // Spring Security filters tienen orden alrededor de 0
        return 0;
    }
}
