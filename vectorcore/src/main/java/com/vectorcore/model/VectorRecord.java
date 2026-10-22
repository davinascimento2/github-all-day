package com.vectorcore.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable vector record holding ID, vector embedding, and metadata payload.
 */
public record VectorRecord(
        String id,
        float[] vector,
        String text,
        Map<String, Object> payload,
        Instant createdAt
) {
    public VectorRecord {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(vector, "Vector cannot be null");
        payload = payload == null ? Collections.emptyMap() : Collections.unmodifiableMap(payload);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public static VectorRecord of(String id, float[] vector) {
        return new VectorRecord(id, vector, "", Collections.emptyMap(), Instant.now());
    }

    public static VectorRecord of(String id, float[] vector, String text, Map<String, Object> payload) {
        return new VectorRecord(id, vector, text, payload, Instant.now());
    }

    public int dimension() {
        return vector.length;
    }
}
