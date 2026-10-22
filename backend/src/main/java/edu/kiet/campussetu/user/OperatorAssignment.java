package edu.kiet.campussetu.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "operator_assignments", indexes = {
        @Index(name = "idx_op_assign_user", columnList = "user_id"),
        @Index(name = "idx_op_assign_target", columnList = "scopeTarget")
})
public class OperatorAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String operatorRole; // HOSTEL_WARDEN, RECTOR, CHIEF_RECTOR, DEPARTMENT_OPERATOR, SERVICE_OPERATOR, SYSTEM_ADMIN

    @Column(nullable = false, length = 30)
    private String scopeType; // HOSTEL, DEPARTMENT, SERVICE, CAMPUS

    @Column(nullable = false, length = 60)
    private String scopeTarget; // e.g. "ARYABHATT", "AIML", "IT_SERVICES", "CAMPUS_WIDE"

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    public OperatorAssignment() {}

    public OperatorAssignment(User user, String operatorRole, String scopeType, String scopeTarget) {
        this.user = user;
        this.operatorRole = operatorRole;
        this.scopeType = scopeType;
        this.scopeTarget = scopeTarget;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getOperatorRole() { return operatorRole; }
    public void setOperatorRole(String operatorRole) { this.operatorRole = operatorRole; }

    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }

    public String getScopeTarget() { return scopeTarget; }
    public void setScopeTarget(String scopeTarget) { this.scopeTarget = scopeTarget; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
}
