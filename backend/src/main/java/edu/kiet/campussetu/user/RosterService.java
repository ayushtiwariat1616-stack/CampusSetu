package edu.kiet.campussetu.user;

import edu.kiet.campussetu.audit.AuditService;
import edu.kiet.campussetu.common.InvalidOperationException;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.*;

@Service
public class RosterService {

    private final RosterEntryRepository rosterEntryRepository;
    private final UserRepository userRepository;
    private final OperatorAssignmentRepository assignmentRepository;
    private final AuditService auditService;

    public RosterService(RosterEntryRepository rosterEntryRepository,
                         UserRepository userRepository,
                         OperatorAssignmentRepository assignmentRepository,
                         AuditService auditService) {
        this.rosterEntryRepository = rosterEntryRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditService = auditService;
    }

    /**
     * Preview CSV import without committing changes.
     */
    public RosterImportPreview previewCsv(String csvContent) {
        List<RosterRowValidation> rows = parseAndValidateCsv(csvContent);
        int validCount = 0;
        int invalidCount = 0;
        int newEntries = 0;
        int updateEntries = 0;

        for (RosterRowValidation row : rows) {
            if (row.valid()) {
                validCount++;
                if (rosterEntryRepository.existsByEmailIgnoreCase(row.entry().getEmail())) {
                    updateEntries++;
                } else {
                    newEntries++;
                }
            } else {
                invalidCount++;
            }
        }

        return new RosterImportPreview(rows.size(), validCount, invalidCount, newEntries, updateEntries, rows);
    }

    /**
     * Commits valid CSV rows to roster and syncs existing registered users.
     */
    @Transactional
    public RosterImportResult applyCsv(String csvContent, String adminEmail) {
        List<RosterRowValidation> rows = parseAndValidateCsv(csvContent);
        int imported = 0;
        int syncedUsers = 0;

        for (RosterRowValidation row : rows) {
            if (!row.valid()) continue;
            RosterEntry entry = row.entry();
            Optional<RosterEntry> existing = rosterEntryRepository.findByEmailIgnoreCase(entry.getEmail());
            if (existing.isPresent()) {
                RosterEntry current = existing.get();
                current.setFullName(entry.getFullName());
                current.setRoleType(entry.getRoleType());
                current.setDepartmentCode(entry.getDepartmentCode());
                current.setHostelCode(entry.getHostelCode());
                current.setRoomNumber(entry.getRoomNumber());
                current.setStudentRollNumber(entry.getStudentRollNumber());
                current.setServiceScope(entry.getServiceScope());
                rosterEntryRepository.save(current);
            } else {
                rosterEntryRepository.save(entry);
            }
            imported++;

            // If user already signed up with pending state, sync immediately
            Optional<User> matchingUser = userRepository.findByEmailIgnoreCase(entry.getEmail());
            if (matchingUser.isPresent()) {
                syncUserWithRoster(matchingUser.get(), entry);
                syncedUsers++;
            }
        }

        auditService.record("ROSTER_CHANGE", adminEmail, "ROSTER_CSV_IMPORTED", "Roster",
                "Imported " + imported + " records, synced " + syncedUsers + " active users", "INTERNAL");

        return new RosterImportResult(imported, syncedUsers);
    }

    /**
     * Matches a newly authenticated user against approved roster.
     */
    @Transactional
    public void matchAndApplyRoster(User user) {
        Optional<RosterEntry> match = rosterEntryRepository.findByEmailIgnoreCase(user.getEmail());
        if (match.isPresent()) {
            syncUserWithRoster(user, match.get());
        } else {
            user.setOnboardingStatus(OnboardingStatus.PENDING_ONBOARDING);
            userRepository.save(user);
        }
    }

