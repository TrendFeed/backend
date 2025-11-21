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
public class ComicResponse {
    private Long id;
    private String repoName;
    private String repoUrl;
    private Integer stars;
    private String language;
    private List<Map<String, Object>> panels;
    private List<String> keyInsights;
    private Boolean isNew;
    private Integer likes;
    private Integer shares;
    private Integer comments;
    private LocalDateTime createdAt;
    private LocalDateTime savedAt; // For saved comics
}
