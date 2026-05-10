package com.internship.tool.service;

import com.internship.tool.entity.Risk;
import com.internship.tool.repository.RiskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RiskRepository riskRepository;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "adminEmail", "admin@test.com");
    }

    @Test
    @DisplayName("Should send welcome email with correct subject and recipient")
    void sendWelcomeEmail_ShouldSendCorrectEmail() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendWelcomeEmail("user@example.com", "johndoe");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).contains("user@example.com");
        assertThat(sent.getSubject()).isEqualTo("Welcome to Residual Risk Calculator");
        assertThat(sent.getText()).contains("johndoe");
    }

    @Test
    @DisplayName("Should send risk-created email with risk details")
    void sendRiskCreatedEmail_ShouldIncludeRiskDetails() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        Risk risk = Risk.builder()
                .name("SQL Injection")
                .category("Security")
                .riskScore(85.0)
                .description("Unsanitized input")
                .build();

        emailService.sendRiskCreatedEmail("user@example.com", risk);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getSubject()).contains("SQL Injection");
        assertThat(sent.getText()).contains("Security");
        assertThat(sent.getText()).contains("85.00");
    }

    @Test
    @DisplayName("Should not throw when welcome email fails")
    void sendWelcomeEmail_ShouldNotThrow_WhenMailFails() {
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Must not propagate the exception
        emailService.sendWelcomeEmail("user@example.com", "johndoe");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should not throw when risk-created email fails")
    void sendRiskCreatedEmail_ShouldNotThrow_WhenMailFails() {
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        Risk risk = Risk.builder()
                .name("Test")
                .category("Security")
                .riskScore(50.0)
                .description("desc")
                .build();

        emailService.sendRiskCreatedEmail("user@example.com", risk);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send daily summary to admin with correct risk count")
    void sendDailyRiskSummary_ShouldSendToAdmin_WithRiskCount() {
        when(riskRepository.countActiveRisks()).thenReturn(18L);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendDailyRiskSummary();

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).contains("admin@test.com");
        assertThat(sent.getSubject()).isEqualTo("Daily Risk Summary");
        assertThat(sent.getText()).contains("18");
    }

    @Test
    @DisplayName("Should not throw when daily summary email fails")
    void sendDailyRiskSummary_ShouldNotThrow_WhenMailFails() {
        when(riskRepository.countActiveRisks()).thenReturn(5L);
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendDailyRiskSummary();

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
