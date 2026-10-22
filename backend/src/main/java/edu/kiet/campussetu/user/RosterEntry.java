package edu.kiet.campussetu.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "roster_entries", indexes = {
        @Index(name = "idx_roster_email", columnList = "email", unique = true)
})
public class RosterEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 50)
    private String roleType; // STUDENT, HOSTEL_WARDEN, RECTOR, CHIEF_RECTOR, DEPARTMENT_OPERATOR, SERVICE_OPERATOR, SYSTEM_ADMIN

    @Column(length = 50)
    private String departmentCode; // e.g. "AIML", "CSE", "IT", "ECE", "EN", "ME", "CE", "MCA"

    @Column(length = 50)
    private String hostelCode; // e.g. "ARYABHATT", "CV_RAMAN", "TAGORE", "SARASWATI"

    @Column(length = 30)
    private String roomNumber;

    @Column(length = 50)
    private String studentRollNumber;

    @Column(length = 60)
    private String serviceScope; // e.g. "IT_SERVICES", "CAMPUS_MAINTENANCE", "CAMPUS_WIDE"

    @Column(nullable = false)
    private boolean approved = true;

    @Column(nullable = false, updatable = false)
    private Instant importedAt = Instant.now();

    public RosterEntry() {}

    public RosterEntry(String email, String fullName, String roleType, String departmentCode, String hostelCode, String roomNumber, String studentRollNumber, String serviceScope) {
        this.email = email;
        this.fullName = fullName;
        this.roleType = roleType;
        this.departmentCode = departmentCode;
        this.hostelCode = hostelCode;
        this.roomNumber = roomNumber;
        this.studentRollNumber = studentRollNumber;
        this.serviceScope = serviceScope;
        this.approved = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getHostelCode() { return hostelCode; }
    public void setHostelCode(String hostelCode) { this.hostelCode = hostelCode; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }

    public String getServiceScope() { return serviceScope; }
    public void setServiceScope(String serviceScope) { this.serviceScope = serviceScope; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public Instant getImportedAt() { return importedAt; }
    public void setImportedAt(Instant importedAt) { this.importedAt = importedAt; }
}
