package order.order_service.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import order.order_service.dto.CreateOrderRequest;
import order.order_service.dto.OrderResponse;
import order.order_service.kafka.OrderCreatedEvent;
import order.order_service.kafka.OrderEventProducer;
import order.order_service.model.Order;
import order.order_service.model.OrderStatus;
import order.order_service.repository.OrderRepository;


@Service
@RequiredArgsConstructor
public class OrderService {
    
      private final OrderRepository orderRepository;
    private final OrderEventProducer producer;
    public OrderResponse  createOrder(CreateOrderRequest request){

        Order order =Order.builder()
        .id(UUID.randomUUID())
        .amount(request.getAmount())
        .status(OrderStatus.CREATED)
        .createdAt(Instant.now())
        .build();

        orderRepository.save(order);
        producer.publish(OrderCreatedEvent.builder()
                .orderId(order.getId())
                .amount(order.getAmount())
                .build());

        return OrderResponse.builder()
                .id(order.getId())
                .amount(order.getAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
    public void confirmOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}

