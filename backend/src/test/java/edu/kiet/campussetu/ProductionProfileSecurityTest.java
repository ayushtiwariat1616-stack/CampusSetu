package edu.kiet.campussetu;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kiet.campussetu.auth.AuthController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "campussetu.dev-mode.enabled=false"
})
class ProductionProfileSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc getMockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Req 16: Production profile strictly disables demo auth endpoints")
    void testProductionRejectsDemoLogin() throws Exception {
        MockMvc mockMvc = getMockMvc();
        AuthController.DemoLoginRequest req = new AuthController.DemoLoginRequest("STUDENT_ARYABHATT", true);

        mockMvc.perform(post("/api/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Demo authentication is strictly disabled in production")));
    }
}
