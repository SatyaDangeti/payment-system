package notification.notification_service.kafka;
import lombok.*;

import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    private UUID orderId;
    private String status;
    private Double amount;
}