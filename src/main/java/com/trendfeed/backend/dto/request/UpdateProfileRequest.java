package com.trendfeed.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Size(max = 255, message = "Display name must not exceed 255 characters")
    private String displayName;
    
    private UserPreferencesDto preferences;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferencesDto {
        private List<String> interests;
        private Map<String, Boolean> notifications;
        private String comicStyle;
    }
}
