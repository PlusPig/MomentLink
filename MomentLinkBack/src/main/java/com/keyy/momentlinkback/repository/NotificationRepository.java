package com.keyy.momentlinkback.repository;

import com.keyy.momentlinkback.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndType(Long userId, Integer type, Pageable pageable);

    Page<Notification> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 1 WHERE n.userId = :userId AND n.status = 0")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}
