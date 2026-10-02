package com.crispyland.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class FixedSizeChunkerTest {

    private final FixedSizeChunker chunker = new FixedSizeChunker();

    private LoadedDocument doc(String text) {
        return new LoadedDocument("test.java", "agent", "java", text);
    }

    @Test
    void textShorterThanChunkSizeProducesOneChunk() {
        List<Chunk> chunks = chunker.chunk(doc("hello world"), 800, 100);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).text()).isEqualTo("hello world");
    }

    @Test
    void emptyTextProducesNoChunks() {
        assertThat(chunker.chunk(doc(""), 800, 100)).isEmpty();
    }

    @Test
    void chunkSizeIsRespected() {
        // 25 chars, chunkSize=10, overlap=0 → 3 chunks: [0,10), [10,20), [20,25)
        String text = "abcdefghijklmnopqrstuvwxy"; // exactly 25
        List<Chunk> chunks = chunker.chunk(doc(text), 10, 0);
        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).text()).isEqualTo("abcdefghij");
        assertThat(chunks.get(1).text()).isEqualTo("klmnopqrst");
        assertThat(chunks.get(2).text()).isEqualTo("uvwxy");
    }

    @Test
    void overlapCausesChunksToShareContent() {
        // 20 chars, chunkSize=10, overlap=5 → step=5
        // chunk 0: [0,10), chunk 1: [5,15), chunk 2: [10,20)
        String text = "abcdefghijklmnopqrst"; // exactly 20
        List<Chunk> chunks = chunker.chunk(doc(text), 10, 5);
        assertThat(chunks).hasSize(3);
        // overlap: end of chunk 0 should match start of chunk 1
        String end0   = chunks.get(0).text().substring(5); // chars 5-9
        String start1 = chunks.get(1).text().substring(0, 5); // chars 5-9
        assertThat(end0).isEqualTo(start1);
    }

    @Test
    void chunkIdsAreUniqueAndContainSourceFile() {
        List<Chunk> chunks = chunker.chunk(doc("0123456789abcdefghij"), 10, 0);
        assertThat(chunks).extracting(Chunk::chunkId).doesNotHaveDuplicates();
        assertThat(chunks).allMatch(c -> c.chunkId().startsWith("test.java:"));
    }

    @Test
    void charOffsetMatchesPosition() {
        String text = "0123456789abcdefghij"; // 20 chars, step=10 no overlap
        List<Chunk> chunks = chunker.chunk(doc(text), 10, 0);
        assertThat(chunks.get(0).charOffset()).isEqualTo(0);
        assertThat(chunks.get(1).charOffset()).isEqualTo(10);
    }

    @Test
    void strategyIsFixed() {
        List<Chunk> chunks = chunker.chunk(doc("hello"), 800, 100);
        assertThat(chunks).allMatch(c -> "fixed".equals(c.strategy()));
    }

    @Test
    void metadataIsPreservedFromDocument() {
        LoadedDocument d = new LoadedDocument("src/Foo.java", "mcp-server", "java", "hello");
        Chunk chunk = chunker.chunk(d, 800, 100).get(0);
        assertThat(chunk.sourceFile()).isEqualTo("src/Foo.java");
        assertThat(chunk.module()).isEqualTo("mcp-server");
        assertThat(chunk.fileType()).isEqualTo("java");
    }

    @Test
    void embeddingIsNullBeforeEmbeddingStep() {
        List<Chunk> chunks = chunker.chunk(doc("hello world"), 800, 100);
        assertThat(chunks).allMatch(c -> c.embedding() == null);
    }
}
