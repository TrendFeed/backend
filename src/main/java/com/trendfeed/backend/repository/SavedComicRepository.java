package com.trendfeed.backend.repository;

import com.trendfeed.backend.entity.SavedComic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedComicRepository extends JpaRepository<SavedComic, Long> {
    
    @Query("SELECT sc FROM SavedComic sc JOIN FETCH sc.comic WHERE sc.userUid = :userUid ORDER BY sc.savedAt DESC")
    Page<SavedComic> findByUserUidWithComic(@Param("userUid") String userUid, Pageable pageable);
    
    Optional<SavedComic> findByUserUidAndComic_Id(String userUid, Long comicId);
    
    boolean existsByUserUidAndComic_Id(String userUid, Long comicId);
    
    long countByUserUid(String userUid);
    
    void deleteByUserUidAndComic_Id(String userUid, Long comicId);
}
