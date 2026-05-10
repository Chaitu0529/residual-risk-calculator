package com.internship.tool.service;

import com.internship.tool.dto.PageResponse;
import com.internship.tool.dto.RiskRequest;
import com.internship.tool.dto.RiskResponse;
import com.internship.tool.entity.Risk;
import com.internship.tool.entity.User;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.RiskRepository;
import com.internship.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final RiskRepository riskRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Cache key includes sort so different orderings don't collide
    @Cacheable(value = "risks",
               key = "'all_p' + #pageable.pageNumber + '_s' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public PageResponse<RiskResponse> getAllRisks(Pageable pageable) {
        log.info("Fetching all risks - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<RiskResponse> page = riskRepository.findByIsDeletedFalse(pageable)
                .map(this::mapToResponse);
        return PageResponse.from(page);
    }

    @Cacheable(value = "risks", key = "#id")
    @Transactional(readOnly = true)
    public RiskResponse getRiskById(Long id) {
        log.info("Fetching risk by id: {}", id);
        Risk risk = riskRepository.findById(id)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Risk", "id", id));
        return mapToResponse(risk);
    }

    @Cacheable(value = "risks",
               key = "'search_' + #query + '_p' + #pageable.pageNumber + '_s' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PageResponse<RiskResponse> searchRisks(String query, Pageable pageable) {
        log.info("Searching risks with query: {}", query);
        Page<RiskResponse> page = riskRepository.searchRisks(query, pageable)
                .map(this::mapToResponse);
        return PageResponse.from(page);
    }

    @CacheEvict(value = "risks", allEntries = true)
    @Transactional
    public RiskResponse createRisk(RiskRequest request) {
        log.info("Creating new risk: {}", request.getName());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Risk risk = Risk.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .riskScore(request.getRiskScore())
                .createdBy(user)
                .build();

        Risk savedRisk = riskRepository.save(risk);
        log.info("Risk created successfully with id: {}", savedRisk.getId());

        emailService.sendRiskCreatedEmail(user.getEmail(), savedRisk);

        return mapToResponse(savedRisk);
    }

    @CacheEvict(value = "risks", allEntries = true)
    @Transactional
    public RiskResponse updateRisk(Long id, RiskRequest request) {
        log.info("Updating risk with id: {}", id);

        Risk risk = riskRepository.findById(id)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Risk", "id", id));

        risk.setName(request.getName());
        risk.setDescription(request.getDescription());
        risk.setCategory(request.getCategory());
        risk.setRiskScore(request.getRiskScore());

        Risk updatedRisk = riskRepository.save(risk);
        log.info("Risk updated successfully: {}", id);

        return mapToResponse(updatedRisk);
    }

    @CacheEvict(value = "risks", allEntries = true)
    @Transactional
    public void deleteRisk(Long id) {
        log.info("Soft deleting risk with id: {}", id);

        Risk risk = riskRepository.findById(id)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Risk", "id", id));

        risk.setIsDeleted(true);
        risk.setDeletedAt(LocalDateTime.now());
        riskRepository.save(risk);

        log.info("Risk soft deleted successfully: {}", id);
    }

    private RiskResponse mapToResponse(Risk risk) {
        return RiskResponse.builder()
                .id(risk.getId())
                .name(risk.getName())
                .description(risk.getDescription())
                .category(risk.getCategory())
                .riskScore(risk.getRiskScore())
                .createdAt(risk.getCreatedAt())
                .updatedAt(risk.getUpdatedAt())
                .createdBy(risk.getCreatedBy() != null ? risk.getCreatedBy().getUsername() : null)
                .build();
    }
}
