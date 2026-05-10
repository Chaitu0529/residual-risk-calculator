package com.internship.tool.controller;

import com.internship.tool.dto.PageResponse;
import com.internship.tool.dto.RiskRequest;
import com.internship.tool.dto.RiskResponse;
import com.internship.tool.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risks")
@RequiredArgsConstructor
@Tag(name = "Risk Management", description = "Risk CRUD and search APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class RiskController {

    private final RiskService riskService;

    @GetMapping
    @Operation(summary = "Get all risks", description = "Retrieve all active risks with pagination and sorting")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PageResponse<RiskResponse>> getAllRisks(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (e.g. createdAt, riskScore, name)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(riskService.getAllRisks(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get risk by ID", description = "Retrieve a specific risk by its ID")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<RiskResponse> getRiskById(
            @Parameter(description = "Risk ID") @PathVariable Long id) {
        return ResponseEntity.ok(riskService.getRiskById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search risks", description = "Search risks by name, category, or description")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PageResponse<RiskResponse>> searchRisks(
            @Parameter(description = "Search query — matches name, category, or description") @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(riskService.searchRisks(q, pageable));
    }

    @PostMapping
    @Operation(summary = "Create a new risk", description = "Create a new risk entry (USER or ADMIN)")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<RiskResponse> createRisk(@Valid @RequestBody RiskRequest request) {
        RiskResponse response = riskService.createRisk(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a risk", description = "Update an existing risk by ID (USER or ADMIN)")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<RiskResponse> updateRisk(
            @Parameter(description = "Risk ID") @PathVariable Long id,
            @Valid @RequestBody RiskRequest request) {
        return ResponseEntity.ok(riskService.updateRisk(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a risk", description = "Soft delete a risk by ID — ADMIN only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRisk(
            @Parameter(description = "Risk ID") @PathVariable Long id) {
        riskService.deleteRisk(id);
        return ResponseEntity.noContent().build();
    }
}
