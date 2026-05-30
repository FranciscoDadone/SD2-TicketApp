package com.TicketApp.orders_service.repository;

import com.TicketApp.orders_service.model.Order;
import com.TicketApp.orders_service.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByBuyerEmail(String email);
    List<Order> findByEvent_Id(Long eventId);
    List<Order> findByStatus(OrderStatus status);
    Optional<Order> findByQrCode(String qrCode);
}
