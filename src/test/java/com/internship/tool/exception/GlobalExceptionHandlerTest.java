package com.internship.tool.exception;

import com.internship.tool.config.JwtUtil;
import com.internship.tool.controller.RiskController;
import com.internship.tool.service.RiskService;
import com.internship.tool.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskController.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiskService riskService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 404 with ErrorResponse when resource not found")
    void shouldReturn404_WhenResourceNotFound() throws Exception {
        when(riskService.getRiskById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Risk", "id", 999L));

        mockMvc.perform(get("/api/risks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 400 with field errors when validation fails")
    void shouldReturn400_WithFieldErrors_WhenValidationFails() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "description": "desc",
                  "category": "",
                  "riskScore": -10.0
                }
                """;

        mockMvc.perform(post("/api/risks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.category").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 400 when required query param is missing")
    void shouldReturn400_WhenRequiredParamMissing() throws Exception {
        // /api/risks/search requires ?q= param
        mockMvc.perform(get("/api/risks/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Required parameter 'q' is missing"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 403 when USER tries to delete (ADMIN only)")
    void shouldReturn403_WhenUserTriesToDelete() throws Exception {
        mockMvc.perform(delete("/api/risks/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 409 when duplicate resource exception thrown")
    void shouldReturn409_WhenDuplicateResource() throws Exception {
        when(riskService.createRisk(any()))
                .thenThrow(new DuplicateResourceException("Risk with this name already exists"));

        String validJson = """
                {
                  "name": "Duplicate Risk",
                  "description": "desc",
                  "category": "Security",
                  "riskScore": 50.0
                }
                """;

        mockMvc.perform(post("/api/risks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 500 for unexpected exceptions")
    void shouldReturn500_ForUnexpectedException() throws Exception {
        when(riskService.getAllRisks(any(Pageable.class)))
                .thenThrow(new RuntimeException("Unexpected DB failure"));

        mockMvc.perform(get("/api/risks"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
