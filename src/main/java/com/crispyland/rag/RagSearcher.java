package com.crispyland.rag;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RagSearcher {

    private final EmbeddingPort embedding;
    private final IndexStore store;
    private final RagProperties props;

    public RagSearcher(EmbeddingPort embedding, IndexStore store, RagProperties props) {
        this.embedding = embedding;
        this.store     = store;
        this.props     = props;
    }

    /**
     * Embeds the query, scores every chunk in the structured index by cosine similarity,
     * and returns the top-K results above minScore, highest score first.
     */
    public List<ScoredChunk> search(String query) {
        if (!props.embedding().enabled()) return List.of();
        Optional<IndexFile> indexFile = store.load("structured");
        if (indexFile.isEmpty()) return List.of();

        float[] queryVec  = embedding.embed(query);
        int     topK      = props.search().topK();
        float   minScore  = props.search().minScore();

        return indexFile.get().chunks().stream()
            .filter(c -> c.embedding() != null)
            .map(c -> new ScoredChunk(c, cosine(queryVec, c.embedding())))
            .filter(sc -> sc.score() >= minScore)
            .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
            .limit(topK)
            .toList();
    }

    private static float cosine(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0f;
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}
