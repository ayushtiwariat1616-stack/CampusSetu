package edu.kiet.campussetu.organization;

import jakarta.persistence.*;

@Entity
@Table(name = "complaint_queues")
public class ComplaintQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String queueCode;

    @Column(nullable = false, length = 150)
    private String queueName;

    @Column(nullable = false, length = 50)
    private String queueType; // HOSTEL_WARDEN, RECTOR, CHIEF_RECTOR, DEPARTMENT, SERVICE, CENTRAL_ADMIN

    @Column(nullable = false, length = 60)
    private String targetCode; // e.g. "ARYABHATT", "AIML", "IT_SERVICES", "CAMPUS_WIDE"

    @Column(length = 80)
    private String escalationQueueCode; // Points to next queue in chain

    @Column(nullable = false)
    private int responseTargetHours = 24;

    @Column(nullable = false)
    private int resolutionTargetHours = 72;

    @Column(nullable = false)
    private boolean active = true;

    public ComplaintQueue() {}

    public ComplaintQueue(String queueCode, String queueName, String queueType, String targetCode, String escalationQueueCode, int responseTargetHours, int resolutionTargetHours) {
        this.queueCode = queueCode;
        this.queueName = queueName;
        this.queueType = queueType;
        this.targetCode = targetCode;
        this.escalationQueueCode = escalationQueueCode;
        this.responseTargetHours = responseTargetHours;
        this.resolutionTargetHours = resolutionTargetHours;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQueueCode() { return queueCode; }
    public void setQueueCode(String queueCode) { this.queueCode = queueCode; }

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getQueueType() { return queueType; }
    public void setQueueType(String queueType) { this.queueType = queueType; }

    public String getTargetCode() { return targetCode; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }

    public String getEscalationQueueCode() { return escalationQueueCode; }
    public void setEscalationQueueCode(String escalationQueueCode) { this.escalationQueueCode = escalationQueueCode; }

    public int getResponseTargetHours() { return responseTargetHours; }
    public void setResponseTargetHours(int responseTargetHours) { this.responseTargetHours = responseTargetHours; }

    public int getResolutionTargetHours() { return resolutionTargetHours; }
    public void setResolutionTargetHours(int resolutionTargetHours) { this.resolutionTargetHours = resolutionTargetHours; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
