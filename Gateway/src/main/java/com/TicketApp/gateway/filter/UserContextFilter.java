package com.TicketApp.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(jwtAuth -> {
                    // 1. Obtenemos los roles del usuario que ya extrajo el SecurityConfig
                    Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();

                    // 2. Verificamos si tiene algún rol especial (como admin o seguridad)
                    boolean isSpecialRole = authorities.stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_admin") ||
                                    auth.getAuthority().equals("ROLE_seguridad"));

                    // 3. Si NO es un rol especial (es decir, es un usuario normal), inyectamos el email
                    if (!isSpecialRole) {
                        String email = jwtAuth.getToken().getClaimAsString("email");

                        if (email != null) {
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Email", email)
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        }
                    }

                    // Si es admin, seguridad, o no se encontró email, dejamos pasar la petición tal cual
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 2;
    }
}