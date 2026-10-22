package edu.kiet.campussetu.complaint;

import edu.kiet.campussetu.audit.AuditService;
import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.InvalidOperationException;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import edu.kiet.campussetu.notification.NotificationService;
import edu.kiet.campussetu.organization.ComplaintQueue;
import edu.kiet.campussetu.organization.ComplaintQueueRepository;
import edu.kiet.campussetu.routing.RoutingService;
import edu.kiet.campussetu.user.OperatorAssignment;
import edu.kiet.campussetu.user.OperatorAssignmentRepository;
import edu.kiet.campussetu.user.User;
import edu.kiet.campussetu.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintEventRepository eventRepository;
    private final ComplaintNoteRepository noteRepository;
    private final AttachmentRepository attachmentRepository;
    private final ComplaintQueueRepository queueRepository;
    private final UserRepository userRepository;
    private final OperatorAssignmentRepository assignmentRepository;
    private final RoutingService routingService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            ComplaintEventRepository eventRepository,
                            ComplaintNoteRepository noteRepository,
                            AttachmentRepository attachmentRepository,
                            ComplaintQueueRepository queueRepository,
                            UserRepository userRepository,
                            OperatorAssignmentRepository assignmentRepository,
                            RoutingService routingService,
                            NotificationService notificationService,
                            AuditService auditService) {
        this.complaintRepository = complaintRepository;
        this.eventRepository = eventRepository;
        this.noteRepository = noteRepository;
        this.attachmentRepository = attachmentRepository;
        this.queueRepository = queueRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.routingService = routingService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Submit a new student complaint.
     */
    @Transactional
    public Complaint submitComplaint(SubmitComplaintRequest req, SessionUser sessionUser) {
        if (!sessionUser.isApproved()) {
            throw new AccessDeniedException("Account is pending institutional verification. Submissions are temporarily blocked.");
        }

        User student = userRepository.findById(sessionUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found"));

        RoutingService.RoutingDecision decision = routingService.resolveRoute(
                req.category(), req.locationType(), req.specificHostelCode(), req.specificDepartmentCode());

        if (!decision.valid()) {
            throw new InvalidOperationException("Unroutable complaint: " + decision.errorMessage());
        }

        // Generate unique tracking number e.g. CMP-2026-X
        String trackingNumber = "CMP-" + LocalDate.now().getYear() + "-" + String.format("%05d", System.currentTimeMillis() % 100000);

        Complaint complaint = new Complaint();
        complaint.setTrackingNumber(trackingNumber);
        complaint.setStudent(student);
        complaint.setTitle(req.title().trim());
        complaint.setDescription(req.description().trim());
        complaint.setCategory(req.category());
        complaint.setLocationType(req.locationType() != null ? req.locationType() : ComplaintLocationType.CAMPUS_FACILITY);
        complaint.setLocationDetail(req.locationDetail().trim());
        complaint.setSpecificHostelCode(req.specificHostelCode());
        complaint.setSpecificDepartmentCode(req.specificDepartmentCode());

        if (req.category() == ComplaintCategory.WOMEN_SAFETY_SOS) {
            complaint.setUrgency(ComplaintUrgency.CRITICAL);
            complaint.setSensitive(true);
        } else {
            complaint.setUrgency(req.urgency() != null ? req.urgency() : ComplaintUrgency.MEDIUM);
            complaint.setSensitive(req.sensitive());
        }

        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setCurrentQueueCode(decision.queueCode());

        Complaint saved = complaintRepository.save(complaint);

        // Record creation event
        ComplaintEvent event = new ComplaintEvent(
                saved.getId(),
                "SUBMITTED",
                student.getEmail(),
                student.getFullName(),
                "STUDENT",
                null,
                decision.queueCode(),
                "Complaint filed. Dispatched to " + decision.receivingOffice() + " (Rule: " + decision.ruleApplied() + ")"
        );
        eventRepository.save(event);

        // Notify student of submission
        notificationService.sendInApp(
                student.getId(),
                "Complaint Submitted: " + trackingNumber,
                "Your issue has been routed to " + decision.receivingOffice() + ". Track status in your portal.",
                "COMPLAINT_UPDATE",
                saved.getId()
        );

        auditService.record("COMPLAINTS", student.getEmail(), "COMPLAINT_SUBMITTED", trackingNumber,
                "Routed to " + decision.queueCode(), "CLIENT");

        return saved;
    }

    /**
     * Operator Acknowledges a complaint.
     */
    @Transactional
    public Complaint acknowledgeComplaint(Long complaintId, SessionUser operator, String message) {
        Complaint complaint = getAuthorizedComplaintForOperatorAction(complaintId, operator);

        if (complaint.getStatus() != ComplaintStatus.SUBMITTED) {
            throw new InvalidOperationException("Only complaints in SUBMITTED status can be acknowledged.");
        }

        complaint.setStatus(ComplaintStatus.ACKNOWLEDGED);
        complaint.setAcknowledgedAt(Instant.now());
        complaint.setAssignedOperatorEmail(operator.getEmail());
        complaintRepository.save(complaint);

        String noteText = message != null && !message.trim().isEmpty() ? message.trim() : "Complaint acknowledged by " + operator.getFullName();
        eventRepository.save(new ComplaintEvent(
                complaint.getId(),
                "ACKNOWLEDGED",
                operator.getEmail(),
                operator.getFullName(),
                "OPERATOR",
                complaint.getCurrentQueueCode(),
                complaint.getCurrentQueueCode(),
                noteText
        ));

        notificationService.sendInApp(
                complaint.getStudent().getId(),
                "Complaint Acknowledged (" + complaint.getTrackingNumber() + ")",
                "Your complaint has been acknowledged by " + operator.getFullName() + ".",
                "COMPLAINT_UPDATE",
                complaint.getId()
        );

        return complaint;
    }

    /**
     * Operator updates status to IN_PROGRESS.
     */
    @Transactional
    public Complaint markInProgress(Long complaintId, SessionUser operator, String workDetails) {
        Complaint complaint = getAuthorizedComplaintForOperatorAction(complaintId, operator);

        if (complaint.getStatus() != ComplaintStatus.ACKNOWLEDGED) {
            throw new InvalidOperationException("Complaint must be acknowledged before moving to IN_PROGRESS.");
        }

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaintRepository.save(complaint);

        eventRepository.save(new ComplaintEvent(
                complaint.getId(),
                "STATUS_CHANGE",
                operator.getEmail(),
                operator.getFullName(),
                "OPERATOR",
                complaint.getCurrentQueueCode(),
                complaint.getCurrentQueueCode(),
                "Work initiated: " + (workDetails != null ? workDetails : "Under active maintenance")
        ));

        notificationService.sendInApp(
                complaint.getStudent().getId(),
                "Maintenance In Progress (" + complaint.getTrackingNumber() + ")",
                "Work is actively underway on your reported issue.",
                "COMPLAINT_UPDATE",
                complaint.getId()
        );

        return complaint;
    }

    /**
     * Operator proposes a resolution (Does NOT close the ticket - student confirmation required!)
     */
    @Transactional
    public Complaint proposeResolution(Long complaintId, SessionUser operator, String resolutionSummary) {
        Complaint complaint = getAuthorizedComplaintForOperatorAction(complaintId, operator);

        if (complaint.getStatus() == ComplaintStatus.CLOSED) {
            throw new InvalidOperationException("Complaint is already closed.");
        }
        if (resolutionSummary == null || resolutionSummary.trim().isEmpty()) {
            throw new InvalidOperationException("A resolution summary describing the fix is required.");
        }

        complaint.setStatus(ComplaintStatus.RESOLUTION_PROPOSED);
        complaint.setResolutionProposedAt(Instant.now());
        complaint.setResolutionSummary(resolutionSummary.trim());
        complaintRepository.save(complaint);

        eventRepository.save(new ComplaintEvent(
                complaint.getId(),
                "RESOLUTION_PROPOSED",
                operator.getEmail(),
                operator.getFullName(),
                "OPERATOR",
                complaint.getCurrentQueueCode(),
                complaint.getCurrentQueueCode(),
                "Resolution proposed: " + resolutionSummary.trim()
        ));

        notificationService.sendInApp(
                complaint.getStudent().getId(),
                "Resolution Proposed: " + complaint.getTrackingNumber(),
                "The maintenance team reports this issue is resolved: \"" + resolutionSummary.trim() + "\". Please verify and confirm or reject.",
                "RESOLUTION",
                complaint.getId()
        );

        return complaint;
    }

    /**
     * Student confirms the resolution -> Closes the complaint.
     */
    @Transactional
    public Complaint confirmResolutionByStudent(Long complaintId, SessionUser student, String feedback) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        if (!complaint.getStudent().getId().equals(student.getUserId())) {
            throw new AccessDeniedException("Only the student who submitted this complaint can confirm its resolution.");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLUTION_PROPOSED) {
            throw new InvalidOperationException("Resolution can only be confirmed when status is RESOLUTION_PROPOSED.");
        }

        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setClosedAt(Instant.now());
        complaint.setClosureNotes(feedback != null ? feedback.trim() : "Student confirmed satisfactory repair.");
        complaintRepository.save(complaint);

        eventRepository.save(new ComplaintEvent(
                complaint.getId(),
                "RESOLUTION_CONFIRMED",
                student.getEmail(),
                student.getFullName(),
                "STUDENT",
                complaint.getCurrentQueueCode(),
                complaint.getCurrentQueueCode(),
                "Repair verified and closed by student: " + complaint.getClosureNotes()
        ));

        auditService.record("COMPLAINTS", student.getEmail(), "STUDENT_CONFIRMED_CLOSURE",
                complaint.getTrackingNumber(), "Student verified repair", "CLIENT");

        return complaint;
    }

    /**
     * Student rejects proposed resolution with reason -> Moves back to IN_PROGRESS.
     */
    @Transactional
    public Complaint rejectResolutionByStudent(Long complaintId, SessionUser student, String reason) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        if (!complaint.getStudent().getId().equals(student.getUserId())) {
            throw new AccessDeniedException("Only the student who submitted this complaint can reject its proposed resolution.");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLUTION_PROPOSED) {
            throw new InvalidOperationException("Can only reject when status is RESOLUTION_PROPOSED.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidOperationException("Please provide a reason explaining why the resolution was unsatisfactory.");
        }

        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setRejectionReason(reason.trim());
        complaintRepository.save(complaint);

        eventRepository.save(new ComplaintEvent(
                complaint.getId(),
                "RESOLUTION_REJECTED",
                student.getEmail(),
                student.getFullName(),
                "STUDENT",
                complaint.getCurrentQueueCode(),
                complaint.getCurrentQueueCode(),
                "Student rejected proposed resolution: " + reason.trim()
        ));

        auditService.record("COMPLAINTS", student.getEmail(), "STUDENT_REJECTED_RESOLUTION",
                complaint.getTrackingNumber(), reason.trim(), "CLIENT");

        return complaint;
    }

    /**
     * Student reopens a closed complaint within documented policy window.
     */
    @Transactional
    public Complaint reopenComplaintByStudent(Long complaintId, SessionUser student, String reopenReason) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        if (!complaint.getStudent().getId().equals(student.getUserId())) {
            throw new AccessDeniedException("Only the student who submitted this complaint can request reopening.");
        }

        if (complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new InvalidOperationException("Only CLOSED complaints can be reopened.");
        }

        if (reopenReason == null || reopenReason.trim().isEmpty()) {
            throw new InvalidOperationException("A documented reason is required to reopen a closed complaint.");
        }

        complaint.setStatus(ComplaintStatus.REOPENED);
        complaintRepository.save(complaint);

        eventRepository.save(new ComplaintEvent(
                complaint.getId(),
                "REOPENED",
                student.getEmail(),
                student.getFullName(),
                "STUDENT",
                complaint.getCurrentQueueCode(),
                complaint.getCurrentQueueCode(),
                "Reopened by student: " + reopenReason.trim()
        ));

        auditService.record("COMPLAINTS", student.getEmail(), "COMPLAINT_REOPENED",
                complaint.getTrackingNumber(), reopenReason.trim(), "CLIENT");

        return complaint;
    }

    /**
     * Escalate / Forward complaint to the next authority in the configured hierarchy.
     */
    @Transactional
    public Complaint escalateComplaint(Long complaintId, SessionUser operator, String escalationReason) {
        Complaint complaint = getAuthorizedComplaintForOperatorAction(complaintId, operator);

        if (escalationReason == null || escalationReason.trim().isEmpty()) {
            throw new InvalidOperationException("An escalation reason must be provided.");
        }

        ComplaintQueue nextQueue = routingService.resolveNextEscalation(complaint.getCurrentQueueCode());
        String previousQueueCode = complaint.getCurrentQueueCode();

        complaint.setCurrentQueueCode(nextQueue.getQueueCode());
        complaint.setAssignedOperatorEmail(null); // Reset assignment for the new queue team
        complaintRepository.save(complaint);

        eventRepository.save(new ComplaintEvent(
                complaint.getId(),
                "ESCALATED",
                operator.getEmail(),
                operator.getFullName(),
                "OPERATOR",
                previousQueueCode,
                nextQueue.getQueueCode(),
                "Escalated from " + previousQueueCode + " to " + nextQueue.getQueueName() + ": " + escalationReason.trim()
        ));

        notificationService.sendInApp(
                complaint.getStudent().getId(),
                "Complaint Escalated: " + complaint.getTrackingNumber(),
                "Your issue has been escalated to " + nextQueue.getQueueName() + ".",
                "ESCALATION",
                complaint.getId()
        );

        auditService.record("ESCALATION", operator.getEmail(), "COMPLAINT_ESCALATED",
                complaint.getTrackingNumber(), "From " + previousQueueCode + " to " + nextQueue.getQueueCode() + ". Reason: " + escalationReason.trim(), "INTERNAL");

        return complaint;
    }

    /**
     * Administrative exceptional closure with reason and audit history.
     */
    @Transactional
    public Complaint administrativeClosure(Long complaintId, SessionUser admin, String reason) {
        if (!admin.isSystemAdmin()) {
            throw new AccessDeniedException("Administrative closure is restricted to authorized System Administrators.");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidOperationException("An administrative justification is mandatory for override closure.");
        }

        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setClosedAt(Instant.now());
        complaint.setClosureNotes("[ADMINISTRATIVE CLOSURE]: " + reason.trim());
        complaintRepository.save(complaint);

        eventRepository.save(new ComplaintEvent(
                complaint.getId(),
                "STATUS_CHANGE",
                admin.getEmail(),
                admin.getFullName(),
                "SYSTEM_ADMIN",
                complaint.getCurrentQueueCode(),
                complaint.getCurrentQueueCode(),
                "Administrative override closure: " + reason.trim()
        ));

        auditService.record("EXCEPTIONAL_ACCESS", admin.getEmail(), "ADMIN_OVERRIDE_CLOSURE",
                complaint.getTrackingNumber(), reason.trim(), "INTERNAL");

        return complaint;
    }

    /**
     * Add note / comment: operator internal note or student/operator public reply.
     */
    @Transactional
    public ComplaintNote addNote(Long complaintId, SessionUser author, String content, boolean internalOnly) {
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidOperationException("Note content cannot be empty.");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        User authorUser = userRepository.findById(author.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Students can only post public replies, never internal notes
        if (author.isStudent()) {
            if (!complaint.getStudent().getId().equals(author.getUserId())) {
                throw new AccessDeniedException("Students can only comment on their own complaints.");
            }
            internalOnly = false;
        }

        // Operators can only post internal notes if authorized
        if (internalOnly && !author.isOperator()) {
            throw new AccessDeniedException("Only authorized operators can record internal maintenance notes.");
        }

        ComplaintNote note = new ComplaintNote(complaintId, authorUser, content.trim(), internalOnly);
        ComplaintNote saved = noteRepository.save(note);

        eventRepository.save(new ComplaintEvent(
                complaint.getId(),
                internalOnly ? "NOTE_ADDED" : "REPLY_ADDED",
                author.getEmail(),
                author.getFullName(),
                author.getRole().name(),
                complaint.getCurrentQueueCode(),
                complaint.getCurrentQueueCode(),
                internalOnly ? "[INTERNAL NOTE RECORDED]" : content.trim()
        ));

        return saved;
    }

    /**
     * Retrieve complaint details ensuring strict privacy and student/operator permissions.
     */
    @Transactional(readOnly = true)
    public ComplaintDetailResponse getComplaintDetails(Long complaintId, SessionUser user) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        boolean isStudentOwner = user.isStudent() && complaint.getStudent().getId().equals(user.getUserId());
        boolean isAuthorizedOperator = isOperatorAuthorizedForQueue(complaint.getCurrentQueueCode(), user);

        if (!isStudentOwner && !isAuthorizedOperator && !user.isSystemAdmin()) {
            throw new AccessDeniedException("You do not have permission to view this complaint.");
        }

        List<ComplaintEvent> events = eventRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId);
        List<Attachment> attachments = attachmentRepository.findByComplaintId(complaintId);

        // Filter notes: students NEVER see internalOnly notes!
        List<ComplaintNote> notes;
        if (user.isStudent()) {
            notes = noteRepository.findByComplaintIdAndInternalOnlyFalseOrderByCreatedAtAsc(complaintId);
        } else {
            notes = noteRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId);
        }

        return new ComplaintDetailResponse(complaint, events, notes, attachments, isAuthorizedOperator);
    }

    /**
     * Get operator inbox filtered by assigned scopes.
     */
    @Transactional(readOnly = true)
    public Page<Complaint> getOperatorQueue(SessionUser operator, Pageable pageable) {
        if (!operator.isOperatorKeyVerified()) {
            throw new AccessDeniedException("Operator key verification is required to access the operator queue.");
        }

        List<String> queueCodes = getAuthorizedQueueCodesForOperator(operator);
        if (queueCodes.isEmpty()) {
            return Page.empty(pageable);
        }

        return complaintRepository.findByCurrentQueueCodeInOrderByCreatedAtDesc(queueCodes, pageable);
    }

    public List<String> getAuthorizedQueueCodesForOperator(SessionUser operator) {
        if (operator.isSystemAdmin()) {
            return queueRepository.findAll().stream().map(ComplaintQueue::getQueueCode).toList();
        }

        List<OperatorAssignment> assignments = assignmentRepository.findByUserIdAndActiveTrue(operator.getUserId());
        Set<String> queueCodes = new HashSet<>();

        for (OperatorAssignment a : assignments) {
            String target = a.getScopeTarget();
            if ("ALL_HOSTELS".equalsIgnoreCase(target)) {
                queueCodes.addAll(queueRepository.findByQueueType("HOSTEL_WARDEN").stream().map(ComplaintQueue::getQueueCode).toList());
                queueCodes.addAll(queueRepository.findByQueueType("RECTOR").stream().map(ComplaintQueue::getQueueCode).toList());
                queueCodes.addAll(queueRepository.findByQueueType("CHIEF_RECTOR").stream().map(ComplaintQueue::getQueueCode).toList());
            } else if ("HOSTEL".equalsIgnoreCase(a.getScopeType())) {
                if ("HOSTEL_WARDEN".equalsIgnoreCase(a.getOperatorRole())) {
                    queueCodes.add("QUEUE_HOSTEL_" + target.toUpperCase() + "_WARDEN");
                } else if ("RECTOR".equalsIgnoreCase(a.getOperatorRole())) {
                    queueCodes.add("QUEUE_HOSTEL_" + target.toUpperCase() + "_RECTOR");
                }
            } else if ("DEPARTMENT".equalsIgnoreCase(a.getScopeType())) {
                queueCodes.add("QUEUE_DEPT_" + target.toUpperCase());
            } else if ("SERVICE".equalsIgnoreCase(a.getScopeType())) {
                queueCodes.add("QUEUE_SERVICE_" + target.toUpperCase().replace("_SERVICES", "").replace("CAMPUS_", ""));
                queueCodes.add("QUEUE_SERVICE_" + target.toUpperCase());
            } else if ("DEAN_SW".equalsIgnoreCase(a.getOperatorRole()) || "DEAN_SW".equalsIgnoreCase(a.getScopeType()) || "CAMPUS_WIDE".equalsIgnoreCase(target)) {
                queueCodes.add("QUEUE_DEAN_STUDENT_WELFARE");
            }
        }
        return new ArrayList<>(queueCodes);
    }

    private boolean isOperatorAuthorizedForQueue(String queueCode, SessionUser operator) {
        if (operator.isSystemAdmin()) return true;
        List<String> authorized = getAuthorizedQueueCodesForOperator(operator);
        return authorized.contains(queueCode);
    }

    private Complaint getAuthorizedComplaintForOperatorAction(Long complaintId, SessionUser operator) {
        if (!operator.isOperatorKeyVerified()) {
            throw new AccessDeniedException("Operator key verification is required for action.");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        if (!isOperatorAuthorizedForQueue(complaint.getCurrentQueueCode(), operator)) {
            throw new AccessDeniedException("This complaint is currently assigned to queue '" +
                    complaint.getCurrentQueueCode() + "', which is outside your active operational scope.");
        }
        return complaint;
    }

    public record SubmitComplaintRequest(
            String title,
            String description,
            ComplaintCategory category,
            ComplaintLocationType locationType,
            String locationDetail,
            String specificHostelCode,
            String specificDepartmentCode,
            ComplaintUrgency urgency,
            boolean sensitive
    ) {}

    public record ComplaintDetailResponse(
            Complaint complaint,
            List<ComplaintEvent> events,
            List<ComplaintNote> notes,
            List<Attachment> attachments,
            boolean canAct
    ) {}
}
