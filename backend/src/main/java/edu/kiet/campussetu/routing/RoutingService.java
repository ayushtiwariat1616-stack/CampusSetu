package edu.kiet.campussetu.routing;

import edu.kiet.campussetu.common.InvalidOperationException;
import edu.kiet.campussetu.complaint.ComplaintCategory;
import edu.kiet.campussetu.complaint.ComplaintLocationType;
import edu.kiet.campussetu.organization.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoutingService {

    private final ComplaintQueueRepository queueRepository;
    private final HostelRepository hostelRepository;
    private final DepartmentRepository departmentRepository;
    private final CampusServiceRepository serviceRepository;
    private final RoutingRuleRepository routingRuleRepository;

    public RoutingService(ComplaintQueueRepository queueRepository,
                          HostelRepository hostelRepository,
                          DepartmentRepository departmentRepository,
                          CampusServiceRepository serviceRepository,
                          RoutingRuleRepository routingRuleRepository) {
        this.queueRepository = queueRepository;
        this.hostelRepository = hostelRepository;
        this.departmentRepository = departmentRepository;
        this.serviceRepository = serviceRepository;
        this.routingRuleRepository = routingRuleRepository;
    }

    /**
     * Deterministically predicts or assigns the initial queue for a complaint.
     * Shows the receiving office to the student before submission.
     */
    public RoutingDecision resolveRoute(ComplaintCategory category,
                                        ComplaintLocationType locationType,
                                        String specificHostelCode,
                                        String specificDepartmentCode) {

        if (locationType == null) {
            return RoutingDecision.unroutable("Location type must be specified.");
        }

        // Scenario A: Hostel issue (e.g. Aryabhatt Hostel fan failure)
        if (locationType == ComplaintLocationType.HOSTEL) {
            if (specificHostelCode == null || specificHostelCode.trim().isEmpty()) {
                return RoutingDecision.unroutable("Please select the specific hostel where the issue is located.");
            }
            String hostelCode = specificHostelCode.trim().toUpperCase();
            Optional<Hostel> hostel = hostelRepository.findById(hostelCode);
            if (hostel.isEmpty() || !hostel.get().isActive()) {
                return RoutingDecision.unroutable("Hostel '" + hostelCode + "' is not recognized or currently inactive.");
            }

            String wardenQueueCode = "QUEUE_HOSTEL_" + hostelCode + "_WARDEN";
            Optional<ComplaintQueue> queue = queueRepository.findByQueueCode(wardenQueueCode);
            if (queue.isPresent() && queue.get().isActive()) {
                return new RoutingDecision(
                        queue.get().getQueueCode(),
                        queue.get().getQueueName(),
                        "Hostel Warden Office - " + hostel.get().getName(),
                        "HOSTEL_WARDEN_DISPATCH",
                        true,
                        null
                );
            }
            // Scoped fallback for hostels: Rector queue
            String rectorQueueCode = "QUEUE_HOSTEL_" + hostelCode + "_RECTOR";
            Optional<ComplaintQueue> rectorQueue = queueRepository.findByQueueCode(rectorQueueCode);
            if (rectorQueue.isPresent() && rectorQueue.get().isActive()) {
                return new RoutingDecision(
                        rectorQueue.get().getQueueCode(),
                        rectorQueue.get().getQueueName(),
                        "Hostel Rector Office - " + hostel.get().getName() + " (Scoped Warden Fallback)",
                        "HOSTEL_RECTOR_FALLBACK",
                        true,
                        null
                );
            }
            return RoutingDecision.unroutable("No active warden or rector queue configured for " + hostelCode + ".");
        }

        // Scenario B: Department issue (e.g. AIML classroom or lab)
        if (locationType == ComplaintLocationType.DEPARTMENT) {
            if (specificDepartmentCode == null || specificDepartmentCode.trim().isEmpty()) {
                return RoutingDecision.unroutable("Please select the specific department where the issue occurred.");
            }
            String deptCode = specificDepartmentCode.trim().toUpperCase();
            Optional<Department> dept = departmentRepository.findById(deptCode);
            if (dept.isEmpty() || !dept.get().isActive()) {
                return RoutingDecision.unroutable("Department '" + deptCode + "' is not recognized or currently inactive.");
            }

            String deptQueueCode = "QUEUE_DEPT_" + deptCode;
            Optional<ComplaintQueue> queue = queueRepository.findByQueueCode(deptQueueCode);
            if (queue.isPresent() && queue.get().isActive()) {
                return new RoutingDecision(
                        queue.get().getQueueCode(),
                        queue.get().getQueueName(),
                        "Department Operations - " + dept.get().getName(),
                        "DEPARTMENT_DISPATCH",
                        true,
                        null
                );
            }
            return RoutingDecision.unroutable("No active queue configured for department " + deptCode + ".");
        }

        // Scenario C: Campus-wide service issue (IT, maintenance, transport, etc.)
        if (category == ComplaintCategory.IT_AND_NETWORK) {
            Optional<ComplaintQueue> queue = queueRepository.findByQueueCode("QUEUE_SERVICE_IT");
            if (queue.isPresent() && queue.get().isActive()) {
                return new RoutingDecision(
                        queue.get().getQueueCode(),
                        queue.get().getQueueName(),
                        "Campus IT & Network Operations Center",
                        "CAMPUS_IT_DISPATCH",
                        true,
                        null
                );
            }
        }

        if (category == ComplaintCategory.MESS_FOOD) {
            Optional<ComplaintQueue> queue = queueRepository.findByQueueCode("QUEUE_SERVICE_MESS");
            if (queue.isPresent() && queue.get().isActive()) {
                return new RoutingDecision(
                        queue.get().getQueueCode(),
                        queue.get().getQueueName(),
                        "Campus Food & Mess Operations Committee",
                        "CAMPUS_MESS_DISPATCH",
                        true,
                        null
                );
            }
        }

        // Default Campus Infrastructure / Maintenance Service Queue
        Optional<ComplaintQueue> maintQueue = queueRepository.findByQueueCode("QUEUE_SERVICE_MAINTENANCE");
        if (maintQueue.isPresent() && maintQueue.get().isActive()) {
            return new RoutingDecision(
                    maintQueue.get().getQueueCode(),
                    maintQueue.get().getQueueName(),
                    "Central Campus Estate & Maintenance Office",
                    "CAMPUS_MAINTENANCE_DISPATCH",
                    true,
                    null
            );
        }

        return RoutingDecision.unroutable("No operational queue found for this category and location. Submission prevented to avoid silent drop.");
    }

    /**
     * Resolves the next queue in the escalation hierarchy, preventing cycles.
     */
    public ComplaintQueue resolveNextEscalation(String currentQueueCode) {
        ComplaintQueue current = queueRepository.findByQueueCode(currentQueueCode)
                .orElseThrow(() -> new InvalidOperationException("Current queue not found: " + currentQueueCode));

        String targetCode = current.getEscalationQueueCode();
        if (targetCode == null || targetCode.trim().isEmpty()) {
            throw new InvalidOperationException("Queue '" + current.getQueueName() + "' has reached the highest escalation authority and cannot be escalated further.");
        }

        // Cycle check
        Set<String> visited = new HashSet<>();
        visited.add(currentQueueCode);
        String cursor = targetCode;
        while (cursor != null) {
            if (visited.contains(cursor)) {
                throw new InvalidOperationException("Escalation cycle detected in routing configuration involving " + cursor);
            }
            visited.add(cursor);
            Optional<ComplaintQueue> next = queueRepository.findByQueueCode(cursor);
            cursor = next.map(ComplaintQueue::getEscalationQueueCode).orElse(null);
        }

        return queueRepository.findByQueueCode(targetCode)
                .orElseThrow(() -> new InvalidOperationException("Escalation target queue not found: " + targetCode));
    }

    public record RoutingDecision(
            String queueCode,
            String queueName,
            String receivingOffice,
            String ruleApplied,
            boolean valid,
            String errorMessage
    ) {
        public static RoutingDecision unroutable(String message) {
            return new RoutingDecision(null, null, null, null, false, message);
        }
    }
}
