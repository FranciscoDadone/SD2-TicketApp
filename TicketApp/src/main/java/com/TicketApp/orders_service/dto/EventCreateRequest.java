package com.TicketApp.orders_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos requeridos para crear un nuevo evento")
public class EventCreateRequest {

    @Schema(description = "Nombre del evento", example = "Concierto de Rock 2026", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Fecha y hora del evento", example = "2026-07-15T21:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime date;

    @Schema(description = "Ubicación del evento", example = "Estadio Monumental, Buenos Aires", requiredMode = Schema.RequiredMode.REQUIRED)
    private String location;

    @Schema(description = "Capacidad máxima del evento", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxCapacity;

    @Schema(description = "Entradas disponibles para la venta", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer availableTickets;

    @Schema(description = "Precio por entrada", example = "5000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal ticketPrice;
}
