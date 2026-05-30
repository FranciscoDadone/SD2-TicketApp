package com.TicketApp.gateway.filter;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;

@Component
public class GcpOidcAuthFilter implements GlobalFilter, Ordered {

    @Value("${ORDER_API_URL}")
    private String audience;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Buscamos el token de Google Cloud
        String gcpToken = fetchGcpOidcToken(audience);

        // 2. Si encontramos token (estamos en GCP), reemplazamos el header Authorization
        if (gcpToken != null) {
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("Authorization", "Bearer " + gcpToken)
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        }

        // 3. Si no hay token (desarrollo local), dejamos pasar la petición tal cual
        return chain.filter(exchange);
    }

    private String fetchGcpOidcToken(String audience) {
        try {
            // Intenta cargar las credenciales por defecto del entorno (Application Default Credentials)
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

            if (credentials instanceof IdTokenProvider) {
                // Genera el token OIDC específicamente firmado para la URL de nuestra Order API
                return ((IdTokenProvider) credentials)
                        .idTokenWithAudience(audience, Collections.emptyList())
                        .getTokenValue();
            }
        } catch (IOException e) {
            // Bloque defensivo: Evita que el Gateway explote localmente cuando no detecta GCP
            System.out.println("⚠️ Aviso: No se pudieron inyectar credenciales de GCP (Omitido para desarrollo local).");
        }
        return null;
    }

    @Override
    public int getOrder() {
        // Le damos una prioridad alta para que corra justo antes de rutear el request a GCP
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}