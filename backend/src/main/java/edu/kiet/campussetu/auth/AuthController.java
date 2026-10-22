package edu.kiet.campussetu.auth;

import edu.kiet.campussetu.audit.AuditService;
import edu.kiet.campussetu.common.ApiResponse;
import edu.kiet.campussetu.common.InvalidOperationException;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import edu.kiet.campussetu.complaint.ComplaintService;
import edu.kiet.campussetu.user.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthHelper authHelper;
    private final OperatorKeyService operatorKeyService;
    private final UserRepository userRepository;
    private final RosterEntryRepository rosterEntryRepository;
    private final OperatorAssignmentRepository assignmentRepository;
    private final ComplaintService complaintService;
    private final AuditService auditService;
    private final RosterService rosterService;

    @Value("${campussetu.dev-mode.enabled:false}")
    private boolean devModeEnabled;

    @Value("${campussetu.allowed-domain:kiet.edu}")
    private String allowedDomain;

    public AuthController(AuthHelper authHelper,
                          OperatorKeyService operatorKeyService,
                          UserRepository userRepository,
                          RosterEntryRepository rosterEntryRepository,
                          OperatorAssignmentRepository assignmentRepository,
                          ComplaintService complaintService,
                          AuditService auditService,
                          RosterService rosterService) {
        this.authHelper = authHelper;
        this.operatorKeyService = operatorKeyService;
        this.userRepository = userRepository;
        this.rosterEntryRepository = rosterEntryRepository;
        this.assignmentRepository = assignmentRepository;
        this.complaintService = complaintService;
        this.auditService = auditService;
        this.rosterService = rosterService;
    }

    /**
     * Return current authenticated user profile and scopes.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthMeResponse>> getCurrentUser(HttpServletRequest request) {
        SessionUser sessionUser = authHelper.getSessionUser(request);
        if (sessionUser == null) {
            return ResponseEntity.ok(ApiResponse.ok("Not authenticated", null));
        }

        AuthMeResponse response = new AuthMeResponse(
                sessionUser.getUserId(),
                sessionUser.getEmail(),
                sessionUser.getFullName(),
                sessionUser.getRole().name(),
                sessionUser.getOnboardingStatus().name(),
                sessionUser.getDepartmentCode(),
                sessionUser.getHostelCode(),
                sessionUser.getRoomNumber(),
                sessionUser.getStudentRollNumber(),
                sessionUser.isOperatorKeyVerified(),
                sessionUser.isOperator() && !sessionUser.isOperatorKeyVerified(), // needsKey
                sessionUser.getAuthorizedQueueCodes(),
                sessionUser.getOperatorRoles()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Operator Step 2: Validate Operator Key within the login page experience.
     */
    @PostMapping("/verify-operator-key")
    public ResponseEntity<ApiResponse<AuthMeResponse>> verifyOperatorKey(
            @RequestBody VerifyKeyRequest req,
            HttpServletRequest request) {

        SessionUser sessionUser = authHelper.getSessionUser(request);
        if (sessionUser == null) {
            throw new AccessDeniedException("Session expired. Please sign in again.");
        }

        if (sessionUser.getRole() != UserRole.OPERATOR && sessionUser.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new AccessDeniedException("This account is not designated as an operator.");
        }

        boolean valid = operatorKeyService.verifyKey(
                sessionUser.getUserId(), req.operatorKey(), request.getRemoteAddr());

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid operator key. Please check your credentials or contact administrator."));
        }

        // Grant operator privileges
        sessionUser.setOperatorKeyVerified(true);
        List<String> queues = complaintService.getAuthorizedQueueCodesForOperator(sessionUser);
        sessionUser.setAuthorizedQueueCodes(queues);
        authHelper.setSessionUser(request, sessionUser);

        AuthMeResponse response = new AuthMeResponse(
                sessionUser.getUserId(),
                sessionUser.getEmail(),
                sessionUser.getFullName(),
                sessionUser.getRole().name(),
                sessionUser.getOnboardingStatus().name(),
                sessionUser.getDepartmentCode(),
                sessionUser.getHostelCode(),
                sessionUser.getRoomNumber(),
                sessionUser.getStudentRollNumber(),
                true,
                false,
                sessionUser.getAuthorizedQueueCodes(),
                sessionUser.getOperatorRoles()
        );
        return ResponseEntity.ok(ApiResponse.ok("Operator access verified successfully.", response));
    }

    /**
     * Logout and destroy session.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        SessionUser user = authHelper.getSessionUser(request);
        if (user != null) {
            auditService.record("AUTH", user.getEmail(), "LOGOUT", "Session", "User signed out", request.getRemoteAddr());
        }
        authHelper.invalidate(request);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully.", null));
    }

    /**
     * Development and Demo Login: Strictly gated behind dev mode.
     * In production, this endpoint throws 403 Forbidden and fails.
     */
    @PostMapping("/demo-login")
    public ResponseEntity<ApiResponse<AuthMeResponse>> demoLogin(
            @RequestBody DemoLoginRequest req,
            HttpServletRequest request) {

        if (!devModeEnabled) {
            throw new AccessDeniedException("Demo authentication is strictly disabled in production.");
        }

        String accountType = req.accountType() != null ? req.accountType().toUpperCase() : "STUDENT_ARYABHATT";
        User user = resolveDemoUser(accountType);

        SessionUser sessionUser = new SessionUser(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPrimaryRole(),
                user.getOnboardingStatus()
        );
        sessionUser.setDepartmentCode(user.getDepartmentCode());
        sessionUser.setHostelCode(user.getHostelCode());
        sessionUser.setRoomNumber(user.getRoomNumber());
        sessionUser.setStudentRollNumber(user.getStudentRollNumber());

        if (user.getPrimaryRole() == UserRole.STUDENT) {
            sessionUser.setOperatorKeyVerified(true);
        } else {
            // Operators require step 2 key verification unless bypassKey is explicitly set
            sessionUser.setOperatorKeyVerified(req.autoVerifyKey() != null && req.autoVerifyKey());
            if (sessionUser.isOperatorKeyVerified()) {
                sessionUser.setAuthorizedQueueCodes(complaintService.getAuthorizedQueueCodesForOperator(sessionUser));
            }
        }

        authHelper.setSessionUser(request, sessionUser);

        auditService.record("AUTH", user.getEmail(), "DEMO_LOGIN", "DemoPortal",
                "Logged in as " + accountType + " (Dev Mode)", request.getRemoteAddr());

        AuthMeResponse response = new AuthMeResponse(
                sessionUser.getUserId(),
                sessionUser.getEmail(),
                sessionUser.getFullName(),
                sessionUser.getRole().name(),
                sessionUser.getOnboardingStatus().name(),
                sessionUser.getDepartmentCode(),
                sessionUser.getHostelCode(),
                sessionUser.getRoomNumber(),
                sessionUser.getStudentRollNumber(),
                sessionUser.isOperatorKeyVerified(),
                sessionUser.isOperator() && !sessionUser.isOperatorKeyVerified(),
                sessionUser.getAuthorizedQueueCodes(),
                sessionUser.getOperatorRoles()
        );

        return ResponseEntity.ok(ApiResponse.ok("Demo login authenticated.", response));
    }

    private User resolveDemoUser(String accountType) {
        String email;
        switch (accountType) {
            case "WARDEN_ARYABHATT" -> email = "warden.aryabhatt@kiet.edu";
            case "WARDEN_TAGORE" -> email = "warden.tagore@kiet.edu";
            case "WARDEN_SAROJINI" -> email = "warden.sarojini@kiet.edu";
            case "RECTOR_ARYABHATT" -> email = "rector.aryabhatt@kiet.edu";
            case "CHIEF_RECTOR" -> email = "chiefrector@kiet.edu";
            case "DEAN_SW" -> email = "dean.sw@kiet.edu";
            case "OPERATOR_AIML" -> email = "aiml.operator@kiet.edu";
            case "OPERATOR_IT" -> email = "it.service@kiet.edu";
            case "SYSTEM_ADMIN" -> email = "admin.campussetu@kiet.edu";
            case "PENDING_STUDENT" -> email = "unverified.student@kiet.edu";
            case "STUDENT_SRIJAN" -> email = "srijan.25002324@kiet.edu";
            case "STUDENT_ANANYA" -> email = "ananya.25001142@kiet.edu";
            case "STUDENT_ARYABHATT" -> email = "ayush.25004108@kiet.edu";
            default -> email = "ayush.25004108@kiet.edu";
        }

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Demo account " + email + " not initialized. Run seeder."));
    }

    /**
     * Google OIDC Authentication Endpoint.
     * Enforces strict institutional domain verification and automatic roster onboarding.
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthMeResponse>> handleGoogleAuth(
            @RequestBody GoogleAuthRequest req,
            HttpServletRequest request) {

        String email = req.email() != null ? req.email().trim().toLowerCase() : "";
        if (!email.endsWith("@" + allowedDomain.toLowerCase())) {
            auditService.record("AUTH_DENIED", email, "GOOGLE_LOGIN_FAILED", "GoogleOIDC",
                    "Rejected non-approved domain for email: " + email, request.getRemoteAddr());
            throw new AccessDeniedException("Access denied: Only official @" + allowedDomain + " accounts are permitted.");
        }

        // Lookup or initialize user
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(req.fullName() != null && !req.fullName().isBlank() ? req.fullName() : email.split("@")[0]);
            newUser.setPrimaryRole(UserRole.STUDENT);
            newUser.setOnboardingStatus(OnboardingStatus.PENDING_ONBOARDING);
            return userRepository.save(newUser);
        });

        // Match against roster to sync branch, hostel, and role
        rosterService.matchAndApplyRoster(user);

        SessionUser sessionUser = new SessionUser(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPrimaryRole(),
                user.getOnboardingStatus()
        );
        sessionUser.setDepartmentCode(user.getDepartmentCode());
        sessionUser.setHostelCode(user.getHostelCode());
        sessionUser.setRoomNumber(user.getRoomNumber());
        sessionUser.setStudentRollNumber(user.getStudentRollNumber());

        if (user.getPrimaryRole() == UserRole.STUDENT) {
            sessionUser.setOperatorKeyVerified(true);
        } else {
            // Operator accounts require Step 2: Key Verification
            sessionUser.setOperatorKeyVerified(false);
        }

        authHelper.setSessionUser(request, sessionUser);
        auditService.record("AUTH", user.getEmail(), "GOOGLE_LOGIN_SUCCESS", "GoogleOIDC",
                "Signed in via Google OIDC", request.getRemoteAddr());

        AuthMeResponse response = buildAuthMeResponse(sessionUser);
        return ResponseEntity.ok(ApiResponse.ok("Google sign-in successful.", response));
    }

    /**
     * Direct Institutional Account Login (Email & Password).
     * Provides native authentication for all KIET accounts.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthMeResponse>> directLogin(
            @RequestBody DirectLoginRequest req,
            HttpServletRequest request) {

        String email = req.email() != null ? req.email().trim().toLowerCase() : "";
        String password = req.password() != null ? req.password() : "";

        if (email.isBlank() || password.isBlank()) {
            throw new InvalidOperationException("Email and password are required.");
        }

        if (!email.endsWith("@" + allowedDomain.toLowerCase())) {
            auditService.record("AUTH_DENIED", email, "DIRECT_LOGIN_FAILED", "InstitutionalAuth",
                    "Domain mismatch: " + email, request.getRemoteAddr());
            throw new AccessDeniedException("Access denied: Only official @" + allowedDomain + " accounts are permitted.");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AccessDeniedException("Account not found for " + email + ". Please register your student account."));

        boolean passwordValid = false;
        if (user.getPasswordHash() != null) {
            passwordValid = passwordEncoder.matches(password, user.getPasswordHash());
        } else if ("CampusSetu@2026".equals(password)) {
            // First time login fallback for pre-seeded roster users
            passwordValid = true;
            user.setPasswordHash(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        if (!passwordValid) {
            auditService.record("AUTH_FAILED", email, "DIRECT_LOGIN_FAILED", "InstitutionalAuth",
                    "Invalid password attempt", request.getRemoteAddr());
            throw new AccessDeniedException("Invalid institutional credentials. Please check your password.");
        }

        rosterService.matchAndApplyRoster(user);

        SessionUser sessionUser = new SessionUser(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPrimaryRole(),
                user.getOnboardingStatus()
        );
        sessionUser.setDepartmentCode(user.getDepartmentCode());
        sessionUser.setHostelCode(user.getHostelCode());
        sessionUser.setRoomNumber(user.getRoomNumber());
        sessionUser.setStudentRollNumber(user.getStudentRollNumber());

        if (user.getPrimaryRole() == UserRole.STUDENT) {
            sessionUser.setOperatorKeyVerified(true);
        } else {
            // Operators require step 2 key verification
            sessionUser.setOperatorKeyVerified(false);
        }

        authHelper.setSessionUser(request, sessionUser);
        auditService.record("AUTH", user.getEmail(), "DIRECT_LOGIN_SUCCESS", "InstitutionalAuth",
                "Signed in via Institutional Direct Login", request.getRemoteAddr());

        AuthMeResponse response = buildAuthMeResponse(sessionUser);
        return ResponseEntity.ok(ApiResponse.ok("Institutional sign-in successful.", response));
    }

    /**
     * Direct Student Registration.
     * Allows students with @kiet.edu email to register, matching automatically against roster.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthMeResponse>> registerStudent(
            @RequestBody StudentRegisterRequest req,
            HttpServletRequest request) {

        String email = req.email() != null ? req.email().trim().toLowerCase() : "";
        String password = req.password() != null ? req.password() : "";

        if (email.isBlank() || password.isBlank() || req.fullName() == null || req.fullName().isBlank()) {
            throw new InvalidOperationException("Full name, institutional email, and password are required.");
        }

        if (!email.endsWith("@" + allowedDomain.toLowerCase())) {
            throw new AccessDeniedException("Registration is restricted to official @" + allowedDomain + " accounts.");
        }

        if (password.length() < 6) {
            throw new InvalidOperationException("Password must be at least 6 characters long.");
        }

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new InvalidOperationException("An account already exists for " + email + ". Please sign in.");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(req.fullName().trim());
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setPrimaryRole(UserRole.STUDENT);
        newUser.setDepartmentCode(req.departmentCode() != null && !req.departmentCode().isBlank() ? req.departmentCode().trim() : "AIML");
        newUser.setHostelCode(req.hostelCode() != null && !req.hostelCode().isBlank() ? req.hostelCode().trim() : "ARYABHATT");
        newUser.setRoomNumber(req.roomNumber() != null ? req.roomNumber().trim() : null);
        newUser.setStudentRollNumber(req.studentRollNumber() != null ? req.studentRollNumber().trim() : null);
        newUser.setOnboardingStatus(OnboardingStatus.APPROVED);

        User savedUser = userRepository.save(newUser);
        rosterService.matchAndApplyRoster(savedUser);

        SessionUser sessionUser = new SessionUser(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getPrimaryRole(),
                savedUser.getOnboardingStatus()
        );
        sessionUser.setDepartmentCode(savedUser.getDepartmentCode());
        sessionUser.setHostelCode(savedUser.getHostelCode());
        sessionUser.setRoomNumber(savedUser.getRoomNumber());
        sessionUser.setStudentRollNumber(savedUser.getStudentRollNumber());
        sessionUser.setOperatorKeyVerified(true);

        authHelper.setSessionUser(request, sessionUser);
        auditService.record("REGISTRATION", savedUser.getEmail(), "STUDENT_REGISTERED", "RegistrationPortal",
                "New student account registered", request.getRemoteAddr());

        AuthMeResponse response = buildAuthMeResponse(sessionUser);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful. Welcome to CampusSetu!", response));
    }

    private AuthMeResponse buildAuthMeResponse(SessionUser sessionUser) {
        return new AuthMeResponse(
                sessionUser.getUserId(),
                sessionUser.getEmail(),
                sessionUser.getFullName(),
                sessionUser.getRole().name(),
                sessionUser.getOnboardingStatus().name(),
                sessionUser.getDepartmentCode(),
                sessionUser.getHostelCode(),
                sessionUser.getRoomNumber(),
                sessionUser.getStudentRollNumber(),
                sessionUser.isOperatorKeyVerified(),
                sessionUser.isOperator() && !sessionUser.isOperatorKeyVerified(),
                sessionUser.getAuthorizedQueueCodes(),
                sessionUser.getOperatorRoles()
        );
    }

    public record DirectLoginRequest(String email, String password) {}
    public record StudentRegisterRequest(
            String email,
            String password,
            String fullName,
            String departmentCode,
            String hostelCode,
            String roomNumber,
            String studentRollNumber
    ) {}
    public record GoogleAuthRequest(String email, String fullName) {}
    public record VerifyKeyRequest(String operatorKey) {}
    public record DemoLoginRequest(String accountType, Boolean autoVerifyKey) {}
    public record AuthMeResponse(
            Long id,
            String email,
            String fullName,
            String role,
            String onboardingStatus,
            String departmentCode,
            String hostelCode,
            String roomNumber,
            String studentRollNumber,
            boolean operatorKeyVerified,
            boolean needsKey,
            List<String> authorizedQueueCodes,
            List<String> operatorRoles
    ) {}
}
