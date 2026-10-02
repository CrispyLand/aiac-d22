package com.crispyland.rag;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Fallback when embedding is disabled — produces zero vectors so the rest
 * of the pipeline (chunking, saving) still works without an API key.
 */
@Service
@ConditionalOnProperty(name = "rag.embedding.enabled", havingValue = "false")
public class NoOpEmbeddingAdapter implements EmbeddingPort {

    private final RagProperties properties;

    public NoOpEmbeddingAdapter(RagProperties properties) {
        this.properties = properties;
    }

    @Override
    public float[] embed(String text) {
        return new float[properties.embedding().dimension()];
    }

    @Override
    public String modelName() {
        return "none";
    }

    @Override
    public int dimension() {
        return properties.embedding().dimension();
    }
}
