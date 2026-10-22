package edu.kiet.campussetu.auth;

import edu.kiet.campussetu.user.OnboardingStatus;
import edu.kiet.campussetu.user.UserRole;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SessionUser implements Serializable {

    private Long userId;
    private String email;
    private String fullName;
    private UserRole role;
    private OnboardingStatus onboardingStatus;
    private String departmentCode;
    private String hostelCode;
    private String roomNumber;
    private String studentRollNumber;
    private String gender; // MALE, FEMALE, OTHER
    private boolean operatorKeyVerified;
    private List<String> authorizedQueueCodes = new ArrayList<>();
    private List<String> authorizedScopeTargets = new ArrayList<>();
    private List<String> operatorRoles = new ArrayList<>();

    public SessionUser() {}

    public SessionUser(Long userId, String email, String fullName, UserRole role, OnboardingStatus onboardingStatus) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.onboardingStatus = onboardingStatus;
        this.operatorKeyVerified = (role == UserRole.STUDENT); // Students do not require operator keys
    }

    public boolean isStudent() {
        return role == UserRole.STUDENT;
    }

    public boolean isOperator() {
        return role == UserRole.OPERATOR || role == UserRole.SYSTEM_ADMIN;
    }

    public boolean isSystemAdmin() {
        return role == UserRole.SYSTEM_ADMIN;
    }

    public boolean isApproved() {
        return onboardingStatus == OnboardingStatus.APPROVED;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

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

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public boolean isFemaleStudent() {
        if (role != UserRole.STUDENT) return false;
        if ("FEMALE".equalsIgnoreCase(gender)) return true;
        if (hostelCode != null) {
            String hc = hostelCode.toUpperCase();
            return hc.equals("GARGI") || hc.equals("SAROJINI") || hc.equals("SARASWATI");
        }
        return false;
    }

    public boolean isOperatorKeyVerified() { return operatorKeyVerified; }
    public void setOperatorKeyVerified(boolean operatorKeyVerified) { this.operatorKeyVerified = operatorKeyVerified; }

    public List<String> getAuthorizedQueueCodes() { return authorizedQueueCodes; }
    public void setAuthorizedQueueCodes(List<String> authorizedQueueCodes) { this.authorizedQueueCodes = authorizedQueueCodes; }

    public List<String> getAuthorizedScopeTargets() { return authorizedScopeTargets; }
    public void setAuthorizedScopeTargets(List<String> authorizedScopeTargets) { this.authorizedScopeTargets = authorizedScopeTargets; }

    public List<String> getOperatorRoles() { return operatorRoles; }
    public void setOperatorRoles(List<String> operatorRoles) { this.operatorRoles = operatorRoles; }
}
