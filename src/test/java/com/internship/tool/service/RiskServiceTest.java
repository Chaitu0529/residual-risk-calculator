package com.internship.tool.service;

import com.internship.tool.dto.RiskRequest;
import com.internship.tool.dto.RiskResponse;
import com.internship.tool.entity.Risk;
import com.internship.tool.entity.User;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.RiskRepository;
import com.internship.tool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RiskService Unit Tests")
class RiskServiceTest {

    @Mock
    private RiskRepository riskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RiskService riskService;

    private User testUser;
    private Risk testRisk;
    private RiskRequest testRiskRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.Role.USER)
                .isActive(true)
                .build();

        testRisk = Risk.builder()
                .id(1L)
                .name("Test Risk")
                .description("Test Description")
                .category("Security")
                .riskScore(75.0)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(testUser)
                .build();

        testRiskRequest = new RiskRequest("Test Risk", "Test Description", "Security", 75.0);
    }

    @Test
    @DisplayName("Should return paginated list of risks")
    void getAllRisks_ShouldReturnPagedRisks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Risk> riskPage = new PageImpl<>(List.of(testRisk));
        when(riskRepository.findByIsDeletedFalse(pageable)).thenReturn(riskPage);

        Page<RiskResponse> result = riskService.getAllRisks(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Risk");
        verify(riskRepository, times(1)).findByIsDeletedFalse(pageable);
    }

    @Test
    @DisplayName("Should return risk by ID")
    void getRiskById_ShouldReturnRisk_WhenExists() {
        when(riskRepository.findById(1L)).thenReturn(Optional.of(testRisk));

        RiskResponse result = riskService.getRiskById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Risk");
        assertThat(result.getCategory()).isEqualTo("Security");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when risk not found")
    void getRiskById_ShouldThrowException_WhenNotFound() {
        when(riskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskService.getRiskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Risk");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for soft-deleted risk")
    void getRiskById_ShouldThrowException_WhenSoftDeleted() {
        testRisk.setIsDeleted(true);
        when(riskRepository.findById(1L)).thenReturn(Optional.of(testRisk));

        assertThatThrownBy(() -> riskService.getRiskById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should create risk successfully")
    void createRisk_ShouldCreateAndReturnRisk() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(riskRepository.save(any(Risk.class))).thenReturn(testRisk);
        doNothing().when(emailService).sendRiskCreatedEmail(anyString(), any(Risk.class));

        RiskResponse result = riskService.createRisk(testRiskRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Risk");
        verify(riskRepository, times(1)).save(any(Risk.class));
        verify(emailService, times(1)).sendRiskCreatedEmail(anyString(), any(Risk.class));
    }

    @Test
    @DisplayName("Should update risk successfully")
    void updateRisk_ShouldUpdateAndReturnRisk() {
        RiskRequest updateRequest = new RiskRequest("Updated Risk", "Updated Desc", "Compliance", 50.0);
        Risk updatedRisk = Risk.builder()
                .id(1L)
                .name("Updated Risk")
                .description("Updated Desc")
                .category("Compliance")
                .riskScore(50.0)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(testUser)
                .build();

        when(riskRepository.findById(1L)).thenReturn(Optional.of(testRisk));
        when(riskRepository.save(any(Risk.class))).thenReturn(updatedRisk);

        RiskResponse result = riskService.updateRisk(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Risk");
        assertThat(result.getCategory()).isEqualTo("Compliance");
        verify(riskRepository, times(1)).save(any(Risk.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent risk")
    void updateRisk_ShouldThrowException_WhenNotFound() {
        when(riskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskService.updateRisk(99L, testRiskRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should soft delete risk successfully")
    void deleteRisk_ShouldSoftDeleteRisk() {
        when(riskRepository.findById(1L)).thenReturn(Optional.of(testRisk));
        when(riskRepository.save(any(Risk.class))).thenReturn(testRisk);

        riskService.deleteRisk(1L);

        verify(riskRepository, times(1)).save(any(Risk.class));
        assertThat(testRisk.getIsDeleted()).isTrue();
        assertThat(testRisk.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent risk")
    void deleteRisk_ShouldThrowException_WhenNotFound() {
        when(riskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskService.deleteRisk(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should search risks by query")
    void searchRisks_ShouldReturnMatchingRisks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Risk> riskPage = new PageImpl<>(List.of(testRisk));
        when(riskRepository.searchRisks("Security", pageable)).thenReturn(riskPage);

        Page<RiskResponse> result = riskService.searchRisks("Security", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("Security");
    }

    @Test
    @DisplayName("Should return empty page when no risks match search")
    void searchRisks_ShouldReturnEmptyPage_WhenNoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Risk> emptyPage = new PageImpl<>(List.of());
        when(riskRepository.searchRisks("nonexistent", pageable)).thenReturn(emptyPage);

        Page<RiskResponse> result = riskService.searchRisks("nonexistent", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should map risk entity to response correctly")
    void createRisk_ShouldMapAllFieldsCorrectly() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(riskRepository.save(any(Risk.class))).thenReturn(testRisk);
        doNothing().when(emailService).sendRiskCreatedEmail(anyString(), any(Risk.class));

        RiskResponse result = riskService.createRisk(testRiskRequest);

        assertThat(result.getId()).isEqualTo(testRisk.getId());
        assertThat(result.getName()).isEqualTo(testRisk.getName());
        assertThat(result.getDescription()).isEqualTo(testRisk.getDescription());
        assertThat(result.getCategory()).isEqualTo(testRisk.getCategory());
        assertThat(result.getRiskScore()).isEqualTo(testRisk.getRiskScore());
        assertThat(result.getCreatedBy()).isEqualTo(testUser.getUsername());
    }
}
