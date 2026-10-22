package com.vectorcore.index;

import com.vectorcore.model.SearchQuery;
import com.vectorcore.model.SearchResult;
import com.vectorcore.model.VectorRecord;

import java.util.List;

/**
 * Common contract for all vector indexes (Flat, HNSW, etc.).
 */
public interface VectorIndex {
    void insert(VectorRecord record);
    boolean delete(String id);
    List<SearchResult> search(SearchQuery query);
    int size();
    void clear();
}
