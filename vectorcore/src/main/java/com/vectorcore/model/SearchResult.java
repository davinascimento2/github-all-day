package com.vectorcore.model;

import java.util.Map;

/**
 * Result of a nearest neighbor search.
 */
public record SearchResult(
        String id,
        float score,
        float distance,
        String text,
        Map<String, Object> payload
) implements Comparable<SearchResult> {

    @Override
    public int compareTo(SearchResult o) {
        // Higher score first (descending)
        return Float.compare(o.score, this.score);
    }
}
