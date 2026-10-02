package com.crispyland.rag;

/**
 * Per-file header written at the top of every index JSON so the retrieval
 * step knows which model and dimension to expect without reading the chunks.
 */
public record IndexMetadata(
        String strategy,
        String embeddingModel,
        int embeddingDimension,
        String indexedAt,
        int totalChunks,
        int avgChunkSize,
        int minChunkSize,
        int maxChunkSize
) {}
