package com.crispyland.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Thin adapter around Spring AI's {@link EmbeddingModel}.
 * Active only when rag.embedding.enabled=true.
 */
@Service
@ConditionalOnProperty(name = "rag.embedding.enabled", havingValue = "true", matchIfMissing = true)
public class OpenAiEmbeddingAdapter implements EmbeddingPort {

    private final EmbeddingModel model;
    private final RagProperties properties;

    public OpenAiEmbeddingAdapter(EmbeddingModel model, RagProperties properties) {
        this.model = model;
        this.properties = properties;
    }

    @Override
    public float[] embed(String text) {
        return model.embed(text);
    }

    @Override
    public String modelName() {
        return properties.embedding().model();
    }

    @Override
    public int dimension() {
        return properties.embedding().dimension();
    }
}
