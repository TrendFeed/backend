package com.trendfeed.backend.controller;

import com.trendfeed.backend.dto.request.NewsletterSubscribeRequest;
import com.trendfeed.backend.dto.response.ApiResponse;
import com.trendfeed.backend.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Newsletter", description = "Newsletter API")
public class NewsletterController {
    
    private final NewsletterService newsletterService;
    
    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to newsletter", description = "Subscribe to newsletter with email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request
    ) {
        log.debug("Newsletter subscription request for: {}", request.getEmail());
        
        Map<String, Object> result = newsletterService.subscribe(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/confirm")
    @Operation(summary = "Confirm newsletter subscription", description = "Confirm newsletter subscription with token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirm(
            @RequestParam String token
    ) {
        log.debug("Newsletter confirmation with token: {}", token);
        
        Map<String, Object> result = newsletterService.confirmSubscription(token);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PostMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe from newsletter", description = "Unsubscribe from newsletter")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unsubscribe(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        String token = request.get("token");
        
        log.debug("Newsletter unsubscribe request for: {}", email);
        
        Map<String, Object> result = newsletterService.unsubscribe(email, token);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @GetMapping("/status")
    @Operation(summary = "Get newsletter subscription status", description = "Get newsletter subscription status by email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(
            @RequestParam String email
    ) {
        log.debug("Newsletter status request for: {}", email);
        
        Map<String, Object> result = newsletterService.getStatus(email);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PostMapping("/send-all")
    @Operation(summary = "Send newsletter to all subscribers", description = "Send newsletter to all active subscribers (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendToAll() {
        log.info("Sending newsletter to all subscribers");
        
        Map<String, Object> result = newsletterService.sendNewsletterToAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PostMapping("/send-test")
    @Operation(summary = "Send test newsletter", description = "Send test newsletter to specific email (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendTest(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");
        log.info("Sending test newsletter to: {}", email);
        
        Map<String, Object> result = newsletterService.sendTestNewsletter(email);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
