package com.deepak.ragchatbot.config;

import module jakarta.validation;
import module spring.boot;

/** One root + nested records
 * Keeps all Azure OpenAI configuration in one place
 * Clear grouping, fewer beans to pass around
 * Works great in TDD—easy to assert binding + defaults
 * Supports shared settings later (e.g., common endpoint or apiVersion)
 */
@ConfigurationProperties("langchain4j.azure-open-ai")
public record AzureOpenAiProperties(
        @Valid ChatModel chatModel,
        @Valid EmbeddingModel embeddingModel,
        @Valid StreamingChatModel streamingChatModel

) {
    // Spring Boot’s relaxed binding maps deployment-name → deploymentName, and chat-model → chatModel automatically.
    public record ChatModel(
            @NotBlank String endpoint,
            @NotBlank String apiKey,
            @NotBlank String deploymentName
    ) {}

    public record EmbeddingModel(
            @NotBlank String endpoint,
            @NotBlank String apiKey,
            @NotBlank String deploymentName
    ) {}


    public record StreamingChatModel(
            @NotBlank String endpoint,
            @NotBlank String apiKey,
            @NotBlank String deploymentName
    ) {}
}