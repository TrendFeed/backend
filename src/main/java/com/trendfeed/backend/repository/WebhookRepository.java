package com.trendfeed.backend.repository;

import com.trendfeed.backend.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    
    // 사용자의 모든 웹훅 조회
    List<Webhook> findByUserUidOrderByCreatedAtDesc(String uid);
    
    // 사용자의 활성화된 웹훅만 조회
    List<Webhook> findByUserUidAndIsActiveTrue(String uid);
    
    // 사용자와 ID로 웹훅 조회
    Optional<Webhook> findByIdAndUserUid(Long id, String uid);
    
    // 특정 이벤트 타입을 구독하는 활성화된 웹훅 조회
    @Query("SELECT w FROM Webhook w WHERE w.isActive = true " +
           "AND FUNCTION('jsonb_exists', w.eventTypes, :eventType) = true")
    List<Webhook> findActiveWebhooksForEvent(@Param("eventType") String eventType);
    
    // 사용자의 웹훅 개수 조회
    long countByUserUid(String uid);
    
    // URL 중복 체크 (같은 사용자)
    boolean existsByUserUidAndUrl(String uid, String url);
}
