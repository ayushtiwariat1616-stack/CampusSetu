package edu.kiet.campussetu;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kiet.campussetu.academic.AcademicController;
import edu.kiet.campussetu.academic.AcademicDocument;
import edu.kiet.campussetu.academic.AcademicDocumentRepository;
import edu.kiet.campussetu.auth.AuthController;
import edu.kiet.campussetu.user.User;
import edu.kiet.campussetu.user.UserRepository;
import edu.kiet.campussetu.user.UserRole;
import edu.kiet.campussetu.user.OnboardingStatus;
import edu.kiet.campussetu.user.RosterEntry;
import edu.kiet.campussetu.user.RosterEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AcademicDocumentIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AcademicDocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RosterEntryRepository rosterEntryRepository;

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
    @DisplayName("Req 14: Academic document retrieval respects department scopes (AIML vs ME)")
    void testDepartmentScopedAcademicDocumentRetrieval() throws Exception {
        MockMvc mockMvc = getMockMvc();

        // 1. Log in as AIML student (Ayush) -> should see seeded AIML document
        MockHttpSession aimlSession = loginAs(mockMvc, "STUDENT_ARYABHATT");
        mockMvc.perform(get("/api/academics/documents").session(aimlSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.departmentCode == 'AIML')]").exists());

        // 2. Create a verified ME student (with matching roster entry so Google login approves user)
        rosterEntryRepository.findByEmailIgnoreCase("me.student@kiet.edu").orElseGet(() ->
                rosterEntryRepository.save(new RosterEntry(
                        "me.student@kiet.edu", "Rohan ME", "STUDENT", "ME", null, null, "2200970100099", null
                ))
        );

        User meStudent = userRepository.findByEmailIgnoreCase("me.student@kiet.edu").orElseGet(() -> {
            User u = new User();
            u.setEmail("me.student@kiet.edu");
            u.setFullName("Rohan ME");
            u.setDepartmentCode("ME");
            u.setPrimaryRole(UserRole.STUDENT);
            u.setOnboardingStatus(OnboardingStatus.APPROVED);
            return userRepository.save(u);
        });

        MockHttpSession meSession = new MockHttpSession();
        AuthController.GoogleAuthRequest authReq = new AuthController.GoogleAuthRequest(meStudent.getEmail(), meStudent.getFullName());
        mockMvc.perform(post("/api/auth/google")
                        .session(meSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isOk());

        // 3. ME student lists documents: AIML document MUST NOT be visible!
        mockMvc.perform(get("/api/academics/documents").session(meSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.departmentCode == 'AIML')]").doesNotExist());
    }

    @Test
    @DisplayName("Req 15: Grounded Q&A returns page citations; transparently indicates AI unavailable state when key not configured")
    void testGroundedQACitationsAndTransparentAIFallback() throws Exception {
        MockMvc mockMvc = getMockMvc();
        MockHttpSession aimlSession = loginAs(mockMvc, "STUDENT_ARYABHATT");

        // Ask a question covered in the seeded syllabus (e.g. Operating Systems / Lab Evaluation)
        AcademicController.AskQuestionRequest req = new AcademicController.AskQuestionRequest(
                "What are the laboratory evaluation criteria and marks distribution?"
        );

        String json = mockMvc.perform(post("/api/academics/ask")
                        .session(aimlSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aiConfigured").value(false))
                .andExpect(jsonPath("$.data.answer").value(org.hamcrest.Matchers.containsString("AI-assisted generative answering is currently unavailable")))
                .andExpect(jsonPath("$.data.citations").isNotEmpty())
                .andExpect(jsonPath("$.data.citations[0].pageNumber").isNumber())
                .andReturn().getResponse().getContentAsString();

        // Verify that exact citations are returned rather than fabricated hallucinated answers
        assertTrue(json.contains("citations"), "Grounded Q&A response must include citations");
        assertTrue(json.contains("pageNumber"), "Citations must include page numbers");
    }
}
