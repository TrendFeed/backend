package com.trendfeed.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ComicCreateRequest {
    @NotNull
    private Long repoId;

    @NotBlank
    private String repoName;

    @NotBlank
    private String repoUrl;

    // 스냅샷 메타
    @NotNull
    private Integer stars;

    @NotBlank
    private String language;

    @NotNull
    private Double trendingScore;

    // 콘텐츠
    private List<String> panels;       
    private List<String> keyInsights;  

    // --- getters/setters ---
    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Double getTrendingScore() { return trendingScore; }
    public void setTrendingScore(Double trendingScore) { this.trendingScore = trendingScore; }
    public List<String> getPanels() { return panels; }
    public void setPanels(List<String> panels) { this.panels = panels; }
    public List<String> getKeyInsights() { return keyInsights; }
    public void setKeyInsights(List<String> keyInsights) { this.keyInsights = keyInsights; }
}
