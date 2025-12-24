package com.keyy.momentlinkback.repository;

import com.keyy.momentlinkback.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    Page<Content> findByStatus(Integer status, Pageable pageable);

    Page<Content> findByUserId(Long userId, Pageable pageable);

    Page<Content> findByTypeAndStatus(Integer type, Integer status, Pageable pageable);

    Page<Content> findByTagsContainingAndStatus(String tags, Integer status, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.status = 1 AND c.userId NOT IN (:excludedUserIds)")
    Page<Content> findByStatusExcludingUsers(
            @Param("excludedUserIds") List<Long> excludedUserIds,
            Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.type = :type AND c.status = 1 AND c.userId NOT IN (:excludedUserIds)")
    Page<Content> findByTypeAndStatusExcludingUsers(
            @Param("type") Integer type,
            @Param("excludedUserIds") List<Long> excludedUserIds,
            Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.tags LIKE %:tags% AND c.status = 1 AND c.userId NOT IN (:excludedUserIds)")
    Page<Content> findByTagsContainingAndStatusExcludingUsers(
            @Param("tags") String tags,
            @Param("excludedUserIds") List<Long> excludedUserIds,
            Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND c.status = 1 " +
            "AND c.createdAt BETWEEN :startDate AND :endDate")
    Page<Content> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT COUNT(c) FROM Content c WHERE c.status = 1 AND c.deleted = 0")
    Long countActiveContents();

    @Query("SELECT COUNT(c) FROM Content c WHERE c.createdAt >= :startDate")
    Long countNewContents(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT c FROM Content c WHERE c.status = 1 " +
            "ORDER BY c.likeCount DESC, c.viewCount DESC")
    List<Content> findHotContents(Pageable pageable);

    @Query("SELECT DISTINCT c.userId FROM Content c WHERE c.status = 1 " +
            "AND c.createdAt >= :startDate AND c.createdAt <= :endDate")
    List<Long> findActiveUserIdsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 管理员查询方法 - 排除已删除的内容
    @Query("SELECT c FROM Content c WHERE c.deleted = :deleted")
    Page<Content> findByDeleted(@Param("deleted") Integer deleted, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND c.deleted = :deleted")
    Page<Content> findByUserIdAndDeleted(
            @Param("userId") Long userId,
            @Param("deleted") Integer deleted,
            Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.type = :type AND c.deleted = :deleted")
    Page<Content> findByTypeAndDeleted(
            @Param("type") Integer type,
            @Param("deleted") Integer deleted,
            Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.status = :status AND c.deleted = :deleted")
    Page<Content> findByStatusAndDeleted(
            @Param("status") Integer status,
            @Param("deleted") Integer deleted,
            Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND c.type = :type AND c.deleted = :deleted")
    Page<Content> findByUserIdAndTypeAndDeleted(
            @Param("userId") Long userId,
            @Param("type") Integer type,
            @Param("deleted") Integer deleted,
            Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND c.status = :status AND c.deleted = :deleted")
    Page<Content> findByUserIdAndStatusAndDeleted(
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("deleted") Integer deleted,
            Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.type = :type AND c.status = :status AND c.deleted = :deleted")
    Page<Content> findByTypeAndStatusAndDeleted(
            @Param("type") Integer type,
            @Param("status") Integer status,
            @Param("deleted") Integer deleted,
            Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.userId = :userId AND c.type = :type AND c.status = :status AND c.deleted = :deleted")
    Page<Content> findByUserIdAndTypeAndStatusAndDeleted(
            @Param("userId") Long userId,
            @Param("type") Integer type,
            @Param("status") Integer status,
            @Param("deleted") Integer deleted,
            Pageable pageable);
}
