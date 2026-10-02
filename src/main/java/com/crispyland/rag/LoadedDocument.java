package com.crispyland.rag;

/** A raw document before chunking. */
public record LoadedDocument(
        String sourceFile,   // e.g. "docs/CLAUDE.md"
        String module,       // "agent" | "mcp-server" | "weather-server" | "docs"
        String fileType,     // "java" | "md" | "yml"
        String text
) {}
