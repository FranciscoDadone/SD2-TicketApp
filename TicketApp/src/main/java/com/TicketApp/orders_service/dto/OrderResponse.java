package com.TicketApp.orders_service.dto;

import com.TicketApp.orders_service.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta con los datos completos de una orden")
public class OrderResponse {
    
    @Schema(description = "ID único de la orden", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "ID del evento", example = "101")
    private Long eventId;
    
    @Schema(description = "Nombre del evento", example = "Concierto de Rock 2026")
    private String eventName;
    
    @Schema(description = "Email del comprador", example = "juan.perez@email.com")
    private String buyerEmail;
    
    @Schema(description = "Nombre completo del comprador", example = "Juan Pérez")
    private String buyerName;
    
    @Schema(description = "Cantidad de tickets", example = "2")
    private Integer quantity;
    
    @Schema(description = "Precio unitario del ticket", example = "5000.00")
    private BigDecimal unitPrice;
    
    @Schema(description = "Monto total de la orden (quantity * unitPrice)", example = "10000.00")
    private BigDecimal totalAmount;
    
    @Schema(description = "Estado actual de la orden", example = "PENDING_PAYMENT", allowableValues = {"PENDING_PAYMENT", "CONFIRMED", "FAILED", "USED"})
    private OrderStatus status;
    
    @Schema(description = "Código QR único para validación del ticket", example = "QR-1234567890")
    private String qrCode;
    
    @Schema(description = "Fecha y hora de creación de la orden", example = "2026-03-26T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha y hora de última actualización", example = "2026-03-26T10:30:00")
    private LocalDateTime updatedAt;
}
