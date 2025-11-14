package com.trendfeed.backend.controller;

import com.trendfeed.backend.dto.ComicCreateRequest;
import com.trendfeed.backend.dto.ComicResponse;
import com.trendfeed.backend.entity.ComicEntity;
import com.trendfeed.backend.service.ComicCommandService;
import com.trendfeed.backend.service.ComicQueryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comics")
public class ComicController {

    private final ComicCommandService commandService;
    private final ComicQueryService queryService;

    public ComicController(ComicCommandService commandService, ComicQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /* ========== CREATE (POST) ========== */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ComicResponse create(@Valid @RequestBody ComicCreateRequest req) {
        ComicEntity e = commandService.create(req);
        return toResponse(e);
    }

    /* ========== LIST (GET) ========== */
    // 예: /api/comics?sortBy=stars&order=desc&page=0&limit=20&language=TypeScript
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ComicResponse> list(
            @RequestParam(defaultValue = "trending") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String language
    ) {
        String sortKey = switch (sortBy) {
            case "stars" -> "stars";
            case "recent" -> "createdAt";
            case "trending" -> "trendingScore";
            default -> "trendingScore";
        };
        Sort sort = "asc".equalsIgnoreCase(order)
                ? Sort.by(sortKey).ascending()
                : Sort.by(sortKey).descending();
        PageRequest pr = PageRequest.of(page, Math.min(limit, 100), sort);

        Page<ComicEntity> p = queryService.list(language, pr);
        return p.map(ComicController::toResponse);
    }

    /* ========== GET ONE (GET) ========== */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ComicResponse getOne(@PathVariable Long id) {
        return toResponse(queryService.get(id));
    }

    /* ========== TRENDING (GET) ========== */
    @GetMapping(value = "/trending", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ComicResponse> trending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pr = PageRequest.of(page, Math.min(limit, 50), Sort.by("trendingScore").descending());
        return queryService.list(null, pr).map(ComicController::toResponse);
    }

    /* ========== SHARE (POST) ========== */
    @PostMapping("/{id}/share")
    public ResponseEntity<Void> share(@PathVariable Long id) {
        commandService.addShare(id);
        return ResponseEntity.ok().build();
    }

    /* ========== mapper ========== */
    private static ComicResponse toResponse(ComicEntity e) {
        ComicResponse r = new ComicResponse();
        r.setId(e.getId());
        r.setRepoName(e.getRepoName());
        r.setRepoUrl(e.getRepoUrl());
        r.setStars(e.getStars());
        r.setLanguage(e.getLanguage());
        r.setPanels(e.getPanels());
        r.setKeyInsights(e.getKeyInsights());
        r.setIsNew(e.getIsNew());
        r.setLikes(e.getLikes());
        r.setShares(e.getShares());
        r.setComments(e.getComments());
        r.setTrendingScore(e.getTrendingScore());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
