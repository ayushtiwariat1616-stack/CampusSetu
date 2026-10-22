package edu.kiet.campussetu.complaint;

import edu.kiet.campussetu.audit.AuditService;
import edu.kiet.campussetu.notification.NotificationService;
import edu.kiet.campussetu.organization.ComplaintQueue;
import edu.kiet.campussetu.organization.ComplaintQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class SlaEscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaEscalationScheduler.class);

    private final ComplaintRepository complaintRepository;
    private final ComplaintQueueRepository queueRepository;
    private final ComplaintEventRepository eventRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public SlaEscalationScheduler(ComplaintRepository complaintRepository,
                                  ComplaintQueueRepository queueRepository,
                                  ComplaintEventRepository eventRepository,
                                  NotificationService notificationService,
                                  AuditService auditService) {
        this.complaintRepository = complaintRepository;
        this.queueRepository = queueRepository;
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Periodic background scanner runs every 60 seconds.
     * Evaluates active tickets against response and resolution SLA targets.
     */
    @Scheduled(fixedRateString = "${campussetu.sla.scan-rate-ms:60000}")
    @Transactional
    public void scanAndEscalateBreachedComplaints() {
        SlaCheckResult result = runSlaScan();
        if (result.escalatedCount() > 0) {
            log.info("SLA Auto-Escalation Engine: Scanned {} active complaints, escalated {} breached tickets.",
                    result.scannedCount(), result.escalatedCount());
        }
    }

    @Transactional
    public SlaCheckResult runSlaScan() {
        List<ComplaintStatus> activeStatuses = List.of(
                ComplaintStatus.SUBMITTED,
                ComplaintStatus.ACKNOWLEDGED,
                ComplaintStatus.IN_PROGRESS
        );

        List<Complaint> activeComplaints = complaintRepository.findByStatusIn(activeStatuses);
        int escalatedCount = 0;
        Instant now = Instant.now();

        for (Complaint complaint : activeComplaints) {
            String currentQueueCode = complaint.getCurrentQueueCode();
            if (currentQueueCode == null) continue;

            Optional<ComplaintQueue> queueOpt = queueRepository.findByQueueCode(currentQueueCode);
            if (queueOpt.isEmpty()) continue;

            ComplaintQueue currentQueue = queueOpt.get();
            String nextQueueCode = currentQueue.getEscalationQueueCode();
            if (nextQueueCode == null || nextQueueCode.isBlank()) {
                // Reached final hierarchy level (Central Directorate / Admin)
                continue;
            }

            Optional<ComplaintQueue> nextQueueOpt = queueRepository.findByQueueCode(nextQueueCode);
            if (nextQueueOpt.isEmpty()) continue;
            ComplaintQueue nextQueue = nextQueueOpt.get();

            long hoursSinceCreation = Duration.between(complaint.getCreatedAt(), now).toHours();
            boolean isBreached = false;
            String breachReason = "";

            // Response SLA: Unacknowledged after responseTargetHours
            if (complaint.getStatus() == ComplaintStatus.SUBMITTED && hoursSinceCreation >= currentQueue.getResponseTargetHours()) {
                isBreached = true;
                breachReason = "First response SLA breached (" + hoursSinceCreation + "h elapsed vs target " + currentQueue.getResponseTargetHours() + "h)";
            }
            // Resolution SLA: In progress past resolutionTargetHours
            else if ((complaint.getStatus() == ComplaintStatus.ACKNOWLEDGED || complaint.getStatus() == ComplaintStatus.IN_PROGRESS)
                    && hoursSinceCreation >= currentQueue.getResolutionTargetHours()) {
                isBreached = true;
                breachReason = "Resolution SLA breached (" + hoursSinceCreation + "h elapsed vs target " + currentQueue.getResolutionTargetHours() + "h)";
            }

            if (isBreached) {
                complaint.setCurrentQueueCode(nextQueue.getQueueCode());
                complaint.setAssignedOperatorEmail(null);
                complaintRepository.save(complaint);

                eventRepository.save(new ComplaintEvent(
                        complaint.getId(),
                        "ESCALATED",
                        "system.sla@kiet.edu",
                        "CampusSetu SLA Sentinel",
                        "SYSTEM_ENGINE",
                        currentQueueCode,
                        nextQueue.getQueueCode(),
                        "Automated SLA Escalation: " + breachReason + ". Reassigned to " + nextQueue.getQueueName() + "."
                ));

                notificationService.sendInApp(
                        complaint.getStudent().getId(),
                        "Complaint Auto-Escalated: " + complaint.getTrackingNumber(),
                        "Your ticket was escalated to " + nextQueue.getQueueName() + " due to response timeline policy.",
                        "ESCALATION",
                        complaint.getId()
                );

                auditService.record("SLA_ENGINE", "system.sla@kiet.edu", "AUTO_ESCALATED",
                        complaint.getTrackingNumber(), "From " + currentQueueCode + " to " + nextQueue.getQueueCode() + ". " + breachReason, "SYSTEM");

                escalatedCount++;
            }
        }

        return new SlaCheckResult(activeComplaints.size(), escalatedCount);
    }

    public record SlaCheckResult(int scannedCount, int escalatedCount) {}
}
