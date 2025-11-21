package com.trendfeed.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class ComicResponse {
    private Long id;
    private String repoName;
    private String repoUrl;
    private Integer stars;
    private String language;
    private List<String> panels;
    private List<String> keyInsights;
    private Boolean isNew;
    private Integer likes;
    private Integer shares;
    private Integer comments;
    private Double trendingScore;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // --- getters/setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public List<String> getPanels() { return panels; }
    public void setPanels(List<String> panels) { this.panels = panels; }
    public List<String> getKeyInsights() { return keyInsights; }
    public void setKeyInsights(List<String> keyInsights) { this.keyInsights = keyInsights; }
    public Boolean getIsNew() { return isNew; }
    public void setIsNew(Boolean isNew) { this.isNew = isNew; }
    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
    public Integer getShares() { return shares; }
    public void setShares(Integer shares) { this.shares = shares; }
    public Integer getComments() { return comments; }
    public void setComments(Integer comments) { this.comments = comments; }
    public Double getTrendingScore() { return trendingScore; }
    public void setTrendingScore(Double trendingScore) { this.trendingScore = trendingScore; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
