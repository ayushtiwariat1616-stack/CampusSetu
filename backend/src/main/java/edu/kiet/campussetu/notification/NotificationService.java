package edu.kiet.campussetu.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final InAppNotificationRepository notificationRepository;

    public NotificationService(InAppNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void sendInApp(Long recipientUserId, String title, String message, String notificationType, Long referenceId) {
        try {
            InAppNotification notification = new InAppNotification(recipientUserId, title, message, notificationType, referenceId);
            notificationRepository.save(notification);
            log.info("Notification sent to user {}: {}", recipientUserId, title);
        } catch (Exception e) {
            log.error("Failed to persist notification for user {}: {}", recipientUserId, e.getMessage());
            // Do not break the caller transaction for notification failures
        }
    }
}
