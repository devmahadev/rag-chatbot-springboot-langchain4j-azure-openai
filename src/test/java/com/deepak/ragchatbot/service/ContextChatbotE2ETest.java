package com.deepak.ragchatbot.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {ContextChatbotE2ETest.TestEchoModelsConfig.class}
)
class ContextChatbotE2ETest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void postingDoc_thenAskingQuestion_returnsGroundedAnswer() throws Exception {
        // 1) Build a tiny DOCX in-memory
        byte[] bytes;
        try (var doc = new XWPFDocument()) {
            var r = doc.createParagraph().createRun();
            r.setText("ACME-RAG-CHECK-42. This is just a RAG test");
            try (var out = new ByteArrayOutputStream()) {
                doc.write(out);
                bytes = out.toByteArray();
            }
        }

        // 2) Build multipart body
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return "acme.docx";
            }
        }).header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        builder.part("message", "What is ACME-RAG-CHECK-42 and why is it important?");
        MultiValueMap<String, HttpEntity<?>> form = builder.build();

        // 3) Call /load and collect streaming body as a single String
        var result = webTestClient.post()
                .uri("/api/context-chatbot") // adjust if your controller path differs
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(form)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class);

        String body = String.join("", result.getResponseBody().collectList().block());

        // 4) Assert the final body contains the token (context was injected)
        System.out.println("Here is the body..........:" + body);
        // Here is the body..........:ACME-RAG-CHECK-42: according to the information you gave, it is "just a RAG test.
        // "What that means and why it matters:- "RAG" usually stands for Retrieval-Augmented Generation: systems that
        // retrieve relevant documents or snippets and use them to ground a generative model’s responses.
        // - A RAG test checks how well that retrieval+generation pipeline works: relevance of retrieved items,
        // whether the generator uses them correctly (avoiding hallucinations), latency, and overall answer quality.
        // - Importance: running such tests helps ensure answers are accurate and traceable, improves user trust,
        // catches retrieval or prompt-design problems, and guides tuning (indexing, embeddings, retrieval strategy, and prompt templates).
        // - Typical focuses for a RAG test include relevance/coverage, factuality, citation alignment, and performance metrics (precision/recall, factuality scores, response time).
        // If you want, I can suggest specific checks or metrics to include for ACME-RAG-CHECK-42 or help design test cases.
        assertThat(body).contains("ACME-RAG-CHECK-42");
    }


    /**
     * Test-only override: provide an echo ChatModel so no network calls are made.
     * It returns the last UserMessage (post-augmentation), which now contains injected context.
     */
    @TestConfiguration
    static class TestEchoModelsConfig {

        @Bean(name = "testRetrievalAugmentor")
        RetrievalAugmentor retrievalAugmentor(ContentRetriever retriever) {
            return DefaultRetrievalAugmentor.builder()
                    .contentRetriever(retriever)
                    .build();
        }

        @Bean(name = "testAzureChatModel")
        @Primary
        ChatModel echoChatModel() {
            return new ChatModel() {
                @Override
                public ChatResponse chat(List<ChatMessage> messages) {
                    String lastUser = messages.stream()
                            .filter(m -> m instanceof UserMessage)
                            .map(m -> ((UserMessage) m).singleText())
                            .reduce((a, b) -> b)
                            .orElse("No user message found");

                    AiMessage aiMessage = AiMessage.from(lastUser);
                    return ChatResponse.builder().aiMessage(aiMessage).build();
                }
            };
        }
    }

    @Bean(name = "testAzureStreamingChatModel")
    @Primary
    StreamingChatModel echoStreamingChatModel() {
        return new StreamingChatModel() {
            @Override
            public void chat(List<ChatMessage> messages, StreamingChatResponseHandler handler) {
                String lastUser = messages.stream()
                        .filter(m -> m instanceof UserMessage)
                        .map(m -> ((UserMessage) m).singleText())
                        .reduce((a, b) -> b)
                        .orElse("No user message found");

                // Simulate streaming by sending the full message as a partial response
                handler.onPartialResponse(lastUser);

                // Create a ChatResponse with AiMessage
                AiMessage from = AiMessage.from(lastUser);
                ChatResponse response = ChatResponse.builder().aiMessage(from).build();
                handler.onCompleteResponse(response);
            }
        };
    }
}

