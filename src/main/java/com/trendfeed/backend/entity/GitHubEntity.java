package com.trendfeed.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "git_repositories")
public class GitHubEntity {

    @Id
    // 깃허브 repo ID 
    private Long id;

    private String nodeId;
    private String name;
    private String fullName;       // "owner/repo"
    private String ownerLogin;     // "owner"
    private String htmlUrl;
    @Column(length = 2000)
    private String description;
    private String language;

    private Integer stargazersCount;      // 지금까지 전체 스타 수 (마지막 크롤링 시점)
    private Integer lastDelta24h;         // 마지막 갱신에서 증가한 스타 수
    private OffsetDateTime lastStarCheck; // 마지막으로 스타 수 비교한 시각

    private OffsetDateTime createdAt;
    private OffsetDateTime pushedAt;
    private OffsetDateTime updatedAt;

    private OffsetDateTime lastCrawledAt; // 메타/README를 마지막으로 가져온 시각

    @Column(columnDefinition = "text")
    private String readmeText; // raw markdown
    private String readmeSha;
    private String readmeEtag;

    // --- getters/setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getOwnerLogin() { return ownerLogin; }
    public void setOwnerLogin(String ownerLogin) { this.ownerLogin = ownerLogin; }

    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getStargazersCount() { return stargazersCount; }
    public void setStargazersCount(Integer stargazersCount) { this.stargazersCount = stargazersCount; }

    public Integer getLastDelta24h() { return lastDelta24h; }
    public void setLastDelta24h(Integer lastDelta24h) { this.lastDelta24h = lastDelta24h; }

    public OffsetDateTime getLastStarCheck() { return lastStarCheck; }
    public void setLastStarCheck(OffsetDateTime lastStarCheck) { this.lastStarCheck = lastStarCheck; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getPushedAt() { return pushedAt; }
    public void setPushedAt(OffsetDateTime pushedAt) { this.pushedAt = pushedAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public OffsetDateTime getLastCrawledAt() { return lastCrawledAt; }
    public void setLastCrawledAt(OffsetDateTime lastCrawledAt) { this.lastCrawledAt = lastCrawledAt; }

    public String getReadmeText() { return readmeText; }
    public void setReadmeText(String readmeText) { this.readmeText = readmeText; }

    public String getReadmeSha() { return readmeSha; }
    public void setReadmeSha(String readmeSha) { this.readmeSha = readmeSha; }

    public String getReadmeEtag() { return readmeEtag; }
    public void setReadmeEtag(String readmeEtag) { this.readmeEtag = readmeEtag; }
}
