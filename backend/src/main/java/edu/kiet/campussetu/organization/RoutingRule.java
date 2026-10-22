package edu.kiet.campussetu.organization;

import jakarta.persistence.*;

@Entity
@Table(name = "routing_rules", indexes = {
        @Index(name = "idx_routing_category", columnList = "category"),
        @Index(name = "idx_routing_location_type", columnList = "locationType")
})
public class RoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String ruleName;

    @Column(nullable = false, length = 50)
    private String category; // HOSTEL_FACILITY, LAB_EQUIPMENT, CLASSROOM_INFRA, IT_AND_NETWORK, ELECTRICAL_PLUMBING, CLEANLINESS, MESS_FOOD, OTHER

    @Column(nullable = false, length = 40)
    private String locationType; // HOSTEL, DEPARTMENT, CAMPUS_FACILITY

    @Column(length = 60)
    private String scopeFilter; // e.g. "ARYABHATT", "AIML", "ALL_HOSTELS", "ALL_DEPTS", "CAMPUS"

    @Column(nullable = false, length = 80)
    private String targetQueueCode;

    @Column(nullable = false)
    private int priorityOrder = 10;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 250)
    private String description;

    public RoutingRule() {}

    public RoutingRule(String ruleName, String category, String locationType, String scopeFilter, String targetQueueCode, int priorityOrder, String description) {
        this.ruleName = ruleName;
        this.category = category;
        this.locationType = locationType;
        this.scopeFilter = scopeFilter;
        this.targetQueueCode = targetQueueCode;
        this.priorityOrder = priorityOrder;
        this.description = description;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }

    public String getScopeFilter() { return scopeFilter; }
    public void setScopeFilter(String scopeFilter) { this.scopeFilter = scopeFilter; }

    public String getTargetQueueCode() { return targetQueueCode; }
    public void setTargetQueueCode(String targetQueueCode) { this.targetQueueCode = targetQueueCode; }

    public int getPriorityOrder() { return priorityOrder; }
    public void setPriorityOrder(int priorityOrder) { this.priorityOrder = priorityOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
