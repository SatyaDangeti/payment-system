package payment.payment_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class PaymentDlqConsumer {

    @PostConstruct
    public void init() {
        System.out.println("🚀 PaymentDlqConsumer initialized");
    }

    @KafkaListener(topics = "payment-dlq", groupId = "payment-dlq-group")
    public void consume(String message) {
        System.out.println("☠️ Received message in payment-dlq");
        System.out.println("📦 DLQ Payload: " + message);
    }
}