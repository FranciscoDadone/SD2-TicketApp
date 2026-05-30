package com.TicketApp.orders_service.repository;

import com.TicketApp.orders_service.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByDateAfter(LocalDateTime date);
    List<Event> findByAvailableTicketsGreaterThan(Integer minTickets);
}
