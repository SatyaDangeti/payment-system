package order.order_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import order.order_service.model.Order;

public interface OrderRepository  extends JpaRepository<Order, UUID>{
    
}
