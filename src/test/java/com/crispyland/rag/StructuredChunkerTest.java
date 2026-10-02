package com.crispyland.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class StructuredChunkerTest {

    private final StructuredChunker chunker = new StructuredChunker();

    private LoadedDocument doc(String type, String text) {
        return new LoadedDocument("file." + type, "agent", type, text);
    }

    // ── Markdown ──────────────────────────────────────────────────────────────

    @Test
    void markdownSplitsAtH1Headings() {
        // minLength=100 for md, so each section body must push the slice over 100 chars
        String body = "x".repeat(120);
        String md = "# Introduction\n" + body + "\n\n# Getting Started\n" + body;
        List<Chunk> chunks = chunker.chunk(doc("md", md));
        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).text()).startsWith("# Introduction");
        assertThat(chunks.get(1).text()).startsWith("# Getting Started");
    }

    @Test
    void markdownSplitsAtH2AndH3() {
        String body = "y".repeat(120);
        String md = "## Section One\n" + body + "\n### Subsection\n" + body;
        List<Chunk> chunks = chunker.chunk(doc("md", md));
        assertThat(chunks).hasSize(2);
    }

    @Test
    void markdownChunksBelowMinLengthAreDropped() {
        // "# Tiny\nok\n\n" is well under 100 chars → dropped
        String md = "# Tiny\nok\n\n# Real Section\n" + "x".repeat(200);
        List<Chunk> chunks = chunker.chunk(doc("md", md));
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).text()).contains("Real Section");
    }

    @Test
    void markdownStrategyIsStructured() {
        String md = "# Section\n" + "x".repeat(200);
        List<Chunk> chunks = chunker.chunk(doc("md", md));
        assertThat(chunks).allMatch(c -> "structured".equals(c.strategy()));
    }

    // ── YAML ─────────────────────────────────────────────────────────────────

    @Test
    void yamlSplitsAtTopLevelKeys() {
        // minLength=50 for yaml; each section must be > 50 chars
        String yaml = "spring:\n" + "  application:\n    name: test\n    timeout: 30s\n    extra: value\n"
                + "server:\n  port: 8080\n  contextPath: /api\n  compression: true\n  maxConnections: 100\n"
                + "agent:\n  key: value\n  model: gpt-oss\n  endpoint: http://localhost\n  enabled: true\n";
        List<Chunk> chunks = chunker.chunk(doc("yml", yaml));
        assertThat(chunks.size()).isGreaterThanOrEqualTo(1);
        assertThat(chunks).allMatch(c -> "structured".equals(c.strategy()));
    }

    // ── Java ─────────────────────────────────────────────────────────────────

    @Test
    void javaSplitsAtMethodBoundaries() {
        // minLength=400 for java; pad method bodies to exceed it
        String body = "        // " + "x".repeat(400) + "\n";
        String java = "package com.example;\n\npublic class Foo {\n"
                + "    public String hello() {\n" + body + "        return \"hello\";\n    }\n\n"
                + "    public int add(int a, int b) {\n" + body + "        return a + b;\n    }\n}";
        List<Chunk> chunks = chunker.chunk(doc("java", java));
        assertThat(chunks).isNotEmpty();
        assertThat(chunks).allMatch(c -> "structured".equals(c.strategy()));
    }

    // ── Unknown type fallback ─────────────────────────────────────────────────

    @Test
    void unknownFileTypeFallsBackToLineSplitting() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append("line ").append(i).append("\n");
        List<Chunk> chunks = chunker.chunk(doc("txt", sb.toString()));
        assertThat(chunks).isNotEmpty();
        assertThat(chunks).allMatch(c -> "structured".equals(c.strategy()));
    }

    // ── Section label ─────────────────────────────────────────────────────────

    @Test
    void sectionLabelIsFirstLineOfChunk() {
        String md = "# My Heading\n" + "x".repeat(200);
        List<Chunk> chunks = chunker.chunk(doc("md", md));
        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).section()).isEqualTo("# My Heading");
    }

    @Test
    void longFirstLineIsTruncatedAt80Chars() {
        String longLine = "# " + "A".repeat(100);
        String md = longLine + "\n" + "x".repeat(200);
        List<Chunk> chunks = chunker.chunk(doc("md", md));
        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).section().length()).isLessThanOrEqualTo(81); // 80 chars + ellipsis
    }
}
