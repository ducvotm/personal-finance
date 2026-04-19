package com.example.finance.controller;

import com.example.finance.dto.response.AiAssistantResponse;
import com.example.finance.security.JwtAuthenticationFilter;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.AiAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiAssistantController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiAssistantService aiAssistantService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void query_ReturnsAssistantResponse() throws Exception {
        AiAssistantResponse response = AiAssistantResponse.builder().answer("Be disciplined.")
                .usedStartDate(LocalDate.of(2026, 4, 1)).usedEndDate(LocalDate.of(2026, 4, 30))
                .highlights(List.of("Net balance in range: 100")).citations(List.of("The Psychology of Money (p. 4)"))
                .build();
        when(aiAssistantService.query(eq(1L), any())).thenReturn(response);

        UserPrincipal principal = new UserPrincipal(1L, "user", "USER", "u@x.com", "pass");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());

        mockMvc.perform(post("/api/ai/assistant/query").principal(auth).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("question", "How much am I spending?",
                        "startDate", "2026-04-01", "endDate", "2026-04-30"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answer").value("Be disciplined."))
                .andExpect(jsonPath("$.data.citations[0]").value("The Psychology of Money (p. 4)"));
    }
}
