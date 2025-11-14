package com.trendfeed.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Authentication
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid or expired token"),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User already exists"),
    
    // Comic
    COMIC_NOT_FOUND(HttpStatus.NOT_FOUND, "Comic not found"),
    COMIC_ALREADY_SAVED(HttpStatus.CONFLICT, "Comic already saved"),
    COMIC_NOT_SAVED(HttpStatus.NOT_FOUND, "Comic not saved"),
    
    // Newsletter
    EMAIL_ALREADY_SUBSCRIBED(HttpStatus.CONFLICT, "Email already subscribed"),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Subscription not found"),
    INVALID_CONFIRMATION_TOKEN(HttpStatus.BAD_REQUEST, "Invalid or expired confirmation token"),
    INVALID_UNSUBSCRIBE_TOKEN(HttpStatus.BAD_REQUEST, "Invalid unsubscribe token"),
    
    // Webhook
    WEBHOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "Webhook not found"),
    WEBHOOK_DELIVERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Webhook delivery failed"),
    
    // Validation
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request parameters"),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "Invalid email address"),
    
    // Resources
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Resource already exists"),
    RESOURCE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Resource limit exceeded"),
    
    // Server
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    
    private final HttpStatus status;
    private final String message;
}
