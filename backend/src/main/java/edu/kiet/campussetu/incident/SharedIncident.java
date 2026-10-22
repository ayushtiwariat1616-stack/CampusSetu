package edu.kiet.campussetu.incident;

import edu.kiet.campussetu.complaint.ComplaintCategory;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shared_incidents", indexes = {
        @Index(name = "idx_incident_number", columnList = "incidentNumber", unique = true),
        @Index(name = "idx_incident_status", columnList = "status"),
        @Index(name = "idx_incident_queue", columnList = "responsibleQueueCode")
})
public class SharedIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String incidentNumber; // e.g. "INC-2026-0001"

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String sanitizedDescription; // strictly sanitized: NO student names, NO room numbers, NO private allegations

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplaintCategory category;

    @Column(nullable = false, length = 150)
    private String generalLocation; // e.g. "Aryabhatt Hostel - B Wing", "Block A 2nd Floor", "Campus Wi-Fi Grid"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SharedIncidentStatus status = SharedIncidentStatus.REPORTED;

    @Column(nullable = false, length = 80)
    private String responsibleQueueCode;

    @Column(nullable = false)
    private int affectedSubscriberCount = 0;

    @Column(length = 2000)
    private String publicProgressUpdates;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant resolvedAt;

    public SharedIncident() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIncidentNumber() { return incidentNumber; }
    public void setIncidentNumber(String incidentNumber) { this.incidentNumber = incidentNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSanitizedDescription() { return sanitizedDescription; }
    public void setSanitizedDescription(String sanitizedDescription) { this.sanitizedDescription = sanitizedDescription; }

    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public String getGeneralLocation() { return generalLocation; }
    public void setGeneralLocation(String generalLocation) { this.generalLocation = generalLocation; }

    public SharedIncidentStatus getStatus() { return status; }
    public void setStatus(SharedIncidentStatus status) { this.status = status; }

    public String getResponsibleQueueCode() { return responsibleQueueCode; }
    public void setResponsibleQueueCode(String responsibleQueueCode) { this.responsibleQueueCode = responsibleQueueCode; }

    public int getAffectedSubscriberCount() { return affectedSubscriberCount; }
    public void setAffectedSubscriberCount(int affectedSubscriberCount) { this.affectedSubscriberCount = affectedSubscriberCount; }

    public String getPublicProgressUpdates() { return publicProgressUpdates; }
    public void setPublicProgressUpdates(String publicProgressUpdates) { this.publicProgressUpdates = publicProgressUpdates; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
