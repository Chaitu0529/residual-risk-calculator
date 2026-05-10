package com.internship.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.config.JwtUtil;
import com.internship.tool.dto.RiskRequest;
import com.internship.tool.dto.RiskResponse;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.service.RiskService;
import com.internship.tool.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskController.class)
@DisplayName("RiskController Integration Tests")
class RiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RiskService riskService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private RiskResponse testRiskResponse;
    private RiskRequest testRiskRequest;

    @BeforeEach
    void setUp() {
        testRiskResponse = RiskResponse.builder()
                .id(1L)
                .name("Test Risk")
                .description("Test Description")
                .category("Security")
                .riskScore(75.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("testuser")
                .build();

        testRiskRequest = new RiskRequest("Test Risk", "Test Description", "Security", 75.0);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/risks - Should return paginated risks")
    void getAllRisks_ShouldReturnPagedRisks() throws Exception {
        Page<RiskResponse> page = new PageImpl<>(List.of(testRiskResponse));
        when(riskService.getAllRisks(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/risks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Risk"))
                .andExpect(jsonPath("$.content[0].category").value("Security"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/risks/{id} - Should return risk by ID")
    void getRiskById_ShouldReturnRisk() throws Exception {
        when(riskService.getRiskById(1L)).thenReturn(testRiskResponse);

        mockMvc.perform(get("/api/risks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Risk"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/risks/{id} - Should return 404 when not found")
    void getRiskById_ShouldReturn404_WhenNotFound() throws Exception {
        when(riskService.getRiskById(99L)).thenThrow(new ResourceNotFoundException("Risk", "id", 99L));

        mockMvc.perform(get("/api/risks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/risks - Should create risk and return 201")
    void createRisk_ShouldReturnCreated() throws Exception {
        when(riskService.createRisk(any(RiskRequest.class))).thenReturn(testRiskResponse);

        mockMvc.perform(post("/api/risks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRiskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Risk"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/risks - Should return 400 for invalid input")
    void createRisk_ShouldReturn400_WhenInvalidInput() throws Exception {
        RiskRequest invalidRequest = new RiskRequest("", "", "", -5.0);

        mockMvc.perform(post("/api/risks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/risks/{id} - Should update risk")
    void updateRisk_ShouldReturnUpdatedRisk() throws Exception {
        when(riskService.updateRisk(eq(1L), any(RiskRequest.class))).thenReturn(testRiskResponse);

        mockMvc.perform(put("/api/risks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRiskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Risk"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/risks/{id} - Should delete risk and return 204")
    void deleteRisk_ShouldReturn204() throws Exception {
        doNothing().when(riskService).deleteRisk(1L);

        mockMvc.perform(delete("/api/risks/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/risks/{id} - Should return 403 for non-admin user")
    void deleteRisk_ShouldReturn403_ForNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/risks/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/risks/search - Should return search results")
    void searchRisks_ShouldReturnResults() throws Exception {
        Page<RiskResponse> page = new PageImpl<>(List.of(testRiskResponse));
        when(riskService.searchRisks(eq("Security"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/risks/search")
                        .param("q", "Security"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("Security"));
    }
}
