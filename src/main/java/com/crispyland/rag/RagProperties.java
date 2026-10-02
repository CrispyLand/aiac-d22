package com.crispyland.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public record RagProperties(
        int chunkSize,
        int overlap,
        Embedding embedding) {

    public RagProperties {
        chunkSize = (chunkSize <= 0) ? 800 : chunkSize;
        overlap   = (overlap < 0)    ? 100 : overlap;
        if (embedding == null) {
            embedding = new Embedding(true, "text-embedding-3-small", 1536);
        }
    }

    public record Embedding(boolean enabled, String model, int dimension) {
        public Embedding {
            model     = (model == null || model.isBlank()) ? "text-embedding-3-small" : model;
            dimension = (dimension <= 0) ? 1536 : dimension;
        }
    }
}
