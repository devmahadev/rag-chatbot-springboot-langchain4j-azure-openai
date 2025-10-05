package com.deepak.ragchatbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "embedding.store")
public record EmbeddingStoreProperties(
        String host,
        int port,
        String database,
        String user,
        String password,
        String table,
        int dimension,
        boolean createTable
) {
}
