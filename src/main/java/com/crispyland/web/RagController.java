package com.crispyland.web;

import com.crispyland.rag.IndexBuilder;
import com.crispyland.rag.LoadedDocument;
import com.crispyland.rag.RagSearcher;
import com.crispyland.rag.RagState;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@ControllerAdvice
public class RagController {

    private static final Set<String> ALLOWED = Set.of("java", "md", "yml", "yaml", "txt", "pdf");

    private final RagState ragState;
    private final IndexBuilder indexBuilder;
    private final RagSearcher searcher;

    public RagController(RagState ragState, IndexBuilder indexBuilder, RagSearcher searcher) {
        this.ragState     = ragState;
        this.indexBuilder = indexBuilder;
        this.searcher     = searcher;
    }

    @ModelAttribute("rag")
    public RagState ragState() {
        return ragState;
    }

    @PostMapping("/rag/toggle")
    public String toggle() {
        ragState.toggle();
        return "redirect:/";
    }

    /**
     * Accepts a single file upload, chunks it with the structured strategy,
     * embeds the chunks, and merges them into the structured index.
     *
     * @param label   optional custom label used as the sourceFile in the index;
     *                falls back to "uploads/<filename>" when blank
     */
    @PostMapping("/rag/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "label", required = false) String label,
                         RedirectAttributes redirectAttrs) {
        if (file.isEmpty()) {
            redirectAttrs.addFlashAttribute("uploadError", "No file selected.");
            return "redirect:/indexer";
        }

        String filename = file.getOriginalFilename();
        String ext = (filename != null && filename.contains("."))
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "txt";

        if (!ALLOWED.contains(ext)) {
            redirectAttrs.addFlashAttribute("uploadError",
                    "Unsupported format: ." + ext + ". Allowed: " + String.join(", ", ALLOWED));
            return "redirect:/indexer";
        }

        String sourceFile = (label != null && !label.isBlank())
                ? label.strip()
                : "uploads/" + filename;

        // Normalise extension for the chunker: yaml → yml
        String chunkExt = "yaml".equals(ext) ? "yml" : ext;

        try {
            String text = extractText(file, ext);
            LoadedDocument doc = new LoadedDocument(sourceFile, "upload", chunkExt, text);
            indexBuilder.mergeDocument(doc);
            redirectAttrs.addFlashAttribute("uploadOk", "Indexed: " + sourceFile);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("uploadError", "Failed: " + e.getMessage());
        }
        return "redirect:/indexer";
    }

    private static String extractText(MultipartFile file, String ext) throws IOException {
        if ("pdf".equals(ext)) {
            try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                return new PDFTextStripper().getText(doc);
            }
        }
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }
}
