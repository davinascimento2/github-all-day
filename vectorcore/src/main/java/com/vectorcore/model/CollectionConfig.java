package com.vectorcore.model;

import com.vectorcore.math.DistanceMetric;

/**
 * Configuration options for a vector collection.
 */
public record CollectionConfig(
        String name,
        int dimension,
        DistanceMetric metric,
        int hnswM,              // Number of bi-directional links per node (e.g. 16)
        int hnswEfConstruction, // Search depth during index build (e.g. 100)
        int hnswEfSearch        // Search depth during query (e.g. 50)
) {
    public static CollectionConfig defaultCosine(String name, int dimension) {
        return new CollectionConfig(name, dimension, DistanceMetric.COSINE, 16, 100, 50);
    }

    public static CollectionConfig defaultEuclidean(String name, int dimension) {
        return new CollectionConfig(name, dimension, DistanceMetric.EUCLIDEAN, 16, 100, 50);
    }
}
