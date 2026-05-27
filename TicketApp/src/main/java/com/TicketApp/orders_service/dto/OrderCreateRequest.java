package com.TicketApp.orders_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos requeridos para crear una nueva orden")
public class OrderCreateRequest {
    
    @Schema(description = "ID del evento", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long eventId;
    
    @Schema(description = "Nombre del evento", example = "Concierto de Rock 2026", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventName;
    
    @Schema(description = "Email del comprador", example = "juan.perez@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String buyerEmail;
    
    @Schema(description = "Nombre completo del comprador", example = "Juan Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String buyerName;
    
    @Schema(description = "Cantidad de tickets a comprar", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
    
    @Schema(description = "Precio unitario del ticket", example = "5000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitPrice;
}
