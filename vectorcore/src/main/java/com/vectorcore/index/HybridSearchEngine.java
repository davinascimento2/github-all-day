package com.vectorcore.index;

import com.vectorcore.model.SearchQuery;
import com.vectorcore.model.SearchResult;
import com.vectorcore.model.VectorRecord;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid Search Engine integrating Dense Vector Search (HNSW / Flat)
 * and Sparse Lexical Search (BM25) via Reciprocal Rank Fusion (RRF).
 */
public class HybridSearchEngine {

    private static final int RRF_K = 60; // Standard IR constant for Reciprocal Rank Fusion

    private final VectorIndex vectorIndex;
    private final InvertedIndex bm25Index;
    private final Map<String, VectorRecord> rawRecords;

    public HybridSearchEngine(VectorIndex vectorIndex, InvertedIndex bm25Index, Map<String, VectorRecord> rawRecords) {
        this.vectorIndex = Objects.requireNonNull(vectorIndex);
        this.bm25Index = Objects.requireNonNull(bm25Index);
        this.rawRecords = Objects.requireNonNull(rawRecords);
    }

    public List<SearchResult> search(SearchQuery query) {
        boolean hasVector = query.vector() != null;
        boolean hasText = query.text() != null && !query.text().isBlank();

        // If only vector is provided, delegate directly to vector index
        if (hasVector && !hasText) {
            return vectorIndex.search(query);
        }

        // If only text is provided, perform BM25 search
        if (!hasVector && hasText) {
            Map<String, Float> bm25Scores = bm25Index.searchBM25(query.text());
            return bm25Scores.entrySet().stream()
                    .map(e -> {
                        VectorRecord rec = rawRecords.get(e.getKey());
                        if (rec == null) return null;
                        if (query.filter() != null && !query.filter().test(rec.payload())) return null;
                        return new SearchResult(rec.id(), e.getValue(), 0.0f, rec.text(), rec.payload());
                    })
                    .filter(Objects::nonNull)
                    .sorted()
                    .limit(query.topK())
                    .toList();
        }

        // Hybrid: Both vector and text are present -> apply Reciprocal Rank Fusion (RRF)
        List<SearchResult> vectorResults = vectorIndex.search(query);
        Map<String, Float> bm25Scores = bm25Index.searchBM25(query.text());

        List<Map.Entry<String, Float>> sortedBm25 = bm25Scores.entrySet().stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .toList();

        float alpha = query.hybridAlpha(); // e.g. 0.7 for 70% dense, 30% sparse
        float beta = 1.0f - alpha;

        Map<String, Float> rrfScores = new HashMap<>();

        // RRF score for vector rank
        for (int rank = 0; rank < vectorResults.size(); rank++) {
            String id = vectorResults.get(rank).id();
            float score = alpha * (1.0f / (RRF_K + rank + 1));
            rrfScores.merge(id, score, Float::sum);
        }

        // RRF score for BM25 rank
        for (int rank = 0; rank < sortedBm25.size(); rank++) {
            String id = sortedBm25.get(rank).getKey();
            float score = beta * (1.0f / (RRF_K + rank + 1));
            rrfScores.merge(id, score, Float::sum);
        }

        return rrfScores.entrySet().stream()
                .map(e -> {
                    VectorRecord rec = rawRecords.get(e.getKey());
                    if (rec == null) return null;
                    if (query.filter() != null && !query.filter().test(rec.payload())) return null;
                    return new SearchResult(rec.id(), e.getValue(), 0.0f, rec.text(), rec.payload());
                })
                .filter(Objects::nonNull)
                .sorted()
                .limit(query.topK())
                .toList();
    }
}
