package com.crispyland.rag;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One piece of a document, ready for retrieval.
 * embedding is null before the embedding step runs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Chunk(
        String chunkId,
        String sourceFile,
        String module,
        String fileType,
        String section,
        String strategy,
        int chunkIndex,
        int charOffset,
        int charLength,
        String text,
        float[] embedding
) {
    /** Copy this chunk with an embedding attached. */
    public Chunk withEmbedding(float[] embedding) {
        return new Chunk(chunkId, sourceFile, module, fileType, section,
                strategy, chunkIndex, charOffset, charLength, text, embedding);
    }
}
