package com.TicketApp.orders_service.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
    
    public OrderNotFoundException(java.util.UUID id) {
        super("Order not found with id: " + id);
    }
}
