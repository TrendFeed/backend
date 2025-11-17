package com.trendfeed.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_uid", referencedColumnName = "uid", nullable = false, unique = true)
    private User user;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interests", columnDefinition = "jsonb")
    private List<String> interests;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notifications", columnDefinition = "jsonb")
    private Map<String, Boolean> notifications;
    
    @Column(name = "comic_style", length = 50)
    private String comicStyle;
}
