package com.algorecall.api.service;

import com.algorecall.api.dto.DashboardResponse;
import com.algorecall.api.model.CodeAnalysis;
import com.algorecall.api.repository.CodeAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CodeAnalysisRepository codeAnalysisRepository;

    public DashboardResponse getDashboardData(String userId) {
        List<CodeAnalysis> allAnalyses = codeAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<CodeAnalysis> recent = codeAnalysisRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);

        long totalAnalyses = allAnalyses.size();
        long totalBookmarks = allAnalyses.stream().filter(CodeAnalysis::isBookmarked).count();

        String favouriteLanguage = allAnalyses.stream()
                .filter(a -> a.getLanguage() != null && !a.getLanguage().trim().isEmpty())
                .collect(Collectors.groupingBy(a -> a.getLanguage().trim(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String mostCommonMistakeCategory = allAnalyses.stream()
                .filter(a -> a.getMistakeCategory() != null && !a.getMistakeCategory().trim().isEmpty())
                .collect(Collectors.groupingBy(a -> a.getMistakeCategory().trim(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        return DashboardResponse.builder()
                .totalAnalyses(totalAnalyses)
                .totalBookmarks(totalBookmarks)
                .favouriteLanguage(favouriteLanguage)
                .mostCommonMistakeCategory(mostCommonMistakeCategory)
                .recentSubmissions(recent)
                .build();
    }
}
