package com.algorecall.api.service;

import com.algorecall.api.dto.AiResponseDto;
import com.algorecall.api.model.CodeAnalysis;
import com.algorecall.api.model.ChatMessage;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public AiResponseDto analyzeCode(String sourceCode, String userPrompt) {
        String systemInstruction = 
                "You are an expert senior software engineer mentoring a junior developer. " +
                "Analyze the submitted code and optional context, and provide detailed, professional, and mentoring feedback. " +
                "You MUST respond ONLY with a valid JSON object matching the schema below. " +
                "Do NOT include any markdown formatting, wrappers, or backticks (like ```json ... ```). Output raw JSON string only.\n\n" +
                "JSON Schema:\n" +
                "{\n" +
                "  \"title\": \"A short, descriptive title of the code/mistake (e.g. Java Binary Search Infinite Loop).\",\n" +
                "  \"language\": \"The programming language of the code (e.g. Java, Python, C++, JavaScript).\",\n" +
                "  \"category\": \"The category or domain of the code/problem (e.g. DSA, Web Development, Systems, Database).\",\n" +
                "  \"problemStatement\": \"A concise summary of what the code is attempting to do.\",\n" +
                "  \"expectedBehaviour\": \"What the code is expected to do when working correctly.\",\n" +
                "  \"actualBehaviourOrError\": \"What the code actually does or the error/crash it produces.\",\n" +
                "  \"overallSummary\": \"A concise summary of the code and the issue encountered.\",\n" +
                "  \"mistakeCategory\": \"The classification of the mistake (e.g., Logical Error, Syntax Error, Resource Leak, Concurrency Issue, Inefficient Algorithm, Null Reference, Index Out of Bounds, Incorrect Library Usage, Other).\",\n" +
                "  \"rootCause\": \"A clear, technical description of the root cause of the error.\",\n" +
                "  \"whyMistakeHappened\": \"Explanation of why this error happens conceptually and why the original code logic was incorrect.\",\n" +
                "  \"explanation\": [\n" +
                "    \"Step 1 of the logical breakdown of the issue.\",\n" +
                "    \"Step 2 of the logical breakdown.\"\n" +
                "  ],\n" +
                "  \"suggestedImprovements\": \"List of suggestions and concrete steps to resolve the issue.\",\n" +
                "  \"optimizedCode\": \"The complete, fully corrected, and optimized source code implementing the best practices.\",\n" +
                "  \"timeComplexity\": \"Time complexity of the original vs optimized code (e.g., Original: O(N^2), Optimized: O(N log N)).\",\n" +
                "  \"spaceComplexity\": \"Space complexity of the original vs optimized code (e.g., Original: O(N), Optimized: O(1)).\",\n" +
                "  \"bestPractices\": [\n" +
                "    \"Best practice recommendation 1\",\n" +
                "    \"Best practice recommendation 2\"\n" +
                "  ],\n" +
                "  \"learningTips\": [\n" +
                "    \"Actionable tip or concept to study to prevent this in the future.\"\n" +
                "  ],\n" +
                "  \"revisionNotes\": \"Key takeaways to revise before a technical coding interview regarding this type of bug.\",\n" +
                "  \"confidenceLevel\": \"HIGH, MEDIUM, or LOW\"\n" +
                "}";

        String formattedUserPrompt = String.format(
                "Source Code:\n%s\n\n" +
                "Optional User Context/Hint:\n%s\n",
                sourceCode, 
                (userPrompt == null || userPrompt.trim().isEmpty() ? "None provided" : userPrompt)
        );

        String combinedPrompt = systemInstruction + "\n\nUser Input:\n" + formattedUserPrompt;

        try {
            String rawResponse = chatModel.call(combinedPrompt);
            String cleanedJson = cleanJson(rawResponse);
            return objectMapper.readValue(cleanedJson, AiResponseDto.class);
        } catch (Exception e) {
            System.err.println("AI Call/Parsing failed: " + e.getMessage());
            e.printStackTrace();
            return AiResponseDto.builder()
                    .title("Failed to analyze code")
                    .language("Unknown")
                    .category("Other")
                    .problemStatement("Unable to analyze code")
                    .expectedBehaviour("Unable to determine")
                    .actualBehaviourOrError("Unable to determine")
                    .overallSummary("Failed to analyze the code due to an AI response error.")
                    .mistakeCategory("Analysis Error")
                    .rootCause(e.getMessage())
                    .whyMistakeHappened("Could not parse AI output or connect to AI provider.")
                    .explanation(List.of("Please verify your API key and connection configuration."))
                    .suggestedImprovements("Check logs for the full exception trace.")
                    .optimizedCode(sourceCode)
                    .timeComplexity("N/A")
                    .spaceComplexity("N/A")
                    .bestPractices(List.of("Verify network parameters."))
                    .learningTips(List.of("Review application.yml configuration."))
                    .revisionNotes("Ensure API limits are not exceeded.")
                    .confidenceLevel("LOW")
                    .build();
        }
    }

    public String generateRevisionAnswer(String question, List<ChatMessage> chatHistory, List<CodeAnalysis> history) {
        String historyContext = history.isEmpty() 
            ? "The user has no previous coding history analyses logged in AlgoRecall AI yet."
            : history.stream().map(h -> String.format(
                "- Title: %s | Language: %s | Mistake Category: %s\n" +
                "  Summary: %s\n" +
                "  Root Cause: %s\n" +
                "  Revision Notes: %s\n" +
                "  User Notes: %s",
                h.getTitle(), h.getLanguage(), h.getMistakeCategory(),
                h.getOverallSummary(), h.getRootCause(), h.getRevisionNotes(), 
                (h.getUserNotes() == null ? "" : h.getUserNotes())
            )).collect(Collectors.joining("\n\n"));

        StringBuilder historyText = new StringBuilder();
        if (chatHistory != null && !chatHistory.isEmpty()) {
            for (ChatMessage msg : chatHistory) {
                historyText.append(String.format("[%s]: %s\n", msg.getSender(), msg.getContent()));
            }
        }
        String formattedChatHistory = historyText.toString();

        String systemInstruction = 
                "You are an AI Revision Assistant inside AlgoRecall AI. " +
                "Your goal is to answer the user's question about their coding mistakes and pattern of errors based on their logged history. " +
                "Be encouraging, analytical, and professional, acting as a senior mentor.\n\n" +
                "User's Logged History:\n" +
                historyContext + "\n\n" +
                "Conversation History Memory:\n" +
                (formattedChatHistory.isEmpty() ? "No previous messages in this session yet." : formattedChatHistory) + "\n\n" +
                "Guidelines:\n" +
                "1. If the user asks about specific mistakes (e.g. 'What mistakes do I repeat?'), aggregate their history and identify patterns.\n" +
                "2. If the user has no history, advise them to submit code analyses first, but offer general tips based on their question.\n" +
                "3. Reference specific submissions by title where appropriate so the user knows you are looking at their history.\n" +
                "4. Structure your response clearly with markdown bullet points, bold text, and code blocks if showing correct vs incorrect code examples.\n" +
                "5. Pay attention to the Conversation History Memory context, which represents the ongoing conversation in this chat session. Use it to answer follow-up questions.";

        String combinedPrompt = systemInstruction + "\n\nUser Question: " + question;

        try {
            return chatModel.call(combinedPrompt);
        } catch (Exception e) {
            System.err.println("AI Revision Call failed: " + e.getMessage());
            return "Unable to retrieve a response from the Revision Assistant at this moment. Error: " + e.getMessage();
        }
    }

    private String cleanJson(String response) {
        if (response == null) return "";
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }
}
