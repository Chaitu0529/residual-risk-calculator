package com.internship.tool.service;

import com.internship.tool.entity.Risk;
import com.internship.tool.repository.RiskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final RiskRepository riskRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    public void sendWelcomeEmail(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to Residual Risk Calculator");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                    "Welcome to Residual Risk Calculator!\n\n" +
                    "Your account has been created successfully.\n\n" +
                    "Best regards,\n" +
                    "Risk Calculator Team",
                    username
            ));
            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    public void sendRiskCreatedEmail(String to, Risk risk) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("New Risk Created: " + risk.getName());
            message.setText(String.format(
                    "A new risk has been created:\n\n" +
                    "Name: %s\n" +
                    "Category: %s\n" +
                    "Risk Score: %.2f\n" +
                    "Description: %s\n\n" +
                    "Best regards,\n" +
                    "Risk Calculator Team",
                    risk.getName(),
                    risk.getCategory(),
                    risk.getRiskScore(),
                    risk.getDescription()
            ));
            mailSender.send(message);
            log.info("Risk creation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send risk creation email to {}: {}", to, e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyRiskSummary() {
        try {
            long totalRisks = riskRepository.countActiveRisks();
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmail);
            message.setSubject("Daily Risk Summary");
            message.setText(String.format(
                    "Daily Risk Summary Report\n\n" +
                    "Total Active Risks: %d\n\n" +
                    "This is your automated daily reminder.\n\n" +
                    "Best regards,\n" +
                    "Risk Calculator System",
                    totalRisks
            ));
            mailSender.send(message);
            log.info("Daily risk summary sent to admin: {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to send daily risk summary: {}", e.getMessage());
        }
    }
}
