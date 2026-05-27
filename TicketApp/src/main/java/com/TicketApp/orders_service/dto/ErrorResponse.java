package com.TicketApp.orders_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta de error estándar")
public class ErrorResponse {
    
    @Schema(description = "Fecha y hora del error", example = "2026-03-26T10:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "Código de estado HTTP", example = "400")
    private Integer status;
    
    @Schema(description = "Tipo de error", example = "Bad Request")
    private String error;
    
    @Schema(description = "Mensaje descriptivo del error", example = "Email del comprador inválido")
    private String message;
    
    @Schema(description = "Path del endpoint que generó el error", example = "/api/orders")
    private String path;
}
