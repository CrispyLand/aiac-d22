package com.crispyland.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public record RagProperties(
        int chunkSize,
        int overlap,
        String indexDir,
        Search search,
        Embedding embedding) {

    public RagProperties {
        chunkSize = (chunkSize <= 0) ? 800 : chunkSize;
        overlap   = (overlap < 0)    ? 100 : overlap;
        indexDir  = (indexDir == null || indexDir.isBlank()) ? "./data/index" : indexDir;
        if (search == null) search = new Search(3, 0.3f);
        if (embedding == null) {
            embedding = new Embedding(true, "text-embedding-3-small", 1536);
        }
    }

    public record Search(int topK, float minScore) {
        public Search {
            topK = (topK <= 0) ? 3 : topK;
        }
    }

    public record Embedding(boolean enabled, String model, int dimension) {
        public Embedding {
            model     = (model == null || model.isBlank()) ? "text-embedding-3-small" : model;
            dimension = (dimension <= 0) ? 1536 : dimension;
        }
    }
}
