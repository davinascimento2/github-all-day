package com.vectorcore;

import com.vectorcore.index.FlatIndex;
import com.vectorcore.index.HnswIndex;
import com.vectorcore.index.InvertedIndex;
import com.vectorcore.math.DistanceMetric;
import com.vectorcore.math.VectorMath;
import com.vectorcore.model.CollectionConfig;
import com.vectorcore.model.SearchQuery;
import com.vectorcore.model.SearchResult;
import com.vectorcore.model.VectorRecord;
import com.vectorcore.storage.Collection;
import com.vectorcore.storage.SnapshotEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class VectorCoreTest {

    @Test
    void testVectorMathDistances() {
        float[] a = new float[]{1.0f, 0.0f, 0.0f};
        float[] b = new float[]{0.0f, 1.0f, 0.0f};
        float[] c = new float[]{1.0f, 0.0f, 0.0f};

        // Cosine distance between identical vectors must be 0
        assertEquals(0.0f, VectorMath.distance(a, c, DistanceMetric.COSINE), 0.0001f);
        // Cosine distance between orthogonal vectors must be 1
        assertEquals(1.0f, VectorMath.distance(a, b, DistanceMetric.COSINE), 0.0001f);

        // Euclidean distance
        assertEquals(0.0f, VectorMath.distance(a, c, DistanceMetric.EUCLIDEAN), 0.0001f);
        assertTrue(VectorMath.distance(a, b, DistanceMetric.EUCLIDEAN) > 1.0f);
    }

    @Test
    void testFlatIndexSearchAndFilter() {
        FlatIndex index = new FlatIndex(3, DistanceMetric.COSINE);

        index.insert(VectorRecord.of("item-1", new float[]{1.0f, 0.0f, 0.0f}, "Tech item", Map.of("category", "tech")));
        index.insert(VectorRecord.of("item-2", new float[]{0.9f, 0.1f, 0.0f}, "Tech item 2", Map.of("category", "tech")));
        index.insert(VectorRecord.of("item-3", new float[]{0.0f, 1.0f, 0.0f}, "Food item", Map.of("category", "food")));

        assertEquals(3, index.size());

        // Search with filter for only "food"
        float[] query = new float[]{1.0f, 0.0f, 0.0f};
        List<SearchResult> results = index.search(SearchQuery.vectorWithFilter(query, 5, p -> "food".equals(p.get("category"))));

        assertEquals(1, results.size());
        assertEquals("item-3", results.get(0).id());
    }

    @Test
    void testHnswIndexNearestNeighbor() {
        HnswIndex hnsw = new HnswIndex(4, DistanceMetric.EUCLIDEAN, 8, 30, 20);

        hnsw.insert(VectorRecord.of("target", new float[]{1.0f, 1.0f, 1.0f, 1.0f}));
        hnsw.insert(VectorRecord.of("distant", new float[]{10.0f, 10.0f, 10.0f, 10.0f}));
        hnsw.insert(VectorRecord.of("medium", new float[]{3.0f, 3.0f, 3.0f, 3.0f}));

        List<SearchResult> res = hnsw.search(SearchQuery.vector(new float[]{1.1f, 1.1f, 1.1f, 1.1f}, 1));
        assertEquals(1, res.size());
        assertEquals("target", res.get(0).id());
    }

    @Test
    void testBM25InvertedIndex() {
        InvertedIndex idx = new InvertedIndex();
        idx.indexDocument("doc-1", "Java virtual threads improve concurrency");
        idx.indexDocument("doc-2", "Python is commonly used in machine learning");
        idx.indexDocument("doc-3", "Java records provide immutable data carriers");

        Map<String, Float> scores = idx.searchBM25("Java concurrency");
        assertTrue(scores.containsKey("doc-1"));
        assertTrue(scores.get("doc-1") > scores.getOrDefault("doc-2", 0.0f));
    }

    @Test
    void testSnapshotPersistence(@TempDir Path tempDir) throws Exception {
        Collection coll = new Collection(CollectionConfig.defaultCosine("persisted", 2));
        coll.upsert(VectorRecord.of("k1", new float[]{1.0f, 0.5f}, "Alpha document", Map.of()));
        coll.upsert(VectorRecord.of("k2", new float[]{0.5f, 1.0f}, "Beta document", Map.of()));

        Path snapshotFile = tempDir.resolve("snapshot.bin");
        SnapshotEngine.saveSnapshot(coll, snapshotFile);

        assertTrue(snapshotFile.toFile().exists());

        Collection restored = SnapshotEngine.loadSnapshot(snapshotFile);
        assertEquals(2, restored.size());
        assertEquals("persisted", restored.getConfig().name());
        assertTrue(restored.get("k1").isPresent());
        assertEquals("Alpha document", restored.get("k1").get().text());
    }
}
