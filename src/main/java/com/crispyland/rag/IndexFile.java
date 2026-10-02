package com.crispyland.rag;

import java.util.List;

/** Root object written to data/chunks-{strategy}.json. */
public record IndexFile(IndexMetadata metadata, List<Chunk> chunks) {}
