package notification.notification_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import notification.notification_service.service.NotificationService;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @PostConstruct
    public void init() {
        System.out.println("🚀 PaymentEventConsumer initialized");
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "notification-group",
            containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void consume(PaymentEvent event) {
        System.out.println("📥 Received payment event");
        System.out.println("🧾 Order ID: " + event.getOrderId());
        System.out.println("💰 Amount: " + event.getAmount());
        System.out.println("📦 Status: " + event.getStatus());

        String message = "Payment " + event.getStatus() + " for order " + event.getOrderId();

        notificationService.saveNotification(event.getOrderId(), message);

        System.out.println("✅ Notification flow completed for order: " + event.getOrderId());
    }
}