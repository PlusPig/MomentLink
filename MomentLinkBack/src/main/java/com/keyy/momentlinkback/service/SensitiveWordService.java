package com.keyy.momentlinkback.service;

import com.keyy.momentlinkback.entity.SensitiveWord;
import com.keyy.momentlinkback.repository.SensitiveWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;
    private Set<String> sensitiveWords = new HashSet<>();

    @PostConstruct
    public void init() {
        loadSensitiveWords();
    }

    public void loadSensitiveWords() {
        List<SensitiveWord> words = sensitiveWordRepository.findByStatus(1);
        sensitiveWords = new HashSet<>();
        words.forEach(word -> sensitiveWords.add(word.getWord()));
        log.info("已加载 {} 个敏感词", sensitiveWords.size());
    }

    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String filtered = text;
        for (String word : sensitiveWords) {
            if (filtered.contains(word)) {
                String replacement = "*".repeat(word.length());
                filtered = filtered.replace(word, replacement);
            }
        }

        return filtered;
    }

    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (String word : sensitiveWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
