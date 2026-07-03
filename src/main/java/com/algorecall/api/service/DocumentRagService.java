package com.algorecall.api.service;

import com.algorecall.api.dto.UploadDocumentRequest;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.PayloadSchemaType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentRagService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final QdrantClient qdrantClient;

    @Value("${spring.ai.vectorstore.qdrant.collection-name}")
    private String collectionName;

    @PostConstruct
    public void initIndex() {
        try {
            qdrantClient.createPayloadIndexAsync(
                collectionName,
                "userId",
                PayloadSchemaType.Keyword,
                null,
                true,
                null,
                null
            ).get();
            System.out.println("Qdrant payload index for userId initialized successfully.");
        } catch (Exception e) {
            System.err.println("Note: Qdrant payload index creation: " + e.getMessage());
        }
    }

    public void uploadPdf(String userId, org.springframework.web.multipart.MultipartFile file) {
        try (java.io.InputStream is = file.getInputStream();
             org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.pdmodel.PDDocument.load(is)) {

            org.apache.pdfbox.text.PDFTextStripper pdfStripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = pdfStripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("The uploaded PDF does not contain extractable text.");
            }

            List<String> chunks = splitText(text, 2000, 200);
            List<Document> docs = new java.util.ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                docs.add(new Document(
                    String.format("Source File: %s (Chunk %d/%d)\nContent:\n%s",
                        file.getOriginalFilename(), (i + 1), chunks.size(), chunks.get(i)),
                    Map.of(
                        "userId", userId,
                        "docType", "user_uploaded_doc",
                        "fileName", file.getOriginalFilename(),
                        "chunkIndex", i
                    )
                ));
            }
            vectorStore.add(docs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse and index PDF file: " + e.getMessage(), e);
        }
    }

    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> chunks = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        int length = text.length();
        int start = 0;
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            chunks.add(text.substring(start, end));
            start += chunkSize - overlap;
            if (start >= length || chunkSize - overlap <= 0) {
                break;
            }
        }
        return chunks;
    }

    public void uploadDocument(String userId, UploadDocumentRequest request) {
        Document doc = new Document(
            String.format("Document Title: %s\nContent:\n%s", request.getTitle(), request.getContent()),
            Map.of(
                "userId", userId,
                "docType", "user_uploaded_doc"
            )
        );
        vectorStore.add(List.of(doc));
    }

    public String queryDocuments(String userId, String question) {
        try {
            // Retrieve top 5 most relevant document context segments uploaded by this user
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(question)
                    .filterExpression(new FilterExpressionBuilder().eq("userId", userId).build())
                    .topK(5)
                    .build();

            List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);

            if (relevantDocs.isEmpty()) {
                return "No matching context found. Please make sure you have uploaded relevant reference documents first.";
            }

            String context = relevantDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n---\n\n"));

            String systemPrompt = String.format(
                "You are an AI Assistant that answers user questions based strictly on the uploaded document context below.\n" +
                "Ground your answer only in the context provided. If the answer cannot be found in the context, politely explain that the document doesn't contain this information.\n\n" +
                "Uploaded Document Context:\n%s\n\n" +
                "User Question: %s\n\n" +
                "Answer:",
                context,
                question
            );

            return chatModel.call(systemPrompt);
        } catch (Exception e) {
            System.err.println("Document RAG query failed: " + e.getMessage());
            e.printStackTrace();
            return "Failed to query documents due to a backend error: " + e.getMessage();
        }
    }
}
