package com.keyy.momentlinkback.repository;

import com.keyy.momentlinkback.entity.LikeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRecordRepository extends JpaRepository<LikeRecord, Long> {

    boolean existsByUserIdAndContentId(Long userId, Long contentId);

    Optional<LikeRecord> findByUserIdAndContentId(Long userId, Long contentId);

    @Query("SELECT COUNT(l) FROM LikeRecord l")
    Long countAllLikes();

    @Query("SELECT DISTINCT l.userId FROM LikeRecord l " +
            "WHERE l.createdAt >= :startDate AND l.createdAt <= :endDate")
    List<Long> findActiveUserIdsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
