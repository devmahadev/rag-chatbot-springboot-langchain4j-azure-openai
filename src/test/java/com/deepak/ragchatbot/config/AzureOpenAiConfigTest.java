package com.deepak.ragchatbot.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureOpenAiConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AzureOpenAiConfig.class)
            .withPropertyValues(
                    "spring.autoconfigure.exclude=dev.langchain4j.azure.openai.spring.AutoConfig",
                    "langchain4j.azure-open-ai.chat-model.endpoint=https://deepak.cognitiveservices.azure.com",
                    "langchain4j.azure-open-ai.chat-model.api-key=dummy",
                    "langchain4j.azure-open-ai.chat-model.deployment-name=gpt-4o-mini",
                    "langchain4j.azure-open-ai.embedding-model.endpoint=https://deepak.cognitiveservices.azure.com",
                    "langchain4j.azure-open-ai.embedding-model.api-key=dummy",
                    "langchain4j.azure-open-ai.embedding-model.deployment-name=text-embedding-3-large",
                    "langchain4j.azure-open-ai.streaming-chat-model.endpoint=https://deepak.cognitiveservices.azure.com",
                    "langchain4j.azure-open-ai.streaming-chat-model.api-key=dummy",
                    "langchain4j.azure-open-ai.streaming-chat-model.deployment-name=gpt-4o-mini"
            );


    @Test
    void models_areCreated_andEmbeddingModel_isPrimary() {
        contextRunner.run(ctx -> {
            // Named beans exist
            assertThat(ctx).hasBean("azureChatModel");
            assertThat(ctx).hasBean("azureStreamingChatModel");
            assertThat(ctx).hasBean("azureEmbeddingModel");

            // Only one bean of each type (no auto-config interference)
            assertThat(ctx).hasSingleBean(ChatModel.class);
            assertThat(ctx).hasSingleBean(StreamingChatModel.class);
            assertThat(ctx).hasSingleBean(EmbeddingModel.class);

            // Because @Primary is on azureEmbeddingModel, a by-type lookup resolves to it
            EmbeddingModel byType = ctx.getBean(EmbeddingModel.class);
            EmbeddingModel byName = (EmbeddingModel) ctx.getBean("azureEmbeddingModel");
            assertThat(byType).isSameAs(byName);
        });
    }

}
