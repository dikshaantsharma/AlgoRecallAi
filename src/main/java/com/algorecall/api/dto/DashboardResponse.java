package com.algorecall.api.dto;

import com.algorecall.api.model.CodeAnalysis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalAnalyses;
    private long totalBookmarks;
    private String favouriteLanguage;
    private String mostCommonMistakeCategory;
    private List<CodeAnalysis> recentSubmissions;
}
