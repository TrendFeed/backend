package com.trendfeed.backend.controller;

import com.trendfeed.backend.dto.request.CreateWebhookRequest;
import com.trendfeed.backend.dto.request.UpdateWebhookRequest;
import com.trendfeed.backend.dto.response.ApiResponse;
import com.trendfeed.backend.dto.response.PaginatedResponse;
import com.trendfeed.backend.dto.response.WebhookDeliveryResponse;
import com.trendfeed.backend.dto.response.WebhookResponse;
import com.trendfeed.backend.security.FirebaseUserDetails;
import com.trendfeed.backend.service.WebhookDeliveryService;
import com.trendfeed.backend.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Webhook Management API")
@SecurityRequirement(name = "Bearer Authentication")
public class WebhookController {
    
    private final WebhookService webhookService;
    private final WebhookDeliveryService deliveryService;
    
    @PostMapping
    @Operation(summary = "Create a new webhook", description = "Register a new webhook endpoint")
    public ResponseEntity<ApiResponse<WebhookResponse>> createWebhook(
            Authentication authentication,
            @Valid @RequestBody CreateWebhookRequest request
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Creating webhook for user: {}", uid);
        
        WebhookResponse webhook = webhookService.createWebhook(uid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(webhook));
    }
    
    @GetMapping
    @Operation(summary = "Get all webhooks", description = "Get list of user's webhooks")
    public ResponseEntity<ApiResponse<List<WebhookResponse>>> getAllWebhooks(
            Authentication authentication
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Getting all webhooks for user: {}", uid);
        
        List<WebhookResponse> webhooks = webhookService.getAllWebhooks(uid);
        return ResponseEntity.ok(ApiResponse.success(webhooks));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get webhook by ID", description = "Get webhook details")
    public ResponseEntity<ApiResponse<WebhookResponse>> getWebhook(
            Authentication authentication,
            @Parameter(description = "Webhook ID") @PathVariable Long id
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Getting webhook: id={}, user={}", id, uid);
        
        WebhookResponse webhook = webhookService.getWebhook(uid, id);
        return ResponseEntity.ok(ApiResponse.success(webhook));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update webhook", description = "Update webhook configuration")
    public ResponseEntity<ApiResponse<WebhookResponse>> updateWebhook(
            Authentication authentication,
            @Parameter(description = "Webhook ID") @PathVariable Long id,
            @Valid @RequestBody UpdateWebhookRequest request
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Updating webhook: id={}, user={}", id, uid);
        
        WebhookResponse webhook = webhookService.updateWebhook(uid, id, request);
        return ResponseEntity.ok(ApiResponse.success(webhook));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete webhook", description = "Delete a webhook")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteWebhook(
            Authentication authentication,
            @Parameter(description = "Webhook ID") @PathVariable Long id
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Deleting webhook: id={}, user={}", id, uid);
        
        webhookService.deleteWebhook(uid, id);
        
        Map<String, Object> response = Map.of(
                "message", "Webhook deleted successfully",
                "webhookId", id
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/regenerate-secret")
    @Operation(summary = "Regenerate webhook secret", description = "Generate a new secret key for webhook")
    public ResponseEntity<ApiResponse<WebhookResponse>> regenerateSecret(
            Authentication authentication,
            @Parameter(description = "Webhook ID") @PathVariable Long id
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Regenerating secret for webhook: id={}, user={}", id, uid);
        
        WebhookResponse webhook = webhookService.regenerateSecret(uid, id);
        return ResponseEntity.ok(ApiResponse.success(webhook));
    }
    
    @GetMapping("/{id}/deliveries")
    @Operation(summary = "Get webhook deliveries", description = "Get webhook delivery history")
    public ResponseEntity<ApiResponse<PaginatedResponse<WebhookDeliveryResponse>>> getDeliveries(
            Authentication authentication,
            @Parameter(description = "Webhook ID") @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Getting deliveries for webhook: id={}, user={}, page={}, limit={}", id, uid, page, limit);
        
        // 웹훅 소유 확인
        webhookService.getWebhook(uid, id);
        
        PaginatedResponse<WebhookDeliveryResponse> deliveries = deliveryService.getDeliveries(id, page, limit);
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }
    
    @GetMapping("/deliveries/{deliveryId}")
    @Operation(summary = "Get delivery details", description = "Get detailed information about a webhook delivery")
    public ResponseEntity<ApiResponse<WebhookDeliveryResponse>> getDelivery(
            Authentication authentication,
            @Parameter(description = "Delivery ID") @PathVariable Long deliveryId
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Getting delivery details: id={}, user={}", deliveryId, uid);
        
        WebhookDeliveryResponse delivery = deliveryService.getDelivery(deliveryId);
        
        // 소유권 확인
        webhookService.getWebhook(uid, delivery.getWebhookId());
        
        return ResponseEntity.ok(ApiResponse.success(delivery));
    }
}
