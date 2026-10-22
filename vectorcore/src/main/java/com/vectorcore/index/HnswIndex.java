package com.vectorcore.index;

import com.vectorcore.math.DistanceMetric;
import com.vectorcore.math.VectorMath;
import com.vectorcore.model.SearchQuery;
import com.vectorcore.model.SearchResult;
import com.vectorcore.model.VectorRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Hierarchical Navigable Small World (HNSW) Index.
 * Industry-standard graph-based Approximate Nearest Neighbor (ANN) search algorithm.
 * Provides logarithmic search complexity O(log N) with sub-millisecond query latency.
 */
public class HnswIndex implements VectorIndex {

    private final int dimension;
    private final DistanceMetric metric;
    private final int M;              // Max outgoing edges per node on layers > 0
    private final int M0;             // Max outgoing edges on layer 0 (typically 2 * M)
    private final int efConstruction; // Beam search width during index build
    private final int efSearch;       // Beam search width during queries
    private final double mL;          // Layer generation normalization factor: 1.0 / ln(M)

    private final Map<String, HnswNode> nodes = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private volatile HnswNode entryPoint = null;
    private volatile int maxLayer = -1;

    private static class HnswNode {
        final VectorRecord record;
        final int level;
        // Adjacency list: layer -> list of neighbor IDs
        final List<Set<String>> neighbors;

        HnswNode(VectorRecord record, int level) {
            this.record = record;
            this.level = level;
            this.neighbors = new ArrayList<>(level + 1);
            for (int i = 0; i <= level; i++) {
                this.neighbors.add(Collections.newSetFromMap(new ConcurrentHashMap<>()));
            }
        }
    }

    private record DistPair(String id, float distance) implements Comparable<DistPair> {
        @Override
        public int compareTo(DistPair o) {
            return Float.compare(this.distance, o.distance);
        }
    }

    public HnswIndex(int dimension, DistanceMetric metric, int m, int efConstruction, int efSearch) {
        this.dimension = dimension;
        this.metric = Objects.requireNonNull(metric, "Metric cannot be null");
        this.M = Math.max(4, m);
        this.M0 = 2 * this.M;
        this.efConstruction = Math.max(16, efConstruction);
        this.efSearch = Math.max(10, efSearch);
        this.mL = 1.0 / Math.log(this.M);
    }

    public HnswIndex(int dimension, DistanceMetric metric) {
        this(dimension, metric, 16, 100, 50);
    }

    private int randomLevel() {
        double r = ThreadLocalRandom.current().nextDouble();
        if (r == 0.0) r = 0.0000001;
        return (int) (-Math.log(r) * mL);
    }

