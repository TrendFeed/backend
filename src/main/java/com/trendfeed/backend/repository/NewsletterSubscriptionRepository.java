package com.trendfeed.backend.repository;

import com.trendfeed.backend.entity.NewsletterSubscription;
import com.trendfeed.backend.entity.NewsletterSubscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {
    
    Optional<NewsletterSubscription> findByEmail(String email);
    
    Optional<NewsletterSubscription> findByConfirmationToken(String token);
    
    Optional<NewsletterSubscription> findByUnsubscribeToken(String token);
    
    boolean existsByEmail(String email);
    
    List<NewsletterSubscription> findByStatus(SubscriptionStatus status);
    
    long countByStatus(SubscriptionStatus status);
}
