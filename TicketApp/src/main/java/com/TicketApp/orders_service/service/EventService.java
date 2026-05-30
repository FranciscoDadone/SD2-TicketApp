package com.TicketApp.orders_service.service;

import com.TicketApp.orders_service.dto.EventCreateRequest;
import com.TicketApp.orders_service.dto.EventResponse;
import com.TicketApp.orders_service.dto.EventUpdateRequest;
import com.TicketApp.orders_service.exception.EventNotFoundException;
import com.TicketApp.orders_service.exception.InvalidOrderDataException;
import com.TicketApp.orders_service.model.Event;
import com.TicketApp.orders_service.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventResponse createEvent(EventCreateRequest request) {
        validateCreateRequest(request);

        Event event = new Event();
        event.setName(request.getName());
        event.setDate(request.getDate());
        event.setLocation(request.getLocation());
        event.setMaxCapacity(request.getMaxCapacity());
        event.setAvailableTickets(request.getAvailableTickets());
        event.setTicketPrice(request.getTicketPrice());

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
        return mapToResponse(event);
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EventResponse> getAvailableEvents() {
        return eventRepository.findByAvailableTicketsGreaterThan(0).stream()
                .filter(e -> e.getDate().isAfter(LocalDateTime.now()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventUpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        if (request.getName() != null) {
            event.setName(request.getName());
        }
        if (request.getDate() != null) {
            event.setDate(request.getDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getMaxCapacity() != null) {
            if (request.getMaxCapacity() < 1) {
                throw new InvalidOrderDataException("La capacidad máxima debe ser mayor a 0");
            }
            event.setMaxCapacity(request.getMaxCapacity());
        }
        if (request.getAvailableTickets() != null) {
            if (request.getAvailableTickets() < 0) {
                throw new InvalidOrderDataException("Las entradas disponibles no pueden ser negativas");
            }
            event.setAvailableTickets(request.getAvailableTickets());
        }
        if (request.getTicketPrice() != null) {
            event.setTicketPrice(request.getTicketPrice());
        }

        Event updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException(id);
        }
        eventRepository.deleteById(id);
    }

    @Transactional
    public void decrementAvailableTickets(Long eventId, int quantity) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getAvailableTickets() < quantity) {
            throw new InvalidOrderDataException(
                    "No hay suficientes entradas disponibles. Disponibles: " + event.getAvailableTickets());
        }

        event.setAvailableTickets(event.getAvailableTickets() - quantity);
        eventRepository.save(event);
    }

    private void validateCreateRequest(EventCreateRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidOrderDataException("El nombre del evento es obligatorio");
        }
        if (request.getDate() == null) {
            throw new InvalidOrderDataException("La fecha del evento es obligatoria");
        }
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            throw new InvalidOrderDataException("La ubicación del evento es obligatoria");
        }
        if (request.getMaxCapacity() == null || request.getMaxCapacity() < 1) {
            throw new InvalidOrderDataException("La capacidad máxima debe ser mayor a 0");
        }
        if (request.getAvailableTickets() == null || request.getAvailableTickets() < 0) {
            throw new InvalidOrderDataException("Las entradas disponibles no pueden ser negativas");
        }
        if (request.getAvailableTickets() > request.getMaxCapacity()) {
            throw new InvalidOrderDataException(
                    "Las entradas disponibles no pueden superar la capacidad máxima");
        }
        if (request.getTicketPrice() == null || request.getTicketPrice().signum() <= 0) {
            throw new InvalidOrderDataException("El precio de la entrada debe ser mayor a 0");
        }
    }

    private EventResponse mapToResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setName(event.getName());
        response.setDate(event.getDate());
        response.setLocation(event.getLocation());
        response.setMaxCapacity(event.getMaxCapacity());
        response.setAvailableTickets(event.getAvailableTickets());
        response.setTicketPrice(event.getTicketPrice());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        return response;
    }
}
