package edu.kiet.campussetu.complaint;

import edu.kiet.campussetu.auth.AuthHelper;
import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.ApiResponse;
import edu.kiet.campussetu.reporting.ReportingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operator")
public class OperatorController {

    private final ComplaintService complaintService;
    private final ReportingService reportingService;
    private final AuthHelper authHelper;

    public OperatorController(ComplaintService complaintService,
                              ReportingService reportingService,
                              AuthHelper authHelper) {
        this.complaintService = complaintService;
        this.reportingService = reportingService;
        this.authHelper = authHelper;
    }

    /**
     * Get complaints in the operator's authorized queue(s).
     */
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<Page<Complaint>>> getOperatorQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        Page<Complaint> complaints = complaintService.getOperatorQueue(operator, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(complaints));
    }

    /**
     * Acknowledge complaint.
     */
    @PostMapping("/complaints/{id}/acknowledge")
    public ResponseEntity<ApiResponse<Complaint>> acknowledge(
            @PathVariable Long id,
            @RequestBody(required = false) ActionMessageRequest req,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        String msg = req != null ? req.message() : null;
        Complaint complaint = complaintService.acknowledgeComplaint(id, operator, msg);
        return ResponseEntity.ok(ApiResponse.ok("Complaint acknowledged.", complaint));
    }

    /**
     * Mark work in progress.
     */
    @PostMapping("/complaints/{id}/in-progress")
    public ResponseEntity<ApiResponse<Complaint>> markInProgress(
            @PathVariable Long id,
            @RequestBody(required = false) ActionMessageRequest req,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        String msg = req != null ? req.message() : null;
        Complaint complaint = complaintService.markInProgress(id, operator, msg);
        return ResponseEntity.ok(ApiResponse.ok("Status updated to In Progress.", complaint));
    }

    /**
     * Propose resolution with repair summary.
     */
    @PostMapping("/complaints/{id}/propose-resolution")
    public ResponseEntity<ApiResponse<Complaint>> proposeResolution(
            @PathVariable Long id,
            @RequestBody ProposeResolutionRequest req,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        Complaint complaint = complaintService.proposeResolution(id, operator, req.resolutionSummary());
        return ResponseEntity.ok(ApiResponse.ok("Resolution proposed. Awaiting student verification.", complaint));
    }

    /**
     * Escalate complaint to next authority in hierarchy.
     */
    @PostMapping("/complaints/{id}/escalate")
    public ResponseEntity<ApiResponse<Complaint>> escalate(
            @PathVariable Long id,
            @RequestBody EscalateRequest req,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        Complaint complaint = complaintService.escalateComplaint(id, operator, req.escalationReason());
        return ResponseEntity.ok(ApiResponse.ok("Complaint escalated successfully.", complaint));
    }

    /**
     * Record internal maintenance note (protected from students).
     */
    @PostMapping("/complaints/{id}/internal-note")
    public ResponseEntity<ApiResponse<ComplaintNote>> addInternalNote(
            @PathVariable Long id,
            @RequestBody InternalNoteRequest req,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        ComplaintNote note = complaintService.addNote(id, operator, req.content(), true);
        return ResponseEntity.ok(ApiResponse.ok("Internal note recorded.", note));
    }

    /**
     * Administrative override closure with mandatory justification.
     */
    @PostMapping("/complaints/{id}/admin-close")
    public ResponseEntity<ApiResponse<Complaint>> adminClose(
            @PathVariable Long id,
            @RequestBody AdminCloseRequest req,
            HttpServletRequest request) {

        SessionUser admin = authHelper.requireSystemAdmin(request);
        Complaint complaint = complaintService.administrativeClosure(id, admin, req.reason());
        return ResponseEntity.ok(ApiResponse.ok("Administrative override closure completed.", complaint));
    }

    /**
     * Get real database-backed operational metrics for this operator's domain.
     */
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<ReportingService.ScopedReportMetrics>> getReports(
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        ReportingService.ScopedReportMetrics metrics = reportingService.getMetrics(operator);
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }

    public record ActionMessageRequest(String message) {}
    public record ProposeResolutionRequest(String resolutionSummary) {}
    public record EscalateRequest(String escalationReason) {}
    public record InternalNoteRequest(String content) {}
    public record AdminCloseRequest(String reason) {}
}
