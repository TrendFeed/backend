package com.trendfeed.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comics", indexes = {
        @Index(name = "idx_comic_trending", columnList = "trendingScore"),
        @Index(name = "idx_comic_stars", columnList = "stars"),
        @Index(name = "idx_comic_createdAt", columnList = "createdAt")
})
public class ComicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long repoId;

    private String repoName;
    private String repoUrl;

    // 정렬/필터용
    private Integer stars;         
    private String language;       
    private Double trendingScore;    

    // 콘텐츠
    @ElementCollection
    @CollectionTable(name = "comic_panels", joinColumns = @JoinColumn(name = "comic_id"))
    @Column(name = "panel_url")
    private List<String> panels = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "comic_insights", joinColumns = @JoinColumn(name = "comic_id"))
    @Column(name = "insight")
    private List<String> keyInsights = new ArrayList<>();

    // 메타데이터
    private Boolean isNew = Boolean.TRUE;
    private Integer likes = 0;
    private Integer shares = 0;
    private Integer comments = 0;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // --- getters/setters ---
    public Long getId() { return id; }
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
    public Boolean getIsNew() { return isNew; }
    public void setIsNew(Boolean isNew) { this.isNew = isNew; }
    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
    public Integer getShares() { return shares; }
    public void setShares(Integer shares) { this.shares = shares; }
    public Integer getComments() { return comments; }
    public void setComments(Integer comments) { this.comments = comments; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
