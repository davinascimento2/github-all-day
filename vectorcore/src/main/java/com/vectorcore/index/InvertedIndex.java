package com.vectorcore.index;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Inverted Index with BM25 (Best Matching 25) scoring.
 * Provides full-text lexical search to complement dense vector embeddings in Hybrid RAG pipelines.
 */
public class InvertedIndex {

    private static final double K1 = 1.2;
    private static final double B = 0.75;
    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

    private static final Set<String> STOPWORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "if", "in", "on", "at", "to", "for", "of", "with", "by", "is", "it"
    );

    // Term -> Map<DocId, TermFrequency>
    private final Map<String, Map<String, Integer>> invertedIndex = new ConcurrentHashMap<>();
    // DocId -> Document Length (number of tokens)
    private final Map<String, Integer> docLengths = new ConcurrentHashMap<>();
    private long totalTokens = 0;

    public void indexDocument(String docId, String text) {
        if (text == null || text.isBlank()) return;

        List<String> tokens = tokenize(text);
        if (tokens.isEmpty()) return;

        docLengths.put(docId, tokens.size());
        totalTokens += tokens.size();

        Map<String, Integer> freqs = new HashMap<>();
        for (String t : tokens) {
            freqs.merge(t, 1, Integer::sum);
        }

        freqs.forEach((term, count) -> {
            invertedIndex.computeIfAbsent(term, k -> new ConcurrentHashMap<>()).put(docId, count);
        });
    }

    public void removeDocument(String docId) {
        Integer len = docLengths.remove(docId);
        if (len != null) {
            totalTokens = Math.max(0, totalTokens - len);
            invertedIndex.values().forEach(m -> m.remove(docId));
        }
    }

    public Map<String, Float> searchBM25(String queryText) {
        List<String> queryTerms = tokenize(queryText);
        if (queryTerms.isEmpty() || docLengths.isEmpty()) {
            return Collections.emptyMap();
        }

        int totalDocs = docLengths.size();
        double avgDocLength = (double) totalTokens / totalDocs;
        Map<String, Float> scores = new HashMap<>();

        for (String term : queryTerms) {
            Map<String, Integer> postings = invertedIndex.get(term);
            if (postings == null) continue;

            int docFreq = postings.size();
            // Standard BM25 IDF formulation
            double idf = Math.log(1.0 + (totalDocs - docFreq + 0.5) / (docFreq + 0.5));

            for (Map.Entry<String, Integer> entry : postings.entrySet()) {
                String docId = entry.getKey();
                int tf = entry.getValue();
                int docLen = docLengths.getOrDefault(docId, 1);

                double numerator = tf * (K1 + 1.0);
                double denominator = tf + K1 * (1.0 - B + B * (docLen / avgDocLength));
                double score = idf * (numerator / denominator);

                scores.merge(docId, (float) score, Float::sum);
            }
        }

        return scores;
    }

    public static List<String> tokenize(String text) {
        if (text == null) return Collections.emptyList();
        return Arrays.stream(WORD_PATTERN.split(text.toLowerCase()))
                .filter(w -> w.length() > 1 && !STOPWORDS.contains(w))
                .collect(Collectors.toList());
    }

    public int totalDocuments() {
        return docLengths.size();
    }

    public void clear() {
        invertedIndex.clear();
        docLengths.clear();
        totalTokens = 0;
    }
}
