package edu.kiet.campussetu.config;

import edu.kiet.campussetu.academic.AcademicDocument;
import edu.kiet.campussetu.academic.AcademicDocumentPage;
import edu.kiet.campussetu.academic.AcademicDocumentPageRepository;
import edu.kiet.campussetu.academic.AcademicDocumentRepository;
import edu.kiet.campussetu.auth.OperatorKeyService;
import edu.kiet.campussetu.complaint.*;
import edu.kiet.campussetu.incident.SharedIncident;
import edu.kiet.campussetu.incident.SharedIncidentRepository;
import edu.kiet.campussetu.incident.SharedIncidentStatus;
import edu.kiet.campussetu.organization.*;
import edu.kiet.campussetu.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departmentRepository;
    private final HostelRepository hostelRepository;
    private final CampusServiceRepository serviceRepository;
    private final ComplaintQueueRepository queueRepository;
    private final RoutingRuleRepository routingRuleRepository;
    private final UserRepository userRepository;
    private final OperatorAssignmentRepository assignmentRepository;
    private final OperatorKeyRepository operatorKeyRepository;
    private final RosterEntryRepository rosterEntryRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintEventRepository eventRepository;
    private final ComplaintNoteRepository noteRepository;
    private final SharedIncidentRepository incidentRepository;
    private final AcademicDocumentRepository academicDocumentRepository;
    private final AcademicDocumentPageRepository academicDocumentPageRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(DepartmentRepository departmentRepository,
                           HostelRepository hostelRepository,
                           CampusServiceRepository serviceRepository,
                           ComplaintQueueRepository queueRepository,
                           RoutingRuleRepository routingRuleRepository,
                           UserRepository userRepository,
                           OperatorAssignmentRepository assignmentRepository,
                           OperatorKeyRepository operatorKeyRepository,
                           RosterEntryRepository rosterEntryRepository,
                           ComplaintRepository complaintRepository,
                           ComplaintEventRepository eventRepository,
                           ComplaintNoteRepository noteRepository,
                           SharedIncidentRepository incidentRepository,
                           AcademicDocumentRepository academicDocumentRepository,
                           AcademicDocumentPageRepository academicDocumentPageRepository) {
        this.departmentRepository = departmentRepository;
        this.hostelRepository = hostelRepository;
        this.serviceRepository = serviceRepository;
        this.queueRepository = queueRepository;
        this.routingRuleRepository = routingRuleRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.operatorKeyRepository = operatorKeyRepository;
        this.rosterEntryRepository = rosterEntryRepository;
        this.complaintRepository = complaintRepository;
        this.eventRepository = eventRepository;
        this.noteRepository = noteRepository;
        this.incidentRepository = incidentRepository;
        this.academicDocumentRepository = academicDocumentRepository;
        this.academicDocumentPageRepository = academicDocumentPageRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() > 0) {
            log.info("Database already initialized with operational records.");
            return;
        }

        log.info("Initializing CampusSetu schema, organizational queues, and baseline campus profiles...");

        // 1. Departments (8 configurable engineering/technical departments)
        List<Department> departments = List.of(
                new Department("AIML", "Artificial Intelligence & Machine Learning", "Academic Block A"),
                new Department("CSE", "Computer Science & Engineering", "Academic Block B"),
                new Department("IT", "Information Technology", "Academic Block B"),
                new Department("ECE", "Electronics & Communication Engineering", "Academic Block C"),
                new Department("EN", "Electrical & Electronics Engineering", "Academic Block C"),
                new Department("ME", "Mechanical Engineering", "Mechanical Workshop Wing"),
                new Department("CE", "Civil Engineering", "Civil Engineering Block"),
                new Department("MCA", "Computer Applications", "Postgraduate Block")
        );
        departmentRepository.saveAll(departments);

        // 2. Hostels (Official KIET Allocation Rules)
        List<Hostel> hostels = List.of(
                // Boys Hostels
                new Hostel("CV_RAMAN", "C.V. Raman Hostel (1st Year Boys)", "BOYS", "QUEUE_HOSTEL_CV_RAMAN_RECTOR"),
                new Hostel("CHANDRAGUPT", "Chandragupt Hostel (1st Year Boys)", "BOYS", "QUEUE_HOSTEL_CHANDRAGUPT_RECTOR"),
                new Hostel("TAGORE", "Tagore Hostel (2nd Year Boys)", "BOYS", "QUEUE_HOSTEL_TAGORE_RECTOR"),
                new Hostel("ARYABHATT", "Aryabhatt Hostel (2nd & 3rd Year Boys)", "BOYS", "QUEUE_HOSTEL_ARYABHATT_RECTOR"),
                new Hostel("VIVEKANAND", "Vivekanand Hostel (2nd & 3rd Year Boys)", "BOYS", "QUEUE_HOSTEL_VIVEKANAND_RECTOR"),
                new Hostel("CHANAKYA", "Chanakya Hostel (4th Year Boys)", "BOYS", "QUEUE_HOSTEL_CHANAKYA_RECTOR"),

                // Girls Hostels
                new Hostel("GARGI", "Gargi Hostel (1st Year Girls)", "GIRLS", "QUEUE_HOSTEL_GARGI_RECTOR"),
                new Hostel("SAROJINI", "Sarojini Hostel (2nd & 3rd Year Girls)", "GIRLS", "QUEUE_HOSTEL_SAROJINI_RECTOR"),
                new Hostel("SARASWATI", "Saraswati Hostel (2nd, 3rd & 4th Year Girls)", "GIRLS", "QUEUE_HOSTEL_SARASWATI_RECTOR")
        );
        hostelRepository.saveAll(hostels);

        // 3. Central Services
        List<CampusService> services = List.of(
                new CampusService("IT_SERVICES", "Campus IT & Network Operations Center", "Wi-Fi, ERP, and institutional compute infrastructure"),
                new CampusService("CAMPUS_MAINTENANCE", "Estate & Maintenance Operations", "Campus electrical, plumbing, sanitation and carpentry"),
                new CampusService("MESS_FOOD", "Hostel Mess & Catering Services", "Food safety, meal schedule, and student mess committees"),
                new CampusService("TRANSPORT", "Fleet & Transit Operations", "College bus shuttles and day-scholar transit")
        );
        serviceRepository.saveAll(services);

        // 4. Complaint Queues & Escalation Hierarchies
        List<ComplaintQueue> queues = List.of(
                // Hostels Escalation Hierarchy: Hostel Warden -> Rector -> Chief Rector -> Central Admin
                new ComplaintQueue("QUEUE_HOSTEL_CV_RAMAN_WARDEN", "C.V. Raman Hostel Warden Queue", "HOSTEL_WARDEN", "CV_RAMAN", "QUEUE_HOSTEL_CV_RAMAN_RECTOR", 12, 48),
                new ComplaintQueue("QUEUE_HOSTEL_CV_RAMAN_RECTOR", "C.V. Raman Hostel Rector Queue", "RECTOR", "CV_RAMAN", "QUEUE_CHIEF_RECTOR", 24, 72),

                new ComplaintQueue("QUEUE_HOSTEL_CHANDRAGUPT_WARDEN", "Chandragupt Hostel Warden Queue", "HOSTEL_WARDEN", "CHANDRAGUPT", "QUEUE_HOSTEL_CHANDRAGUPT_RECTOR", 12, 48),
                new ComplaintQueue("QUEUE_HOSTEL_CHANDRAGUPT_RECTOR", "Chandragupt Hostel Rector Queue", "RECTOR", "CHANDRAGUPT", "QUEUE_CHIEF_RECTOR", 24, 72),

                new ComplaintQueue("QUEUE_HOSTEL_TAGORE_WARDEN", "Tagore Hostel Warden Queue", "HOSTEL_WARDEN", "TAGORE", "QUEUE_HOSTEL_TAGORE_RECTOR", 12, 48),
                new ComplaintQueue("QUEUE_HOSTEL_TAGORE_RECTOR", "Tagore Hostel Rector Queue", "RECTOR", "TAGORE", "QUEUE_CHIEF_RECTOR", 24, 72),

                new ComplaintQueue("QUEUE_HOSTEL_ARYABHATT_WARDEN", "Aryabhatt Hostel Warden Queue", "HOSTEL_WARDEN", "ARYABHATT", "QUEUE_HOSTEL_ARYABHATT_RECTOR", 12, 48),
                new ComplaintQueue("QUEUE_HOSTEL_ARYABHATT_RECTOR", "Aryabhatt Hostel Rector Queue", "RECTOR", "ARYABHATT", "QUEUE_CHIEF_RECTOR", 24, 72),

                new ComplaintQueue("QUEUE_HOSTEL_VIVEKANAND_WARDEN", "Vivekanand Hostel Warden Queue", "HOSTEL_WARDEN", "VIVEKANAND", "QUEUE_HOSTEL_VIVEKANAND_RECTOR", 12, 48),
                new ComplaintQueue("QUEUE_HOSTEL_VIVEKANAND_RECTOR", "Vivekanand Hostel Rector Queue", "RECTOR", "VIVEKANAND", "QUEUE_CHIEF_RECTOR", 24, 72),

                new ComplaintQueue("QUEUE_HOSTEL_CHANAKYA_WARDEN", "Chanakya Hostel Warden Queue", "HOSTEL_WARDEN", "CHANAKYA", "QUEUE_HOSTEL_CHANAKYA_RECTOR", 12, 48),
                new ComplaintQueue("QUEUE_HOSTEL_CHANAKYA_RECTOR", "Chanakya Hostel Rector Queue", "RECTOR", "CHANAKYA", "QUEUE_CHIEF_RECTOR", 24, 72),

                new ComplaintQueue("QUEUE_HOSTEL_GARGI_WARDEN", "Gargi Hostel Warden Queue", "HOSTEL_WARDEN", "GARGI", "QUEUE_HOSTEL_GARGI_RECTOR", 12, 48),
                new ComplaintQueue("QUEUE_HOSTEL_GARGI_RECTOR", "Gargi Hostel Rector Queue", "RECTOR", "GARGI", "QUEUE_CHIEF_RECTOR", 24, 72),

                new ComplaintQueue("QUEUE_HOSTEL_SAROJINI_WARDEN", "Sarojini Hostel Warden Queue", "HOSTEL_WARDEN", "SAROJINI", "QUEUE_HOSTEL_SAROJINI_RECTOR", 12, 48),
                new ComplaintQueue("QUEUE_HOSTEL_SAROJINI_RECTOR", "Sarojini Hostel Rector Queue", "RECTOR", "SAROJINI", "QUEUE_CHIEF_RECTOR", 24, 72),

                new ComplaintQueue("QUEUE_HOSTEL_SARASWATI_WARDEN", "Saraswati Hostel Warden Queue", "HOSTEL_WARDEN", "SARASWATI", "QUEUE_HOSTEL_SARASWATI_RECTOR", 12, 48),
                new ComplaintQueue("QUEUE_HOSTEL_SARASWATI_RECTOR", "Saraswati Hostel Rector Queue", "RECTOR", "SARASWATI", "QUEUE_CHIEF_RECTOR", 24, 72),

                new ComplaintQueue("QUEUE_CHIEF_RECTOR", "Chief Rector Office Queue", "CHIEF_RECTOR", "ALL_HOSTELS", "QUEUE_CENTRAL_ADMIN", 24, 48),

                // Scenario B Hierarchy: AIML Department Operator -> Central Admin
                new ComplaintQueue("QUEUE_DEPT_AIML", "AIML Department Operations", "DEPARTMENT", "AIML", "QUEUE_CENTRAL_ADMIN", 24, 72),
                new ComplaintQueue("QUEUE_DEPT_CSE", "CSE Department Operations", "DEPARTMENT", "CSE", "QUEUE_CENTRAL_ADMIN", 24, 72),
                new ComplaintQueue("QUEUE_DEPT_IT", "IT Department Operations", "DEPARTMENT", "IT", "QUEUE_CENTRAL_ADMIN", 24, 72),
                new ComplaintQueue("QUEUE_DEPT_ECE", "ECE Department Operations", "DEPARTMENT", "ECE", "QUEUE_CENTRAL_ADMIN", 24, 72),
                new ComplaintQueue("QUEUE_DEPT_EN", "EN Department Operations", "DEPARTMENT", "EN", "QUEUE_CENTRAL_ADMIN", 24, 72),
                new ComplaintQueue("QUEUE_DEPT_ME", "Mechanical Dept Operations", "DEPARTMENT", "ME", "QUEUE_CENTRAL_ADMIN", 24, 72),
                new ComplaintQueue("QUEUE_DEPT_CE", "Civil Dept Operations", "DEPARTMENT", "CE", "QUEUE_CENTRAL_ADMIN", 24, 72),
                new ComplaintQueue("QUEUE_DEPT_MCA", "MCA Dept Operations", "DEPARTMENT", "MCA", "QUEUE_CENTRAL_ADMIN", 24, 72),

                // Scenario C: Central Campus Service Queues
                new ComplaintQueue("QUEUE_SERVICE_IT", "Campus IT Helpdesk Queue", "SERVICE", "IT_SERVICES", "QUEUE_CENTRAL_ADMIN", 12, 36),
                new ComplaintQueue("QUEUE_SERVICE_MAINTENANCE", "Central Estate Maintenance", "SERVICE", "CAMPUS_MAINTENANCE", "QUEUE_CENTRAL_ADMIN", 24, 72),
                new ComplaintQueue("QUEUE_SERVICE_MESS", "Campus Mess Council Queue", "SERVICE", "MESS_FOOD", "QUEUE_CENTRAL_ADMIN", 12, 24),
                new ComplaintQueue("QUEUE_SERVICE_TRANSPORT", "Campus Transport Services", "SERVICE", "TRANSPORT", "QUEUE_CENTRAL_ADMIN", 24, 48),

                // Executive Women's Safety & Anti-Harassment Priority Queue: Directly to Dean SW
                new ComplaintQueue("QUEUE_DEAN_STUDENT_WELFARE", "Dean Student Welfare (Dean SW) Emergency Desk", "DEAN_SW", "CAMPUS_WIDE", "QUEUE_CENTRAL_ADMIN", 1, 12),

                // Central Administrator Final Escalation Target
                new ComplaintQueue("QUEUE_CENTRAL_ADMIN", "Director General & Central Grievance Cell", "CENTRAL_ADMIN", "CAMPUS_WIDE", null, 24, 48)
        );
        queueRepository.saveAll(queues);

        // 5. Pre-Approved Roster Entries
        List<RosterEntry> rosterList = List.of(
                new RosterEntry("ayush.25004108@kiet.edu", "Ayush Sharma", "STUDENT", "AIML", "ARYABHATT", "214", "25004108", null),
                new RosterEntry("srijan.25002324@kiet.edu", "Srijan Shrivastava", "STUDENT", "CSE", "ARYABHATT", "105", "25002324", null),
                new RosterEntry("ananya.25001142@kiet.edu", "Ananya Singh", "STUDENT", "IT", "SAROJINI", "204", "25001142", null),
                new RosterEntry("dean.sw@kiet.edu", "Prof. Dean Student Welfare (Dean SW)", "DEAN_SW", null, null, null, null, "CAMPUS_WIDE"),
                new RosterEntry("warden.aryabhatt@kiet.edu", "Hostel Warden Aryabhatt", "HOSTEL_WARDEN", null, "ARYABHATT", null, null, null),
                new RosterEntry("warden.tagore@kiet.edu", "Hostel Warden Tagore", "HOSTEL_WARDEN", null, "TAGORE", null, null, null),
                new RosterEntry("warden.sarojini@kiet.edu", "Hostel Warden Sarojini", "HOSTEL_WARDEN", null, "SAROJINI", null, null, null),
                new RosterEntry("rector.aryabhatt@kiet.edu", "Rector Aryabhatt Hostel", "RECTOR", null, "ARYABHATT", null, null, null),
                new RosterEntry("chiefrector@kiet.edu", "Chief Rector Hostels", "CHIEF_RECTOR", null, null, null, null, "ALL_HOSTELS"),
                new RosterEntry("aiml.operator@kiet.edu", "AIML Lab & Facility Incharge", "DEPARTMENT_OPERATOR", "AIML", null, null, null, null),
                new RosterEntry("it.service@kiet.edu", "Campus IT Service Desk", "SERVICE_OPERATOR", null, null, null, null, "IT_SERVICES"),
                new RosterEntry("admin.campussetu@kiet.edu", "CampusSetu System Administrator", "SYSTEM_ADMIN", null, null, null, null, "CAMPUS_WIDE")
        );
        rosterEntryRepository.saveAll(rosterList);

        // 6. Seed Users with Scopes & Individual Operator Keys
        String defaultPasswordHash = passwordEncoder.encode("CampusSetu@2026");

        // Student 1: Ayush Sharma (Boy)
        User student = new User("ayush.25004108@kiet.edu", "sub-google-ayush", "Ayush Sharma", UserRole.STUDENT, OnboardingStatus.APPROVED);
        student.setDepartmentCode("AIML");
        student.setHostelCode("ARYABHATT");
        student.setRoomNumber("214");
        student.setStudentRollNumber("25004108");
        student.setGender("MALE");
        student.setPasswordHash(defaultPasswordHash);
        student = userRepository.save(student);

        // Student 2: Srijan Shrivastava (Boy in Aryabhatt Room 105)
        User srijan = new User("srijan.25002324@kiet.edu", "sub-google-srijan", "Srijan Shrivastava", UserRole.STUDENT, OnboardingStatus.APPROVED);
        srijan.setDepartmentCode("CSE");
        srijan.setHostelCode("ARYABHATT");
        srijan.setRoomNumber("105");
        srijan.setStudentRollNumber("25002324");
        srijan.setGender("MALE");
        srijan.setPasswordHash(defaultPasswordHash);
        userRepository.save(srijan);

        // Student 3: Ananya Singh (Girl in Sarojini Room 204)
        User ananya = new User("ananya.25001142@kiet.edu", "sub-google-ananya", "Ananya Singh", UserRole.STUDENT, OnboardingStatus.APPROVED);
        ananya.setDepartmentCode("IT");
        ananya.setHostelCode("SAROJINI");
        ananya.setRoomNumber("204");
        ananya.setStudentRollNumber("25001142");
        ananya.setGender("FEMALE");
        ananya.setPasswordHash(defaultPasswordHash);
        userRepository.save(ananya);

        // Warden Aryabhatt
        User warden = new User("warden.aryabhatt@kiet.edu", "sub-google-warden", "Hostel Warden Aryabhatt", UserRole.OPERATOR, OnboardingStatus.APPROVED);
        warden.setHostelCode("ARYABHATT");
        warden.setPasswordHash(defaultPasswordHash);
        warden = userRepository.save(warden);
        assignmentRepository.save(new OperatorAssignment(warden, "HOSTEL_WARDEN", "HOSTEL", "ARYABHATT"));
        seedOperatorKey(warden, "OPK-ARYA-WARD-2026");

        // Warden Tagore (Boys)
        User wardenTagore = new User("warden.tagore@kiet.edu", "sub-google-warden-tagore", "Hostel Warden Tagore", UserRole.OPERATOR, OnboardingStatus.APPROVED);
        wardenTagore.setHostelCode("TAGORE");
        wardenTagore.setPasswordHash(defaultPasswordHash);
        wardenTagore = userRepository.save(wardenTagore);
        assignmentRepository.save(new OperatorAssignment(wardenTagore, "HOSTEL_WARDEN", "HOSTEL", "TAGORE"));
        seedOperatorKey(wardenTagore, "OPK-TAGO-WARD-2026");

        // Warden Sarojini (Girls)
        User wardenSarojini = new User("warden.sarojini@kiet.edu", "sub-google-warden-sarojini", "Hostel Warden Sarojini", UserRole.OPERATOR, OnboardingStatus.APPROVED);
        wardenSarojini.setHostelCode("SAROJINI");
        wardenSarojini.setPasswordHash(defaultPasswordHash);
        wardenSarojini = userRepository.save(wardenSarojini);
        assignmentRepository.save(new OperatorAssignment(wardenSarojini, "HOSTEL_WARDEN", "HOSTEL", "SAROJINI"));
        seedOperatorKey(wardenSarojini, "OPK-SARO-WARD-2026");

        // Rector Aryabhatt
        User rector = new User("rector.aryabhatt@kiet.edu", "sub-google-rector", "Prof. Rector Aryabhatt", UserRole.OPERATOR, OnboardingStatus.APPROVED);
        rector.setHostelCode("ARYABHATT");
        rector.setPasswordHash(defaultPasswordHash);
        rector = userRepository.save(rector);
        assignmentRepository.save(new OperatorAssignment(rector, "RECTOR", "HOSTEL", "ARYABHATT"));
        seedOperatorKey(rector, "OPK-ARYA-RECT-2026");

        // Chief Rector
        User chiefRector = new User("chiefrector@kiet.edu", "sub-google-chiefrector", "Chief Rector Hostels", UserRole.OPERATOR, OnboardingStatus.APPROVED);
        chiefRector.setPasswordHash(defaultPasswordHash);
        chiefRector = userRepository.save(chiefRector);
        assignmentRepository.save(new OperatorAssignment(chiefRector, "CHIEF_RECTOR", "HOSTEL", "ALL_HOSTELS"));
        seedOperatorKey(chiefRector, "OPK-CHIEF-RECT-2026");

        // Dean Student Welfare (Dean SW) - Executive Women's Safety & Student Affairs
        User deanSw = new User("dean.sw@kiet.edu", "sub-google-deansw", "Prof. Dean Student Welfare (Dean SW)", UserRole.OPERATOR, OnboardingStatus.APPROVED);
        deanSw.setPasswordHash(defaultPasswordHash);
        deanSw = userRepository.save(deanSw);
        assignmentRepository.save(new OperatorAssignment(deanSw, "DEAN_SW", "CAMPUS_WIDE", "CAMPUS_WIDE"));
        seedOperatorKey(deanSw, "OPK-DEAN-SW-2026");

        // AIML Dept Operator
        User aimlOp = new User("aiml.operator@kiet.edu", "sub-google-aimlop", "AIML Department Officer", UserRole.OPERATOR, OnboardingStatus.APPROVED);
        aimlOp.setDepartmentCode("AIML");
        aimlOp.setPasswordHash(defaultPasswordHash);
        aimlOp = userRepository.save(aimlOp);
        assignmentRepository.save(new OperatorAssignment(aimlOp, "DEPARTMENT_OPERATOR", "DEPARTMENT", "AIML"));
        seedOperatorKey(aimlOp, "OPK-AIML-DEPT-2026");

        // Campus IT Operator
        User itOp = new User("it.service@kiet.edu", "sub-google-itop", "Network Engineer IT Cell", UserRole.OPERATOR, OnboardingStatus.APPROVED);
        itOp.setPasswordHash(defaultPasswordHash);
        itOp = userRepository.save(itOp);
        assignmentRepository.save(new OperatorAssignment(itOp, "SERVICE_OPERATOR", "SERVICE", "IT_SERVICES"));
        seedOperatorKey(itOp, "OPK-ITSRV-CAMP-2026");

        // System Admin
        User admin = new User("admin.campussetu@kiet.edu", "sub-google-admin", "Directorate Administrator", UserRole.SYSTEM_ADMIN, OnboardingStatus.APPROVED);
        admin.setPasswordHash(defaultPasswordHash);
        admin = userRepository.save(admin);
        assignmentRepository.save(new OperatorAssignment(admin, "SYSTEM_ADMIN", "CAMPUS", "CAMPUS_WIDE"));
        seedOperatorKey(admin, "OPK-ADMIN-ROOT-2026");

        // Pending Unverified User
        User pending = new User("unverified.student@kiet.edu", "sub-google-unverified", "Rahul Verma (Guest)", UserRole.STUDENT, OnboardingStatus.PENDING_ONBOARDING);
        pending.setPasswordHash(defaultPasswordHash);
        userRepository.save(pending);

        // 7. Seed Demo Complaints Demonstrating Scenarios A, B, and C
        // Scenario A: Aryabhatt Hostel Room 214 Fan Failure
        Complaint c1 = new Complaint();
        c1.setTrackingNumber("CMP-2026-00101");
        c1.setStudent(student);
        c1.setTitle("Ceiling fan regulator burned out in Room 214");
        c1.setDescription("The speed regulator on the ceiling fan in Aryabhatt Hostel Room 214 started sparking and has stopped working completely. The fan cannot be switched on.");
        c1.setCategory(ComplaintCategory.HOSTEL_FACILITY);
        c1.setLocationType(ComplaintLocationType.HOSTEL);
        c1.setLocationDetail("Aryabhatt Hostel, 2nd Floor, Room 214");
        c1.setSpecificHostelCode("ARYABHATT");
        c1.setStatus(ComplaintStatus.ACKNOWLEDGED);
        c1.setUrgency(ComplaintUrgency.HIGH);
        c1.setCurrentQueueCode("QUEUE_HOSTEL_ARYABHATT_WARDEN");
        c1.setAssignedOperatorEmail(warden.getEmail());
        c1.setAcknowledgedAt(Instant.now().minus(2, ChronoUnit.HOURS));
        c1 = complaintRepository.save(c1);

        eventRepository.save(new ComplaintEvent(c1.getId(), "SUBMITTED", student.getEmail(), student.getFullName(), "STUDENT", null, "QUEUE_HOSTEL_ARYABHATT_WARDEN", "Dispatched to Aryabhatt Hostel Warden Queue"));
        eventRepository.save(new ComplaintEvent(c1.getId(), "ACKNOWLEDGED", warden.getEmail(), warden.getFullName(), "OPERATOR", "QUEUE_HOSTEL_ARYABHATT_WARDEN", "QUEUE_HOSTEL_ARYABHATT_WARDEN", "Warden acknowledged. Electrician work order assigned."));
        noteRepository.save(new ComplaintNote(c1.getId(), warden, "Electrician Mr. Ram Singh assigned under slip #AR-412. Will visit room between 4:00 PM and 5:30 PM.", true)); // Internal note
        noteRepository.save(new ComplaintNote(c1.getId(), warden, "Your complaint has been scheduled for electrician visit this evening.", false)); // Public note

        // Scenario B: AIML Classroom / Lab Workstation Issue
        Complaint c2 = new Complaint();
        c2.setTrackingNumber("CMP-2026-00102");
        c2.setStudent(student);
        c2.setTitle("AIML Lab 3 Workstation #08 GPU cuda initialization failure");
        c2.setDescription("Workstation #08 in AIML Computer Lab 3 freezes whenever PyTorch initializes NVIDIA CUDA runtime during lab assignments. Driver reload required.");
        c2.setCategory(ComplaintCategory.LAB_EQUIPMENT);
        c2.setLocationType(ComplaintLocationType.DEPARTMENT);
        c2.setLocationDetail("Academic Block A, 1st Floor, AIML Lab 3, Workstation #08");
        c2.setSpecificDepartmentCode("AIML");
        c2.setStatus(ComplaintStatus.IN_PROGRESS);
        c2.setUrgency(ComplaintUrgency.MEDIUM);
        c2.setCurrentQueueCode("QUEUE_DEPT_AIML");
        c2.setAssignedOperatorEmail(aimlOp.getEmail());
        c2.setAcknowledgedAt(Instant.now().minus(8, ChronoUnit.HOURS));
        c2 = complaintRepository.save(c2);

        eventRepository.save(new ComplaintEvent(c2.getId(), "SUBMITTED", student.getEmail(), student.getFullName(), "STUDENT", null, "QUEUE_DEPT_AIML", "Dispatched to AIML Department Operations"));
        eventRepository.save(new ComplaintEvent(c2.getId(), "ACKNOWLEDGED", aimlOp.getEmail(), aimlOp.getFullName(), "OPERATOR", "QUEUE_DEPT_AIML", "QUEUE_DEPT_AIML", "Lab staff verified workstation error"));
        eventRepository.save(new ComplaintEvent(c2.getId(), "STATUS_CHANGE", aimlOp.getEmail(), aimlOp.getFullName(), "OPERATOR", "QUEUE_DEPT_AIML", "QUEUE_DEPT_AIML", "NVIDIA 550 driver reinstall in progress"));

        // Scenario C: Central Campus Service - Wi-Fi Access Point Issue
        Complaint c3 = new Complaint();
        c3.setTrackingNumber("CMP-2026-00103");
        c3.setStudent(student);
        c3.setTitle("Campus Wi-Fi authentication portal loop in Block A Corridor");
        c3.setDescription("Students connecting to KIET_Student SSID on Block A 2nd floor get trapped in captive portal redirect loop with ERR_CONNECTION_REFUSED.");
        c3.setCategory(ComplaintCategory.IT_AND_NETWORK);
        c3.setLocationType(ComplaintLocationType.CAMPUS_FACILITY);
        c3.setLocationDetail("Academic Block A, 2nd Floor Central Corridor");
        c3.setStatus(ComplaintStatus.RESOLUTION_PROPOSED);
        c3.setUrgency(ComplaintUrgency.HIGH);
        c3.setCurrentQueueCode("QUEUE_SERVICE_IT");
        c3.setAssignedOperatorEmail(itOp.getEmail());
        c3.setAcknowledgedAt(Instant.now().minus(24, ChronoUnit.HOURS));
        c3.setResolutionProposedAt(Instant.now().minus(1, ChronoUnit.HOURS));
        c3.setResolutionSummary("Access Point AP-BLK-A2 rebooted and RADIUS lease cache cleared. Signal re-verified at 82 Mbps.");
        c3 = complaintRepository.save(c3);

        eventRepository.save(new ComplaintEvent(c3.getId(), "SUBMITTED", student.getEmail(), student.getFullName(), "STUDENT", null, "QUEUE_SERVICE_IT", "Dispatched to Campus IT Helpdesk"));
        eventRepository.save(new ComplaintEvent(c3.getId(), "ACKNOWLEDGED", itOp.getEmail(), itOp.getFullName(), "OPERATOR", "QUEUE_SERVICE_IT", "QUEUE_SERVICE_IT", "NOC ticket opened"));
        eventRepository.save(new ComplaintEvent(c3.getId(), "RESOLUTION_PROPOSED", itOp.getEmail(), itOp.getFullName(), "OPERATOR", "QUEUE_SERVICE_IT", "QUEUE_SERVICE_IT", "RADIUS authentication reset completed"));

        // Scenario E: Srijan Shrivastava (Aryabhatt Room 105)
        Complaint c4 = new Complaint();
        c4.setTrackingNumber("CMP-2026-00104");
        c4.setStudent(srijan);
        c4.setTitle("Desk power socket sparking and loose contact in Room 105");
        c4.setDescription("The primary desk outlet switch in Aryabhatt 105 is sparking whenever a laptop charger is connected. Needs socket plate replacement.");
        c4.setCategory(ComplaintCategory.ELECTRICAL_PLUMBING);
        c4.setLocationType(ComplaintLocationType.HOSTEL);
        c4.setLocationDetail("Aryabhatt Hostel, 1st Floor, Room 105");
        c4.setSpecificHostelCode("ARYABHATT");
        c4.setStatus(ComplaintStatus.ACKNOWLEDGED);
        c4.setUrgency(ComplaintUrgency.MEDIUM);
        c4.setCurrentQueueCode("QUEUE_HOSTEL_ARYABHATT_WARDEN");
        c4.setAssignedOperatorEmail(warden.getEmail());
        c4.setAcknowledgedAt(Instant.now().minus(3, ChronoUnit.HOURS));
        c4 = complaintRepository.save(c4);

        eventRepository.save(new ComplaintEvent(c4.getId(), "SUBMITTED", srijan.getEmail(), srijan.getFullName(), "STUDENT", null, "QUEUE_HOSTEL_ARYABHATT_WARDEN", "Dispatched to Aryabhatt Hostel Warden Queue"));
        eventRepository.save(new ComplaintEvent(c4.getId(), "ACKNOWLEDGED", warden.getEmail(), warden.getFullName(), "OPERATOR", "QUEUE_HOSTEL_ARYABHATT_WARDEN", "QUEUE_HOSTEL_ARYABHATT_WARDEN", "Warden acknowledged. Electrician work order assigned."));
        noteRepository.save(new ComplaintNote(c4.getId(), warden, "Electrician slip #AR-415 assigned. Visit scheduled for 5:30 PM.", false));

        // Scenario F: Ananya Singh (Sarojini Room 204)
        Complaint c5 = new Complaint();
        c5.setTrackingNumber("CMP-2026-00105");
        c5.setStudent(ananya);
        c5.setTitle("Hot water geyser thermostat tripping in Sarojini 2nd Floor Wing");
        c5.setDescription("The common wing geyser on Sarojini 2nd Floor trips the main circuit breaker after 5 minutes of operation. Requires heating element and thermostat check.");
        c5.setCategory(ComplaintCategory.HOSTEL_FACILITY);
        c5.setLocationType(ComplaintLocationType.HOSTEL);
        c5.setLocationDetail("Sarojini Hostel, 2nd Floor, Room 204 Corridor");
        c5.setSpecificHostelCode("SAROJINI");
        c5.setStatus(ComplaintStatus.IN_PROGRESS);
        c5.setUrgency(ComplaintUrgency.HIGH);
        c5.setCurrentQueueCode("QUEUE_HOSTEL_SAROJINI_WARDEN");
        c5.setAssignedOperatorEmail(wardenSarojini.getEmail());
        c5.setAcknowledgedAt(Instant.now().minus(5, ChronoUnit.HOURS));
        c5 = complaintRepository.save(c5);

        eventRepository.save(new ComplaintEvent(c5.getId(), "SUBMITTED", ananya.getEmail(), ananya.getFullName(), "STUDENT", null, "QUEUE_HOSTEL_SAROJINI_WARDEN", "Dispatched to Sarojini Hostel Warden Queue"));
        eventRepository.save(new ComplaintEvent(c5.getId(), "ACKNOWLEDGED", wardenSarojini.getEmail(), wardenSarojini.getFullName(), "OPERATOR", "QUEUE_HOSTEL_SAROJINI_WARDEN", "QUEUE_HOSTEL_SAROJINI_WARDEN", "Hostel Warden verified issue with floor proctor"));
        eventRepository.save(new ComplaintEvent(c5.getId(), "STATUS_CHANGE", wardenSarojini.getEmail(), wardenSarojini.getFullName(), "OPERATOR", "QUEUE_HOSTEL_SAROJINI_WARDEN", "QUEUE_HOSTEL_SAROJINI_WARDEN", "Plumbing & electrical team dispatched for thermostat replacement"));
        noteRepository.save(new ComplaintNote(c5.getId(), wardenSarojini, "Technician dispatched to Sarojini 2nd floor bathroom block.", false));

        // 8. Scenario D: Shared Incident (Sanitized, no student PII)
        SharedIncident incident = new SharedIncident();
        incident.setIncidentNumber("INC-2026-0001");
        incident.setTitle("Scheduled Water Pump Maintenance - Aryabhatt Hostel B-Wing");
        incident.setSanitizedDescription("Routine preventive maintenance and motor pump replacement on the secondary water line servicing Aryabhatt Hostel B-Wing. Municipal pressure backfill expected by 6:00 PM.");
        incident.setCategory(ComplaintCategory.ELECTRICAL_PLUMBING);
        incident.setGeneralLocation("Aryabhatt Hostel - B Wing Utility Shaft");
        incident.setStatus(SharedIncidentStatus.IN_PROGRESS);
        incident.setResponsibleQueueCode("QUEUE_HOSTEL_ARYABHATT_WARDEN");
        incident.setAffectedSubscriberCount(4);
        incident.setPublicProgressUpdates("[2026-10-21 15:30 - Hostel Warden]: New pump motor delivered and electrical coupling is currently being wired by maintenance technicians.");
        incidentRepository.save(incident);

        // 9. Academic Document with Grounded Pages
        AcademicDocument doc = new AcademicDocument();
        doc.setTitle("KAI-601: Artificial Intelligence & Expert Systems - Core Concepts & Search Algorithms");
        doc.setSubjectCode("KAI-601");
        doc.setSubjectName("Artificial Intelligence");
        doc.setDepartmentCode("AIML");
        doc.setSemester(6);
        doc.setOriginalFileName("KAI-601_Unit1_Search_Algorithms.pdf");
        doc.setStoragePath("./storage/academics/demo_kai601.pdf");
        doc.setFileSizeBytes(142000);
        doc.setUploadedBy(admin);
        doc.setPageCount(4);
        doc.setExtracted(true);
        doc = academicDocumentRepository.save(doc);

        academicDocumentPageRepository.save(new AcademicDocumentPage(
                doc.getId(), 1,
                "KIET Group of Institutions, Ghaziabad. Department of AIML. Course: KAI-601 Artificial Intelligence. " +
                "Unit 1: Uninformed vs Informed Search Strategies. Breadth-First Search (BFS) traverses graph level by level with O(b^d) time complexity. " +
                "Depth-First Search (DFS) explores deepest nodes first using LIFO stack structure. Depth-limited search imposes cutoff depth limit l."
        ));

        academicDocumentPageRepository.save(new AcademicDocumentPage(
                doc.getId(), 2,
                "Informed Heuristic Search: A* Search combines path cost g(n) with admissible heuristic estimate h(n): f(n) = g(n) + h(n). " +
                "Admissibility condition: h(n) <= h*(n), meaning heuristic never overestimates the actual cost to reach goal node. " +
                "Consistency / Monotonicity: for every node n and successor n' generated by action a, h(n) <= c(n,a,n') + h(n'). " +
                "If h(n) is consistent, A* using graph search is guaranteed to be optimal."
        ));

        academicDocumentPageRepository.save(new AcademicDocumentPage(
                doc.getId(), 3,
                "Adversarial Search in Multi-Agent Environments: Minimax Algorithm and Alpha-Beta Pruning. " +
                "Alpha is the best value for MAX along the path so far. Beta is the best value for MIN along the path so far. " +
                "Alpha-Beta pruning reduces the branching factor from b to b^(d/2) in the ideal move ordering, allowing twice the search depth."
        ));

        academicDocumentPageRepository.save(new AcademicDocumentPage(
                doc.getId(), 4,
                "Laboratory Evaluation Criteria & Internal Assessment Marks Distribution: " +
                "Continuous Lab Assessment accounts for 30 marks (10 marks for lab record journal, 10 marks for regular practical performance, and 10 marks for viva-voce). " +
                "Mid-term practical exam accounts for 20 marks. End-term University Practical Examination accounts for 50 marks. Total lab credit marks: 100."
        ));

        // 10. Semester 2 Official Core Guide (Engineering Maths & Problem Solving Data Structures)
        AcademicDocument docSem2 = new AcademicDocument();
        docSem2.setTitle("BAS-203 / BCS-201: Engineering Mathematics-II & Data Structures Core Guide");
        docSem2.setSubjectCode("BAS-203");
        docSem2.setSubjectName("Engineering Mathematics-II & Data Structures");
        docSem2.setDepartmentCode("COMMON");
        docSem2.setSemester(2);
        docSem2.setOriginalFileName("KIET_Semester_2_Core_Curriculum.pdf");
        docSem2.setStoragePath("./storage/academics/demo_sem2_core.pdf");
        docSem2.setFileSizeBytes(184320);
        docSem2.setUploadedBy(admin);
        docSem2.setPageCount(3);
        docSem2.setExtracted(true);
        docSem2 = academicDocumentRepository.save(docSem2);

        academicDocumentPageRepository.save(new AcademicDocumentPage(
                docSem2.getId(), 1,
                "KIET Group of Institutions, Ghaziabad. First Year B.Tech Core Curriculum. Course: BAS-203 Engineering Mathematics-II. " +
                "Unit 1: Ordinary Differential Equations of Higher Order. Linear differential equations of second and higher order with constant coefficients. " +
                "Method of variation of parameters, Cauchy-Euler equations, and simultaneous linear differential equations with applications to harmonic oscillators."
        ));

        academicDocumentPageRepository.save(new AcademicDocumentPage(
                docSem2.getId(), 2,
                "Course: BCS-201 Programming for Problem Solving & Data Structures. " +
                "Linear Data Structures: Arrays, singly linked lists, doubly linked lists, circular linked lists. " +
                "Stack operations: Push, Pop, Peek with O(1) time complexity. Applications in infix-to-postfix expression conversion and recursion stack tracing. " +
                "Queue operations: Enqueue, Dequeue using circular array to prevent memory drift."
        ));

        academicDocumentPageRepository.save(new AcademicDocumentPage(
                docSem2.getId(), 3,
                "Course: BCS-201 Unit 3: Non-Linear Data Structures. Binary Trees, Binary Search Trees (BST), and Tree Traversals (Inorder, Preorder, Postorder). " +
                "BST property: For any node n, keys in left subtree are smaller than key(n), and keys in right subtree are greater than key(n). " +
                "Search time complexity: Average O(log n), worst case O(n) in degenerate skewed trees. AVL Tree height balance property ensures O(log n) worst case."
        ));

        log.info("Data initialization complete. Seeded 8 departments, 9 hostels, 25 complaint queues, 11 institutional accounts, 5 complaints, 1 incident, and 2 academic course guides.");
    }

    private void seedOperatorKey(User user, String plaintextKey) {
        String hash = passwordEncoder.encode(plaintextKey);
        String prefix = plaintextKey.substring(0, 8);
        OperatorKey key = new OperatorKey(user, hash, prefix, Instant.now().plus(90, ChronoUnit.DAYS));
        operatorKeyRepository.save(key);
    }
}
