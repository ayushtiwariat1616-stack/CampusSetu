package edu.kiet.campussetu;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kiet.campussetu.auth.AuthController;
import edu.kiet.campussetu.complaint.ComplaintCategory;
import edu.kiet.campussetu.complaint.ComplaintLocationType;
import edu.kiet.campussetu.complaint.ComplaintService;
import edu.kiet.campussetu.complaint.ComplaintUrgency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityAndAuthIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc getMockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Req 1: Non-approved Google domain rejection")
    void testNonApprovedGoogleDomainRejection() throws Exception {
        MockMvc mockMvc = getMockMvc();
        AuthController.GoogleAuthRequest request = new AuthController.GoogleAuthRequest("external.user@gmail.com", "External User");

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Only official @kiet.edu accounts are permitted")));
    }

    @Test
    @DisplayName("Req 2: Unapproved institutional account receives PENDING_ONBOARDING and cannot file complaints")
    void testUnapprovedAccountReceivesPendingOnboarding() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession session = new MockHttpSession();
        AuthController.GoogleAuthRequest request = new AuthController.GoogleAuthRequest("unlisted.student@kiet.edu", "Unlisted Student");

        // 1. Log in via Google with unlisted account
        mockMvc.perform(post("/api/auth/google")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingStatus").value("PENDING_ONBOARDING"));

        // 2. Attempt to file a complaint while in pending state
        ComplaintService.SubmitComplaintRequest complaintReq = new ComplaintService.SubmitComplaintRequest(
                "Broken window",
                "Window handle broken",
                ComplaintCategory.HOSTEL_FACILITY,
                ComplaintLocationType.HOSTEL,
                "Room 101",
                "ARYABHATT",
                null,
                ComplaintUrgency.MEDIUM,
                false
        );

        mockMvc.perform(post("/api/complaints")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaintReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("pending roster verification")));
    }

    @Test
    @DisplayName("Req 3: Student cannot access operator endpoints")
    void testStudentCannotAccessOperatorEndpoints() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession session = new MockHttpSession();

        // Sign in as Ayush (verified student)
        AuthController.DemoLoginRequest loginReq = new AuthController.DemoLoginRequest("STUDENT_ARYABHATT", true);
        mockMvc.perform(post("/api/auth/demo-login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk());

        // Attempt to access operator queue
        mockMvc.perform(get("/api/operator/queue").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Operator privileges are required")));
    }

    @Test
    @DisplayName("Req 4 & 5: Partially authenticated operator cannot access operator queue; invalid key is rejected")
    void testOperatorKeyVerificationAndLockout() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession session = new MockHttpSession();

        // 1. Operator logs in without key verification (Step 1 only)
        AuthController.DemoLoginRequest loginReq = new AuthController.DemoLoginRequest("WARDEN_ARYABHATT", false);
        mockMvc.perform(post("/api/auth/demo-login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operatorKeyVerified").value(false))
                .andExpect(jsonPath("$.data.needsKey").value(true));

        // 2. Accessing operator queue with only Step 1 returns 403
        mockMvc.perform(get("/api/operator/queue").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Operator key verification is required")));

        // 3. Step 2 with incorrect key returns 401
        AuthController.VerifyKeyRequest wrongKeyReq = new AuthController.VerifyKeyRequest("INCORRECT-KEY-12345");
        mockMvc.perform(post("/api/auth/verify-operator-key")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongKeyReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid operator key")));

        // 4. Step 2 with correct key succeeds and unlocks operator queue
        AuthController.VerifyKeyRequest correctKeyReq = new AuthController.VerifyKeyRequest("OPK-ARYA-WARD-2026");
        mockMvc.perform(post("/api/auth/verify-operator-key")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctKeyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operatorKeyVerified").value(true));

        // 5. Operator queue now accessible
        mockMvc.perform(get("/api/operator/queue").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
