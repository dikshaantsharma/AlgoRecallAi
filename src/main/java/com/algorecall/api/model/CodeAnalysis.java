package com.algorecall.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "code_analyses")
public class CodeAnalysis {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private String language;
    private String category;
    private String problemStatement;
    private String sourceCode;
    private String expectedBehaviour;
    private String actualBehaviourOrError;

    // AI Analysis fields
    private String overallSummary;
    private String mistakeCategory;
    private String rootCause;
    private String whyMistakeHappened;
    private List<String> explanation; // Step-by-step explanation
    private String suggestedImprovements;
    private String optimizedCode;
    private String timeComplexity;
    private String spaceComplexity;
    private List<String> bestPractices;
    private List<String> learningTips;
    private String revisionNotes;
    private String confidenceLevel;

    // User metadata
    private boolean bookmarked;
    private String userNotes;
    
    @Indexed
    private Instant createdAt;
}
