package payment.payment_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import payment.payment_service.dto.OrderCreatedEvent;
import payment.payment_service.dto.PaymentRequest;
import payment.payment_service.service.PaymentService;

@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final PaymentService paymentService;

    @PostConstruct
    public void init() {
        System.out.println("🚀 OrderCreatedConsumer initialized");
    }

    @KafkaListener(
            topics = "order-created",
            groupId = "payment-group",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    public void consume(OrderCreatedEvent event) {
        System.out.println("🔥 Received order-created event: " + event.getOrderId());
        System.out.println("💰 Amount: " + event.getAmount());

        PaymentRequest request = PaymentRequest.builder()
                .orderId(event.getOrderId())
                .amount(event.getAmount())
                .build();

        paymentService.createPayment(request, "order-" + event.getOrderId());
    }
}