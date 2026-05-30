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
@Schema(description = "Respuesta con los datos completos de un evento")
public class EventResponse {

    @Schema(description = "ID único del evento", example = "1")
    private Long id;

    @Schema(description = "Nombre del evento", example = "Concierto de Rock 2026")
    private String name;

    @Schema(description = "Fecha y hora del evento", example = "2026-07-15T21:00:00")
    private LocalDateTime date;

    @Schema(description = "Ubicación del evento", example = "Estadio Monumental, Buenos Aires")
    private String location;

    @Schema(description = "Capacidad máxima del evento", example = "50000")
    private Integer maxCapacity;

    @Schema(description = "Entradas disponibles para la venta", example = "48500")
    private Integer availableTickets;

    @Schema(description = "Precio por entrada", example = "5000.00")
    private BigDecimal ticketPrice;

    @Schema(description = "Fecha y hora de creación del evento", example = "2026-03-26T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha y hora de última actualización", example = "2026-03-26T10:30:00")
    private LocalDateTime updatedAt;
}
