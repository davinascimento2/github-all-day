package com.vectorcore.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vectorcore.math.DistanceMetric;
import com.vectorcore.model.CollectionConfig;
import com.vectorcore.model.SearchQuery;
import com.vectorcore.model.SearchResult;
import com.vectorcore.model.VectorRecord;
import com.vectorcore.storage.Collection;
import com.vectorcore.storage.CollectionManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Lightweight Zero-Dependency Embedded HTTP REST API & Web Dashboard Server.
 */
public class EmbeddedHttpServer {

    private final int port;
    private final CollectionManager manager;
    private HttpServer server;

    public EmbeddedHttpServer(int port, CollectionManager manager) {
        this.port = port;
        this.manager = Objects.requireNonNull(manager);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/", new DashboardHandler());
        server.createContext("/api/v1/collections", new CollectionsHandler());
        server.createContext("/api/v1/search", new SearchHandler());
        server.createContext("/api/v1/seed-demo", new SeedDemoHandler());

        server.start();
        System.out.println("✓ VectorCore HTTP Server running at: http://localhost:" + port);
        System.out.println("✓ Interactive Web Dashboard:        http://localhost:" + port + "/");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
        }
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            String html = getDashboardHtml();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            sendResponse(exchange, 200, html);
        }
    }

    private class CollectionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                StringBuilder json = new StringBuilder("[");
                int idx = 0;
                for (String name : manager.listCollections()) {
                    var coll = manager.getCollection(name).orElseThrow();
                    if (idx++ > 0) json.append(",");
                    json.append(String.format("{\"name\":\"%s\",\"dimension\":%d,\"metric\":\"%s\",\"size\":%d}",
                            coll.getConfig().name(), coll.getConfig().dimension(), coll.getConfig().metric(), coll.size()));
                }
                json.append("]");
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 200, json.toString());
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }
    }

    private class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String collectionName = extractJsonString(body, "collection", "demo");
            String queryText = extractJsonString(body, "query", "");
            int topK = extractJsonInt(body, "topK", 5);
            float alpha = extractJsonFloat(body, "alpha", 0.7f);

            var collOpt = manager.getCollection(collectionName);
            if (collOpt.isEmpty()) {
                sendResponse(exchange, 404, "{\"error\":\"Collection not found\"}");
                return;
            }

            Collection coll = collOpt.get();
            long t0 = System.nanoTime();

            // Pseudo-embedding for text query or extract vector if provided
            float[] queryVector = generateTextVector(queryText, coll.getConfig().dimension());
            SearchQuery query = SearchQuery.hybrid(queryVector, queryText, topK, alpha);

            List<SearchResult> results = coll.search(query);
            long latencyUs = (System.nanoTime() - t0) / 1000;

            StringBuilder json = new StringBuilder();
            json.append(String.format("{\"latencyUs\":%d,\"results\":[", latencyUs));
            for (int i = 0; i < results.size(); i++) {
                SearchResult r = results.get(i);
                if (i > 0) json.append(",");
                json.append(String.format("{\"id\":\"%s\",\"score\":%.4f,\"text\":\"%s\"}",
                        r.id(), r.score(), escapeJson(r.text())));
            }
            json.append("]}");

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, json.toString());
        }
    }

    private class SeedDemoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Collection coll = manager.getOrCreate(new CollectionConfig("demo", 16, DistanceMetric.COSINE, 16, 100, 50));

            String[] sampleTexts = {
                    "VectorCore is a high-speed in-memory vector database built in Java 21.",
                    "HNSW graphs enable approximate nearest neighbor search with sub-millisecond latency.",
                    "BM25 lexical search provides accurate exact-keyword matching for document retrieval.",
                    "Hybrid search fuses dense vector semantics with sparse lexical matching via RRF.",
                    "Spring Boot microservices benefit from lightweight embeddable vector storage.",
                    "Retrieval-Augmented Generation (RAG) connects LLMs to verified enterprise knowledge.",
                    "Cosine similarity measures the angular distance between high-dimensional embeddings.",
                    "Zero external dependencies ensure instant application startup and zero CVE vulnerabilities.",
                    "Clean Architecture and SOLID principles guarantee maintainable and testable code.",
                    "Java 21 Virtual Threads and Records streamline concurrent asynchronous workloads."
            };

            for (int i = 0; i < sampleTexts.length; i++) {
                String text = sampleTexts[i];
                float[] vec = generateTextVector(text, 16);
                coll.upsert(new VectorRecord("doc-" + (i + 1), vec, text, Collections.emptyMap(), null));
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 200, "{\"status\":\"ok\",\"seeded\":10,\"collection\":\"demo\"}");
        }
    }

    public static float[] generateTextVector(String text, int dim) {
        float[] vec = new float[dim];
        if (text == null || text.isBlank()) return vec;

        // Deterministic feature hashing for demo embedding representation
        for (String word : text.toLowerCase().split("\\s+")) {
            int hash = Math.abs(word.hashCode());
            for (int d = 0; d < dim; d++) {
                if ((hash & (1 << (d % 31))) != 0) {
                    vec[d] += 1.0f;
                }
            }
        }
        return com.vectorcore.math.VectorMath.normalize(vec);
    }

    private static void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String extractJsonString(String json, String key, String defaultVal) {
        var m = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : defaultVal;
    }

    private static int extractJsonInt(String json, String key, int defaultVal) {
        var m = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9]+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : defaultVal;
    }

    private static float extractJsonFloat(String json, String key, float defaultVal) {
        var m = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9.]+)").matcher(json);
        return m.find() ? Float.parseFloat(m.group(1)) : defaultVal;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ");
    }

    private String getDashboardHtml() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>VectorCore - Vector Database & Hybrid RAG Engine</title>
              <style>
                :root {
                  --bg: #090d16;
                  --card: #121826;
                  --border: #232d42;
                  --accent: #38bdf8;
                  --accent-glow: rgba(56, 189, 248, 0.2);
                  --text: #f1f5f9;
                  --muted: #94a3b8;
                  --green: #10b981;
                }
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                body { background: var(--bg); color: var(--text); padding: 30px; }
                .container { max-width: 1000px; margin: 0 auto; }
                header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border); padding-bottom: 20px; margin-bottom: 30px; }
                h1 { font-size: 24px; font-weight: 700; display: flex; align-items: center; gap: 10px; color: var(--accent); }
                .badge { background: var(--border); padding: 4px 10px; border-radius: 999px; font-size: 12px; color: var(--green); }
                .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-bottom: 30px; }
                .card { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 20px; box-shadow: 0 4px 20px var(--accent-glow); }
                .card h3 { font-size: 14px; text-transform: uppercase; color: var(--muted); margin-bottom: 8px; letter-spacing: 0.05em; }
                .card .val { font-size: 28px; font-weight: 700; color: var(--text); }
                .search-box { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 25px; margin-bottom: 30px; }
                .input-group { display: flex; gap: 10px; margin-top: 15px; }
                input[type="text"] { flex: 1; padding: 12px 16px; border-radius: 8px; border: 1px solid var(--border); background: #0c1220; color: #fff; font-size: 15px; }
                button { background: var(--accent); color: #000; font-weight: 600; padding: 12px 24px; border: none; border-radius: 8px; cursor: pointer; transition: 0.2s; }
                button:hover { opacity: 0.9; }
                .btn-secondary { background: var(--border); color: var(--text); }
                .controls { display: flex; gap: 20px; align-items: center; margin-top: 15px; font-size: 13px; color: var(--muted); }
                .results { margin-top: 25px; }
                .result-item { background: #0c1220; border: 1px solid var(--border); border-radius: 8px; padding: 15px; margin-bottom: 12px; }
                .result-header { display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 12px; color: var(--muted); }
                .result-score { color: var(--accent); font-weight: 700; }
                .result-text { font-size: 14px; line-height: 1.5; color: #e2e8f0; }
              </style>
            </head>
            <body>
              <div class="container">
                <header>
                  <div>
                    <h1>⚡ VectorCore <span>Engine</span></h1>
                    <p style="color: var(--muted); font-size: 13px; margin-top: 4px;">In-Memory HNSW Vector Search & Hybrid BM25 RAG in Pure Java 21</p>
                  </div>
                  <div>
                    <button class="btn-secondary" onclick="seedDemo()">🌱 Seed 10 Demo Docs</button>
                  </div>
                </header>

                <div class="grid">
                  <div class="card">
                    <h3>Indexed Vectors</h3>
                    <div class="val" id="totalVectors">-</div>
                  </div>
                  <div class="card">
                    <h3>Collections Active</h3>
                    <div class="val" id="totalCollections">-</div>
                  </div>
                  <div class="card">
                    <h3>Query Latency</h3>
                    <div class="val" id="lastLatency">0.18 ms</div>
                  </div>
                </div>

                <div class="search-box">
                  <h2 style="font-size: 18px; margin-bottom: 6px;">Hybrid Semantic & Lexical Search</h2>
                  <p style="color: var(--muted); font-size: 13px;">Combines HNSW Dense Embeddings with BM25 Sparse Keyword matching via RRF.</p>
                  
                  <div class="input-group">
                    <input type="text" id="queryInput" placeholder="Try: 'vector search latency', 'spring boot', 'cosine similarity'..." value="vector search latency">
                    <button onclick="performSearch()">Search</button>
                  </div>

                  <div class="controls">
                    <div>
                      <label>Hybrid Weight (Alpha): <span id="alphaVal">0.7</span></label>
                      <input type="range" id="alphaRange" min="0" max="1" step="0.1" value="0.7" oninput="document.getElementById('alphaVal').innerText = this.value">
                    </div>
                    <div>
                      <label>Top-K: </label>
                      <select id="topKSelect" style="background: #0c1220; color: #fff; border: 1px solid var(--border); padding: 4px 8px; border-radius: 4px;">
                        <option value="3">3</option>
                        <option value="5" selected>5</option>
                        <option value="10">10</option>
                      </select>
                    </div>
                  </div>

                  <div class="results" id="resultsContainer"></div>
                </div>
              </div>

              <script>
                async function updateStats() {
                  try {
                    const res = await fetch('/api/v1/collections');
                    const data = await res.json();
                    document.getElementById('totalCollections').innerText = data.length;
                    let count = data.reduce((acc, c) => acc + c.size, 0);
                    document.getElementById('totalVectors').innerText = count;
                  } catch (e) {}
                }

                async function seedDemo() {
                  await fetch('/api/v1/seed-demo', { method: 'POST' });
                  await updateStats();
                  performSearch();
                }

                async function performSearch() {
                  const query = document.getElementById('queryInput').value;
                  const alpha = parseFloat(document.getElementById('alphaRange').value);
                  const topK = parseInt(document.getElementById('topKSelect').value);

                  const t0 = performance.now();
                  const res = await fetch('/api/v1/search', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ collection: 'demo', query: query, alpha: alpha, topK: topK })
                  });
                  const data = await res.json();
                  const elapsed = (performance.now() - t0).toFixed(2);
                  document.getElementById('lastLatency').innerText = elapsed + ' ms';

                  const container = document.getElementById('resultsContainer');
                  container.innerHTML = '';

                  if (!data.results || data.results.length === 0) {
                    container.innerHTML = '<p style="color: var(--muted); font-size: 14px;">No matches found. Click "Seed 10 Demo Docs" above to load sample vectors!</p>';
                    return;
                  }

                  data.results.forEach((r, idx) => {
                    container.innerHTML += `
                      <div class="result-item">
                        <div class="result-header">
                          <span>#${idx + 1} • ID: ${r.id}</span>
                          <span class="result-score">Score: ${(r.score * 100).toFixed(2)}%</span>
                        </div>
                        <div class="result-text">${r.text}</div>
                      </div>
                    `;
                  });
                }

                window.onload = async () => {
                  await updateStats();
                  if (document.getElementById('totalVectors').innerText === '0') {
                    await seedDemo();
                  } else {
                    performSearch();
                  }
                };
              </script>
            </body>
            </html>
            """;
    }
}
