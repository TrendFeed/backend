package com.trendfeed.backend.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "webhook_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDelivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_id", nullable = false)
    private Webhook webhook;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "event_id")
    private String eventId;
    
    // 요청 정보
    @Column(name = "request_url", nullable = false, length = 2048)
    private String requestUrl;
    
    @Column(name = "request_method", nullable = false, length = 10)
    @Builder.Default
    private String requestMethod = "POST";
    
    @Column(name = "request_headers", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private Map<String, String> requestHeaders;
    
    @Column(name = "request_body", columnDefinition = "jsonb", nullable = false)
    @Type(JsonBinaryType.class)
    private Map<String, Object> requestBody;
    
    // 응답 정보
    @Column(name = "response_status")
    private Integer responseStatus;
    
    @Column(name = "response_headers", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private Map<String, String> responseHeaders;
    
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(name = "response_time_ms")
    private Integer responseTimeMs;
    
    // 전송 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;
    
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;
    
    // 에러 정보
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "error_stack_trace", columnDefinition = "TEXT")
    private String errorStackTrace;
    
    // 타임스탬프
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 전송 상태 enum
    public enum DeliveryStatus {
        PENDING,   // 전송 대기
        SENT,      // 전송됨
        SUCCESS,   // 성공
        FAILED,    // 실패 (재시도 횟수 초과)
        RETRYING   // 재시도 중
    }
    
    // 비즈니스 메서드
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    public void markAsSent() {
        this.status = DeliveryStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }
    
    public void markAsSuccess(int responseStatus, String responseBody, int responseTimeMs) {
        this.status = DeliveryStatus.SUCCESS;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.responseTimeMs = responseTimeMs;
        this.completedAt = LocalDateTime.now();
    }
    
    public void markAsFailedWithRetry(String errorMessage, int retryDelaySeconds) {
        this.status = DeliveryStatus.RETRYING;
        this.errorMessage = errorMessage;
        this.retryCount++;
        this.nextRetryAt = LocalDateTime.now().plusSeconds(retryDelaySeconds);
    }
    
    public void markAsPermanentlyFailed(String errorMessage) {
        this.status = DeliveryStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
    
    public boolean isSuccessfulResponse() {
        return responseStatus != null && responseStatus >= 200 && responseStatus < 300;
    }
}
