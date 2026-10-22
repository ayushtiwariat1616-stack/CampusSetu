package edu.kiet.campussetu.incident;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "incident_subscriptions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_incident_student", columnNames = {"incident_id", "student_id"})
}, indexes = {
        @Index(name = "idx_subscription_student", columnList = "student_id")
})
public class IncidentSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false, updatable = false)
    private Instant subscribedAt = Instant.now();

    public IncidentSubscription() {}

    public IncidentSubscription(Long incidentId, Long studentId) {
        this.incidentId = incidentId;
        this.studentId = studentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Instant getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(Instant subscribedAt) { this.subscribedAt = subscribedAt; }
}
