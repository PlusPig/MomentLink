package com.keyy.momentlinkback.repository;

import com.keyy.momentlinkback.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByContentIdAndParentIdAndDeleted(
            Long contentId, Long parentId, Integer deleted, Pageable pageable);

    List<Comment> findByParentIdAndDeleted(Long parentId, Integer deleted);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.contentId = :contentId AND c.deleted = 0")
    int countByContentId(Long contentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.deleted = 0")
    Long countActiveComments();

    @Query("SELECT DISTINCT c.userId FROM Comment c WHERE c.deleted = 0 " +
            "AND c.createdAt >= :startDate AND c.createdAt <= :endDate")
    List<Long> findActiveUserIdsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
