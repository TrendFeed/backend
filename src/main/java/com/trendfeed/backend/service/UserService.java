package com.trendfeed.backend.service;

import com.trendfeed.backend.dto.request.UpdateProfileRequest;
import com.trendfeed.backend.dto.response.ComicResponse;
import com.trendfeed.backend.dto.response.PaginatedResponse;
import com.trendfeed.backend.dto.response.UserResponse;
import com.trendfeed.backend.entity.*;
import com.trendfeed.backend.exception.CustomException;
import com.trendfeed.backend.exception.ErrorCode;
import com.trendfeed.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final SavedComicRepository savedComicRepository;
    private final ComicRepository comicRepository;
    
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String uid) {
        User user = userRepository.findByUidWithPreferences(uid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        return mapToUserResponse(user);
    }
    
    @Transactional
    public UserResponse updateUserProfile(String uid, UpdateProfileRequest request) {
        User user = userRepository.findByUidWithPreferences(uid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // Update display name
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        
        // Update preferences
        if (request.getPreferences() != null) {
            UserPreferences preferences = user.getPreferences();
            if (preferences == null) {
                preferences = UserPreferences.builder()
                        .user(user)
                        .build();
                user.setPreferences(preferences);
            }
            
            UpdateProfileRequest.UserPreferencesDto prefDto = request.getPreferences();
            if (prefDto.getInterests() != null) {
                preferences.setInterests(prefDto.getInterests());
            }
            if (prefDto.getNotifications() != null) {
                preferences.setNotifications(prefDto.getNotifications());
            }
            if (prefDto.getComicStyle() != null) {
                preferences.setComicStyle(prefDto.getComicStyle());
            }
        }
        
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }
    
    @Transactional(readOnly = true)
    public PaginatedResponse<ComicResponse> getSavedComics(String uid, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<SavedComic> savedComicsPage = savedComicRepository.findByUserUidWithComic(uid, pageable);
        
        List<ComicResponse> comics = savedComicsPage.getContent().stream()
                .map(this::mapToComicResponse)
                .collect(Collectors.toList());
        
        PaginatedResponse.PaginationInfo pagination = PaginatedResponse.PaginationInfo.builder()
                .currentPage(page)
                .totalPages(savedComicsPage.getTotalPages())
                .totalItems(savedComicsPage.getTotalElements())
                .itemsPerPage(limit)
                .build();
        
        return PaginatedResponse.<ComicResponse>builder()
                .data(comics)
                .pagination(pagination)
                .build();
    }
    
    @Transactional
    public Map<String, Object> saveComic(String uid, Long comicId) {
        // Check if user exists
        if (!userRepository.existsById(uid)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        
        // Check if comic exists
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMIC_NOT_FOUND));
        
        // Check if already saved
        if (savedComicRepository.existsByUserUidAndComic_Id(uid, comicId)) {
            throw new CustomException(ErrorCode.COMIC_ALREADY_SAVED);
        }
        
        SavedComic savedComic = SavedComic.builder()
                .userUid(uid)
                .comic(comic)
                .build();
        
        SavedComic saved = savedComicRepository.save(savedComic);
        
        Map<String, Object> response = new HashMap<>();
        response.put("comicId", comicId);
        response.put("saved", true);
        response.put("savedAt", saved.getSavedAt());
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> unsaveComic(String uid, Long comicId) {
        SavedComic savedComic = savedComicRepository.findByUserUidAndComic_Id(uid, comicId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMIC_NOT_SAVED));
        
        savedComicRepository.delete(savedComic);
        
        Map<String, Object> response = new HashMap<>();
        response.put("comicId", comicId);
        response.put("saved", false);
        
        return response;
    }
    
    private UserResponse mapToUserResponse(User user) {
        UserResponse.UserPreferencesDto preferencesDto = null;
        if (user.getPreferences() != null) {
            preferencesDto = UserResponse.UserPreferencesDto.builder()
                    .interests(user.getPreferences().getInterests())
                    .notifications(user.getPreferences().getNotifications())
                    .comicStyle(user.getPreferences().getComicStyle())
                    .build();
        }
        
        long savedComicsCount = savedComicRepository.countByUserUid(user.getUid());
        UserResponse.UserStatsDto stats = UserResponse.UserStatsDto.builder()
                .savedComics(savedComicsCount)
                .likedComics(0L)
                .commentsCount(0L)
                .build();
        
        return UserResponse.builder()
                .uid(user.getUid())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .photoURL(user.getPhotoUrl())
                .preferences(preferencesDto)
                .stats(stats)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    private ComicResponse mapToComicResponse(SavedComic savedComic) {
        Comic comic = savedComic.getComic();
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
                .savedAt(savedComic.getSavedAt())
                .build();
    }
}
