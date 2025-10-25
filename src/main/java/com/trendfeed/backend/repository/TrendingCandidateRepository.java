package com.trendfeed.backend.repository;

import com.trendfeed.backend.entity.TrendingCandidateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrendingCandidateRepository extends JpaRepository<TrendingCandidateEntity, Long> {

    Optional<TrendingCandidateEntity> findByFullName(String fullName);

    Optional<TrendingCandidateEntity> findByRepoId(Long repoId);

    Optional<TrendingCandidateEntity>
    findFirstByProcessedFalseOrderByCapturedAtAsc();
}
