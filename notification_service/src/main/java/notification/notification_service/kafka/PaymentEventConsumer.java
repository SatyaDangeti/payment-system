
package notification.notification_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import notification.notification_service.service.NotificationService;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "payment-events", groupId = "notification-group", containerFactory = "paymentKafkaListenerContainerFactory")
    public void consume(PaymentEvent event) {
        String message = "Payment " + event.getStatus() + " for order " + event.getOrderId();
        notificationService.saveNotification(event.getOrderId(), message);
        System.out.println(message);
    }
}