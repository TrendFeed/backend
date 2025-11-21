package com.trendfeed.backend.dto.response;

import com.trendfeed.backend.entity.Webhook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookResponse {
    
    private Long id;
    private String url;
    private String description;
    private List<String> eventTypes;
    private Boolean isActive;
    
    // 통계
    private Integer totalDeliveries;
    private Integer successfulDeliveries;
    private Integer failedDeliveries;
    private Double successRate;
    
    private LocalDateTime lastDeliveryAt;
    private LocalDateTime lastSuccessAt;
    private LocalDateTime lastFailureAt;
    
    // 설정
    private Integer maxRetries;
    private Integer retryDelaySeconds;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Entity -> DTO 변환
    public static WebhookResponse from(Webhook webhook) {
        double successRate = 0.0;
        if (webhook.getTotalDeliveries() > 0) {
            successRate = (double) webhook.getSuccessfulDeliveries() / webhook.getTotalDeliveries() * 100;
        }
        
        return WebhookResponse.builder()
                .id(webhook.getId())
                .url(webhook.getUrl())
                .description(webhook.getDescription())
                .eventTypes(webhook.getEventTypes())
                .isActive(webhook.getIsActive())
                .totalDeliveries(webhook.getTotalDeliveries())
                .successfulDeliveries(webhook.getSuccessfulDeliveries())
                .failedDeliveries(webhook.getFailedDeliveries())
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .lastDeliveryAt(webhook.getLastDeliveryAt())
                .lastSuccessAt(webhook.getLastSuccessAt())
                .lastFailureAt(webhook.getLastFailureAt())
                .maxRetries(webhook.getMaxRetries())
                .retryDelaySeconds(webhook.getRetryDelaySeconds())
                .createdAt(webhook.getCreatedAt())
                .updatedAt(webhook.getUpdatedAt())
                .build();
    }
}
