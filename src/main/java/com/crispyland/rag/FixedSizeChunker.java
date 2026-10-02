package com.crispyland.rag;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Splits a document into chunks of a fixed character count with an overlap.
 * Language-agnostic: it does not look at the content at all.
 */
@Service
public class FixedSizeChunker {

    public List<Chunk> chunk(LoadedDocument doc, int chunkSize, int overlap) {
        List<Chunk> result = new ArrayList<>();
        String text = doc.text();
        int step = Math.max(1, chunkSize - overlap);
        int index = 0;

        for (int offset = 0; offset < text.length(); offset += step) {
            int end = Math.min(offset + chunkSize, text.length());
            String slice = text.substring(offset, end);

            result.add(new Chunk(
                    doc.sourceFile() + ":" + index,
                    doc.sourceFile(),
                    doc.module(),
                    doc.fileType(),
                    "chars " + offset + "\u2013" + end,
                    "fixed",
                    index,
                    offset,
                    slice.length(),
                    slice,
                    null
            ));
            index++;
            if (end == text.length()) break;
        }
        return result;
    }
}
