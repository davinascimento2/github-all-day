package com.vectorcore;

import com.vectorcore.index.FlatIndex;
import com.vectorcore.index.HnswIndex;
import com.vectorcore.math.DistanceMetric;
import com.vectorcore.math.VectorMath;
import com.vectorcore.model.CollectionConfig;
import com.vectorcore.model.SearchQuery;
import com.vectorcore.model.SearchResult;
import com.vectorcore.model.VectorRecord;
import com.vectorcore.server.EmbeddedHttpServer;
import com.vectorcore.storage.Collection;
import com.vectorcore.storage.CollectionManager;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * VectorCore - Main Entrypoint & CLI Command Runner.
 */
public class VectorCoreApp {

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            printBanner();
            return;
        }

        String cmd = args[0].toLowerCase();
        try {
            switch (cmd) {
                case "server", "start" -> {
                    int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
                    startServer(port);
                }
                case "benchmark", "bench" -> {
                    int vectorCount = args.length > 1 ? Integer.parseInt(args[1]) : 10000;
                    int dim = args.length > 2 ? Integer.parseInt(args[2]) : 64;
                    runBenchmark(vectorCount, dim);
                }
                case "demo" -> runDemo();
                default -> printBanner();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startServer(int port) throws Exception {
        CollectionManager manager = new CollectionManager();
        // Pre-create demo collection
        Collection demo = manager.createCollection(
                new CollectionConfig("demo", 16, DistanceMetric.COSINE, 16, 100, 50));

        EmbeddedHttpServer server = new EmbeddedHttpServer(port, manager);
        server.start();

        System.out.println("\nPress Ctrl+C to stop the server.");
        // Keep main thread alive
        Thread.currentThread().join();
    }

    private static void runBenchmark(int count, int dim) {
        System.out.println("========================================================================");
        System.out.println("   VECTORCORE - HNSW vs FLAT (BRUTE-FORCE) RECALL & LATENCY BENCHMARK   ");
        System.out.println("========================================================================");
        System.out.printf("Dataset: %d vectors | Dimension: %d | Metric: Cosine Similarity\n\n", count, dim);

        System.out.println("1. Generating normalized random vector embeddings...");
        List<VectorRecord> dataset = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            float[] v = randomVector(dim);
            dataset.add(new VectorRecord("vec-" + i, v, "Sample record " + i, Collections.emptyMap(), Instant.now()));
        }

        // Test Flat Index
        System.out.print("2. Building Flat (Brute-Force) Index... ");
        long t0 = System.currentTimeMillis();
        FlatIndex flatIndex = new FlatIndex(dim, DistanceMetric.COSINE);
        dataset.forEach(flatIndex::insert);
        long flatBuildTime = System.currentTimeMillis() - t0;
        System.out.printf("Done in %d ms.\n", flatBuildTime);

        // Test HNSW Index
        System.out.print("3. Building HNSW Graph Index (M=16, efConstruction=100)... ");
        t0 = System.currentTimeMillis();
        HnswIndex hnswIndex = new HnswIndex(dim, DistanceMetric.COSINE, 16, 100, 50);
        dataset.forEach(hnswIndex::insert);
        long hnswBuildTime = System.currentTimeMillis() - t0;
        System.out.printf("Done in %d ms.\n\n", hnswBuildTime);

        // Measure Search Query Throughput & Recall
        int queries = 200;
        int topK = 10;
        System.out.printf("4. Executing %d test queries (Top-K = %d)...\n", queries, topK);

        List<float[]> testQueries = new ArrayList<>(queries);
        for (int i = 0; i < queries; i++) {
            testQueries.add(randomVector(dim));
        }

        // Flat Search
        t0 = System.nanoTime();
        List<List<SearchResult>> groundTruth = testQueries.stream()
                .map(q -> flatIndex.search(SearchQuery.vector(q, topK)))
                .toList();
        long flatSearchNanos = System.nanoTime() - t0;
        double flatAvgLatencyMs = (flatSearchNanos / 1_000_000.0) / queries;

        // HNSW Search
        t0 = System.nanoTime();
        List<List<SearchResult>> hnswResults = testQueries.stream()
                .map(q -> hnswIndex.search(SearchQuery.vector(q, topK)))
                .toList();
        long hnswSearchNanos = System.nanoTime() - t0;
        double hnswAvgLatencyMs = (hnswSearchNanos / 1_000_000.0) / queries;

        // Calculate Recall Rate
        int totalHits = 0;
        for (int i = 0; i < queries; i++) {
            Set<String> trueIds = groundTruth.get(i).stream().map(SearchResult::id).collect(java.util.stream.Collectors.toSet());
            for (SearchResult r : hnswResults.get(i)) {
                if (trueIds.contains(r.id())) totalHits++;
            }
        }
        double recall = (totalHits * 100.0) / (queries * topK);

        System.out.println("========================================================================");
        System.out.println("   BENCHMARK RESULTS                                                    ");
        System.out.println("========================================================================");
        System.out.printf("  Metric                  | Flat (Brute Force)   | HNSW Graph (VectorCore)\n");
        System.out.printf("  ------------------------+----------------------+------------------------\n");
        System.out.printf("  Build Time              | %8d ms             | %8d ms\n", flatBuildTime, hnswBuildTime);
        System.out.printf("  Average Query Latency   | %8.3f ms             | %8.3f ms (Speedup: %.1fx)\n",
                flatAvgLatencyMs, hnswAvgLatencyMs, (flatAvgLatencyMs / Math.max(0.001, hnswAvgLatencyMs)));
        System.out.printf("  Queries Per Sec (QPS)   | %8.0f req/s          | %8.0f req/s\n",
                (queries / (flatSearchNanos / 1_000_000_000.0)), (queries / (hnswSearchNanos / 1_000_000_000.0)));
        System.out.printf("  Recall @ 10             | 100.00%% (Baseline)   | %8.2f%%\n", recall);
        System.out.println("========================================================================");
    }

    private static void runDemo() {
        System.out.println("========================================================================");
        System.out.println("   VECTORCORE - HYBRID RAG SEARCH DEMO                                  ");
        System.out.println("========================================================================");

        Collection coll = new Collection(CollectionConfig.defaultCosine("demo", 16));

        String[] docs = {
                "Retrieval-Augmented Generation connects language models with private vector storage.",
                "HNSW is an efficient algorithm for nearest neighbor search in metric spaces.",
                "Java 21 delivers virtual threads, records, and pattern matching for modern backends.",
                "BM25 provides exact keyword search relevance scoring based on term frequency."
        };

        for (int i = 0; i < docs.length; i++) {
            float[] vec = EmbeddedHttpServer.generateTextVector(docs[i], 16);
            coll.upsert(new VectorRecord("doc-" + (i + 1), vec, docs[i], Collections.emptyMap(), Instant.now()));
            System.out.printf("  ✓ Indexed doc-%d: \"%s\"\n", (i + 1), docs[i]);
        }

        System.out.println("\nQuerying: \"Java virtual threads and language models\" with Hybrid Alpha = 0.5");
        float[] queryVec = EmbeddedHttpServer.generateTextVector("Java virtual threads and language models", 16);
        List<SearchResult> results = coll.search(SearchQuery.hybrid(queryVec, "Java virtual threads", 3, 0.5f));

        System.out.println("\n[TOP 3 HYBRID MATCHES]");
        results.forEach(r -> System.out.printf("  Score: %.4f | ID: %s | Text: %s\n", r.score(), r.id(), r.text()));
        System.out.println("========================================================================");
    }

    private static float[] randomVector(int dim) {
        float[] v = new float[dim];
        for (int i = 0; i < dim; i++) {
            v[i] = (float) ThreadLocalRandom.current().nextGaussian();
        }
        return VectorMath.normalize(v);
    }

    private static void printBanner() {
        System.out.println("""
            ========================================================================
               ⚡ VECTORCORE - IN-MEMORY VECTOR SEARCH & HYBRID RAG ENGINE (JAVA 21)
            ========================================================================
            Usage:
              java -jar target/vectorcore.jar <command> [options]

            Commands:
              server [port]              - Start embedded HTTP REST API & Web Dashboard (default: 8080)
              benchmark [count] [dim]    - Run HNSW vs Flat Recall & Latency Benchmark
              demo                       - Run instant in-memory Hybrid RAG Search demo
              help                       - Display this help message
            ========================================================================
            """);
    }
}