    @Override
    public void insert(VectorRecord record) {
        if (record.dimension() != dimension) {
            throw new IllegalArgumentException("Dimension mismatch: expected " + dimension + ", got " + record.dimension());
        }

        rwLock.writeLock().lock();
        try {
            int level = randomLevel();
            HnswNode newNode = new HnswNode(record, level);
            nodes.put(record.id(), newNode);

            if (entryPoint == null) {
                entryPoint = newNode;
                maxLayer = level;
                return;
            }

            HnswNode currObj = entryPoint;
            float[] qv = record.vector();
            float currDist = VectorMath.distance(qv, currObj.record.vector(), metric);

            // Step 1: Greedily traverse down to insertion layer
            for (int l = maxLayer; l > level; l--) {
                boolean changed = true;
                while (changed) {
                    changed = false;
                    for (String neighborId : currObj.neighbors.get(l)) {
                        HnswNode neighbor = nodes.get(neighborId);
                        if (neighbor == null) continue;
                        float d = VectorMath.distance(qv, neighbor.record.vector(), metric);
                        if (d < currDist) {
                            currDist = d;
                            currObj = neighbor;
                            changed = true;
                        }
                    }
                }
            }

            // Step 2: From min(level, maxLayer) down to 0, search and connect neighbors
            int topConnectLayer = Math.min(level, maxLayer);
            for (int l = topConnectLayer; l >= 0; l--) {
                List<DistPair> candidates = searchLayer(qv, currObj, efConstruction, l);

                int maxEdges = (l == 0) ? M0 : M;
                List<DistPair> selected = selectNeighborsHeuristic(candidates, maxEdges);

                for (DistPair p : selected) {
                    newNode.neighbors.get(l).add(p.id());
                    HnswNode neighborNode = nodes.get(p.id());
                    if (neighborNode != null && neighborNode.level >= l) {
                        Set<String> neighborEdges = neighborNode.neighbors.get(l);
                        neighborEdges.add(newNode.record.id());
                        // Shrink if degree exceeds capacity
                        if (neighborEdges.size() > maxEdges) {
                            pruneConnections(neighborNode, l, maxEdges);
                        }
                    }
                }
                if (!candidates.isEmpty()) {
                    currObj = nodes.get(candidates.get(0).id());
                }
            }

            if (level > maxLayer) {
                maxLayer = level;
                entryPoint = newNode;
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private List<DistPair> searchLayer(float[] query, HnswNode entry, int ef, int layer) {
        Set<String> visited = new HashSet<>();
        // Max-heap of furthest candidates found so far (worse candidates on top)
        PriorityQueue<DistPair> topCandidates = new PriorityQueue<>(Collections.reverseOrder());
        // Min-heap for exploration
        PriorityQueue<DistPair> candidateSet = new PriorityQueue<>();

        float entryDist = VectorMath.distance(query, entry.record.vector(), metric);
        DistPair entryPair = new DistPair(entry.record.id(), entryDist);

        visited.add(entry.record.id());
        candidateSet.add(entryPair);
        topCandidates.add(entryPair);

        while (!candidateSet.isEmpty()) {
            DistPair current = candidateSet.poll();
            DistPair furthest = topCandidates.peek();

            if (current.distance() > furthest.distance() && topCandidates.size() >= ef) {
                break;
            }

            HnswNode node = nodes.get(current.id());
            if (node == null || node.level < layer) continue;

            for (String neighborId : node.neighbors.get(layer)) {
                if (visited.add(neighborId)) {
                    HnswNode neighbor = nodes.get(neighborId);
                    if (neighbor == null) continue;

                    float dist = VectorMath.distance(query, neighbor.record.vector(), metric);
                    DistPair neighborPair = new DistPair(neighborId, dist);

                    if (topCandidates.size() < ef || dist < topCandidates.peek().distance()) {
                        candidateSet.add(neighborPair);
                        topCandidates.add(neighborPair);
                        if (topCandidates.size() > ef) {
                            topCandidates.poll(); // Remove furthest
                        }
                    }
                }
            }
        }

        List<DistPair> result = new ArrayList<>(topCandidates);
        Collections.sort(result); // Closest first
        return result;
    }

    private List<DistPair> selectNeighborsHeuristic(List<DistPair> candidates, int m) {
        return candidates.stream().limit(m).toList();
    }

    private void pruneConnections(HnswNode node, int layer, int maxEdges) {
        Set<String> edgeSet = node.neighbors.get(layer);
        if (edgeSet.size() <= maxEdges) return;

        float[] origin = node.record.vector();
        List<DistPair> sorted = edgeSet.stream()
                .map(id -> {
                    HnswNode n = nodes.get(id);
                    float d = (n != null) ? VectorMath.distance(origin, n.record.vector(), metric) : Float.MAX_VALUE;
                    return new DistPair(id, d);
                })
                .sorted()
                .limit(maxEdges)
                .toList();

        edgeSet.clear();
        sorted.forEach(p -> edgeSet.add(p.id()));
    }

    @Override
    public boolean delete(String id) {
        rwLock.writeLock().lock();
        try {
            HnswNode removed = nodes.remove(id);
            if (removed == null) return false;

            // Remove backlinks
            for (int l = 0; l <= removed.level; l++) {
                for (String neighborId : removed.neighbors.get(l)) {
                    HnswNode neighbor = nodes.get(neighborId);
                    if (neighbor != null && neighbor.level >= l) {
                        neighbor.neighbors.get(l).remove(id);
                    }
                }
            }

            if (entryPoint != null && entryPoint.record.id().equals(id)) {
                entryPoint = nodes.values().stream().findFirst().orElse(null);
                maxLayer = (entryPoint != null) ? entryPoint.level : -1;
            }
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public List<SearchResult> search(SearchQuery query) {
        rwLock.readLock().lock();
        try {
            if (entryPoint == null || nodes.isEmpty()) {
                return Collections.emptyList();
            }

            float[] qv = query.vector();
            HnswNode currObj = entryPoint;
            float currDist = VectorMath.distance(qv, currObj.record.vector(), metric);

            // Navigate top down to layer 1 greedily
            for (int l = maxLayer; l > 0; l--) {
                boolean changed = true;
                while (changed) {
                    changed = false;
                    for (String neighborId : currObj.neighbors.get(l)) {
                        HnswNode neighbor = nodes.get(neighborId);
                        if (neighbor == null) continue;
                        float d = VectorMath.distance(qv, neighbor.record.vector(), metric);
                        if (d < currDist) {
                            currDist = d;
                            currObj = neighbor;
                            changed = true;
                        }
                    }
                }
            }

            // Beam search on layer 0 with efSearch
            int ef = Math.max(query.topK(), efSearch);
            List<DistPair> layer0Candidates = searchLayer(qv, currObj, ef, 0);

            return layer0Candidates.stream()
                    .map(pair -> nodes.get(pair.id()))
                    .filter(Objects::nonNull)
                    .filter(n -> query.filter() == null || query.filter().test(n.record.payload()))
                    .map(n -> {
                        float dist = VectorMath.distance(qv, n.record.vector(), metric);
                        float sim = VectorMath.similarity(qv, n.record.vector(), metric);
                        return new SearchResult(n.record.id(), sim, dist, n.record.text(), n.record.payload());
                    })
                    .sorted()
                    .limit(query.topK())
                    .toList();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public void clear() {
        rwLock.writeLock().lock();
        try {
            nodes.clear();
            entryPoint = null;
            maxLayer = -1;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
