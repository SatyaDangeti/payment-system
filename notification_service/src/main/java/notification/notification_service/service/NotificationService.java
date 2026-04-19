package notification.notification_service.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import notification.notification_service.model.NotificationLog;
import notification.notification_service.repository.NotificationLogRepository;
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationLogRepository notificationRepository;

    public void saveNotification(UUID orderId, String message) {
        notificationRepository.save(NotificationLog.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .message(message)
                .createdAt(Instant.now())
                .build());
    }  
}
