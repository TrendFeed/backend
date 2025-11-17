package com.trendfeed.backend.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "webhooks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Webhook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uid", referencedColumnName = "uid", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 2048)
    private String url;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "secret_key", nullable = false)
    private String secretKey;
    
    @Column(name = "event_types", columnDefinition = "jsonb", nullable = false)
    @Type(JsonBinaryType.class)
    @Builder.Default
    private List<String> eventTypes = new ArrayList<>();
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // 통계
    @Column(name = "total_deliveries", nullable = false)
    @Builder.Default
    private Integer totalDeliveries = 0;
    
    @Column(name = "successful_deliveries", nullable = false)
    @Builder.Default
    private Integer successfulDeliveries = 0;
    
    @Column(name = "failed_deliveries", nullable = false)
    @Builder.Default
    private Integer failedDeliveries = 0;
    
    @Column(name = "last_delivery_at")
    private LocalDateTime lastDeliveryAt;
    
    @Column(name = "last_success_at")
    private LocalDateTime lastSuccessAt;
    
    @Column(name = "last_failure_at")
    private LocalDateTime lastFailureAt;
    
    // 재시도 설정
    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;
    
    @Column(name = "retry_delay_seconds", nullable = false)
    @Builder.Default
    private Integer retryDelaySeconds = 60;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 비즈니스 메서드
    public void incrementTotalDeliveries() {
        this.totalDeliveries++;
    }
    
    public void incrementSuccessfulDeliveries() {
        this.successfulDeliveries++;
        this.lastSuccessAt = LocalDateTime.now();
    }
    
    public void incrementFailedDeliveries() {
        this.failedDeliveries++;
        this.lastFailureAt = LocalDateTime.now();
    }
    
    public void updateLastDelivery() {
        this.lastDeliveryAt = LocalDateTime.now();
    }
    
    public boolean supportsEvent(String eventType) {
        return eventTypes != null && eventTypes.contains(eventType);
    }
}
