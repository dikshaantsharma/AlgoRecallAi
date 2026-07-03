package com.algorecall.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponseDto {
    private String title;
    private String language;
    private String category;
    private String problemStatement;
    private String expectedBehaviour;
    private String actualBehaviourOrError;

    private String overallSummary;
    private String mistakeCategory;
    private String rootCause;
    private String whyMistakeHappened;
    private List<String> explanation;
    private String suggestedImprovements;
    private String optimizedCode;
    private String timeComplexity;
    private String spaceComplexity;
    private List<String> bestPractices;
    private List<String> learningTips;
    private String revisionNotes;
    private String confidenceLevel;
}
