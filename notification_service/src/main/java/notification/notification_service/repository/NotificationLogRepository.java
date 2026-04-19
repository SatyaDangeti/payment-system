package notification.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import notification.notification_service.model.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
}
