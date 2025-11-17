package com.trendfeed.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "comics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "repo_name", nullable = false, length = 500)
    private String repoName;
    
    @Column(name = "repo_url", nullable = false, columnDefinition = "TEXT")
    private String repoUrl;
    
    @Builder.Default
    @Column(name = "stars")
    private Integer stars = 0;
    
    @Column(name = "language", length = 100)
    private String language;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "panels", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> panels;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_insights", columnDefinition = "jsonb")
    private List<String> keyInsights;
    
    @Builder.Default
    @Column(name = "is_new")
    private Boolean isNew = true;
    
    @Builder.Default
    @Column(name = "likes")
    private Integer likes = 0;
    
    @Builder.Default
    @Column(name = "shares")
    private Integer shares = 0;
    
    @Builder.Default
    @Column(name = "comments")
    private Integer comments = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
