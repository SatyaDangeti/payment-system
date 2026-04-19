package order.order_service.kafka;



import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import order.order_service.service.OrderService;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-group",
            containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void consume(PaymentEvent event) {
        if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
            orderService.confirmOrder(event.getOrderId());
        } else {
            orderService.cancelOrder(event.getOrderId());
        }
    }
}