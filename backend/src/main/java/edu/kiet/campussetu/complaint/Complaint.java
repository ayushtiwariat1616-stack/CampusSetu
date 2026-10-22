package edu.kiet.campussetu.complaint;

import edu.kiet.campussetu.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "complaints", indexes = {
        @Index(name = "idx_complaint_tracking", columnList = "trackingNumber", unique = true),
        @Index(name = "idx_complaint_student", columnList = "student_id"),
        @Index(name = "idx_complaint_queue", columnList = "currentQueueCode"),
        @Index(name = "idx_complaint_status", columnList = "status"),
        @Index(name = "idx_complaint_category", columnList = "category")
})
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String trackingNumber; // e.g. "CMP-2026-0001"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintLocationType locationType;

    @Column(nullable = false, length = 200)
    private String locationDetail;

    @Column(length = 50)
    private String specificHostelCode; // e.g. "ARYABHATT"

    @Column(length = 50)
    private String specificDepartmentCode; // e.g. "AIML"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintUrgency urgency = ComplaintUrgency.MEDIUM;

    @Column(nullable = false, length = 80)
    private String currentQueueCode;

    @Column(length = 120)
    private String assignedOperatorEmail;

    @Column(length = 2000)
    private String resolutionSummary;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(length = 1000)
    private String closureNotes;

    @Column(nullable = false)
    private boolean sensitive = false;

    private Long sharedIncidentId;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant acknowledgedAt;

    private Instant resolutionProposedAt;

    private Instant closedAt;

    public Complaint() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public ComplaintLocationType getLocationType() { return locationType; }
    public void setLocationType(ComplaintLocationType locationType) { this.locationType = locationType; }

    public String getLocationDetail() { return locationDetail; }
    public void setLocationDetail(String locationDetail) { this.locationDetail = locationDetail; }

    public String getSpecificHostelCode() { return specificHostelCode; }
    public void setSpecificHostelCode(String specificHostelCode) { this.specificHostelCode = specificHostelCode; }

    public String getSpecificDepartmentCode() { return specificDepartmentCode; }
    public void setSpecificDepartmentCode(String specificDepartmentCode) { this.specificDepartmentCode = specificDepartmentCode; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public ComplaintUrgency getUrgency() { return urgency; }
    public void setUrgency(ComplaintUrgency urgency) { this.urgency = urgency; }

    public String getCurrentQueueCode() { return currentQueueCode; }
    public void setCurrentQueueCode(String currentQueueCode) { this.currentQueueCode = currentQueueCode; }

    public String getAssignedOperatorEmail() { return assignedOperatorEmail; }
    public void setAssignedOperatorEmail(String assignedOperatorEmail) { this.assignedOperatorEmail = assignedOperatorEmail; }

    public String getResolutionSummary() { return resolutionSummary; }
    public void setResolutionSummary(String resolutionSummary) { this.resolutionSummary = resolutionSummary; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getClosureNotes() { return closureNotes; }
    public void setClosureNotes(String closureNotes) { this.closureNotes = closureNotes; }

    public boolean isSensitive() { return sensitive; }
    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }

    public Long getSharedIncidentId() { return sharedIncidentId; }
    public void setSharedIncidentId(Long sharedIncidentId) { this.sharedIncidentId = sharedIncidentId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public Instant getResolutionProposedAt() { return resolutionProposedAt; }
    public void setResolutionProposedAt(Instant resolutionProposedAt) { this.resolutionProposedAt = resolutionProposedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}
