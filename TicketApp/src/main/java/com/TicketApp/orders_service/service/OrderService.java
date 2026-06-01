package com.TicketApp.orders_service.service;

import com.TicketApp.orders_service.dto.OrderCreateRequest;
import com.TicketApp.orders_service.dto.OrderResponse;
import com.TicketApp.orders_service.dto.OrderUpdateRequest;
import com.TicketApp.orders_service.enums.OrderStatus;
import com.TicketApp.orders_service.exception.InvalidOrderDataException;
import com.TicketApp.orders_service.exception.InvalidOrderStateException;
import com.TicketApp.orders_service.exception.OrderNotFoundException;
import com.TicketApp.orders_service.exception.TicketAlreadyUsedException;
import com.TicketApp.orders_service.exception.EventNotFoundException;
import com.TicketApp.orders_service.model.Event;
import com.TicketApp.orders_service.model.Order;
import com.TicketApp.orders_service.repository.EventRepository;
import com.TicketApp.orders_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;
    private final PubSubService pubSubService;
    
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        validateOrderRequest(request);
        
        // Create order entity
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException(request.getEventId()));
        Order order = new Order();
        order.setEvent(event);
        order.setEventName(request.getEventName());
        order.setBuyerEmail(request.getBuyerEmail());
        order.setBuyerName(request.getBuyerName());
        order.setQuantity(request.getQuantity());
        order.setUnitPrice(request.getUnitPrice());
        
        BigDecimal totalAmount = request.getUnitPrice().multiply(new BigDecimal(request.getQuantity()));
        order.setTotalAmount(totalAmount);
        
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        
        order.setQrCode(generateQrCode());
        
        Order savedOrder = orderRepository.save(order);
        pubSubService.publishOrderCreated(savedOrder);
        
        return mapToResponse(savedOrder);
    }
    
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return mapToResponse(order);
    }
    
    public List<OrderResponse> getOrdersByBuyer(String buyerEmail) {
        List<Order> orders = orderRepository.findByBuyerEmail(buyerEmail);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<OrderResponse> getOrders(String buyerEmail, Long eventId, OrderStatus status) {
        List<Order> orders;
        
        // Apply filters based on provided parameters
        if (buyerEmail != null && !buyerEmail.trim().isEmpty()) {
            orders = orderRepository.findByBuyerEmail(buyerEmail);
        } else if (eventId != null) {
            orders = orderRepository.findByEvent_Id(eventId);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }
        
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(UUID id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        
        OrderStatus currentStatus = order.getStatus();
        
        validateStatusTransition(currentStatus, newStatus);
        
        order.setStatus(newStatus);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        
        return mapToResponse(updatedOrder);
    }
    
    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        
        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.USED) {
            throw new InvalidOrderStateException(
                    String.format("Cannot cancel order in status %s. Only PENDING_PAYMENT orders can be cancelled", 
                            order.getStatus()));
        }
        
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStateException(
                    String.format("Cannot cancel order in status %s. Only PENDING_PAYMENT orders can be cancelled", 
                            order.getStatus()));
        }
        
        order.setStatus(OrderStatus.FAILED);
        Order cancelledOrder = orderRepository.save(order);
        
        return mapToResponse(cancelledOrder);
    }
    
    @Transactional
    public OrderResponse validateTicket(String qrCode) {
        Order order = orderRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with QR code: " + qrCode));
        
        if (order.getStatus() == OrderStatus.USED) {
            throw new TicketAlreadyUsedException(
                    "Cannot validate ticket that has already been used");
        }
        
        if (order.getStatus() == OrderStatus.FAILED) {
            throw new InvalidOrderStateException(
                    "Cannot validate ticket for a cancelled order");
        }
        
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException(
                    String.format("Cannot validate ticket in status %s. Only CONFIRMED orders can be marked as USED", 
                            order.getStatus()));
        }
        
        order.setStatus(OrderStatus.USED);
        Order usedOrder = orderRepository.save(order);
        
        return mapToResponse(usedOrder);
    }
    
    
    private void validateOrderRequest(OrderCreateRequest request) {
        if (request.getEventId() == null) {
            throw new InvalidOrderDataException("Event ID is required");
        }
        
        if (request.getEventName() == null || request.getEventName().trim().isEmpty()) {
            throw new InvalidOrderDataException("Event name is required");
        }
        
        if (request.getBuyerEmail() == null || request.getBuyerEmail().trim().isEmpty()) {
            throw new InvalidOrderDataException("Buyer email is required");
        }
        
        if (!isValidEmail(request.getBuyerEmail())) {
            throw new InvalidOrderDataException("Buyer email must be a valid email address");
        }
        
        if (request.getBuyerName() == null || request.getBuyerName().trim().isEmpty()) {
            throw new InvalidOrderDataException("Buyer name is required");
        }
        
        if (request.getQuantity() == null || request.getQuantity() < 1 || request.getQuantity() > 5) {
            throw new InvalidOrderDataException("Quantity must be between 1 and 5");
        }
        
        if (request.getUnitPrice() == null || request.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderDataException("Unit price must be greater than 0");
        }
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValidTransition = false;
        
        switch (currentStatus) {
            case PENDING_PAYMENT:
                isValidTransition = (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.FAILED);
                break;
            case CONFIRMED:
                isValidTransition = (newStatus == OrderStatus.USED);
                break;
            case FAILED:
            case USED:
                isValidTransition = false;
                break;
        }
        
        if (!isValidTransition) {
            throw new InvalidOrderStateException(currentStatus, newStatus);
        }
    }
    
    private String generateQrCode() {
        return "QR-" + UUID.randomUUID().toString();
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setEventId(order.getEvent().getId());
        response.setEventName(order.getEventName());
        response.setBuyerEmail(order.getBuyerEmail());
        response.setBuyerName(order.getBuyerName());
        response.setQuantity(order.getQuantity());
        response.setUnitPrice(order.getUnitPrice());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setQrCode(order.getQrCode());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }
}
