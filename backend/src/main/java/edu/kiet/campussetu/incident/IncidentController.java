package edu.kiet.campussetu.incident;

import edu.kiet.campussetu.auth.AuthHelper;
import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final SharedIncidentService incidentService;
    private final AuthHelper authHelper;

    public IncidentController(SharedIncidentService incidentService, AuthHelper authHelper) {
        this.incidentService = incidentService;
        this.authHelper = authHelper;
    }

    /**
     * Get active sanitized shared incidents (Public/Student/Operator).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SharedIncident>>> getActiveIncidents() {
        List<SharedIncident> incidents = incidentService.getActiveSharedIncidents();
        return ResponseEntity.ok(ApiResponse.ok(incidents));
    }

    /**
     * Check if current student is subscribed to this incident.
     */
    @GetMapping("/{id}/subscription-status")
    public ResponseEntity<ApiResponse<Boolean>> getSubscriptionStatus(
            @PathVariable Long id,
            HttpServletRequest request) {

        SessionUser user = authHelper.getSessionUser(request);
        if (user == null || !user.isStudent()) {
            return ResponseEntity.ok(ApiResponse.ok(false));
        }
        boolean subscribed = incidentService.isStudentSubscribed(id, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(subscribed));
    }

    /**
     * Toggle "I am affected too" subscription (Student only).
     */
    @PostMapping("/{id}/subscribe")
    public ResponseEntity<ApiResponse<Boolean>> toggleSubscription(
            @PathVariable Long id,
            HttpServletRequest request) {

        SessionUser student = authHelper.requireStudent(request);
        boolean subscribed = incidentService.toggleSubscription(id, student);
        String msg = subscribed ? "Subscribed to incident updates." : "Unsubscribed from incident.";
        return ResponseEntity.ok(ApiResponse.ok(msg, subscribed));
    }

    /**
     * Create shared incident (Operator only).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SharedIncident>> createIncident(
            @RequestBody SharedIncidentService.CreateSharedIncidentRequest req,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        SharedIncident incident = incidentService.createIncident(req, operator);
        return ResponseEntity.ok(ApiResponse.ok("Shared incident published.", incident));
    }

    /**
     * Update incident progress / status (Operator only).
     */
    @PostMapping("/{id}/update-progress")
    public ResponseEntity<ApiResponse<SharedIncident>> updateProgress(
            @PathVariable Long id,
            @RequestBody UpdateProgressRequest req,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        SharedIncident updated = incidentService.updateIncidentProgress(
                id, req.newStatus(), req.updateMessage(), operator);
        return ResponseEntity.ok(ApiResponse.ok("Incident progress updated.", updated));
    }

    /**
     * Link complaint to incident (Operator only).
     */
    @PostMapping("/{id}/link-complaint")
    public ResponseEntity<ApiResponse<Void>> linkComplaint(
            @PathVariable Long id,
            @RequestBody LinkComplaintRequest req,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        incidentService.linkComplaintToIncident(req.complaintId(), id, operator);
        return ResponseEntity.ok(ApiResponse.ok("Complaint linked to incident.", null));
    }

    /**
     * Unlink complaint from incident (Operator only).
     */
    @PostMapping("/{id}/unlink-complaint")
    public ResponseEntity<ApiResponse<Void>> unlinkComplaint(
            @PathVariable Long id,
            @RequestBody LinkComplaintRequest req,
            HttpServletRequest request) {

        SessionUser operator = authHelper.requireVerifiedOperator(request);
        incidentService.unlinkComplaint(req.complaintId(), operator);
        return ResponseEntity.ok(ApiResponse.ok("Complaint unlinked from incident.", null));
    }

    public record UpdateProgressRequest(SharedIncidentStatus newStatus, String updateMessage) {}
    public record LinkComplaintRequest(Long complaintId) {}
}
