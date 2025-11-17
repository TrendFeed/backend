package com.trendfeed.backend.repository;

import com.trendfeed.backend.entity.Comic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Long> {
    
    Optional<Comic> findByRepoName(String repoName);
    
    Page<Comic> findByIsNewTrue(Pageable pageable);
    
    Page<Comic> findByLanguage(String language, Pageable pageable);
}
