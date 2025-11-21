package com.trendfeed.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String uid;
    private String email;
    private String displayName;
    private String photoURL;
    private UserPreferencesDto preferences;
    private UserStatsDto stats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferencesDto {
        private List<String> interests;
        private Map<String, Boolean> notifications;
        private String comicStyle;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatsDto {
        private long savedComics;
        private long likedComics;
        private long commentsCount;
    }
}
