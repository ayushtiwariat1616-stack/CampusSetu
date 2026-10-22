package edu.kiet.campussetu.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_category", columnList = "eventCategory"),
        @Index(name = "idx_audit_actor", columnList = "actorEmail")
})
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String eventCategory; // AUTH, OPERATOR_KEY, ROSTER_CHANGE, ESCALATION, REOPEN, EXCEPTIONAL_ACCESS

    @Column(nullable = false, length = 120)
    private String actorEmail;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 150)
    private String targetResource;

    @Column(length = 2000)
    private String details;

    @Column(length = 60)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AuditEvent() {}

    public AuditEvent(String eventCategory, String actorEmail, String action, String targetResource, String details, String ipAddress) {
        this.eventCategory = eventCategory;
        this.actorEmail = actorEmail;
        this.action = action;
        this.targetResource = targetResource;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }

    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTargetResource() { return targetResource; }
    public void setTargetResource(String targetResource) { this.targetResource = targetResource; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
