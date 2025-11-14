package com.trendfeed.backend.service;

import com.trendfeed.backend.dto.request.CreateWebhookRequest;
import com.trendfeed.backend.dto.request.UpdateWebhookRequest;
import com.trendfeed.backend.dto.response.WebhookResponse;
import com.trendfeed.backend.entity.User;
import com.trendfeed.backend.entity.Webhook;
import com.trendfeed.backend.exception.CustomException;
import com.trendfeed.backend.exception.ErrorCode;
import com.trendfeed.backend.repository.UserRepository;
import com.trendfeed.backend.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    
    private final WebhookRepository webhookRepository;
    private final UserRepository userRepository;
    private static final int MAX_WEBHOOKS_PER_USER = 10;
    
    /**
     * 웹훅 생성
     */
    @Transactional
    public WebhookResponse createWebhook(String uid, CreateWebhookRequest request) {
        log.debug("Creating webhook for user: {}, url: {}", uid, request.getUrl());
        
        // 사용자 조회
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 웹훅 개수 제한 확인
        long webhookCount = webhookRepository.countByUserUid(uid);
        if (webhookCount >= MAX_WEBHOOKS_PER_USER) {
            throw new CustomException(ErrorCode.RESOURCE_LIMIT_EXCEEDED, 
                "Maximum " + MAX_WEBHOOKS_PER_USER + " webhooks allowed per user");
        }
        
        // URL 중복 확인
        if (webhookRepository.existsByUserUidAndUrl(uid, request.getUrl())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, 
                "Webhook with this URL already exists");
        }
        
        // 시크릿 키 생성
        String secretKey = generateSecretKey();
        
        // 웹훅 생성
        Webhook webhook = Webhook.builder()
                .user(user)
                .url(request.getUrl())
                .description(request.getDescription())
                .secretKey(secretKey)
                .eventTypes(request.getEventTypes())
                .maxRetries(request.getMaxRetries())
                .retryDelaySeconds(request.getRetryDelaySeconds())
                .build();
        
        Webhook saved = webhookRepository.save(webhook);
        log.info("Webhook created: id={}, user={}, url={}", saved.getId(), uid, saved.getUrl());
        
        return WebhookResponse.from(saved);
    }
    
    /**
     * 사용자의 모든 웹훅 조회
     */
    @Transactional(readOnly = true)
    public List<WebhookResponse> getAllWebhooks(String uid) {
        log.debug("Getting all webhooks for user: {}", uid);
        
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        return webhookRepository.findByUserUidOrderByCreatedAtDesc(uid)
                .stream()
                .map(WebhookResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 웹훅 상세 조회
     */
    @Transactional(readOnly = true)
    public WebhookResponse getWebhook(String uid, Long webhookId) {
        log.debug("Getting webhook: id={}, user={}", webhookId, uid);
        
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Webhook webhook = webhookRepository.findByIdAndUserUid(webhookId, uid)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Webhook not found"));
        
        return WebhookResponse.from(webhook);
    }
    
    /**
     * 웹훅 수정
     */
    @Transactional
    public WebhookResponse updateWebhook(String uid, Long webhookId, UpdateWebhookRequest request) {
        log.debug("Updating webhook: id={}, user={}", webhookId, uid);
        
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Webhook webhook = webhookRepository.findByIdAndUserUid(webhookId, uid)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Webhook not found"));
        
        // 업데이트
        if (request.getUrl() != null && !request.getUrl().equals(webhook.getUrl())) {
            // URL 변경 시 중복 확인
            if (webhookRepository.existsByUserUidAndUrl(uid, request.getUrl())) {
                throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, 
                    "Webhook with this URL already exists");
            }
            webhook.setUrl(request.getUrl());
        }
        
        if (request.getDescription() != null) {
            webhook.setDescription(request.getDescription());
        }
        
        if (request.getEventTypes() != null) {
            webhook.setEventTypes(request.getEventTypes());
        }
        
        if (request.getIsActive() != null) {
            webhook.setIsActive(request.getIsActive());
        }
        
        if (request.getMaxRetries() != null) {
            webhook.setMaxRetries(request.getMaxRetries());
        }
        
        if (request.getRetryDelaySeconds() != null) {
            webhook.setRetryDelaySeconds(request.getRetryDelaySeconds());
        }
        
        Webhook updated = webhookRepository.save(webhook);
        log.info("Webhook updated: id={}", webhookId);
        
        return WebhookResponse.from(updated);
    }
    
    /**
     * 웹훅 삭제
     */
    @Transactional
    public void deleteWebhook(String uid, Long webhookId) {
        log.debug("Deleting webhook: id={}, user={}", webhookId, uid);
        
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Webhook webhook = webhookRepository.findByIdAndUserUid(webhookId, uid)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Webhook not found"));
        
        webhookRepository.delete(webhook);
        log.info("Webhook deleted: id={}", webhookId);
    }
    
    /**
     * 웹훅 시크릿 키 재생성
     */
    @Transactional
    public WebhookResponse regenerateSecret(String uid, Long webhookId) {
        log.debug("Regenerating secret for webhook: id={}, user={}", webhookId, uid);
        
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Webhook webhook = webhookRepository.findByIdAndUserUid(webhookId, uid)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Webhook not found"));
        
        String newSecretKey = generateSecretKey();
        webhook.setSecretKey(newSecretKey);
        
        Webhook updated = webhookRepository.save(webhook);
        log.info("Webhook secret regenerated: id={}", webhookId);
        
        return WebhookResponse.from(updated);
    }
    
    /**
     * 시크릿 키 생성 (32바이트 랜덤)
     */
    private String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
