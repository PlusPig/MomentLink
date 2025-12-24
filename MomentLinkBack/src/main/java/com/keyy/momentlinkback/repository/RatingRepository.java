package com.keyy.momentlinkback.repository;

import com.keyy.momentlinkback.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByContentIdAndUserId(Long contentId, Long userId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.contentId = :contentId")
    Double calculateAvgRating(@Param("contentId") Long contentId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.contentId = :contentId")
    int countByContentId(@Param("contentId") Long contentId);

    @Query("SELECT AVG(r.score) FROM Rating r")
    Double calculateOverallAvgRating();
}
