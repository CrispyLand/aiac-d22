package com.crispyland.rag;

/**
 * Abstraction over embedding providers so the rest of the RAG pipeline
 * does not import Spring AI or OpenAI classes directly.
 * Swap out OpenAiEmbeddingAdapter for an ONNX implementation without
 * touching callers.
 */
public interface EmbeddingPort {
    /** Embed a single piece of text. Returns a float array of length {@link #dimension()}. */
    float[] embed(String text);

    /** Batch embed. Default implementation calls embed() per item. */
    default float[][] embedAll(java.util.List<String> texts) {
        return texts.stream()
                .map(this::embed)
                .toArray(float[][]::new);
    }

    /** The model identifier recorded in every index file. */
    String modelName();

    /** Output dimensionality (e.g. 1536 for text-embedding-3-small). */
    int dimension();
}
