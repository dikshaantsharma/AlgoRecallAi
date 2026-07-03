package com.algorecall.api.service;

import com.algorecall.api.dto.AiResponseDto;
import com.algorecall.api.dto.SubmitCodeRequest;
import com.algorecall.api.exception.ResourceNotFoundException;
import com.algorecall.api.model.CodeAnalysis;
import com.algorecall.api.repository.CodeAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeAnalysisService {

    private final CodeAnalysisRepository codeAnalysisRepository;
    private final AiService aiService;

    public CodeAnalysis createAnalysis(SubmitCodeRequest request, String userId) {
        AiResponseDto aiFeedback = aiService.analyzeCode(
                request.getSourceCode(),
                request.getUserPrompt()
        );

        CodeAnalysis analysis = CodeAnalysis.builder()
                .userId(userId)
                .title(aiFeedback.getTitle())
                .language(aiFeedback.getLanguage())
                .category(aiFeedback.getCategory())
                .problemStatement(aiFeedback.getProblemStatement())
                .sourceCode(request.getSourceCode())
                .expectedBehaviour(aiFeedback.getExpectedBehaviour())
                .actualBehaviourOrError(aiFeedback.getActualBehaviourOrError())
                
                // AI Response fields
                .overallSummary(aiFeedback.getOverallSummary())
                .mistakeCategory(aiFeedback.getMistakeCategory())
                .rootCause(aiFeedback.getRootCause())
                .whyMistakeHappened(aiFeedback.getWhyMistakeHappened())
                .explanation(aiFeedback.getExplanation())
                .suggestedImprovements(aiFeedback.getSuggestedImprovements())
                .optimizedCode(aiFeedback.getOptimizedCode())
                .timeComplexity(aiFeedback.getTimeComplexity())
                .spaceComplexity(aiFeedback.getSpaceComplexity())
                .bestPractices(aiFeedback.getBestPractices())
                .learningTips(aiFeedback.getLearningTips())
                .revisionNotes(aiFeedback.getRevisionNotes())
                .confidenceLevel(aiFeedback.getConfidenceLevel())
                
                // User metadata
                .bookmarked(false)
                .userNotes("")
                .createdAt(Instant.now())
                .build();

        return codeAnalysisRepository.save(analysis);
    }

    public List<CodeAnalysis> getUserHistory(String userId, String keyword, Boolean bookmarked, String language) {
        List<CodeAnalysis> results;

        if (keyword != null && !keyword.trim().isEmpty()) {
            results = codeAnalysisRepository.search(userId, keyword.trim());
        } else if (bookmarked != null) {
            results = codeAnalysisRepository.findByUserIdAndBookmarkedOrderByCreatedAtDesc(userId, bookmarked);
        } else {
            results = codeAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        // Apply secondary filtering (e.g. language or bookmark combinators)
        return results.stream()
                .filter(a -> language == null || language.trim().isEmpty() || a.getLanguage().equalsIgnoreCase(language.trim()))
                .filter(a -> bookmarked == null || keyword == null || keyword.trim().isEmpty() || a.isBookmarked() == bookmarked)
                .collect(Collectors.toList());
    }

    public CodeAnalysis getAnalysisById(String id, String userId) {
        return codeAnalysisRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis record not found with ID: " + id));
    }

    public CodeAnalysis toggleBookmark(String id, String userId) {
        CodeAnalysis analysis = getAnalysisById(id, userId);
        analysis.setBookmarked(!analysis.isBookmarked());
        return codeAnalysisRepository.save(analysis);
    }

    public CodeAnalysis updateNotes(String id, String userId, String userNotes) {
        CodeAnalysis analysis = getAnalysisById(id, userId);
        analysis.setUserNotes(userNotes);
        return codeAnalysisRepository.save(analysis);
    }

    public void deleteAnalysis(String id, String userId) {
        CodeAnalysis analysis = getAnalysisById(id, userId);
        codeAnalysisRepository.delete(analysis);
    }
}
