package edu.kiet.campussetu.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_google_sub", columnList = "googleSub")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 120)
    private String googleSub;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(length = 500)
    private String pictureUrl;

    @Column(length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole primaryRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OnboardingStatus onboardingStatus;

    @Column(length = 50)
    private String departmentCode;

    @Column(length = 50)
    private String hostelCode;

    @Column(length = 30)
    private String roomNumber;

    @Column(length = 50)
    private String studentRollNumber;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public User() {}

    public User(String email, String googleSub, String fullName, UserRole primaryRole, OnboardingStatus onboardingStatus) {
        this.email = email;
        this.googleSub = googleSub;
        this.fullName = fullName;
        this.primaryRole = primaryRole;
        this.onboardingStatus = onboardingStatus;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGoogleSub() { return googleSub; }
    public void setGoogleSub(String googleSub) { this.googleSub = googleSub; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }

    public UserRole getPrimaryRole() { return primaryRole; }
    public void setPrimaryRole(UserRole primaryRole) { this.primaryRole = primaryRole; }

    public OnboardingStatus getOnboardingStatus() { return onboardingStatus; }
    public void setOnboardingStatus(OnboardingStatus onboardingStatus) { this.onboardingStatus = onboardingStatus; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getHostelCode() { return hostelCode; }
    public void setHostelCode(String hostelCode) { this.hostelCode = hostelCode; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
