package com.deepak.ragchatbot.config;

import module langchain4j.core;
import module langchain4j.pgvector;
import module spring.context;
import module spring.boot;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;


@Configuration
@EnableConfigurationProperties({EmbeddingStoreProperties.class})
public class ChatAssistantConfiguration {

    private final EmbeddingModel embeddingModel;

    public ChatAssistantConfiguration(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     *  Provides a per-chat memory that:
     *  - maintains individual chat history for each chat Id
     *  - keeps 10 last messages in memory to maintain conversational context
     * @return
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        return chatId -> MessageWindowChatMemory.withMaxMessages(10);
    }

    /**
     * Defines a Spring bean that configures
     * - a PostgreSQL-backed embedding store using the pgvector extension for storing and querying text segment embeddings
     *   with automatic table creation and dimension matching.
     * - Returns a fully built PgVectorEmbeddingStore instance for storing and querying TextSegment embeddings.
     * @param embeddingStoreProperties
     * @return
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingStoreProperties embeddingStoreProperties){
        return PgVectorEmbeddingStore.builder()
                .host(embeddingStoreProperties.host())
                .port(embeddingStoreProperties.port())
                .database(embeddingStoreProperties.database())
                .user(embeddingStoreProperties.user())
                .password(embeddingStoreProperties.password())
                .table(embeddingStoreProperties.table())
                .dimension(embeddingModel.dimension())
                .createTable(true)
                .build();
    }
}
