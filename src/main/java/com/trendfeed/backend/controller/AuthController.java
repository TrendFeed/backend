package com.trendfeed.backend.controller;

import com.trendfeed.backend.dto.response.ApiResponse;
import com.trendfeed.backend.dto.response.UserResponse;
import com.trendfeed.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/verify")
    @Operation(summary = "Verify Firebase ID Token", description = "Verify Firebase ID Token and create/get user")
    public ResponseEntity<ApiResponse<UserResponse>> verifyToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        log.debug("Verifying Firebase token");
        
        // Extract token from "Bearer <token>"
        String token = authHeader.substring(7);
        
        UserResponse user = authService.verifyToken(token);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
