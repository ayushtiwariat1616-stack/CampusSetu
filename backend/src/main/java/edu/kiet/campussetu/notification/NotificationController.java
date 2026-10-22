package edu.kiet.campussetu.notification;

import edu.kiet.campussetu.auth.AuthHelper;
import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.ApiResponse;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final InAppNotificationRepository notificationRepository;
    private final AuthHelper authHelper;

    public NotificationController(InAppNotificationRepository notificationRepository, AuthHelper authHelper) {
        this.notificationRepository = notificationRepository;
        this.authHelper = authHelper;
    }

    /**
     * Get recent notifications and unread count for current user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireApprovedUser(request);
        Page<InAppNotification> notifications = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(
                user.getUserId(), PageRequest.of(page, size));
        long unreadCount = notificationRepository.countByRecipientUserIdAndIsReadFalse(user.getUserId());

        return ResponseEntity.ok(ApiResponse.ok(new NotificationListResponse(notifications, unreadCount)));
    }

    /**
     * Mark notification as read.
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireApprovedUser(request);
        InAppNotification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));

        if (notif.getRecipientUserId().equals(user.getUserId())) {
            notif.setRead(true);
            notificationRepository.save(notif);
        }
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", null));
    }

    public record NotificationListResponse(Page<InAppNotification> notifications, long unreadCount) {}
}
