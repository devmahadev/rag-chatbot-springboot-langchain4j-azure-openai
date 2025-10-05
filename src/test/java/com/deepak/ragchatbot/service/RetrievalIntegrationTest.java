package com.deepak.ragchatbot.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RetrievalIntegrationTest {

    @Autowired
    RagChatbotService ragChatbotService;
    @Autowired
    EmbeddingStore<TextSegment> embeddingStore;
    @Autowired
    ContentRetriever contentRetriever;

    @TempDir
    Path tmp;

    @Test
    void uploadedDoc_isIngested_andRetrieved() throws Exception {
        // 1) Create a tiny DOCX: "ACME-RAG-CHECK-42. This is just a RAG test"
        String content = "ACME-RAG-CHECK-42. This is just a RAG test";
        File docx = tmp.resolve("acme.docx").toFile();
        try (var doc = new XWPFDocument()) {
            var p = doc.createParagraph().createRun();
            p.setText(content);
            try (FileOutputStream out = new FileOutputStream(docx)) {
                doc.write(out);
            }
        }

        // 2) Ingest it through your RagService (same path your controller uses)
        var resource = new FileSystemResource(docx);
        ragChatbotService.saveSegments(resource);


        // 3) Retrieval should return content containing the token
        List<Content> hits = contentRetriever.retrieve(Query.from("What is ACME-RAG-CHECK-42?"));
        assertThat(hits).isNotEmpty();

        String concatenated = hits.stream()
                .map(c -> c.textSegment().text())
                .reduce("", (a, b) -> a + "\n" + b);

        //System.out.println("Full Response: " + concatenated);
        assertThat(concatenated).contains("ACME-RAG-CHECK-42");
    }
}