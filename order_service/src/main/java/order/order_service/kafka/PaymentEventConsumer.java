package order.order_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import order.order_service.service.OrderService;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @PostConstruct
    public void init() {
        System.out.println("🚀 PaymentEventConsumer initialized");
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-group",
            containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void consume(PaymentEvent event) {
        System.out.println("📥 Received payment event in order service");
        System.out.println("🧾 Order ID: " + event.getOrderId());
        System.out.println("📦 Payment Status: " + event.getStatus());
        System.out.println("💰 Amount: " + event.getAmount());

        if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
            orderService.confirmOrder(event.getOrderId());
            System.out.println("✅ Order confirmed: " + event.getOrderId());
        } else if ("FAILED".equalsIgnoreCase(event.getStatus())) {
            orderService.cancelOrder(event.getOrderId());
            System.out.println("❌ Order cancelled due to payment failure: " + event.getOrderId());
        } else {
            System.out.println("⚠️ Unknown payment status received: " + event.getStatus());
        }
    }
}