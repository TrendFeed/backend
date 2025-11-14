package com.trendfeed.backend.service;

import com.trendfeed.backend.dto.ComicCreateRequest;
import com.trendfeed.backend.entity.ComicEntity;
import com.trendfeed.backend.repository.ComicRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ComicCommandService {

    private final ComicRepository repo;

    public ComicCommandService(ComicRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ComicEntity create(ComicCreateRequest req) {
        ComicEntity e = new ComicEntity();
        e.setRepoId(req.getRepoId());
        e.setRepoName(req.getRepoName());
        e.setRepoUrl(req.getRepoUrl());
        e.setStars(req.getStars());
        e.setLanguage(req.getLanguage());
        e.setTrendingScore(req.getTrendingScore());
        if (req.getPanels() != null) e.setPanels(req.getPanels());
        if (req.getKeyInsights() != null) e.setKeyInsights(req.getKeyInsights());
        e.setIsNew(Boolean.TRUE);
        return repo.save(e);
    }

    @Transactional
    public void addShare(Long id) {
        ComicEntity e = repo.findById(id).orElseThrow();
        e.setShares(e.getShares() == null ? 1 : e.getShares() + 1);
        repo.save(e);
    }
}
