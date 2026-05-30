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
@Schema(description = "Datos para actualizar un evento existente")
public class EventUpdateRequest {

    @Schema(description = "Nombre del evento", example = "Concierto de Rock 2026 - Edición Especial")
    private String name;

    @Schema(description = "Fecha y hora del evento", example = "2026-07-15T21:00:00")
    private LocalDateTime date;

    @Schema(description = "Ubicación del evento", example = "Estadio Monumental, Buenos Aires")
    private String location;

    @Schema(description = "Capacidad máxima del evento", example = "55000")
    private Integer maxCapacity;

    @Schema(description = "Entradas disponibles para la venta", example = "45000")
    private Integer availableTickets;

    @Schema(description = "Precio por entrada", example = "6000.00")
    private BigDecimal ticketPrice;
}
