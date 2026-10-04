package com.crispyland.rag;

/** A chunk paired with its cosine similarity score against a query. */
public record ScoredChunk(Chunk chunk, float score) {}
