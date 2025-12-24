package com.keyy.momentlinkback.repository;

import com.keyy.momentlinkback.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    List<SensitiveWord> findByStatus(Integer status);

    boolean existsByWord(String word);
}
