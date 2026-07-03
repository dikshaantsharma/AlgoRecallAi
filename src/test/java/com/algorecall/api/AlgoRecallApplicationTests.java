package com.algorecall.api;

import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

@SpringBootTest
class AlgoRecallApplicationTests {

    @MockitoBean
    private VectorStore vectorStore;

    @MockitoBean
    private QdrantClient qdrantClient;

    @MockitoBean
    private EmbeddingModel embeddingModel;

    @MockitoBean
    private ChatModel chatModel;

    @Test
    void contextLoads() {
    }

}
