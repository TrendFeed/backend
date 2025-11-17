package com.trendfeed.backend.controller;

import com.trendfeed.backend.dto.response.ApiResponse;
import com.trendfeed.backend.dto.response.ComicResponse;
import com.trendfeed.backend.dto.response.PaginatedResponse;
import com.trendfeed.backend.service.ComicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comics", description = "Comics Public API")
public class ComicController {
    
    private final ComicService comicService;
    
    @GetMapping
    @Operation(summary = "Get all comics", description = "Get paginated list of all comics")
    public ResponseEntity<ApiResponse<PaginatedResponse<ComicResponse>>> getAllComics(
            @Parameter(description = "Page number (1-indexed)") 
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Items per page") 
            @RequestParam(defaultValue = "20") int limit,
            
            @Parameter(description = "Sort by: latest, stars, likes") 
            @RequestParam(defaultValue = "latest") String sortBy
    ) {
        log.debug("Getting all comics: page={}, limit={}, sortBy={}", page, limit, sortBy);
        
        PaginatedResponse<ComicResponse> comics = comicService.getAllComics(page, limit, sortBy);
        return ResponseEntity.ok(ApiResponse.success(comics));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get comic by ID", description = "Get detailed information of a specific comic")
    public ResponseEntity<ApiResponse<ComicResponse>> getComicById(
            @Parameter(description = "Comic ID") 
            @PathVariable Long id
    ) {
        log.debug("Getting comic by id: {}", id);
        
        ComicResponse comic = comicService.getComicById(id);
        return ResponseEntity.ok(ApiResponse.success(comic));
    }
    
    @GetMapping("/new")
    @Operation(summary = "Get new comics", description = "Get list of newly added comics")
    public ResponseEntity<ApiResponse<PaginatedResponse<ComicResponse>>> getNewComics(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        log.debug("Getting new comics: page={}, limit={}", page, limit);
        
        PaginatedResponse<ComicResponse> comics = comicService.getNewComics(page, limit);
        return ResponseEntity.ok(ApiResponse.success(comics));
    }
    
    @GetMapping("/language/{language}")
    @Operation(summary = "Get comics by language", description = "Get comics filtered by programming language")
    public ResponseEntity<ApiResponse<PaginatedResponse<ComicResponse>>> getComicsByLanguage(
            @Parameter(description = "Programming language") 
            @PathVariable String language,
            
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        log.debug("Getting comics by language: {}, page={}, limit={}", language, page, limit);
        
        PaginatedResponse<ComicResponse> comics = comicService.getComicsByLanguage(language, page, limit);
        return ResponseEntity.ok(ApiResponse.success(comics));
    }
    
    @PostMapping("/{id}/like")
    @Operation(summary = "Like a comic", description = "Increment like count for a comic")
    public ResponseEntity<ApiResponse<Map<String, Object>>> likeComic(
            @Parameter(description = "Comic ID") 
            @PathVariable Long id
    ) {
        log.debug("Liking comic: {}", id);
        
        comicService.incrementLikes(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("comicId", id);
        response.put("action", "liked");
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/share")
    @Operation(summary = "Share a comic", description = "Increment share count for a comic")
    public ResponseEntity<ApiResponse<Map<String, Object>>> shareComic(
            @Parameter(description = "Comic ID") 
            @PathVariable Long id
    ) {
        log.debug("Sharing comic: {}", id);
        
        comicService.incrementShares(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("comicId", id);
        response.put("action", "shared");
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
