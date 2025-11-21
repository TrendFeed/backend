package com.trendfeed.backend.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.trendfeed.backend.dto.response.UserResponse;
import com.trendfeed.backend.entity.User;
import com.trendfeed.backend.entity.UserPreferences;
import com.trendfeed.backend.exception.CustomException;
import com.trendfeed.backend.exception.ErrorCode;
import com.trendfeed.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public UserResponse verifyToken(String token) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String displayName = decodedToken.getName();
            String photoUrl = decodedToken.getPicture();
            
            log.debug("Token verified for user: {}", uid);
            
            // Get or create user
            User user = userRepository.findById(uid)
                    .orElseGet(() -> createNewUser(uid, email, displayName, photoUrl));
            
            return mapToUserResponse(user);
            
        } catch (Exception e) {
            log.error("Failed to verify Firebase token: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
    
    private User createNewUser(String uid, String email, String displayName, String photoUrl) {
        log.info("Creating new user: {}", uid);
        
        User user = User.builder()
                .uid(uid)
                .email(email)
                .displayName(displayName)
                .photoUrl(photoUrl)
                .build();
        
        // Create default preferences
        UserPreferences preferences = UserPreferences.builder()
                .user(user)
                .interests(java.util.List.of())
                .notifications(createDefaultNotifications())
                .comicStyle("western")
                .build();
        
        user.setPreferences(preferences);
        
        return userRepository.save(user);
    }
    
    private Map<String, Boolean> createDefaultNotifications() {
        Map<String, Boolean> notifications = new HashMap<>();
        notifications.put("dailyDigest", true);
        notifications.put("newTrending", true);
        return notifications;
    }
    
    private UserResponse mapToUserResponse(User user) {
        UserResponse.UserPreferencesDto preferencesDto = null;
        if (user.getPreferences() != null) {
            preferencesDto = UserResponse.UserPreferencesDto.builder()
                    .interests(user.getPreferences().getInterests())
                    .notifications(user.getPreferences().getNotifications())
                    .comicStyle(user.getPreferences().getComicStyle())
                    .build();
        }
        
        return UserResponse.builder()
                .uid(user.getUid())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .photoURL(user.getPhotoUrl())
                .preferences(preferencesDto)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
