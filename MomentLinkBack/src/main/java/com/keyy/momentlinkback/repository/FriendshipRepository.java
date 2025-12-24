package com.keyy.momentlinkback.repository;

import com.keyy.momentlinkback.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    List<Friendship> findByUserIdAndStatus(Long userId, Integer status);

    List<Friendship> findByFriendIdAndStatus(Long friendId, Integer status);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.userId = :userId OR f.friendId = :userId) AND f.status = :status")
    List<Friendship> findAllFriendships(
            @Param("userId") Long userId,
            @Param("status") Integer status);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.userId = :userId OR f.friendId = :userId) AND f.status IN (:statuses)")
    List<Friendship> findAllFriendshipsByStatuses(
            @Param("userId") Long userId,
            @Param("statuses") List<Integer> statuses);

    @Query("SELECT DISTINCT CASE WHEN f.userId = :userId THEN f.friendId ELSE f.userId END " +
            "FROM Friendship f " +
            "WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = 3")
    List<Long> findBlockedPeerUserIds(@Param("userId") Long userId);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}
