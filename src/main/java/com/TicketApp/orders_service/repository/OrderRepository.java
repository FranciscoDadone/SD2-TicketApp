package com.TicketApp.orders_service.repository;

import com.TicketApp.orders_service.model.Order;
import com.TicketApp.orders_service.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerEmail(String email);
    List<Order> findByEventId(Long eventId);
    List<Order> findByStatus(OrderStatus status);
    Optional<Order> findByQrCode(String qrCode);
}
