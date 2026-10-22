package edu.kiet.campussetu.complaint;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "complaint_events", indexes = {
        @Index(name = "idx_event_complaint", columnList = "complaint_id")
})
public class ComplaintEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(nullable = false, length = 50)
    private String eventType; // SUBMITTED, ACKNOWLEDGED, STATUS_CHANGE, FORWARDED, ESCALATED, RESOLUTION_PROPOSED, RESOLUTION_CONFIRMED, RESOLUTION_REJECTED, REOPENED, NOTE_ADDED, REPLY_ADDED

    @Column(nullable = false, length = 120)
    private String actorEmail;

    @Column(nullable = false, length = 150)
    private String actorName;

    @Column(nullable = false, length = 50)
    private String actorRole;

    @Column(length = 80)
    private String fromQueueCode;

    @Column(length = 80)
    private String toQueueCode;

    @Column(length = 2000)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ComplaintEvent() {}

    public ComplaintEvent(Long complaintId, String eventType, String actorEmail, String actorName, String actorRole, String fromQueueCode, String toQueueCode, String message) {
        this.complaintId = complaintId;
        this.eventType = eventType;
        this.actorEmail = actorEmail;
        this.actorName = actorName;
        this.actorRole = actorRole;
        this.fromQueueCode = fromQueueCode;
        this.toQueueCode = toQueueCode;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getFromQueueCode() { return fromQueueCode; }
    public void setFromQueueCode(String fromQueueCode) { this.fromQueueCode = fromQueueCode; }

    public String getToQueueCode() { return toQueueCode; }
    public void setToQueueCode(String toQueueCode) { this.toQueueCode = toQueueCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
