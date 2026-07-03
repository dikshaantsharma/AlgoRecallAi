package com.algorecall.api.controller;

import com.algorecall.api.dto.SubmitCodeRequest;
import com.algorecall.api.dto.UpdateNotesRequest;
import com.algorecall.api.model.CodeAnalysis;
import com.algorecall.api.model.User;
import com.algorecall.api.service.CodeAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class CodeAnalysisController {

    private final CodeAnalysisService codeAnalysisService;

    @PostMapping
    public ResponseEntity<CodeAnalysis> submitCode(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SubmitCodeRequest request
    ) {
        CodeAnalysis analysis = codeAnalysisService.createAnalysis(request, user.getId());
        return new ResponseEntity<>(analysis, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CodeAnalysis>> getHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean bookmarked,
            @RequestParam(required = false) String language
    ) {
        List<CodeAnalysis> history = codeAnalysisService.getUserHistory(user.getId(), keyword, bookmarked, language);
        return ResponseEntity.ok(history);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CodeAnalysis> getAnalysisDetails(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        CodeAnalysis analysis = codeAnalysisService.getAnalysisById(id, user.getId());
        return ResponseEntity.ok(analysis);
    }

    @PutMapping("/{id}/bookmark")
    public ResponseEntity<CodeAnalysis> toggleBookmark(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        CodeAnalysis analysis = codeAnalysisService.toggleBookmark(id, user.getId());
        return ResponseEntity.ok(analysis);
    }

    @PutMapping("/{id}/notes")
    public ResponseEntity<CodeAnalysis> updateNotes(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @RequestBody UpdateNotesRequest request
    ) {
        CodeAnalysis analysis = codeAnalysisService.updateNotes(id, user.getId(), request.getUserNotes());
        return ResponseEntity.ok(analysis);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        codeAnalysisService.deleteAnalysis(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
