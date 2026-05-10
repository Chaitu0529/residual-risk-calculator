package com.internship.tool.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRequest {
    
    @NotBlank(message = "Risk name is required")
    @Size(max = 200, message = "Risk name must not exceed 200 characters")
    private String name;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    @NotNull(message = "Risk score is required")
    @DecimalMin(value = "0.0", message = "Risk score must be at least 0.0")
    @DecimalMax(value = "100.0", message = "Risk score must not exceed 100.0")
    private Double riskScore;
}
