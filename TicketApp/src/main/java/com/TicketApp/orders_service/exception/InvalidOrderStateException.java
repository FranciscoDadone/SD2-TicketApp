package com.TicketApp.orders_service.exception;

import com.TicketApp.orders_service.enums.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
    
    public InvalidOrderStateException(OrderStatus currentStatus, OrderStatus newStatus) {
        super(String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
    }
}
