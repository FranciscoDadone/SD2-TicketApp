package com.TicketApp.orders_service.controller;

import com.TicketApp.orders_service.dto.ErrorResponse;
import com.TicketApp.orders_service.dto.EventCreateRequest;
import com.TicketApp.orders_service.dto.EventResponse;
import com.TicketApp.orders_service.dto.EventUpdateRequest;
import com.TicketApp.orders_service.service.EventService;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "Events", description = "API para gestión de eventos musicales")
public class EventController {

    private final EventService eventService;

    // ─── Endpoints públicos (usuarios autenticados) ───────────────────────────

    @Operation(summary = "Listar eventos disponibles", description = "Retorna todos los eventos con entradas disponibles y fecha futura")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class)))
    })
    @GetMapping("/api/events")
    public ResponseEntity<List<EventResponse>> getAvailableEvents() {
        return ResponseEntity.ok(eventService.getAvailableEvents());
    }

    @Operation(summary = "Obtener evento por ID", description = "Obtiene los detalles de un evento específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Evento encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "404", description = "Evento no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/events/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @Parameter(description = "ID del evento", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    // ─── Endpoints de administración (solo admin) ─────────────────────────────

    @Operation(summary = "Listar todos los eventos", description = "Retorna todos los eventos (incluyendo pasados y sin stock). Solo administradores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista completa de eventos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class)))
    })
    @GetMapping("/api/admin/events")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @Operation(summary = "Crear evento", description = "Crea un nuevo evento. Solo administradores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Evento creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos del evento inválidos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/api/admin/events")
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventCreateRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Actualizar evento", description = "Actualiza los datos de un evento existente. Solo administradores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Evento actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Evento no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/api/admin/events/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @Parameter(description = "ID del evento", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody EventUpdateRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @Operation(summary = "Eliminar evento", description = "Elimina un evento del sistema. Solo administradores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Evento eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Evento no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/api/admin/events/{id}")
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID del evento", required = true, example = "1")
            @PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
