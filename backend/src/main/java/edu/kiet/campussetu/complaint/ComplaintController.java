package edu.kiet.campussetu.complaint;

import edu.kiet.campussetu.auth.AuthHelper;
import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.ApiResponse;
import edu.kiet.campussetu.routing.RoutingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;
    private final RoutingService routingService;
    private final AuthHelper authHelper;

    public ComplaintController(ComplaintService complaintService,
                               ComplaintRepository complaintRepository,
                               RoutingService routingService,
                               AuthHelper authHelper) {
        this.complaintService = complaintService;
        this.complaintRepository = complaintRepository;
        this.routingService = routingService;
        this.authHelper = authHelper;
    }

    /**
     * Live routing preview before submission.
     */
    @GetMapping("/routing-preview")
    public ResponseEntity<ApiResponse<RoutingService.RoutingDecision>> getRoutingPreview(
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) ComplaintLocationType locationType,
            @RequestParam(required = false) String hostelCode,
            @RequestParam(required = false) String departmentCode) {

        RoutingService.RoutingDecision decision = routingService.resolveRoute(
                category, locationType, hostelCode, departmentCode);
        return ResponseEntity.ok(ApiResponse.ok(decision));
    }

    /**
     * Submit a new complaint (Student).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Complaint>> submitComplaint(
            @RequestBody ComplaintService.SubmitComplaintRequest req,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireStudent(request);
        Complaint saved = complaintService.submitComplaint(req, user);
        return ResponseEntity.ok(ApiResponse.ok("Complaint registered with ID " + saved.getTrackingNumber(), saved));
    }

    /**
     * View complaints submitted by the authenticated student.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<Complaint>>> getMyComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireStudent(request);
        Page<Complaint> complaints = complaintRepository.findByStudentIdOrderByCreatedAtDesc(
                user.getUserId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(complaints));
    }

    /**
     * Get complaint details with timeline, notes, and attachments.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintService.ComplaintDetailResponse>> getComplaintDetails(
            @PathVariable Long id,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireApprovedUser(request);
        ComplaintService.ComplaintDetailResponse response = complaintService.getComplaintDetails(id, user);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Student confirms the proposed resolution -> Closes ticket.
     */
    @PostMapping("/{id}/confirm-resolution")
    public ResponseEntity<ApiResponse<Complaint>> confirmResolution(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmResolutionRequest req,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireStudent(request);
        String feedback = req != null ? req.feedback() : null;
        Complaint complaint = complaintService.confirmResolutionByStudent(id, user, feedback);
        return ResponseEntity.ok(ApiResponse.ok("Resolution confirmed and complaint closed.", complaint));
    }

    /**
     * Student rejects the proposed resolution with reason -> Moves back to IN_PROGRESS.
     */
    @PostMapping("/{id}/reject-resolution")
    public ResponseEntity<ApiResponse<Complaint>> rejectResolution(
            @PathVariable Long id,
            @RequestBody RejectResolutionRequest req,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireStudent(request);
        Complaint complaint = complaintService.rejectResolutionByStudent(id, user, req.reason());
        return ResponseEntity.ok(ApiResponse.ok("Resolution rejected. Assigned team notified.", complaint));
    }

    /**
     * Student reopens closed complaint with documented reason.
     */
    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<Complaint>> reopenComplaint(
            @PathVariable Long id,
            @RequestBody ReopenRequest req,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireStudent(request);
        Complaint complaint = complaintService.reopenComplaintByStudent(id, user, req.reopenReason());
        return ResponseEntity.ok(ApiResponse.ok("Complaint reopened successfully.", complaint));
    }

    /**
     * Add public comment / reply.
     */
    @PostMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<ComplaintNote>> addPublicNote(
            @PathVariable Long id,
            @RequestBody AddNoteRequest req,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireApprovedUser(request);
        ComplaintNote note = complaintService.addNote(id, user, req.content(), false);
        return ResponseEntity.ok(ApiResponse.ok("Reply added successfully.", note));
    }

    public record ConfirmResolutionRequest(String feedback) {}
    public record RejectResolutionRequest(String reason) {}
    public record ReopenRequest(String reopenReason) {}
    public record AddNoteRequest(String content) {}
}
