package com.trendfeed.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trending_candidates")
public class TrendingCandidateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateId;

    // 어떤 리포인지
    private Long repoId;          // GitHubEntity.id (FK)
    private String fullName;      // "owner/repo"
    private String htmlUrl;
    @Column(length = 2000)
    private String description;
    private String language;

    private Integer starCountAtCapture; // 캡처 시점 총 스타 수
    private Integer deltaLast24h;       // 하루 증가량 등 트리거 근거

    private OffsetDateTime capturedAt;  // 후보로 올린 시간

    // AI가 쓸 재료
    @Column(columnDefinition = "text")
    private String readmeSnapshot;      // 그 시점 README markdown

    private Boolean processed;          // AI가 이미 사용했는지 여부

    // --- getters/setters ---
    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getStarCountAtCapture() { return starCountAtCapture; }
    public void setStarCountAtCapture(Integer starCountAtCapture) { this.starCountAtCapture = starCountAtCapture; }

    public Integer getDeltaLast24h() { return deltaLast24h; }
    public void setDeltaLast24h(Integer deltaLast24h) { this.deltaLast24h = deltaLast24h; }

    public OffsetDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(OffsetDateTime capturedAt) { this.capturedAt = capturedAt; }

    public String getReadmeSnapshot() { return readmeSnapshot; }
    public void setReadmeSnapshot(String readmeSnapshot) { this.readmeSnapshot = readmeSnapshot; }

    public Boolean getProcessed() { return processed; }
    public void setProcessed(Boolean processed) { this.processed = processed; }
}
