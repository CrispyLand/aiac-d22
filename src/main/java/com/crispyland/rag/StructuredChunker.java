package com.crispyland.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Splits documents at meaningful structural boundaries:
 * <ul>
 *   <li>Java  — at top-level class / method declarations</li>
 *   <li>Markdown — at headings (H1–H3)</li>
 *   <li>YAML — at top-level keys (zero-indented)</li>
 * </ul>
 * Chunks are semantically coherent and vary widely in size, which is
 * the key difference from the fixed-size strategy.
 */
@Service
public class StructuredChunker {

    // Java: lines that start a new top-level or first-level declaration
    private static final Pattern JAVA_BOUNDARY = Pattern.compile(
            "^(    )?(public|private|protected|static|abstract|final|@Override)",
            Pattern.MULTILINE);

    // Markdown: H1, H2, or H3 headings
    private static final Pattern MD_BOUNDARY = Pattern.compile(
            "^#{1,3} .+", Pattern.MULTILINE);

    // YAML: top-level keys (no leading spaces)
    private static final Pattern YAML_BOUNDARY = Pattern.compile(
            "^[a-zA-Z]", Pattern.MULTILINE);

    public List<Chunk> chunk(LoadedDocument doc) {
        return switch (doc.fileType()) {
            case "java" -> splitByPattern(doc, JAVA_BOUNDARY, 400);
            case "md"   -> splitByPattern(doc, MD_BOUNDARY, 100);
            case "yml"  -> splitByPattern(doc, YAML_BOUNDARY, 50);
            default     -> splitByLines(doc, 60);
        };
    }

    /**
     * Splits text wherever the pattern matches the start of a line.
     * minChunkLength prevents tiny one-line "chunks" from cluttering the index.
     */
    private List<Chunk> splitByPattern(LoadedDocument doc, Pattern boundary, int minLength) {
        String text = doc.text();
        List<Integer> cuts = new ArrayList<>();
        cuts.add(0);

        var matcher = boundary.matcher(text);
        while (matcher.find()) {
            int pos = matcher.start();
            if (pos > 0) cuts.add(pos);
        }
        cuts.add(text.length());

        List<Chunk> result = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < cuts.size() - 1; i++) {
            int from = cuts.get(i);
            int to   = cuts.get(i + 1);
            String slice = text.substring(from, to).stripLeading();
            if (slice.length() < minLength) continue;

            String section = firstLine(slice);
            result.add(new Chunk(
                    doc.sourceFile() + ":" + index,
                    doc.sourceFile(),
                    doc.module(),
                    doc.fileType(),
                    section,
                    "structured",
                    index,
                    from,
                    slice.length(),
                    slice,
                    null
            ));
            index++;
        }
        return result;
    }

    /** Fallback: split every N lines. */
    private List<Chunk> splitByLines(LoadedDocument doc, int linesPerChunk) {
        String[] lines = doc.text().split("\n");
        List<Chunk> result = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < lines.length; i += linesPerChunk) {
            int end = Math.min(i + linesPerChunk, lines.length);
            String slice = String.join("\n", java.util.Arrays.copyOfRange(lines, i, end));
            if (slice.isBlank()) continue;
            result.add(new Chunk(
                    doc.sourceFile() + ":" + index,
                    doc.sourceFile(), doc.module(), doc.fileType(),
                    "lines " + i + "\u2013" + end, "structured",
                    index, 0, slice.length(), slice, null
            ));
            index++;
        }
        return result;
    }

    private static String firstLine(String text) {
        int nl = text.indexOf('\n');
        String line = (nl < 0 ? text : text.substring(0, nl)).strip();
        return line.length() > 80 ? line.substring(0, 80) + "\u2026" : line;
    }
}
