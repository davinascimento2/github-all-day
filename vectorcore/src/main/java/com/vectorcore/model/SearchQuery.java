package com.vectorcore.model;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Parameters for vector and hybrid search queries.
 */
public record SearchQuery(
        float[] vector,
        String text,
        int topK,
        Predicate<Map<String, Object>> filter,
        float hybridAlpha // 1.0 = 100% dense vector, 0.0 = 100% sparse BM25, 0.5 = 50/50
) {
    public SearchQuery {
        if (topK <= 0) topK = 10;
        if (hybridAlpha < 0.0f) hybridAlpha = 0.0f;
        if (hybridAlpha > 1.0f) hybridAlpha = 1.0f;
    }

    public static SearchQuery vector(float[] vector, int topK) {
        return new SearchQuery(vector, null, topK, null, 1.0f);
    }

    public static SearchQuery vectorWithFilter(float[] vector, int topK, Predicate<Map<String, Object>> filter) {
        return new SearchQuery(vector, null, topK, filter, 1.0f);
    }

    public static SearchQuery hybrid(float[] vector, String text, int topK, float alpha) {
        return new SearchQuery(vector, text, topK, null, alpha);
    }
}
