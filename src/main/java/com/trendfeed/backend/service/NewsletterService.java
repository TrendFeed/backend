package com.trendfeed.backend.service;

import com.trendfeed.backend.dto.response.ComicResponse;
import com.trendfeed.backend.entity.NewsletterSubscription;
import com.trendfeed.backend.entity.NewsletterSubscription.SubscriptionStatus;
import com.trendfeed.backend.exception.CustomException;
import com.trendfeed.backend.exception.ErrorCode;
import com.trendfeed.backend.repository.NewsletterSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterService {
    
    private final NewsletterSubscriptionRepository subscriptionRepository;
    private final EmailService emailService;
    private final ComicService comicService;
    
    @Transactional
    public Map<String, Object> subscribe(String email) {
        // Check if already subscribed
        if (subscriptionRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_SUBSCRIBED);
        }
        
        // Generate tokens
        String confirmationToken = UUID.randomUUID().toString();
        String unsubscribeToken = UUID.randomUUID().toString();
        
        NewsletterSubscription subscription = NewsletterSubscription.builder()
                .email(email)
                .status(SubscriptionStatus.PENDING)
                .confirmationToken(confirmationToken)
                .unsubscribeToken(unsubscribeToken)
                .build();
        
        subscriptionRepository.save(subscription);
        
        log.info("Newsletter subscription created for: {}", email);
        
        // Send confirmation email asynchronously
        emailService.sendNewsletterConfirmation(email, confirmationToken)
                .thenAccept(success -> {
                    if (success) {
                        log.info("Confirmation email sent successfully to: {}", email);
                    } else {
                        log.warn("Failed to send confirmation email to: {}", email);
                    }
                });
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("status", "pending");
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> confirmSubscription(String token) {
        NewsletterSubscription subscription = subscriptionRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CONFIRMATION_TOKEN));
        
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            Map<String, Object> response = new HashMap<>();
            response.put("email", subscription.getEmail());
            response.put("status", "active");
            response.put("confirmedAt", subscription.getConfirmedAt());
            return response;
        }
        
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setConfirmedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        
        log.info("Newsletter subscription confirmed for: {}", subscription.getEmail());
        
        // Send welcome email
        emailService.sendNewsletterWelcome(subscription.getEmail())
                .thenAccept(success -> {
                    if (success) {
                        log.info("Welcome email sent successfully to: {}", subscription.getEmail());
                    } else {
                        log.warn("Failed to send welcome email to: {}", subscription.getEmail());
                    }
                });
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", subscription.getEmail());
        response.put("status", "active");
        response.put("confirmedAt", subscription.getConfirmedAt());
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> unsubscribe(String email, String token) {
        NewsletterSubscription subscription = subscriptionRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        
        if (!subscription.getUnsubscribeToken().equals(token)) {
            throw new CustomException(ErrorCode.INVALID_UNSUBSCRIBE_TOKEN);
        }
        
        subscription.setStatus(SubscriptionStatus.UNSUBSCRIBED);
        subscription.setUnsubscribedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        
        log.info("Newsletter subscription cancelled for: {}", email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("status", "unsubscribed");
        response.put("unsubscribedAt", subscription.getUnsubscribedAt());
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getStatus(String email) {
        NewsletterSubscription subscription = subscriptionRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", subscription.getEmail());
        response.put("status", subscription.getStatus().name().toLowerCase());
        response.put("subscribedAt", subscription.getSubscribedAt());
        if (subscription.getConfirmedAt() != null) {
            response.put("confirmedAt", subscription.getConfirmedAt());
        }
        
        return response;
    }
    
    /**
     * 모든 활성 구독자에게 뉴스레터 발송
     */
    @Transactional
    public Map<String, Object> sendNewsletterToAll() {
        // 활성 구독자 조회
        List<NewsletterSubscription> activeSubscribers = 
                subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        
        if (activeSubscribers.isEmpty()) {
            log.info("No active subscribers found");
            Map<String, Object> response = new HashMap<>();
            response.put("message", "No active subscribers");
            response.put("sent", 0);
            return response;
        }
        
        // 최신 트렌딩 코믹 조회 (상위 10개)
        List<ComicResponse> trendingComics = comicService.getAllComics(1, 10, "stars")
                .getData();
        
        if (trendingComics.isEmpty()) {
            log.warn("No comics found to send");
            Map<String, Object> response = new HashMap<>();
            response.put("message", "No comics to send");
            response.put("sent", 0);
            return response;
        }
        
        log.info("Sending newsletter to {} subscribers with {} comics", 
                activeSubscribers.size(), trendingComics.size());
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        // 각 구독자에게 비동기로 발송
        List<CompletableFuture<Boolean>> futures = activeSubscribers.stream()
                .map(subscriber -> emailService.sendNewsletter(
                        subscriber.getEmail(), 
                        subscriber.getUnsubscribeToken(),
                        trendingComics
                ).thenApply(success -> {
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                    return success;
                }))
                .toList();
        
        // 모든 발송 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        log.info("Newsletter sent: {} succeeded, {} failed", successCount.get(), failCount.get());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Newsletter sent");
        response.put("totalSubscribers", activeSubscribers.size());
        response.put("sent", successCount.get());
        response.put("failed", failCount.get());
        response.put("comicCount", trendingComics.size());
        response.put("sentAt", LocalDateTime.now());
        
        return response;
    }
    
    /**
     * 특정 이메일로 테스트 뉴스레터 발송
     */
    @Transactional
    public Map<String, Object> sendTestNewsletter(String email) {
        // 최신 트렌딩 코믹 조회
        List<ComicResponse> trendingComics = comicService.getAllComics(1, 5, "stars")
                .getData();
        
        if (trendingComics.isEmpty()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "No comics found");
        }
        
        log.info("Sending test newsletter to: {}", email);
        
        // 테스트용 임시 토큰
        String testToken = UUID.randomUUID().toString();
        
        boolean success = emailService.sendNewsletter(email, testToken, trendingComics)
                .join();
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("success", success);
        response.put("comicCount", trendingComics.size());
        response.put("sentAt", LocalDateTime.now());
        
        return response;
    }
}
