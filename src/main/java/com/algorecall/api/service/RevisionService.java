package com.algorecall.api.service;

import com.algorecall.api.exception.ResourceNotFoundException;
import com.algorecall.api.model.CodeAnalysis;
import com.algorecall.api.model.ChatMessage;
import com.algorecall.api.model.ChatSession;
import com.algorecall.api.repository.CodeAnalysisRepository;
import com.algorecall.api.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevisionService {

    private final CodeAnalysisRepository codeAnalysisRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final AiService aiService;

    // Deprecated single-turn helper (fallback)
    public String askRevision(String userId, String question) {
        List<CodeAnalysis> history = codeAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return aiService.generateRevisionAnswer(question, new ArrayList<>(), history);
    }

    public ChatSession createSession(String userId, String title) {
        String sessionTitle = (title == null || title.trim().isEmpty()) ? "New Chat Session" : title.trim();
        ChatSession session = ChatSession.builder()
                .userId(userId)
                .title(sessionTitle)
                .messages(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return chatSessionRepository.save(session);
    }

    public List<ChatSession> getSessions(String userId) {
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public ChatSession getSessionDetails(String sessionId, String userId) {
        return chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with ID: " + sessionId));
    }

    public String askInSession(String sessionId, String userId, String question) {
        ChatSession session = getSessionDetails(sessionId, userId);
        List<CodeAnalysis> codeHistory = codeAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // Call AI with current chat messages + code history context
        String aiAnswer = aiService.generateRevisionAnswer(question, session.getMessages(), codeHistory);

        // Save USER message
        session.getMessages().add(ChatMessage.builder()
                .sender("USER")
                .content(question)
                .timestamp(Instant.now())
                .build());

        // Save AI message
        session.getMessages().add(ChatMessage.builder()
                .sender("AI")
                .content(aiAnswer)
                .timestamp(Instant.now())
                .build());

        session.setUpdatedAt(Instant.now());
        chatSessionRepository.save(session);

        return aiAnswer;
    }

    public void deleteSession(String sessionId, String userId) {
        ChatSession session = getSessionDetails(sessionId, userId);
        chatSessionRepository.delete(session);
    }
}
