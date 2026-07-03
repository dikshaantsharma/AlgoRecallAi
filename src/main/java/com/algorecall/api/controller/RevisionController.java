package com.algorecall.api.controller;

import com.algorecall.api.dto.AskRevisionRequest;
import com.algorecall.api.dto.AskRevisionResponse;
import com.algorecall.api.dto.CreateSessionRequest;
import com.algorecall.api.model.ChatSession;
import com.algorecall.api.model.User;
import com.algorecall.api.service.RevisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/revision")
@RequiredArgsConstructor
public class RevisionController {

    private final RevisionService revisionService;

    // Single-turn chat helper (grounded in code history)
    @PostMapping("/ask")
    public ResponseEntity<AskRevisionResponse> askRevision(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AskRevisionRequest request
    ) {
        String answer = revisionService.askRevision(user.getId(), request.getQuestion());
        return ResponseEntity.ok(new AskRevisionResponse(answer));
    }

    // List all chat sessions
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getSessions(
            @AuthenticationPrincipal User user
    ) {
        List<ChatSession> sessions = revisionService.getSessions(user.getId());
        return ResponseEntity.ok(sessions);
    }

    // Create a new chat session
    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(
            @AuthenticationPrincipal User user,
            @RequestBody(required = false) CreateSessionRequest request
    ) {
        String title = (request != null) ? request.getTitle() : null;
        ChatSession session = revisionService.createSession(user.getId(), title);
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    // Get chat session details (messages)
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSession> getSessionDetails(
            @AuthenticationPrincipal User user,
            @PathVariable String sessionId
    ) {
        ChatSession session = revisionService.getSessionDetails(sessionId, user.getId());
        return ResponseEntity.ok(session);
    }

    // Ask a question in a specific session (with multi-turn memory)
    @PostMapping("/sessions/{sessionId}/ask")
    public ResponseEntity<AskRevisionResponse> askInSession(
            @AuthenticationPrincipal User user,
            @PathVariable String sessionId,
            @Valid @RequestBody AskRevisionRequest request
    ) {
        String answer = revisionService.askInSession(sessionId, user.getId(), request.getQuestion());
        return ResponseEntity.ok(new AskRevisionResponse(answer));
    }

    // Delete a chat session
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @AuthenticationPrincipal User user,
            @PathVariable String sessionId
    ) {
        revisionService.deleteSession(sessionId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
