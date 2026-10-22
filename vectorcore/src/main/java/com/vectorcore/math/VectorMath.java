package com.vectorcore.math;

import java.util.Objects;

/**
 * High-performance vector arithmetic and distance calculations.
 * Implemented with cache-friendly loop unrolling and validation.
 */
public final class VectorMath {

    private VectorMath() {}

    public static float distance(float[] a, float[] b, DistanceMetric metric) {
        validateDimensions(a, b);
        return switch (metric) {
            case COSINE -> cosineDistance(a, b);
            case EUCLIDEAN -> euclideanDistance(a, b);
            case DOT_PRODUCT -> -dotProduct(a, b); // Negative so smaller means more similar
            case MANHATTAN -> manhattanDistance(a, b);
        };
    }

    public static float similarity(float[] a, float[] b, DistanceMetric metric) {
        return switch (metric) {
            case COSINE -> 1.0f - cosineDistance(a, b);
            case EUCLIDEAN -> 1.0f / (1.0f + euclideanDistance(a, b));
            case DOT_PRODUCT -> dotProduct(a, b);
            case MANHATTAN -> 1.0f / (1.0f + manhattanDistance(a, b));
        };
    }

    public static float dotProduct(float[] a, float[] b) {
        float sum = 0.0f;
        int i = 0;
        int limit = a.length - 3;
        // 4-way loop unrolling for hardware pipelining
        for (; i < limit; i += 4) {
            sum += (a[i] * b[i]) + (a[i + 1] * b[i + 1]) + (a[i + 2] * b[i + 2]) + (a[i + 3] * b[i + 3]);
        }
        for (; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public static float euclideanDistance(float[] a, float[] b) {
        float sum = 0.0f;
        int i = 0;
        int limit = a.length - 3;
        for (; i < limit; i += 4) {
            float d0 = a[i] - b[i];
            float d1 = a[i + 1] - b[i + 1];
            float d2 = a[i + 2] - b[i + 2];
            float d3 = a[i + 3] - b[i + 3];
            sum += (d0 * d0) + (d1 * d1) + (d2 * d2) + (d3 * d3);
        }
        for (; i < a.length; i++) {
            float diff = a[i] - b[i];
            sum += diff * diff;
        }
        return (float) Math.sqrt(sum);
    }

    public static float cosineDistance(float[] a, float[] b) {
        float dot = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0.0f || normB == 0.0f) {
            return 1.0f;
        }

        float cosSim = dot / ((float) (Math.sqrt(normA) * Math.sqrt(normB)));
        // Clamp to [-1.0, 1.0] for floating point precision issues
        cosSim = Math.max(-1.0f, Math.min(1.0f, cosSim));
        return 1.0f - cosSim;
    }

    public static float manhattanDistance(float[] a, float[] b) {
        float sum = 0.0f;
        for (int i = 0; i < a.length; i++) {
            sum += Math.abs(a[i] - b[i]);
        }
        return sum;
    }

    public static float[] normalize(float[] v) {
        float norm = 0.0f;
        for (float f : v) {
            norm += f * f;
        }
        if (norm == 0.0f) return v.clone();
        float inv = 1.0f / (float) Math.sqrt(norm);
        float[] res = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            res[i] = v[i] * inv;
        }
        return res;
    }

    public static void validateDimensions(float[] a, float[] b) {
        Objects.requireNonNull(a, "Vector a cannot be null");
        Objects.requireNonNull(b, "Vector b cannot be null");
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vector dimension mismatch: " + a.length + " vs " + b.length);
        }
    }
}
