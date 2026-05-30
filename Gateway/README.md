# API Gateway - TicketApp

Este microservicio actúa como el **guardián de seguridad perimetral** (Punto de entrada único) para el sistema distribuido TicketApp, implementando el patrón *API Gateway* y principios de seguridad *Zero Trust*.

## Arquitectura y Decisiones de Diseño

El Gateway está construido sobre **Spring Cloud Gateway** y **Spring WebFlux** (stack reactivo) para garantizar un enrutamiento asíncrono y no bloqueante de alto rendimiento.

**Justificación de decisiones:**
* **Stack Reactivo:** Se eligió WebFlux por sobre Web MVC porque Spring Cloud Gateway requiere un modelo no bloqueante para manejar eficientemente múltiples conexiones concurrentes sin agotar los hilos del servidor.
* **Desacoplamiento Perimetral:** El Gateway aísla a los microservicios internos (como `orders-service`) de Internet. Ningún cliente externo puede acceder directamente a la lógica de negocio sin pasar por los filtros de este servicio.

## Seguridad (Zero Trust y Autorización)

El modelo de seguridad se divide en dos fases implementadas a través de filtros:

1. **Validación del Cliente (Keycloak):** El Gateway actúa como un *OAuth2 Resource Server*. Intercepta cada petición entrante y valida la firma criptográfica del JWT consultando las llaves públicas del Identity Provider (Keycloak - Realm `Final-TP`). Si el token es inválido o no existe, se rechaza la petición con un `401 Unauthorized`. Adicionalmente, verifica el rol `admin` extraído del claim `realm_access.roles` para restringir endpoints sensibles (POST, PUT, DELETE).
2. **Autenticación Interna (GCP OIDC):** Una vez que el usuario es validado, un Filtro Global (`GcpOidcAuthFilter`) intercepta la petición antes de enrutarla. Elimina el JWT del cliente y utiliza la **Service Account** asignada en Google Cloud para solicitar un token OIDC. Este nuevo token de infraestructura se inyecta en el header `Authorization` para invocar de forma segura al `orders-service` privado.

