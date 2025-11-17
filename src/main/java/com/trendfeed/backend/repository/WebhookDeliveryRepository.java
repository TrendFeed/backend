package com.trendfeed.backend.repository;

import com.trendfeed.backend.entity.WebhookDelivery;
import com.trendfeed.backend.entity.WebhookDelivery.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    
    // 웹훅의 전송 이력 조회 (페이지네이션)
    Page<WebhookDelivery> findByWebhookIdOrderByCreatedAtDesc(Long webhookId, Pageable pageable);
    
    // 재시도 대기 중인 전송 조회
    @Query("SELECT wd FROM WebhookDelivery wd " +
           "WHERE wd.status = :status " +
           "AND wd.nextRetryAt IS NOT NULL " +
           "AND wd.nextRetryAt <= :now " +
           "ORDER BY wd.nextRetryAt ASC")
    List<WebhookDelivery> findPendingRetries(
        @Param("status") DeliveryStatus status,
        @Param("now") LocalDateTime now
    );
    
    // 전송 대기 중인 이벤트 조회
    List<WebhookDelivery> findByStatusOrderByCreatedAtAsc(DeliveryStatus status);
    
    // 최근 전송 이력 조회
    @Query("SELECT wd FROM WebhookDelivery wd " +
           "WHERE wd.webhook.id = :webhookId " +
           "AND wd.createdAt >= :since " +
           "ORDER BY wd.createdAt DESC")
    List<WebhookDelivery> findRecentDeliveries(
        @Param("webhookId") Long webhookId,
        @Param("since") LocalDateTime since
    );
    
    // 특정 이벤트의 전송 이력
    List<WebhookDelivery> findByEventTypeAndEventIdOrderByCreatedAtDesc(
        String eventType, 
        String eventId
    );
    
    // 웹훅의 전송 통계
    @Query("SELECT COUNT(wd), " +
           "SUM(CASE WHEN wd.status = 'SUCCESS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN wd.status = 'FAILED' THEN 1 ELSE 0 END), " +
           "AVG(wd.responseTimeMs) " +
           "FROM WebhookDelivery wd " +
           "WHERE wd.webhook.id = :webhookId")
    Object[] getDeliveryStats(@Param("webhookId") Long webhookId);
    
    // 오래된 전송 이력 삭제용
    List<WebhookDelivery> findByCreatedAtBeforeAndStatusIn(
        LocalDateTime before, 
        List<DeliveryStatus> statuses
    );
}
