package edu.kiet.campussetu;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kiet.campussetu.auth.AuthController;
import edu.kiet.campussetu.complaint.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ComplaintRoutingAndLifecycleIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ComplaintRepository complaintRepository;

    private MockMvc getMockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private MockHttpSession loginAs(MockMvc mockMvc, String personaKey) throws Exception {
        MockHttpSession session = new MockHttpSession();
        AuthController.DemoLoginRequest loginReq = new AuthController.DemoLoginRequest(personaKey, true);
        mockMvc.perform(post("/api/auth/demo-login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk());
        return session;
    }

    @Test
    @DisplayName("Req 6: Aryabhatt Hostel complaint routes strictly to Aryabhatt Warden Queue")
    void testAryabhattComplaintRouting() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession studentSession = loginAs(mockMvc, "STUDENT_ARYABHATT");

        ComplaintService.SubmitComplaintRequest req = new ComplaintService.SubmitComplaintRequest(
                "Geyser electrical trip in Room 214",
                "Water heater trips the MCB whenever switched on.",
                ComplaintCategory.HOSTEL_FACILITY,
                ComplaintLocationType.HOSTEL,
                "Aryabhatt Hostel Room 214",
                "ARYABHATT",
                null,
                ComplaintUrgency.HIGH,
                false
        );

        mockMvc.perform(post("/api/complaints")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentQueueCode").value("QUEUE_HOSTEL_ARYABHATT_WARDEN"));

        // Verify Aryabhatt Warden sees it in their queue
        MockHttpSession wardenSession = loginAs(mockMvc, "WARDEN_ARYABHATT");
        mockMvc.perform(get("/api/operator/queue").session(wardenSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.title == 'Geyser electrical trip in Room 214')]").exists());

        // Verify AIML Dept Incharge does NOT see it in their queue
        MockHttpSession aimlSession = loginAs(mockMvc, "OPERATOR_AIML");
        mockMvc.perform(get("/api/operator/queue").session(aimlSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.title == 'Geyser electrical trip in Room 214')]").doesNotExist());
    }

    @Test
    @DisplayName("Req 7: Cross-department complaint routes to physical location queue (ME workshop), isolated from AIML")
    void testCrossDepartmentRouting() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession studentSession = loginAs(mockMvc, "STUDENT_ARYABHATT");

        // AIML student encounters a safety issue in Mechanical Department Workshop
        ComplaintService.SubmitComplaintRequest req = new ComplaintService.SubmitComplaintRequest(
                "Damaged belt guard on Lathe Machine 4",
                "Safety guard on lathe machine 4 is loose and vibrating.",
                ComplaintCategory.LAB_EQUIPMENT,
                ComplaintLocationType.DEPARTMENT,
                "Mechanical Engineering Central Workshop, Bay 2",
                null,
                "ME",
                ComplaintUrgency.HIGH,
                false
        );

        mockMvc.perform(post("/api/complaints")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentQueueCode").value("QUEUE_DEPT_ME"));

        // AIML Operator should NOT see ME department complaints
        MockHttpSession aimlSession = loginAs(mockMvc, "OPERATOR_AIML");
        mockMvc.perform(get("/api/operator/queue").session(aimlSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.title == 'Damaged belt guard on Lathe Machine 4')]").doesNotExist());
    }

    @Test
    @DisplayName("Req 8: Central service complaints route strictly to central service queue")
    void testCentralServiceRouting() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession studentSession = loginAs(mockMvc, "STUDENT_ARYABHATT");

        ComplaintService.SubmitComplaintRequest req = new ComplaintService.SubmitComplaintRequest(
                "Central Library WiFi AP offline",
                "Access point KIET-STUDENT-LIB-02 shows blinking red LED.",
                ComplaintCategory.IT_AND_NETWORK,
                ComplaintLocationType.CAMPUS_FACILITY,
                "Central Knowledge Park Library 2nd Floor",
                null,
                null,
                ComplaintUrgency.MEDIUM,
                false
        );

        mockMvc.perform(post("/api/complaints")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentQueueCode").value("QUEUE_SERVICE_IT"));

        // Campus IT Desk should see it
        MockHttpSession itSession = loginAs(mockMvc, "OPERATOR_IT");
        mockMvc.perform(get("/api/operator/queue").session(itSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.title == 'Central Library WiFi AP offline')]").exists());
    }

    @Test
    @DisplayName("Req 9: Escalation follows configured chain (Warden -> Rector -> Chief Rector) and preserves history")
    void testEscalationHierarchyAndHistory() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession studentSession = loginAs(mockMvc, "STUDENT_ARYABHATT");

        ComplaintService.SubmitComplaintRequest req = new ComplaintService.SubmitComplaintRequest(
                "Water cooler compressor burnt",
                "Water cooler stopped cooling and smells of burnt insulation.",
                ComplaintCategory.HOSTEL_FACILITY,
                ComplaintLocationType.HOSTEL,
                "Aryabhatt Hostel 1st Floor Wing B",
                "ARYABHATT",
                null,
                ComplaintUrgency.HIGH,
                false
        );

        String json = mockMvc.perform(post("/api/complaints")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long complaintId = objectMapper.readTree(json).get("data").get("id").asLong();

        // 1. Aryabhatt Warden escalates to Rector
        MockHttpSession wardenSession = loginAs(mockMvc, "WARDEN_ARYABHATT");
        OperatorController.EscalateRequest escReq1 = new OperatorController.EscalateRequest(
                "Requires vendor contract replacement above warden financial threshold."
        );
        mockMvc.perform(post("/api/operator/complaints/" + complaintId + "/escalate")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(escReq1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentQueueCode").value("QUEUE_HOSTEL_ARYABHATT_RECTOR"));

        // 2. Aryabhatt Rector escalates to Chief Rector
        MockHttpSession rectorSession = loginAs(mockMvc, "RECTOR_ARYABHATT");
        OperatorController.EscalateRequest escReq2 = new OperatorController.EscalateRequest(
                "Campus-wide refrigeration vendor tender required."
        );
        mockMvc.perform(post("/api/operator/complaints/" + complaintId + "/escalate")
                        .session(rectorSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(escReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentQueueCode").value("QUEUE_CHIEF_RECTOR"));

        // 3. Verify complete immutable timeline is preserved
        mockMvc.perform(get("/api/complaints/" + complaintId).session(studentSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events[?(@.eventType == 'ESCALATED')]").isNotEmpty());
    }

    @Test
    @DisplayName("Req 10: Internal operator notes are strictly hidden from student queries")
    void testInternalNotesPrivacy() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession studentSession = loginAs(mockMvc, "STUDENT_ARYABHATT");

        ComplaintService.SubmitComplaintRequest req = new ComplaintService.SubmitComplaintRequest(
                "Ceiling plaster peeling off",
                "Plaster pieces falling near desk.",
                ComplaintCategory.HOSTEL_FACILITY,
                ComplaintLocationType.HOSTEL,
                "Aryabhatt Hostel Room 214",
                "ARYABHATT",
                null,
                ComplaintUrgency.MEDIUM,
                false
        );

        String json = mockMvc.perform(post("/api/complaints")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long complaintId = objectMapper.readTree(json).get("data").get("id").asLong();

        // Operator adds an internal confidential note (e.g. contractor quotation and private phone)
        MockHttpSession wardenSession = loginAs(mockMvc, "WARDEN_ARYABHATT");
        OperatorController.InternalNoteRequest internalNote = new OperatorController.InternalNoteRequest(
                "CONFIDENTIAL: Mason Contractor Sharma (+91-9876543210) estimated quote INR 2400 for ceiling patch."
        );
        mockMvc.perform(post("/api/operator/complaints/" + complaintId + "/internal-note")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(internalNote)))
                .andExpect(status().isOk());

        // Student views complaint: Internal note MUST NOT appear
        mockMvc.perform(get("/api/complaints/" + complaintId).session(studentSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notes[?(@.internalOnly == true)]").doesNotExist())
                .andExpect(jsonPath("$.data.notes[?(@.content =~ /.*Sharma.*/)]").doesNotExist());

        // Authorized Operator views complaint: Internal note MUST appear
        mockMvc.perform(get("/api/complaints/" + complaintId).session(wardenSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notes[?(@.internalOnly == true)]").exists())
                .andExpect(jsonPath("$.data.notes[?(@.content =~ /.*Sharma.*/)]").exists());
    }

    @Test
    @DisplayName("Req 13: Student confirmed closure lifecycle and resolution rejection/reopen")
    void testStudentConfirmedClosureAndRejectionLifecycle() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession studentSession = loginAs(mockMvc, "STUDENT_ARYABHATT");

        ComplaintService.SubmitComplaintRequest req = new ComplaintService.SubmitComplaintRequest(
                "Door latch broken",
                "Main door latch is misaligned.",
                ComplaintCategory.HOSTEL_FACILITY,
                ComplaintLocationType.HOSTEL,
                "Aryabhatt Hostel Room 214",
                "ARYABHATT",
                null,
                ComplaintUrgency.MEDIUM,
                false
        );

        String json = mockMvc.perform(post("/api/complaints")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long complaintId = objectMapper.readTree(json).get("data").get("id").asLong();

        // 1. Warden Acknowledges
        MockHttpSession wardenSession = loginAs(mockMvc, "WARDEN_ARYABHATT");
        OperatorController.ActionMessageRequest ackReq = new OperatorController.ActionMessageRequest("Carpentry technician assigned.");
        mockMvc.perform(post("/api/operator/complaints/" + complaintId + "/acknowledge")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ackReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACKNOWLEDGED"));

        // 2. Warden Marks In Progress
        OperatorController.ActionMessageRequest progReq = new OperatorController.ActionMessageRequest("Carpenter inspecting strike plate.");
        mockMvc.perform(post("/api/operator/complaints/" + complaintId + "/in-progress")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // 3. Warden Proposes Resolution (Ticket MUST remain open as RESOLUTION_PROPOSED, not CLOSED)
        OperatorController.ProposeResolutionRequest propReq = new OperatorController.ProposeResolutionRequest(
                "Replaced screws and realigned latch strike plate."
        );
        mockMvc.perform(post("/api/operator/complaints/" + complaintId + "/propose-resolution")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLUTION_PROPOSED"));

        Complaint complaintAfterProposal = complaintRepository.findById(complaintId).orElseThrow();
        assertEquals(ComplaintStatus.RESOLUTION_PROPOSED, complaintAfterProposal.getStatus());
        assertNull(complaintAfterProposal.getClosedAt(), "Ticket must not be closed without student confirmation");

        // 4. Student rejects resolution because latch still sticks
        ComplaintController.RejectResolutionRequest rejReq = new ComplaintController.RejectResolutionRequest(
                "The latch still sticks when pulling firmly. Please adjust strike plate depth."
        );
        mockMvc.perform(post("/api/complaints/" + complaintId + "/reject-resolution")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // 5. Operator re-fixes and proposes resolution again
        OperatorController.ProposeResolutionRequest propReq2 = new OperatorController.ProposeResolutionRequest(
                "Chiseled 2mm deeper mortise pocket and lubricated latch."
        );
        mockMvc.perform(post("/api/operator/complaints/" + complaintId + "/propose-resolution")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLUTION_PROPOSED"));

        // 6. Student confirms resolution -> Now ticket becomes CLOSED
        ComplaintController.ConfirmResolutionRequest confReq = new ComplaintController.ConfirmResolutionRequest(
                "Works smoothly now. Thanks!"
        );
        mockMvc.perform(post("/api/complaints/" + complaintId + "/confirm-resolution")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));

        Complaint closedComplaint = complaintRepository.findById(complaintId).orElseThrow();
        assertEquals(ComplaintStatus.CLOSED, closedComplaint.getStatus());
        assertNotNull(closedComplaint.getClosedAt());
        assertNotNull(closedComplaint.getClosureNotes());
    }
}
