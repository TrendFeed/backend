package com.trendfeed.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWebhookRequest {
    
    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String url;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Event types are required")
    @Size(min = 1, message = "At least one event type is required")
    private List<String> eventTypes;
    
    @Min(value = 0, message = "Max retries must be at least 0")
    @Max(value = 10, message = "Max retries must not exceed 10")
    private Integer maxRetries = 3;
    
    @Min(value = 10, message = "Retry delay must be at least 10 seconds")
    @Max(value = 3600, message = "Retry delay must not exceed 3600 seconds")
    private Integer retryDelaySeconds = 60;
}
