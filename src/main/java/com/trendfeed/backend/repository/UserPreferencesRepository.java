package com.trendfeed.backend.repository;

import com.trendfeed.backend.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    
    Optional<UserPreferences> findByUser_Uid(String userUid);
    
    void deleteByUser_Uid(String userUid);
}
