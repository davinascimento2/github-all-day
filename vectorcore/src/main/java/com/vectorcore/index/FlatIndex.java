package com.vectorcore.index;

import com.vectorcore.math.DistanceMetric;
import com.vectorcore.math.VectorMath;
import com.vectorcore.model.SearchQuery;
import com.vectorcore.model.SearchResult;
import com.vectorcore.model.VectorRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Exact k-Nearest Neighbors (KNN) Flat Index.
 * Guarantees 100% recall by computing exact distances across all vectors.
 * Uses parallel streams for high throughput on multi-core architectures.
 */
public class FlatIndex implements VectorIndex {

    private final Map<String, VectorRecord> storage = new ConcurrentHashMap<>();
    private final DistanceMetric metric;
    private final int dimension;

    public FlatIndex(int dimension, DistanceMetric metric) {
        this.dimension = dimension;
        this.metric = Objects.requireNonNull(metric, "Metric cannot be null");
    }

    @Override
    public void insert(VectorRecord record) {
        if (record.dimension() != dimension) {
            throw new IllegalArgumentException(
                    "Dimension mismatch: expected " + dimension + ", got " + record.dimension());
        }
        storage.put(record.id(), record);
    }

    @Override
    public boolean delete(String id) {
        return storage.remove(id) != null;
    }

    @Override
    public List<SearchResult> search(SearchQuery query) {
        if (query.vector() == null || query.vector().length != dimension) {
            throw new IllegalArgumentException("Query vector dimension must match index dimension: " + dimension);
        }

        return storage.values().parallelStream()
                .filter(rec -> query.filter() == null || query.filter().test(rec.payload()))
                .map(rec -> {
                    float dist = VectorMath.distance(query.vector(), rec.vector(), metric);
                    float sim = VectorMath.similarity(query.vector(), rec.vector(), metric);
                    return new SearchResult(rec.id(), sim, dist, rec.text(), rec.payload());
                })
                .sorted() // sorted by score descending
                .limit(query.topK())
                .toList();
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    public void clear() {
        storage.clear();
    }

    public Map<String, VectorRecord> getAll() {
        return Collections.unmodifiableMap(storage);
    }
}
