package com.trendfeed.backend.repository;

import com.trendfeed.backend.entity.ComicEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComicRepository extends JpaRepository<ComicEntity, Long> {
    Page<ComicEntity> findByLanguageIgnoreCase(String language, Pageable pageable);
    Page<ComicEntity> findAll(Pageable pageable);
}
