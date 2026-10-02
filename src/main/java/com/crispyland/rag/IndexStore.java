package com.crispyland.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IndexStore {

    private static final Logger log = LoggerFactory.getLogger(IndexStore.class);
    private static final Path DATA_DIR = Path.of("data");

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public void save(IndexFile file, String strategy) throws IOException {
        Files.createDirectories(DATA_DIR);
        Path target = DATA_DIR.resolve("chunks-" + strategy + ".json");
        mapper.writeValue(target.toFile(), file);
        log.info("IndexStore: saved {} chunks to {}", file.chunks().size(), target);
    }

    public Optional<IndexFile> load(String strategy) {
        Path path = DATA_DIR.resolve("chunks-" + strategy + ".json");
        if (!Files.exists(path)) return Optional.empty();
        try {
            return Optional.of(mapper.readValue(path.toFile(), IndexFile.class));
        } catch (IOException e) {
            log.warn("IndexStore: could not read {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }
}
