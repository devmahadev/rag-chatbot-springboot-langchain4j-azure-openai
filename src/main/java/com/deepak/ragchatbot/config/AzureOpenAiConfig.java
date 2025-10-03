package com.deepak.ragchatbot.config;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(AzureOpenAiProperties.class)
public class AzureOpenAiConfig {

    /** Creates standard chat model for:
     *        - Regular question-answering
     *        - Single response generation
     *        - Zero temperature for consistent outputs
     */

    /** To Address the error:
     * Conflict: multiple beans of type dev.langchain4j.model.chat.ChatModel are found: [chatModel, openAiChatModelByAPIKey].
     *
     * Startup error is happening because Spring has two ChatModel beans in the context:
     *
     * Mine @Bean ChatModel chatModel(...), and
     * an auto‑configured bean named openAiChatModelByAPIKey that’s created by the LangChain4j OpenAI Spring Boot Starter
     * when OpenAI properties (or its starter) are present.
     *
     * In automatic wiring mode, LangChain4j throws a conflict if more than one candidate exists and asks you to switch to explicit wiring.
     *
     * Solution:
     * Give beans stable names (I like prefixing with azure…), then point @AiService to them:  wiringMode = AiServiceWiringMode.EXPLICIT
     */
    @Bean(name = "azureChatModel")
    ChatModel chatModel(AzureOpenAiProperties azureOpenAiProperties){
        var chatModelProperties = azureOpenAiProperties.chatModel();
        return AzureOpenAiChatModel.builder()
                .endpoint(chatModelProperties.endpoint())
                .apiKey(chatModelProperties.apiKey())
                .deploymentName(chatModelProperties.deploymentName())
                .temperature(0.0)
                .build();
    }

    /** Creates embedding model for:
     *        - Converting text to vectors
     *        - Document indexing
     *        - Similarity search
     */
    /**
     * RagAutoConfig (from the LangChain4j Spring RAG starter) tries to create a ContentRetriever and needs one EmbeddingModel to inject.
     * Since the context has two:
     *
     * azureEmbeddingModel (mine), and
     * openAiEmbeddingModelByApiKey (auto-created by the Azure/OpenAI starter)
     *
     * Spring can’t decide which one to use and fails.
     *
     * Solution: @Primary tells Spring which bean to pick when multiple beans of the same type exist.
     * It does solve this kind of ambiguity for standard Spring injection
     */
    @Bean(name = "azureEmbeddingModel")
    @Primary // <-- pick this one whenever an EmbeddingModel is requested -->
    EmbeddingModel embeddingModel(AzureOpenAiProperties azureOpenAiProperties){
        var embeddingModelProperties = azureOpenAiProperties.embeddingModel();
        return AzureOpenAiEmbeddingModel.builder()
                .endpoint(embeddingModelProperties.endpoint())
                .apiKey(embeddingModelProperties.apiKey())
                .deploymentName(embeddingModelProperties.deploymentName())
                .build();
    }

    /**
     *  Creates streaming model for:
     *        - Real-time response generation
     *        - Token-by-token output
     *        - Better user experience with immediate feedback
     */
    @Bean(name = "azureStreamingChatModel")
    StreamingChatModel streamingChatModel(AzureOpenAiProperties azureOpenAiProperties){
        var streamingModelProperties = azureOpenAiProperties.streamingChatModel();
        return AzureOpenAiStreamingChatModel.builder()
                .endpoint(streamingModelProperties.endpoint())
                .apiKey(streamingModelProperties.apiKey())
                .deploymentName(streamingModelProperties.deploymentName())
                .temperature(0.0)
                .build();
    }
}
