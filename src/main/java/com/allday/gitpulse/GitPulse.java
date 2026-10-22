package com.allday.gitpulse;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * GitPulse - Git Repository Productivity & Streak Analyzer.
 * Functional Java 21 tool using Streams, Records, and Lambdas.
 */
public class GitPulse {

    public record CommitRecord(String hash, String author, Instant timestamp, String message) {
        public LocalDate date() {
            return timestamp.atZone(ZoneId.systemDefault()).toLocalDate();
        }
        public int hour() {
            return timestamp.atZone(ZoneId.systemDefault()).getHour();
        }
    }

    public record PulseReport(
            long totalCommits,
            long activeDays,
            long currentStreak,
            long maxStreak,
            Map<String, Long> authorBreakdown,
            Map<Integer, Long> hourlyActivity,
            Map<LocalDate, Long> dailyCommitCounts
    ) {}

    public static void main(String[] args) {
        Path repoPath = args.length > 0 ? Path.of(args[0]) : Path.of(".");
        System.out.println("=================================================");
        System.out.println("   GITPULSE - Git Analytics & Streak Tracker     ");
        System.out.println("=================================================");
        System.out.println("Analyzing repository at: " + repoPath.toAbsolutePath().normalize());

        try {
            List<CommitRecord> commits = extractCommits(repoPath);
            if (commits.isEmpty()) {
                System.out.println("No commits found or not a valid git repository.");
                return;
            }

            PulseReport report = analyze(commits);
            renderReport(report);
        } catch (Exception e) {
            System.err.println("Error analyzing repository: " + e.getMessage());
        }
    }

    public static List<CommitRecord> extractCommits(Path repoDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "log", "--pretty=format:%H|%an|%at|%s");
        pb.directory(repoDir.toFile());
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines()
                    .filter(line -> !line.isBlank())
                    .map(GitPulse::parseLine)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing(CommitRecord::timestamp))
                    .collect(Collectors.toList());
        }
    }

    private static Optional<CommitRecord> parseLine(String line) {
        String[] parts = line.split("\\|", 4);
        if (parts.length < 4) return Optional.empty();
        try {
            long epochSec = Long.parseLong(parts[2].trim());
            return Optional.of(new CommitRecord(parts[0], parts[1], Instant.ofEpochSecond(epochSec), parts[3]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static PulseReport analyze(List<CommitRecord> commits) {
        long total = commits.size();

        Map<String, Long> byAuthor = commits.stream()
                .collect(Collectors.groupingBy(CommitRecord::author, Collectors.counting()));

        Map<Integer, Long> byHour = commits.stream()
                .collect(Collectors.groupingBy(CommitRecord::hour, Collectors.counting()));

        Map<LocalDate, Long> byDate = commits.stream()
                .collect(Collectors.groupingBy(CommitRecord::date, TreeMap::new, Collectors.counting()));

        List<LocalDate> sortedDates = new ArrayList<>(byDate.keySet());
        long activeDays = sortedDates.size();

        // Calculate streaks functionally
        long currentStreak = 0;
        long maxStreak = 0;
        long tempStreak = 0;
        LocalDate prev = null;

        for (LocalDate date : sortedDates) {
            if (prev == null || prev.plusDays(1).equals(date)) {
                tempStreak++;
            } else {
                tempStreak = 1;
            }
            maxStreak = Math.max(maxStreak, tempStreak);
            prev = date;
        }

        if (prev != null && (prev.equals(LocalDate.now()) || prev.equals(LocalDate.now().minusDays(1)))) {
            currentStreak = tempStreak;
        }

        return new PulseReport(total, activeDays, currentStreak, maxStreak, byAuthor, byHour, byDate);
    }

    public static void renderReport(PulseReport report) {
        System.out.println("\n[SUMMARY]");
        System.out.printf("  Total Commits   : %d\n", report.totalCommits());
        System.out.printf("  Active Days     : %d\n", report.activeDays());
        System.out.printf("  Current Streak  : %d day(s)\n", report.currentStreak());
        System.out.printf("  Longest Streak  : %d day(s)\n", report.maxStreak());

        System.out.println("\n[TOP AUTHORS]");
        report.authorBreakdown().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> System.out.printf("  %-25s : %d commits (%.1f%%)\n",
                        e.getKey(), e.getValue(), (e.getValue() * 100.0 / report.totalCommits())));

        System.out.println("\n[HOURLY ACTIVITY HEATMAP]");
        for (int h = 0; h < 24; h++) {
            long count = report.hourlyActivity().getOrDefault(h, 0L);
            String bar = "█".repeat((int) Math.min(25, count));
            System.out.printf("  %02d:00 | %-25s (%d)\n", h, bar, count);
        }
        System.out.println("=================================================");
    }
}
