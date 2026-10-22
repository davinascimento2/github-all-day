package com.allday.filejanitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * FileJanitor - Duplicate File & Build Artifact Cleanup Utility.
 * Functional Java 21 implementation using Streams, MessageDigest, and Records.
 */
public class FileJanitor {

    public record DuplicateGroup(String sha256, long fileSize, List<Path> files) {
        public long wastedBytes() {
            return (files.size() - 1) * fileSize;
        }
    }

    public record BuildArtifact(Path path, String type, long totalSizeBytes) {}

    public static void main(String[] args) {
        if (args.length < 2) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();
        Path targetDir = Path.of(args[1]);

        try {
            switch (command) {
                case "duplicates", "dups" -> {
                    System.out.println("Scanning for duplicate files in: " + targetDir.toAbsolutePath());
                    List<DuplicateGroup> duplicates = findDuplicates(targetDir);
                    long totalWasted = duplicates.stream().mapToLong(DuplicateGroup::wastedBytes).sum();

                    System.out.println("=================================================");
                    System.out.println("   FILEJANITOR - Duplicate File Audit Report     ");
                    System.out.println("=================================================");
                    System.out.printf("Total duplicate groups : %d\n", duplicates.size());
                    System.out.printf("Total wasted storage   : %.2f MB\n\n", totalWasted / (1024.0 * 1024.0));

                    duplicates.forEach(group -> {
                        System.out.printf("  [HASH: %s...] (Size: %d bytes | Count: %d)\n",
                                group.sha256().substring(0, 12), group.fileSize(), group.files().size());
                        group.files().forEach(f -> System.out.println("    -> " + f));
                    });
                    System.out.println("=================================================");
                }
                case "clean-builds", "builds" -> {
                    boolean executeDelete = args.length > 2 && args[2].equalsIgnoreCase("--force");
                    System.out.println("Searching build artifact folders in: " + targetDir.toAbsolutePath());
                    List<BuildArtifact> artifacts = findBuildArtifacts(targetDir);
                    long totalSize = artifacts.stream().mapToLong(BuildArtifact::totalSizeBytes).sum();

                    System.out.println("=================================================");
                    System.out.println("   FILEJANITOR - Heavy Build Artifacts Audit     ");
                    System.out.println("=================================================");
                    System.out.printf("Found %d artifact folder(s) occupying %.2f MB total\n\n",
                            artifacts.size(), totalSize / (1024.0 * 1024.0));

                    artifacts.forEach(a -> System.out.printf("  %-15s [%.2f MB] -> %s\n",
                            a.type(), a.totalSizeBytes() / (1024.0 * 1024.0), a.path()));

                    if (executeDelete) {
                        System.out.println("\nCleaning artifact folders...");
                        artifacts.forEach(a -> deleteRecursively(a.path()));
                        System.out.println("✓ Cleanup finished!");
                    } else {
                        System.out.println("\n(Dry-run mode. Pass '--force' as the 3rd argument to delete these folders)");
                    }
                    System.out.println("=================================================");
                }
                default -> printHelp();
            }
        } catch (Exception e) {
            System.err.println("FileJanitor error: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("""
            FileJanitor Usage:
              java -cp . com.allday.filejanitor.FileJanitor <command> <directory> [--force]

            Commands:
              duplicates <dir>         - Find duplicate files by SHA-256 hash
              clean-builds <dir>       - Find heavy build dirs (node_modules, target, .next)
              clean-builds <dir> --force - Actually delete found build dirs
            """);
    }

    public static List<DuplicateGroup> findDuplicates(Path dir) throws IOException {
        try (Stream<Path> stream = Files.walk(dir)) {
            // Step 1: Pre-filter by file size (files with unique sizes can never be duplicates)
            Map<Long, List<Path>> bySize = stream
                    .filter(Files::isRegularFile)
                    .collect(Collectors.groupingBy(p -> {
                        try { return Files.size(p); } catch (Exception e) { return -1L; }
                    }));

            // Step 2: Hash only size collision candidates
            return bySize.entrySet().stream()
                    .filter(e -> e.getKey() > 0 && e.getValue().size() > 1)
                    .flatMap(e -> groupByHash(e.getValue(), e.getKey()).stream())
                    .toList();
        }
    }

    private static List<DuplicateGroup> groupByHash(List<Path> files, long size) {
        Map<String, List<Path>> byHash = files.stream()
                .collect(Collectors.groupingBy(FileJanitor::computeHash));

        return byHash.entrySet().stream()
                .filter(e -> !e.getKey().isEmpty() && e.getValue().size() > 1)
                .map(e -> new DuplicateGroup(e.getKey(), size, e.getValue()))
                .toList();
    }

    private static String computeHash(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            return "";
        }
    }

    public static List<BuildArtifact> findBuildArtifacts(Path root) throws IOException {
        Set<String> artifactNames = Set.of("node_modules", "target", ".gradle", "build", ".next", "dist", "vendor");
        List<BuildArtifact> results = new ArrayList<>();

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String name = dir.getFileName().toString();
                if (artifactNames.contains(name)) {
                    long size = calculateDirectorySize(dir);
                    results.add(new BuildArtifact(dir, name, size));
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return results;
    }

    private static long calculateDirectorySize(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            return walk.filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try { return Files.size(p); } catch (Exception e) { return 0L; }
                    }).sum();
        } catch (IOException e) {
            return 0L;
        }
    }

    private static void deleteRecursively(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                    Files.delete(d);
                    return FileVisitResult.CONTINUE;
                }
            });
            System.out.println("  ✓ Deleted: " + dir);
        } catch (Exception e) {
            System.err.println("  ✗ Failed to delete " + dir + ": " + e.getMessage());
        }
    }
}
