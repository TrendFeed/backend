package com.trendfeed.backend.service;

import com.trendfeed.backend.dto.response.ComicResponse;
import com.trendfeed.backend.dto.response.PaginatedResponse;
import com.trendfeed.backend.entity.Comic;
import com.trendfeed.backend.exception.CustomException;
import com.trendfeed.backend.exception.ErrorCode;
import com.trendfeed.backend.repository.ComicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComicService {
    
    private final ComicRepository comicRepository;
    
    @Transactional(readOnly = true)
    public PaginatedResponse<ComicResponse> getAllComics(int page, int limit, String sortBy) {
        Sort sort = getSortOption(sortBy);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        
        Page<Comic> comicsPage = comicRepository.findAll(pageable);
        
        List<ComicResponse> comics = comicsPage.getContent().stream()
                .map(this::mapToComicResponse)
                .collect(Collectors.toList());
        
        PaginatedResponse.PaginationInfo pagination = PaginatedResponse.PaginationInfo.builder()
                .currentPage(page)
                .totalPages(comicsPage.getTotalPages())
                .totalItems(comicsPage.getTotalElements())
                .itemsPerPage(limit)
                .build();
        
        return PaginatedResponse.<ComicResponse>builder()
                .data(comics)
                .pagination(pagination)
                .build();
    }
    
    @Transactional(readOnly = true)
    public ComicResponse getComicById(Long id) {
        Comic comic = comicRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COMIC_NOT_FOUND));
        
        return mapToComicResponse(comic);
    }
    
    @Transactional(readOnly = true)
    public PaginatedResponse<ComicResponse> getNewComics(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comic> comicsPage = comicRepository.findByIsNewTrue(pageable);
        
        List<ComicResponse> comics = comicsPage.getContent().stream()
                .map(this::mapToComicResponse)
                .collect(Collectors.toList());
        
        PaginatedResponse.PaginationInfo pagination = PaginatedResponse.PaginationInfo.builder()
                .currentPage(page)
                .totalPages(comicsPage.getTotalPages())
                .totalItems(comicsPage.getTotalElements())
                .itemsPerPage(limit)
                .build();
        
        return PaginatedResponse.<ComicResponse>builder()
                .data(comics)
                .pagination(pagination)
                .build();
    }
    
    @Transactional(readOnly = true)
    public PaginatedResponse<ComicResponse> getComicsByLanguage(String language, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "stars"));
        Page<Comic> comicsPage = comicRepository.findByLanguage(language, pageable);
        
        List<ComicResponse> comics = comicsPage.getContent().stream()
                .map(this::mapToComicResponse)
                .collect(Collectors.toList());
        
        PaginatedResponse.PaginationInfo pagination = PaginatedResponse.PaginationInfo.builder()
                .currentPage(page)
                .totalPages(comicsPage.getTotalPages())
                .totalItems(comicsPage.getTotalElements())
                .itemsPerPage(limit)
                .build();
        
        return PaginatedResponse.<ComicResponse>builder()
                .data(comics)
                .pagination(pagination)
                .build();
    }
    
    @Transactional
    public void incrementLikes(Long comicId) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMIC_NOT_FOUND));
        
        comic.setLikes(comic.getLikes() + 1);
        comicRepository.save(comic);
    }
    
    @Transactional
    public void incrementShares(Long comicId) {
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMIC_NOT_FOUND));
        
        comic.setShares(comic.getShares() + 1);
        comicRepository.save(comic);
    }
    
    private Sort getSortOption(String sortBy) {
        return switch (sortBy) {
            case "stars" -> Sort.by(Sort.Direction.DESC, "stars");
            case "likes" -> Sort.by(Sort.Direction.DESC, "likes");
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
    
    private ComicResponse mapToComicResponse(Comic comic) {
        return ComicResponse.builder()
                .id(comic.getId())
                .repoName(comic.getRepoName())
                .repoUrl(comic.getRepoUrl())
                .stars(comic.getStars())
                .language(comic.getLanguage())
                .panels(comic.getPanels())
                .keyInsights(comic.getKeyInsights())
                .isNew(comic.getIsNew())
                .likes(comic.getLikes())
                .shares(comic.getShares())
                .comments(comic.getComments())
                .createdAt(comic.getCreatedAt())
                .build();
    }
}
