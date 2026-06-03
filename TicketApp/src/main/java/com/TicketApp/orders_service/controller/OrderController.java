package com.TicketApp.orders_service.controller;

import com.TicketApp.orders_service.dto.ErrorResponse;
import com.TicketApp.orders_service.dto.OrderCreateRequest;
import com.TicketApp.orders_service.dto.OrderResponse;
import com.TicketApp.orders_service.dto.OrderUpdateRequest;
import com.TicketApp.orders_service.enums.OrderStatus;
import com.TicketApp.orders_service.exception.InvalidOrderDataException;
import com.TicketApp.orders_service.service.OrderService;
import com.TicketApp.orders_service.service.PubSubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API para gestión de órdenes de tickets")
public class OrderController {
    
    private final OrderService orderService;
    private final PubSubService pubSubService;
    
    @Operation(summary = "Crear una nueva orden", description = "Crea una nueva orden de compra de tickets para un evento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de la orden inválidos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestBody OrderCreateRequest request) {
        
        // Validar que el email del JWT esté presente
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new InvalidOrderDataException("No se pudo obtener el email del usuario autenticado");
        }
        
        request.setBuyerEmail(userEmail);
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "Obtener orden por ID", description = "Obtiene los detalles de una orden específica por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @Parameter(description = "ID de la orden", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        OrderResponse response = orderService.getOrderById(id);
        
        // Si se proporciona el header X-User-Email, validar que la orden pertenece a ese usuario
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            if (!response.getBuyerEmail().equals(userEmail)) {
                throw new InvalidOrderDataException("La orden no pertenece al usuario especificado");
            }
        }
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Listar órdenes", description = "Obtiene una lista de órdenes con filtros opcionales")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @Parameter(description = "Filtrar por email del comprador", example = "juan.perez@email.com")
            @RequestParam(required = false) String buyerEmail,
            @Parameter(description = "Filtrar por ID del evento", example = "101")
            @RequestParam(required = false) Long eventId,
            @Parameter(description = "Filtrar por estado de la orden", example = "PENDING_PAYMENT")
            @RequestParam(required = false) OrderStatus status) {
        
        // Si se proporciona el header X-User-Email, filtrar por ese email
        // De lo contrario, usar el parámetro buyerEmail (puede ser null para obtener todas)
        String emailFilter = (userEmail != null && !userEmail.trim().isEmpty()) ? userEmail : buyerEmail;
        
        List<OrderResponse> orders = orderService.getOrders(emailFilter, eventId, status);
        return ResponseEntity.ok(orders);
    }
    
    @Operation(summary = "Actualizar estado de orden", description = "Actualiza el estado de una orden existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Estado de orden inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Estado de orden conflictivo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "ID de la orden", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @RequestBody OrderUpdateRequest request) {
        
        orderService.updateOrderStatus(id, request.getStatus());

        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Cancelar orden", description = "Cancela una orden existente cambiando su estado a FAILED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden cancelada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "No se puede cancelar la orden en su estado actual",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "ID de la orden", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        OrderResponse response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Validar ticket por código QR", description = "Valida un ticket usando su código QR y marca la orden como USED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket validado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Ticket ya fue usado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/validate/{qrCode}")
    public ResponseEntity<OrderResponse> validateTicket(
            @Parameter(description = "Código QR del ticket", required = true, example = "QR-1234567890")
            @PathVariable String qrCode) {
        OrderResponse response = orderService.validateTicket(qrCode);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Enviar mensaje a Pub/Sub", description = "Envía un mensaje a Google Cloud Pub/Sub")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mensaje enviado exitosamente"),
        @ApiResponse(responseCode = "400", description = "El campo 'mensaje' es obligatorio",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Error enviando mensaje a Pub/Sub",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/send")
    public ResponseEntity<String> dispararEvento(@RequestBody Map<String, String> payload) {
        String mensaje = payload.get("mensaje");
        if (mensaje == null || mensaje.isEmpty()) {
            return ResponseEntity.badRequest().body("El campo 'mensaje' es obligatorio.");
        }
        String resultado = pubSubService.enviarMensaje(mensaje);
        return ResponseEntity.ok(resultado);
    }
}
