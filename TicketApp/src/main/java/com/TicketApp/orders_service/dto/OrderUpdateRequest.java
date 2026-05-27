package com.TicketApp.orders_service.dto;

import com.TicketApp.orders_service.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para actualizar el estado de una orden")
public class OrderUpdateRequest {
    
    @Schema(description = "Nuevo estado de la orden", example = "CONFIRMED", allowableValues = {"PENDING_PAYMENT", "CONFIRMED", "FAILED", "USED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private OrderStatus status;
}
