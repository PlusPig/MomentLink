package com.keyy.momentlinkback.repository;

import com.keyy.momentlinkback.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findByStatus(Integer status, Pageable pageable);

    Page<User> findByUsernameContainingOrNicknameContaining(
            String username, String nickname, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1 AND u.deleted = 0")
    Long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    Long countNewUsers(java.time.LocalDateTime startDate);
}
