package com.TicketApp.orders_service.exception;

public class TicketAlreadyUsedException extends RuntimeException {
    public TicketAlreadyUsedException(String ticketId) {
        super(String.format("Ticket %s has already been used", ticketId));
    }
}
