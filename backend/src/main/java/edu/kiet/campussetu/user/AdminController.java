package edu.kiet.campussetu.user;

import edu.kiet.campussetu.audit.AuditEvent;
import edu.kiet.campussetu.audit.AuditEventRepository;
import edu.kiet.campussetu.audit.AuditService;
import edu.kiet.campussetu.auth.AuthHelper;
import edu.kiet.campussetu.auth.OperatorKeyService;
import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.ApiResponse;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import edu.kiet.campussetu.complaint.SlaEscalationScheduler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RosterService rosterService;
    private final OperatorKeyService operatorKeyService;
    private final UserRepository userRepository;
    private final AuditEventRepository auditEventRepository;
    private final AuditService auditService;
    private final AuthHelper authHelper;
    private final SlaEscalationScheduler slaScheduler;

    public AdminController(RosterService rosterService,
                           OperatorKeyService operatorKeyService,
                           UserRepository userRepository,
                           AuditEventRepository auditEventRepository,
                           AuditService auditService,
                           AuthHelper authHelper,
                           SlaEscalationScheduler slaScheduler) {
        this.rosterService = rosterService;
        this.operatorKeyService = operatorKeyService;
        this.userRepository = userRepository;
        this.auditEventRepository = auditEventRepository;
        this.auditService = auditService;
        this.authHelper = authHelper;
        this.slaScheduler = slaScheduler;
    }

    /**
     * Preview CSV upload before committing.
     */
    @PostMapping("/roster/preview")
    public ResponseEntity<ApiResponse<RosterService.RosterImportPreview>> previewRoster(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        authHelper.requireSystemAdmin(request);
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        RosterService.RosterImportPreview preview = rosterService.previewCsv(content);
        return ResponseEntity.ok(ApiResponse.ok(preview));
    }

    /**
     * Apply CSV roster import.
     */
    @PostMapping("/roster/apply")
    public ResponseEntity<ApiResponse<RosterService.RosterImportResult>> applyRoster(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        SessionUser admin = authHelper.requireSystemAdmin(request);
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        RosterService.RosterImportResult result = rosterService.applyCsv(content, admin.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("Roster imported successfully.", result));
    }

    /**
     * Generate new individual Operator Key for an approved operator.
     * Returned ONCE to the administrator.
     */
    @PostMapping("/operators/{userId}/generate-key")
    public ResponseEntity<ApiResponse<OperatorKeyService.GeneratedKeyResult>> generateOperatorKey(
            @PathVariable Long userId,
            HttpServletRequest request) {

        SessionUser admin = authHelper.requireSystemAdmin(request);
        OperatorKeyService.GeneratedKeyResult keyResult = operatorKeyService.generateNewKey(userId, admin.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("New Operator Key generated. Save this now; it will not be shown again.", keyResult));
    }

    /**
     * View users pending onboarding approval.
     */
    @GetMapping("/pending-users")
    public ResponseEntity<ApiResponse<List<User>>> getPendingUsers(HttpServletRequest request) {
        authHelper.requireSystemAdmin(request);
        List<User> pending = userRepository.findAll().stream()
                .filter(u -> u.getOnboardingStatus() == OnboardingStatus.PENDING_ONBOARDING)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(pending));
    }

    /**
     * View all operators for key management.
     */
    @GetMapping("/operators")
    public ResponseEntity<ApiResponse<List<User>>> getOperators(HttpServletRequest request) {
        authHelper.requireSystemAdmin(request);
        List<User> operators = userRepository.findAll().stream()
                .filter(u -> u.getPrimaryRole() == UserRole.OPERATOR || u.getPrimaryRole() == UserRole.SYSTEM_ADMIN)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(operators));
    }

    /**
     * Approve a pending user account manually.
     */
    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<ApiResponse<User>> approveUser(
            @PathVariable Long userId,
            @RequestBody ApproveUserRequest req,
            HttpServletRequest request) {

        SessionUser admin = authHelper.requireSystemAdmin(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setOnboardingStatus(OnboardingStatus.APPROVED);
        if (req.role() != null) user.setPrimaryRole(req.role());
        if (req.departmentCode() != null) user.setDepartmentCode(req.departmentCode().toUpperCase());
        if (req.hostelCode() != null) user.setHostelCode(req.hostelCode().toUpperCase());
        if (req.roomNumber() != null) user.setRoomNumber(req.roomNumber());
        if (req.studentRollNumber() != null) user.setStudentRollNumber(req.studentRollNumber());

        User saved = userRepository.save(user);

        auditService.record("ROSTER_CHANGE", admin.getEmail(), "MANUAL_USER_APPROVAL",
                user.getEmail(), "Approved with role " + user.getPrimaryRole(), request.getRemoteAddr());

        return ResponseEntity.ok(ApiResponse.ok("User approved successfully.", saved));
    }

    /**
     * View regulatory audit log events.
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditEvent>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            HttpServletRequest request) {

        authHelper.requireSystemAdmin(request);
        Page<AuditEvent> logs = auditEventRepository.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    /**
     * Trigger on-demand SLA breach inspection and auto-escalation.
     */
    @PostMapping("/sla-check")
    public ResponseEntity<ApiResponse<SlaEscalationScheduler.SlaCheckResult>> triggerSlaCheck(
            HttpServletRequest request) {

        authHelper.requireSystemAdmin(request);
        SlaEscalationScheduler.SlaCheckResult result = slaScheduler.runSlaScan();
        return ResponseEntity.ok(ApiResponse.ok("SLA evaluation completed successfully.", result));
    }

    public record ApproveUserRequest(
            UserRole role,
            String departmentCode,
            String hostelCode,
            String roomNumber,
            String studentRollNumber
    ) {}
}
