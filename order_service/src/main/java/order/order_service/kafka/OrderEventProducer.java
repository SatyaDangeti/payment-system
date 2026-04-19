package order.order_service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> orderKafkaTemplate;

    public void publish(OrderCreatedEvent event) {
        orderKafkaTemplate.send("order-created", event);
    }
}