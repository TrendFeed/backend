package com.trendfeed.backend.service;

import com.trendfeed.backend.dto.response.WebhookDeliveryResponse;
import com.trendfeed.backend.dto.response.PaginatedResponse;
import com.trendfeed.backend.entity.Webhook;
import com.trendfeed.backend.entity.WebhookDelivery;
import com.trendfeed.backend.entity.WebhookDelivery.DeliveryStatus;
import com.trendfeed.backend.repository.WebhookDeliveryRepository;
import com.trendfeed.backend.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryService {
    
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookRepository webhookRepository;
    private final WebClient.Builder webClientBuilder;
    
    private static final String SIGNATURE_HEADER = "X-Webhook-Signature";
    private static final String EVENT_TYPE_HEADER = "X-Event-Type";
    private static final String EVENT_ID_HEADER = "X-Event-ID";
    private static final String DELIVERY_ID_HEADER = "X-Delivery-ID";
    
    /**
     * 웹훅 이벤트 전송 (비동기)
     */
    @Async
    @Transactional
    public void sendWebhookEvent(String eventType, String eventId, Map<String, Object> payload) {
        log.info("Sending webhook event: type={}, id={}", eventType, eventId);
        
        // 해당 이벤트를 구독하는 모든 활성화된 웹훅 조회
        List<Webhook> webhooks = webhookRepository.findActiveWebhooksForEvent(eventType);
        
        if (webhooks.isEmpty()) {
            log.debug("No active webhooks found for event type: {}", eventType);
            return;
        }
        
        log.info("Found {} active webhooks for event type: {}", webhooks.size(), eventType);
        
        for (Webhook webhook : webhooks) {
            createAndSendDelivery(webhook, eventType, eventId, payload);
        }
    }
    
    /**
     * 웹훅 전송 생성 및 전송
     */
    private void createAndSendDelivery(Webhook webhook, String eventType, String eventId, Map<String, Object> payload) {
        try {
            // 전송 레코드 생성
            WebhookDelivery delivery = WebhookDelivery.builder()
                    .webhook(webhook)
                    .eventType(eventType)
                    .eventId(eventId)
                    .requestUrl(webhook.getUrl())
                    .requestMethod("POST")
                    .requestBody(payload)
                    .maxRetries(webhook.getMaxRetries())
                    .build();
            
            delivery = deliveryRepository.save(delivery);
            
            // 비동기로 실제 전송 수행
            sendDelivery(delivery, webhook);
            
        } catch (Exception e) {
            log.error("Failed to create webhook delivery: webhook={}, event={}", 
                    webhook.getId(), eventType, e);
        }
    }
    
    /**
     * 웹훅 전송 실행
     */
    private void sendDelivery(WebhookDelivery delivery, Webhook webhook) {
        long startTime = System.currentTimeMillis();
        
        try {
            delivery.markAsSent();
            deliveryRepository.save(delivery);
            
            // 페이로드 준비
            Map<String, Object> fullPayload = buildPayload(delivery);
            String payloadJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(fullPayload);
            
            // HMAC 서명 생성
            String signature = generateSignature(payloadJson, webhook.getSecretKey());
            
            // HTTP 헤더 구성
            Map<String, String> headers = new HashMap<>();
            headers.put(SIGNATURE_HEADER, signature);
            headers.put(EVENT_TYPE_HEADER, delivery.getEventType());
            headers.put(EVENT_ID_HEADER, delivery.getEventId() != null ? delivery.getEventId() : "");
            headers.put(DELIVERY_ID_HEADER, delivery.getId().toString());
            
            delivery.setRequestHeaders(headers);
            
            // WebClient로 전송
            WebClient webClient = webClientBuilder.build();
            
            webClient.post()
                    .uri(webhook.getUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                    .bodyValue(fullPayload)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(30))
                    .doOnSuccess(response -> {
                        int responseTime = (int) (System.currentTimeMillis() - startTime);
                        handleSuccess(delivery, webhook, response.getStatusCode().value(), responseTime);
                    })
                    .doOnError(error -> {
                        handleError(delivery, webhook, error);
                    })
                    .subscribe();
                    
        } catch (Exception e) {
            log.error("Failed to send webhook delivery: id={}", delivery.getId(), e);
            handleError(delivery, webhook, e);
        }
    }
    
    /**
     * 전송 성공 처리
     */
    @Transactional
    protected void handleSuccess(WebhookDelivery delivery, Webhook webhook, int statusCode, int responseTime) {
        log.info("Webhook delivery successful: id={}, status={}, time={}ms", 
                delivery.getId(), statusCode, responseTime);
        
        delivery.markAsSuccess(statusCode, "OK", responseTime);
        deliveryRepository.save(delivery);
        
        // 웹훅 통계 업데이트
        webhook.incrementTotalDeliveries();
        webhook.incrementSuccessfulDeliveries();
        webhook.updateLastDelivery();
        webhookRepository.save(webhook);
    }
    
    /**
     * 전송 실패 처리
     */
    @Transactional
    protected void handleError(WebhookDelivery delivery, Webhook webhook, Throwable error) {
        String errorMessage = error.getMessage();
        int statusCode = 0;
        
        if (error instanceof WebClientResponseException) {
            WebClientResponseException webClientError = (WebClientResponseException) error;
            statusCode = webClientError.getStatusCode().value();
            errorMessage = String.format("HTTP %d: %s", statusCode, webClientError.getResponseBodyAsString());
        }
        
        log.warn("Webhook delivery failed: id={}, error={}", delivery.getId(), errorMessage);
        
        delivery.setResponseStatus(statusCode);
        delivery.setErrorMessage(errorMessage);
        
        // 재시도 가능 여부 확인
        if (delivery.canRetry()) {
            log.info("Scheduling retry for delivery: id={}, attempt={}/{}", 
                    delivery.getId(), delivery.getRetryCount() + 1, delivery.getMaxRetries());
            delivery.markAsFailedWithRetry(errorMessage, webhook.getRetryDelaySeconds());
        } else {
            log.warn("Max retries exceeded for delivery: id={}", delivery.getId());
            delivery.markAsPermanentlyFailed(errorMessage);
        }
        
        deliveryRepository.save(delivery);
        
        // 웹훅 통계 업데이트
        webhook.incrementTotalDeliveries();
        webhook.incrementFailedDeliveries();
        webhook.updateLastDelivery();
        webhookRepository.save(webhook);
    }
    
    /**
     * 재시도 대기 중인 전송 처리 (스케줄러)
     */
    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    @Transactional
    public void processRetries() {
        LocalDateTime now = LocalDateTime.now();
        List<WebhookDelivery> retriesReady = deliveryRepository.findPendingRetries(
                DeliveryStatus.RETRYING, now);
        
        if (!retriesReady.isEmpty()) {
            log.info("Processing {} webhook retries", retriesReady.size());
            
            for (WebhookDelivery delivery : retriesReady) {
                Webhook webhook = delivery.getWebhook();
                if (webhook.getIsActive()) {
                    sendDelivery(delivery, webhook);
                } else {
                    log.warn("Skipping retry for inactive webhook: id={}", webhook.getId());
                    delivery.markAsPermanentlyFailed("Webhook deactivated");
                    deliveryRepository.save(delivery);
                }
            }
        }
    }
    
    /**
     * 웹훅 전송 이력 조회
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<WebhookDeliveryResponse> getDeliveries(Long webhookId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<WebhookDelivery> deliveryPage = deliveryRepository.findByWebhookIdOrderByCreatedAtDesc(
                webhookId, pageable);
        
        List<WebhookDeliveryResponse> deliveries = deliveryPage.getContent().stream()
                .map(WebhookDeliveryResponse::summary)
                .toList();
        
        PaginatedResponse.PaginationInfo paginationInfo = PaginatedResponse.PaginationInfo.builder()
                .currentPage(deliveryPage.getNumber() + 1)
                .totalPages(deliveryPage.getTotalPages())
                .totalItems(deliveryPage.getTotalElements())
                .itemsPerPage(deliveryPage.getSize())
                .build();
        
        return PaginatedResponse.<WebhookDeliveryResponse>builder()
                .data(deliveries)
                .pagination(paginationInfo)
                .build();
    }
    
    /**
     * 웹훅 전송 상세 조회
     */
    @Transactional(readOnly = true)
    public WebhookDeliveryResponse getDelivery(Long deliveryId) {
        WebhookDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));
        
        return WebhookDeliveryResponse.from(delivery);
    }
    
    /**
     * 페이로드 구성
     */
    private Map<String, Object> buildPayload(WebhookDelivery delivery) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event_type", delivery.getEventType());
        payload.put("event_id", delivery.getEventId());
        payload.put("delivery_id", delivery.getId());
        payload.put("timestamp", LocalDateTime.now().toString());
        payload.put("data", delivery.getRequestBody());
        
        return payload;
    }
    
    /**
     * HMAC-SHA256 서명 생성
     */
    private String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + Base64.getEncoder().encodeToString(hash);
            
        } catch (Exception e) {
            log.error("Failed to generate HMAC signature", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}
