package com.crispyland.rag;

import org.springframework.stereotype.Component;

/** Thread-safe RAG on/off switch. Starts enabled. */
@Component
public class RagState {
    private volatile boolean enabled = true;

    public boolean isEnabled() { return enabled; }

    public void toggle() { enabled = !enabled; }
}
