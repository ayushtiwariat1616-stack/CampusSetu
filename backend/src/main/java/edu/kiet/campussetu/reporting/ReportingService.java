package edu.kiet.campussetu.reporting;

import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.complaint.*;
import edu.kiet.campussetu.incident.SharedIncidentRepository;
import edu.kiet.campussetu.incident.SharedIncidentStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class ReportingService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintService complaintService;
    private final SharedIncidentRepository incidentRepository;

    public ReportingService(ComplaintRepository complaintRepository,
                            ComplaintService complaintService,
                            SharedIncidentRepository incidentRepository) {
        this.complaintRepository = complaintRepository;
        this.complaintService = complaintService;
        this.incidentRepository = incidentRepository;
    }

    /**
     * Compute real metrics calculated directly from database records scoped to operator's domain.
     * No fake statistics! If empty, numbers are legitimately 0.
     */
    public ScopedReportMetrics getMetrics(SessionUser operator) {
        List<String> queues = complaintService.getAuthorizedQueueCodesForOperator(operator);
        List<Complaint> allComplaints = complaintRepository.findAll();

        // Scope filter
        List<Complaint> scopedComplaints;
        if (operator.isSystemAdmin()) {
            scopedComplaints = allComplaints;
        } else {
            scopedComplaints = allComplaints.stream()
                    .filter(c -> queues.contains(c.getCurrentQueueCode()))
                    .toList();
        }

        long totalCount = scopedComplaints.size();
        long submittedCount = 0;
        long acknowledgedCount = 0;
        long inProgressCount = 0;
        long resolutionProposedCount = 0;
        long closedCount = 0;
        long reopenedCount = 0;

        List<Long> ackDurationsHours = new ArrayList<>();
        List<Long> resolutionDurationsHours = new ArrayList<>();

        for (Complaint c : scopedComplaints) {
            switch (c.getStatus()) {
                case SUBMITTED -> submittedCount++;
                case ACKNOWLEDGED -> acknowledgedCount++;
                case IN_PROGRESS -> inProgressCount++;
                case RESOLUTION_PROPOSED -> resolutionProposedCount++;
                case CLOSED -> closedCount++;
                case REOPENED -> reopenedCount++;
            }

            if (c.getAcknowledgedAt() != null && c.getCreatedAt() != null) {
                long hours = Duration.between(c.getCreatedAt(), c.getAcknowledgedAt()).toHours();
                ackDurationsHours.add(Math.max(0, hours));
            }
            if (c.getResolutionProposedAt() != null && c.getCreatedAt() != null) {
                long hours = Duration.between(c.getCreatedAt(), c.getResolutionProposedAt()).toHours();
                resolutionDurationsHours.add(Math.max(0, hours));
            }
        }

        double avgHoursToAck = ackDurationsHours.isEmpty() ? 0.0 :
                ackDurationsHours.stream().mapToLong(Long::longValue).average().orElse(0.0);

        double avgHoursToResolution = resolutionDurationsHours.isEmpty() ? 0.0 :
                resolutionDurationsHours.stream().mapToLong(Long::longValue).average().orElse(0.0);

        // Student-confirmed closure rate: (Closed / (Closed + Resolution Proposed)) * 100
        long resolvedOrClosed = closedCount + resolutionProposedCount;
        double studentConfirmedClosureRate = resolvedOrClosed == 0 ? 0.0 :
                Math.round(((double) closedCount / resolvedOrClosed) * 1000.0) / 10.0;

        // Active incidents count
        long activeIncidents = incidentRepository.findAll().stream()
                .filter(i -> i.getStatus() != SharedIncidentStatus.RESOLVED)
                .count();

        return new ScopedReportMetrics(
                totalCount,
                submittedCount,
                acknowledgedCount,
                inProgressCount,
                resolutionProposedCount,
                closedCount,
                reopenedCount,
                Math.round(avgHoursToAck * 10.0) / 10.0,
                Math.round(avgHoursToResolution * 10.0) / 10.0,
                studentConfirmedClosureRate,
                activeIncidents
        );
    }

    public record ScopedReportMetrics(
            long totalComplaints,
            long submitted,
            long acknowledged,
            long inProgress,
            long resolutionProposed,
            long closed,
            long reopened,
            double avgHoursToAck,
            double avgHoursToResolution,
            double studentConfirmedClosureRate,
            long activeSharedIncidents
    ) {}
}
