package edu.kiet.campussetu.notification;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "in_app_notifications", indexes = {
        @Index(name = "idx_notif_recipient", columnList = "recipientUserId"),
        @Index(name = "idx_notif_read", columnList = "isRead")
})
public class InAppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientUserId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 50)
    private String notificationType; // COMPLAINT_UPDATE, ESCALATION, RESOLUTION, INCIDENT_UPDATE, SYSTEM

    private Long referenceId;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public InAppNotification() {}

    public InAppNotification(Long recipientUserId, String title, String message, String notificationType, Long referenceId) {
        this.recipientUserId = recipientUserId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.referenceId = referenceId;
        this.isRead = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(Long recipientUserId) { this.recipientUserId = recipientUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
