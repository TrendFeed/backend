package com.trendfeed.backend.dto.response;

import com.trendfeed.backend.entity.WebhookDelivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDeliveryResponse {
    
    private Long id;
    private Long webhookId;
    private String eventType;
    private String eventId;
    
    // 요청 정보
    private String requestUrl;
    private String requestMethod;
    private Map<String, Object> requestBody;
    
    // 응답 정보
    private Integer responseStatus;
    private String responseBody;
    private Integer responseTimeMs;
    
    // 상태
    private String status;
    private Integer retryCount;
    private Integer maxRetries;
    
    // 에러 정보
    private String errorMessage;
    
    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime completedAt;
    private LocalDateTime nextRetryAt;
    
    // Entity -> DTO 변환
    public static WebhookDeliveryResponse from(WebhookDelivery delivery) {
        return WebhookDeliveryResponse.builder()
                .id(delivery.getId())
                .webhookId(delivery.getWebhook().getId())
                .eventType(delivery.getEventType())
                .eventId(delivery.getEventId())
                .requestUrl(delivery.getRequestUrl())
                .requestMethod(delivery.getRequestMethod())
                .requestBody(delivery.getRequestBody())
                .responseStatus(delivery.getResponseStatus())
                .responseBody(delivery.getResponseBody())
                .responseTimeMs(delivery.getResponseTimeMs())
                .status(delivery.getStatus().name())
                .retryCount(delivery.getRetryCount())
                .maxRetries(delivery.getMaxRetries())
                .errorMessage(delivery.getErrorMessage())
                .createdAt(delivery.getCreatedAt())
                .sentAt(delivery.getSentAt())
                .completedAt(delivery.getCompletedAt())
                .nextRetryAt(delivery.getNextRetryAt())
                .build();
    }
    
    // 요약 버전 (리스트 조회용)
    public static WebhookDeliveryResponse summary(WebhookDelivery delivery) {
        return WebhookDeliveryResponse.builder()
                .id(delivery.getId())
                .eventType(delivery.getEventType())
                .eventId(delivery.getEventId())
                .responseStatus(delivery.getResponseStatus())
                .responseTimeMs(delivery.getResponseTimeMs())
                .status(delivery.getStatus().name())
                .retryCount(delivery.getRetryCount())
                .errorMessage(delivery.getErrorMessage())
                .createdAt(delivery.getCreatedAt())
                .completedAt(delivery.getCompletedAt())
                .build();
    }
}
