package com.deepak.ragchatbot.config;

import module langchain4j;
import module langchain4j.core;
import module spring.beans;
import module spring.context;

@Configuration
public class EmbeddingConfig {

    /**
     * Splits documents into chunks of 1000 tokens.
     * Applies a 100-token overlap between chunks to preserve context.
     * Uses the provided embedding model to convert text chunks into vector embeddings.
     * Stores the generated vectors in the configured embedding store.
     *
     * @param embeddingModel
     * @param embeddingStore
     * @return
     */
    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(1000, 100))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    /**
     * Performs vector similarity search to identify relevant document segments.
     * Returns up to 3 top-matching results based on similarity.
     * Filters results to include only those with a similarity score above 0.6.
     * Uses the same embedding model to encode the query for accurate matching.
     *
     * @param embeddingModel
     * @param embeddingStore
     * @return
     */

    @Bean(name = "contentRetriever")
    public EmbeddingStoreContentRetriever embeddingStoreContentRetriever(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.5)
                .build();
    }

    /**
     * RetrievalAugmentor
     * - Injects retrieved context into the user’s prompt before sending it to the LLM.
     * - Ensures the model has relevant information from knowledge base (e.g., document embeddings) to generate a grounded and accurate response.
     *
     * @param retriever
     * @return
     */
    @Bean(name = "retrievalAugmentor")
    public RetrievalAugmentor retrievalAugmentor(@Qualifier("contentRetriever") ContentRetriever retriever) {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(retriever)
                .build();
    }

    /**
     * How It Works in Your Code
     *
     * 1. ContentRetriever fetches relevant text segments from the EmbeddingStore based on the user query.
     *
     * 2. RetrievalAugmentor takes:
     * - The original user message.
     * - The retrieved segments.
     *
     * 3. Combines them into an augmented prompt (for example: "User asked: X. Context: [retrieved text]").
     * 4. Passes the augmented prompt to the ChatModel for response generation.
     */
}