    private void syncUserWithRoster(User user, RosterEntry roster) {
        user.setFullName(roster.getFullName());
        user.setDepartmentCode(roster.getDepartmentCode());
        user.setHostelCode(roster.getHostelCode());
        user.setRoomNumber(roster.getRoomNumber());
        user.setStudentRollNumber(roster.getStudentRollNumber());
        user.setOnboardingStatus(OnboardingStatus.APPROVED);

        if ("STUDENT".equalsIgnoreCase(roster.getRoleType())) {
            user.setPrimaryRole(UserRole.STUDENT);
        } else if ("SYSTEM_ADMIN".equalsIgnoreCase(roster.getRoleType())) {
            user.setPrimaryRole(UserRole.SYSTEM_ADMIN);
            createOrUpdateAssignment(user, "SYSTEM_ADMIN", "CAMPUS", "CAMPUS_WIDE");
        } else {
            user.setPrimaryRole(UserRole.OPERATOR);
            String roleType = roster.getRoleType().toUpperCase();
            if ("HOSTEL_WARDEN".equals(roleType)) {
                createOrUpdateAssignment(user, "HOSTEL_WARDEN", "HOSTEL", roster.getHostelCode());
            } else if ("RECTOR".equals(roleType)) {
                createOrUpdateAssignment(user, "RECTOR", "HOSTEL", roster.getHostelCode());
            } else if ("CHIEF_RECTOR".equals(roleType)) {
                createOrUpdateAssignment(user, "CHIEF_RECTOR", "HOSTEL", "ALL_HOSTELS");
            } else if ("DEPARTMENT_OPERATOR".equals(roleType)) {
                createOrUpdateAssignment(user, "DEPARTMENT_OPERATOR", "DEPARTMENT", roster.getDepartmentCode());
            } else if ("SERVICE_OPERATOR".equals(roleType)) {
                createOrUpdateAssignment(user, "SERVICE_OPERATOR", "SERVICE", roster.getServiceScope());
            }
        }
        userRepository.save(user);
    }

    private void createOrUpdateAssignment(User user, String opRole, String scopeType, String scopeTarget) {
        List<OperatorAssignment> existing = assignmentRepository.findByUserId(user.getId());
        boolean hasAssignment = existing.stream().anyMatch(a ->
                a.getOperatorRole().equals(opRole) && a.getScopeTarget().equalsIgnoreCase(scopeTarget));
        if (!hasAssignment && scopeTarget != null) {
            OperatorAssignment assignment = new OperatorAssignment(user, opRole, scopeType, scopeTarget.toUpperCase());
            assignmentRepository.save(assignment);
        }
    }

    private List<RosterRowValidation> parseAndValidateCsv(String csvContent) {
        List<RosterRowValidation> result = new ArrayList<>();
        if (csvContent == null || csvContent.trim().isEmpty()) return result;

        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Skip header if line starts with email
                if (lineNum == 1 && line.toLowerCase().startsWith("email")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 3) {
                    result.add(new RosterRowValidation(lineNum, null, false, "Invalid format: requires at least email, fullName, roleType"));
                    continue;
                }

                String email = parts[0].trim().toLowerCase();
                String fullName = parts[1].trim();
                String roleType = parts[2].trim().toUpperCase();
                String dept = parts.length > 3 ? parts[3].trim().toUpperCase() : null;
                String hostel = parts.length > 4 ? parts[4].trim().toUpperCase() : null;
                String room = parts.length > 5 ? parts[5].trim() : null;
                String roll = parts.length > 6 ? parts[6].trim() : null;
                String service = parts.length > 7 ? parts[7].trim().toUpperCase() : null;

                if (!email.endsWith("@kiet.edu")) {
                    result.add(new RosterRowValidation(lineNum, null, false, "Email must be an official @kiet.edu address"));
                    continue;
                }

                Set<String> validRoles = Set.of("STUDENT", "HOSTEL_WARDEN", "RECTOR", "CHIEF_RECTOR", "DEPARTMENT_OPERATOR", "SERVICE_OPERATOR", "SYSTEM_ADMIN");
                if (!validRoles.contains(roleType)) {
                    result.add(new RosterRowValidation(lineNum, null, false, "Invalid roleType: " + roleType));
                    continue;
                }

                RosterEntry entry = new RosterEntry(email, fullName, roleType, dept, hostel, room, roll, service);
                result.add(new RosterRowValidation(lineNum, entry, true, "Valid"));
            }
        } catch (Exception e) {
            throw new InvalidOperationException("Failed to parse CSV: " + e.getMessage());
        }
        return result;
    }

    public record RosterRowValidation(int lineNumber, RosterEntry entry, boolean valid, String message) {}
    public record RosterImportPreview(int totalRows, int validCount, int invalidCount, int newCount, int updateCount, List<RosterRowValidation> rows) {}
    public record RosterImportResult(int importedCount, int syncedUsers) {}
}
