package com.trendfeed.backend.controller;

import com.trendfeed.backend.dto.request.UpdateProfileRequest;
import com.trendfeed.backend.dto.response.ApiResponse;
import com.trendfeed.backend.dto.response.ComicResponse;
import com.trendfeed.backend.dto.response.PaginatedResponse;
import com.trendfeed.backend.dto.response.UserResponse;
import com.trendfeed.backend.security.FirebaseUserDetails;
import com.trendfeed.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User API")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(Authentication authentication) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Getting profile for user: {}", uid);
        
        UserResponse profile = userService.getUserProfile(uid);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
    
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Updating profile for user: {}", uid);
        
        UserResponse updatedProfile = userService.updateUserProfile(uid, request);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile));
    }
    
    @GetMapping("/saved")
    @Operation(summary = "Get saved comics", description = "Get list of saved comics")
    public ResponseEntity<ApiResponse<PaginatedResponse<ComicResponse>>> getSavedComics(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Getting saved comics for user: {} (page: {}, limit: {})", uid, page, limit);
        
        PaginatedResponse<ComicResponse> savedComics = userService.getSavedComics(uid, page, limit);
        return ResponseEntity.ok(ApiResponse.success(savedComics));
    }
    
    @PostMapping("/saved")
    @Operation(summary = "Save a comic", description = "Save a comic to user's collection")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveComic(
            Authentication authentication,
            @RequestBody Map<String, Long> request
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        Long comicId = request.get("comicId");
        
        log.debug("Saving comic {} for user: {}", comicId, uid);
        
        Map<String, Object> result = userService.saveComic(uid, comicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @DeleteMapping("/saved/{id}")
    @Operation(summary = "Unsave a comic", description = "Remove a comic from user's saved collection")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unsaveComic(
            Authentication authentication,
            @PathVariable Long id
    ) {
        FirebaseUserDetails userDetails = (FirebaseUserDetails) authentication.getPrincipal();
        String uid = userDetails.getUid();
        
        log.debug("Unsaving comic {} for user: {}", id, uid);
        
        Map<String, Object> result = userService.unsaveComic(uid, id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
