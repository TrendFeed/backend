package com.trendfeed.backend.repository;

import com.trendfeed.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.preferences WHERE u.uid = :uid")
    Optional<User> findByUidWithPreferences(@Param("uid") String uid);
    
    boolean existsByEmail(String email);
}
