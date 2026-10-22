package com.allday.logpulse;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * LogPulse - High-Performance Log File & Error Anomaly Analyzer.
 * Functional Java 21 tool using Streams, Pattern Matching, and Records.
 */
public class LogPulse {

    public record LogEntry(String level, String timestamp, String message, String exceptionType) {}

    public record LogReport(
            long totalLines,
            long parsedEntries,
            Map<String, Long> levelCounts,
            double errorRatePercentage,
            List<Map.Entry<String, Long>> topErrors
    ) {}

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -cp . com.allday.logpulse.LogPulse <log_file>");
            return;
        }

        Path logPath = Path.of(args[0]);
        if (!Files.exists(logPath)) {
            System.err.println("File not found: " + logPath);
            return;
        }

        try {
            System.out.println("Analyzing log file: " + logPath.toAbsolutePath());
            LogReport report = analyze(logPath);

            System.out.println("=================================================");
            System.out.println("   LOGPULSE - Application Log Health Audit       ");
            System.out.println("=================================================");
            System.out.printf("Total Lines Scanned : %d\n", report.totalLines());
            System.out.printf("Structured Entries  : %d\n", report.parsedEntries());
            System.out.printf("Error Rate          : %.2f%%\n\n", report.errorRatePercentage());

            System.out.println("[DISTRIBUTION BY LOG LEVEL]");
            report.levelCounts().forEach((lvl, count) -> {
                double pct = (count * 100.0) / Math.max(1, report.parsedEntries());
                String bar = "■".repeat((int) Math.min(20, pct / 5));
                System.out.printf("  %-7s : %-6d (%5.1f%%) %s\n", lvl, count, pct, bar);
            });

            if (!report.topErrors().isEmpty()) {
                System.out.println("\n[TOP DETECTED EXCEPTIONS / ERRORS]");
                report.topErrors().forEach(e ->
                        System.out.printf("  %4d occurrences -> %s\n", e.getValue(), e.getKey()));
            }
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("Error analyzing log: " + e.getMessage());
        }
    }

    public static LogReport analyze(Path path) throws Exception {
        Pattern pattern = Pattern.compile("(?i)\\b(INFO|WARN|WARNING|ERROR|FATAL|DEBUG|TRACE)\\b");
        Pattern exceptionPattern = Pattern.compile("([a-zA-Z0-9_.]+(?:Exception|Error))");

        long totalLines;
        List<LogEntry> entries;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            List<String> lines = reader.lines().toList();
            totalLines = lines.size();

            entries = lines.stream()
                    .map(line -> {
                        Matcher m = pattern.matcher(line);
                        if (m.find()) {
                            String level = m.group(1).toUpperCase();
                            Matcher exMatcher = exceptionPattern.matcher(line);
                            String ex = exMatcher.find() ? exMatcher.group(1) : "Generic " + level;
                            return new LogEntry(level, "", line, ex);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }

        Map<String, Long> levelCounts = entries.stream()
                .collect(Collectors.groupingBy(LogEntry::level, Collectors.counting()));

        long errors = levelCounts.getOrDefault("ERROR", 0L) + levelCounts.getOrDefault("FATAL", 0L);
        double errorRate = entries.isEmpty() ? 0.0 : (errors * 100.0) / entries.size();

        List<Map.Entry<String, Long>> topErrors = entries.stream()
                .filter(e -> e.level().equals("ERROR") || e.level().equals("FATAL"))
                .collect(Collectors.groupingBy(LogEntry::exceptionType, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .toList();

        return new LogReport(totalLines, entries.size(), levelCounts, errorRate, topErrors);
    }
}
