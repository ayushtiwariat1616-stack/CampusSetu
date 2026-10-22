package edu.kiet.campussetu.incident;

import edu.kiet.campussetu.audit.AuditService;
import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.InvalidOperationException;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import edu.kiet.campussetu.complaint.Complaint;
import edu.kiet.campussetu.complaint.ComplaintCategory;
import edu.kiet.campussetu.complaint.ComplaintRepository;
import edu.kiet.campussetu.notification.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SharedIncidentService {

    private final SharedIncidentRepository incidentRepository;
    private final IncidentSubscriptionRepository subscriptionRepository;
    private final ComplaintRepository complaintRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public SharedIncidentService(SharedIncidentRepository incidentRepository,
                                 IncidentSubscriptionRepository subscriptionRepository,
                                 ComplaintRepository complaintRepository,
                                 NotificationService notificationService,
                                 AuditService auditService) {
        this.incidentRepository = incidentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.complaintRepository = complaintRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Create a new sanitized community shared incident (Operators only).
     */
    @Transactional
    public SharedIncident createIncident(CreateSharedIncidentRequest req, SessionUser operator) {
        if (!operator.isOperatorKeyVerified()) {
            throw new AccessDeniedException("Operator key verification is required.");
        }

        String incidentNumber = "INC-" + LocalDate.now().getYear() + "-" + String.format("%04d", System.currentTimeMillis() % 10000);

        SharedIncident incident = new SharedIncident();
        incident.setIncidentNumber(incidentNumber);
        incident.setTitle(req.title().trim());
        incident.setSanitizedDescription(req.sanitizedDescription().trim());
        incident.setCategory(req.category());
        incident.setGeneralLocation(req.generalLocation().trim());
        incident.setResponsibleQueueCode(req.responsibleQueueCode());
        incident.setStatus(SharedIncidentStatus.REPORTED);
        incident.setAffectedSubscriberCount(0);

        SharedIncident saved = incidentRepository.save(incident);

        auditService.record("INCIDENTS", operator.getEmail(), "SHARED_INCIDENT_CREATED", incidentNumber,
                "Created shared incident: " + req.title(), "OPERATOR");

        return saved;
    }

    /**
     * Student subscribes or marks "I am affected too". Exactly one subscription per student.
     */
    @Transactional
    public boolean toggleSubscription(Long incidentId, SessionUser student) {
        SharedIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        Optional<IncidentSubscription> existing = subscriptionRepository.findByIncidentIdAndStudentId(incidentId, student.getUserId());
        if (existing.isPresent()) {
            subscriptionRepository.delete(existing.get());
            int count = subscriptionRepository.countByIncidentId(incidentId);
            incident.setAffectedSubscriberCount(count);
            incidentRepository.save(incident);
            return false; // unsubscribed
        } else {
            subscriptionRepository.save(new IncidentSubscription(incidentId, student.getUserId()));
            int count = subscriptionRepository.countByIncidentId(incidentId);
            incident.setAffectedSubscriberCount(count);
            incidentRepository.save(incident);
            return true; // subscribed
        }
    }

    /**
     * Operator links a private complaint to a shared incident after manual review.
     */
    @Transactional
    public void linkComplaintToIncident(Long complaintId, Long incidentId, SessionUser operator) {
        if (!operator.isOperatorKeyVerified()) {
            throw new AccessDeniedException("Operator key verification required.");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        if (complaint.isSensitive()) {
            throw new InvalidOperationException("Sensitive complaints cannot be linked to shared community incidents.");
        }

        SharedIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        complaint.setSharedIncidentId(incident.getId());
        complaintRepository.save(complaint);

        // Auto-subscribe the complainant if not already subscribed
        if (!subscriptionRepository.existsByIncidentIdAndStudentId(incidentId, complaint.getStudent().getId())) {
            subscriptionRepository.save(new IncidentSubscription(incidentId, complaint.getStudent().getId()));
            incident.setAffectedSubscriberCount(subscriptionRepository.countByIncidentId(incidentId));
            incidentRepository.save(incident);
        }

        auditService.record("INCIDENTS", operator.getEmail(), "COMPLAINT_LINKED_TO_INCIDENT",
                complaint.getTrackingNumber(), "Linked to " + incident.getIncidentNumber(), "OPERATOR");
    }

    /**
     * Operator unlinks a complaint from an incident (correction).
     */
    @Transactional
    public void unlinkComplaint(Long complaintId, SessionUser operator) {
        if (!operator.isOperatorKeyVerified()) {
            throw new AccessDeniedException("Operator key verification required.");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        complaint.setSharedIncidentId(null);
        complaintRepository.save(complaint);

        auditService.record("INCIDENTS", operator.getEmail(), "COMPLAINT_UNLINKED",
                complaint.getTrackingNumber(), "Unlinked from shared incident", "OPERATOR");
    }

    /**
     * Operator publishes a public progress update on the incident.
     */
    @Transactional
    public SharedIncident updateIncidentProgress(Long incidentId, SharedIncidentStatus newStatus, String updateMessage, SessionUser operator) {
        if (!operator.isOperatorKeyVerified()) {
            throw new AccessDeniedException("Operator key verification required.");
        }

        SharedIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        incident.setStatus(newStatus);
        if (updateMessage != null && !updateMessage.trim().isEmpty()) {
            String timestamp = Instant.now().toString().substring(0, 16).replace("T", " ");
            String updateEntry = "[" + timestamp + " - " + operator.getFullName() + "]: " + updateMessage.trim();
            if (incident.getPublicProgressUpdates() != null && !incident.getPublicProgressUpdates().isEmpty()) {
                incident.setPublicProgressUpdates(updateEntry + "\n---\n" + incident.getPublicProgressUpdates());
            } else {
                incident.setPublicProgressUpdates(updateEntry);
            }
        }

        if (newStatus == SharedIncidentStatus.RESOLVED) {
            incident.setResolvedAt(Instant.now());
            // Notify linked individual complainants that the shared incident is resolved,
            // BUT remind them that individual complaint closure requires their verification!
            List<Complaint> linkedComplaints = complaintRepository.findBySharedIncidentId(incidentId);
            for (Complaint c : linkedComplaints) {
                notificationService.sendInApp(
                        c.getStudent().getId(),
                        "Shared Incident Resolved (" + incident.getIncidentNumber() + ")",
                        "The central shared issue '" + incident.getTitle() + "' has been resolved. Your private ticket " + c.getTrackingNumber() + " remains active until your individual inspection.",
                        "INCIDENT_UPDATE",
                        c.getId()
                );
            }
        }

        return incidentRepository.save(incident);
    }

    /**
     * Get active shared incidents for students and operators (sanitized view).
     */
    public List<SharedIncident> getActiveSharedIncidents() {
        return incidentRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public boolean isStudentSubscribed(Long incidentId, Long studentId) {
        return subscriptionRepository.existsByIncidentIdAndStudentId(incidentId, studentId);
    }

    public record CreateSharedIncidentRequest(
            String title,
            String sanitizedDescription,
            ComplaintCategory category,
            String generalLocation,
            String responsibleQueueCode
    ) {}
}
