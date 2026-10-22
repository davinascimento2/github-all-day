package com.allday.portwatch;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * PortWatch - Fast Asynchronous Port & Service Health Scanner.
 * Functional Java 21 tool using CompletableFuture, Streams, and Records.
 */
public class PortWatch {

    public record PortStatus(int port, boolean isOpen, long latencyMs, String serviceGuess) {}

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int startPort = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        int endPort = args.length > 2 ? Integer.parseInt(args[2]) : 1024;

        System.out.println("=================================================");
        System.out.println("   PORTWATCH - Asynchronous Port & Health Probe  ");
        System.out.println("=================================================");
        System.out.printf("Scanning target: %s (ports %d to %d)\n\n", host, startPort, endPort);

        long start = System.currentTimeMillis();
        List<PortStatus> openPorts = scanRange(host, startPort, endPort, 200, 50);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("-------------------------------------------------");
        System.out.printf("Scan completed in %d ms. Open ports found: %d\n", elapsed, openPorts.size());
        System.out.println("-------------------------------------------------");
        System.out.printf("  %-7s | %-12s | %s\n", "PORT", "LATENCY", "SERVICE / ROLE");
        System.out.println("  --------+--------------+-----------------------");

        openPorts.forEach(p -> System.out.printf("  %-7d | %-8d ms | %s\n",
                p.port(), p.latencyMs(), p.serviceGuess()));

        System.out.println("=================================================");
    }

    public static List<PortStatus> scanRange(String host, int start, int end, int timeoutMs, int parallelism) {
        ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        try {
            List<CompletableFuture<PortStatus>> futures = IntStream.rangeClosed(start, end)
                    .mapToObj(port -> CompletableFuture.supplyAsync(() -> probePort(host, port, timeoutMs), executor))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(PortStatus::isOpen)
                    .toList();
        } finally {
            executor.shutdown();
        }
    }

    public static PortStatus probePort(String host, int port, int timeoutMs) {
        long t0 = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            long latency = System.currentTimeMillis() - t0;
            return new PortStatus(port, true, latency, guessService(port));
        } catch (Exception e) {
            return new PortStatus(port, false, -1, "Closed");
        }
    }

    public static String guessService(int port) {
        return switch (port) {
            case 21 -> "FTP";
            case 22 -> "SSH / SFTP";
            case 25 -> "SMTP";
            case 53 -> "DNS";
            case 80 -> "HTTP Web Server";
            case 443 -> "HTTPS Secure Web";
            case 1433 -> "MS SQL Server";
            case 1521 -> "Oracle DB";
            case 3000 -> "Node.js / React Dev Server";
            case 3306 -> "MySQL Database";
            case 5432 -> "PostgreSQL Database";
            case 6379 -> "Redis In-Memory Cache";
            case 8080 -> "HTTP Alternative / Spring Boot";
            case 8443 -> "HTTPS Alternative / Tomcat";
            case 9092 -> "Apache Kafka";
            case 27017 -> "MongoDB Database";
            default -> "Custom / Unassigned";
        };
    }
}
