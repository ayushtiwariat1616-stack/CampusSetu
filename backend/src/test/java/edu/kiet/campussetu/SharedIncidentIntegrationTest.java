package edu.kiet.campussetu;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kiet.campussetu.auth.AuthController;
import edu.kiet.campussetu.complaint.*;
import edu.kiet.campussetu.incident.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SharedIncidentIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SharedIncidentRepository incidentRepository;

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
    @DisplayName("Req 11: Shared incident subscription is unique, toggles cleanly, and reveals zero student PII")
    void testSharedIncidentSubscriptionAndPrivacy() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession wardenSession = loginAs(mockMvc, "WARDEN_ARYABHATT");

        // 1. Create a shared incident
        SharedIncidentService.CreateSharedIncidentRequest createReq = new SharedIncidentService.CreateSharedIncidentRequest(
                "Aryabhatt Overhead Tank Booster Pump Tripped",
                "Water pressure dropped across 2nd and 3rd floor west wing. Electrician inspecting starter relay.",
                ComplaintCategory.HOSTEL_FACILITY,
                "Aryabhatt Hostel West Wing",
                "QUEUE_HOSTEL_ARYABHATT"
        );

        String createJson = mockMvc.perform(post("/api/incidents")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.affectedSubscriberCount").value(0))
                .andReturn().getResponse().getContentAsString();

        Long incidentId = objectMapper.readTree(createJson).get("data").get("id").asLong();

        // 2. Student subscribes ("I am affected too")
        MockHttpSession studentSession = loginAs(mockMvc, "STUDENT_ARYABHATT");
        mockMvc.perform(post("/api/incidents/" + incidentId + "/subscribe").session(studentSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        // 3. Verify public incident list has count = 1 and contains NO student roll numbers or room numbers
        MvcResult listResult = mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + incidentId + ")].affectedSubscriberCount").value(1))
                .andReturn();

        String listContent = listResult.getResponse().getContentAsString();
        assertFalse(listContent.contains("25004108"), "Incident list must not contain student roll number");
        assertFalse(listContent.contains("ayush.25004108"), "Incident list must not contain student email");
        assertFalse(listContent.contains("Room 214"), "Incident list must not disclose complainant room number");

        // 4. Student toggles subscription again -> un-subscribes cleanly
        mockMvc.perform(post("/api/incidents/" + incidentId + "/subscribe").session(studentSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + incidentId + ")].affectedSubscriberCount").value(0));
    }

    @Test
    @DisplayName("Req 12: Resolving a shared incident does NOT auto-close private individual complaints")
    void testResolvingIncidentDoesNotClosePrivateComplaints() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession studentSession = loginAs(mockMvc, "STUDENT_ARYABHATT");
        MockHttpSession wardenSession = loginAs(mockMvc, "WARDEN_ARYABHATT");

        // 1. Student files private complaint about low water pressure in Room 214
        ComplaintService.SubmitComplaintRequest complaintReq = new ComplaintService.SubmitComplaintRequest(
                "No water pressure in washroom",
                "Taps are running dry since morning.",
                ComplaintCategory.HOSTEL_FACILITY,
                ComplaintLocationType.HOSTEL,
                "Aryabhatt Hostel Room 214",
                "ARYABHATT",
                null,
                ComplaintUrgency.HIGH,
                false
        );

        String compJson = mockMvc.perform(post("/api/complaints")
                        .session(studentSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaintReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long complaintId = objectMapper.readTree(compJson).get("data").get("id").asLong();

        // 2. Warden creates community incident for water outage
        SharedIncidentService.CreateSharedIncidentRequest incReq = new SharedIncidentService.CreateSharedIncidentRequest(
                "Aryabhatt Main Water Inflow Pipeline Valve Maintenance",
                "Municipal feeder supply shut down for pipeline repair.",
                ComplaintCategory.HOSTEL_FACILITY,
                "Aryabhatt Hostel Complex",
                "QUEUE_HOSTEL_ARYABHATT"
        );

        String incJson = mockMvc.perform(post("/api/incidents")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long incidentId = objectMapper.readTree(incJson).get("data").get("id").asLong();

        // 3. Warden links the student's complaint to this shared incident
        IncidentController.LinkComplaintRequest linkReq = new IncidentController.LinkComplaintRequest(complaintId);
        mockMvc.perform(post("/api/incidents/" + incidentId + "/link-complaint")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkReq)))
                .andExpect(status().isOk());

        // 4. Warden resolves the shared community incident
        IncidentController.UpdateProgressRequest resolveReq = new IncidentController.UpdateProgressRequest(
                SharedIncidentStatus.RESOLVED,
                "Main line pressure restored to 4.2 bar at 15:30."
        );
        mockMvc.perform(post("/api/incidents/" + incidentId + "/update-progress")
                        .session(wardenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        // 5. CRITICAL CHECK: The private complaint MUST NOT be automatically CLOSED!
        Complaint privateComplaint = complaintRepository.findById(complaintId).orElseThrow();
        assertNotEquals(ComplaintStatus.CLOSED, privateComplaint.getStatus(),
                "Resolving shared incident must NOT auto-close student's private complaint without their confirmation!");
        assertNull(privateComplaint.getClosedAt());
    }
}
