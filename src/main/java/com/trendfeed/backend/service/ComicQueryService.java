package com.trendfeed.backend.service;

import com.trendfeed.backend.entity.ComicEntity;
import com.trendfeed.backend.repository.ComicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ComicQueryService {
    private final ComicRepository repo;
    public ComicQueryService(ComicRepository repo) { this.repo = repo; }

    public Page<ComicEntity> list(String language, Pageable pageable) {
        if (language != null && !language.isBlank() && !"all".equalsIgnoreCase(language)) {
            return repo.findByLanguageIgnoreCase(language, pageable);
        }
        return repo.findAll(pageable);
    }

    public ComicEntity get(Long id) {
        return repo.findById(id).orElseThrow();
    }
}
