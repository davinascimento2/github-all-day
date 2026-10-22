package com.vectorcore.storage;

import com.vectorcore.index.HnswIndex;
import com.vectorcore.index.HybridSearchEngine;
import com.vectorcore.index.InvertedIndex;
import com.vectorcore.model.CollectionConfig;
import com.vectorcore.model.SearchQuery;
import com.vectorcore.model.SearchResult;
import com.vectorcore.model.VectorRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Collection represents an isolated vector space with an HNSW index and BM25 index.
 */
public class Collection {

    private final CollectionConfig config;
    private final HnswIndex hnswIndex;
    private final InvertedIndex bm25Index;
    private final Map<String, VectorRecord> records = new ConcurrentHashMap<>();
    private final HybridSearchEngine hybridEngine;

    public Collection(CollectionConfig config) {
        this.config = Objects.requireNonNull(config);
        this.hnswIndex = new HnswIndex(
                config.dimension(),
                config.metric(),
                config.hnswM(),
                config.hnswEfConstruction(),
                config.hnswEfSearch()
        );
        this.bm25Index = new InvertedIndex();
        this.hybridEngine = new HybridSearchEngine(hnswIndex, bm25Index, records);
    }

    public void upsert(VectorRecord record) {
        if (record.dimension() != config.dimension()) {
            throw new IllegalArgumentException("Vector dimension " + record.dimension()
                    + " does not match collection dimension " + config.dimension());
        }
        records.put(record.id(), record);
        hnswIndex.insert(record);
        if (record.text() != null && !record.text().isBlank()) {
            bm25Index.indexDocument(record.id(), record.text());
        }
    }

    public void upsertBatch(List<VectorRecord> batch) {
        batch.forEach(this::upsert);
    }

    public boolean delete(String id) {
        VectorRecord removed = records.remove(id);
        if (removed != null) {
            hnswIndex.delete(id);
            bm25Index.removeDocument(id);
            return true;
        }
        return false;
    }

    public Optional<VectorRecord> get(String id) {
        return Optional.ofNullable(records.get(id));
    }

    public List<SearchResult> search(SearchQuery query) {
        return hybridEngine.search(query);
    }

    public int size() {
        return records.size();
    }

    public CollectionConfig getConfig() {
        return config;
    }

    public java.util.Collection<VectorRecord> getAllRecords() {
        return Collections.unmodifiableCollection(records.values());
    }

    public void clear() {
        records.clear();
        hnswIndex.clear();
        bm25Index.clear();
    }
}
