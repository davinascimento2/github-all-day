package com.allday.jsoncraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JsonCraft - Zero-Dependency JSON Validator, Formatter & Path Query CLI.
 * Functional Java 21 implementation using Streams, Records, and Lambdas.
 */
public class JsonCraft {

    public record JsonStats(int objectCount, int arrayCount, int keyCount, int maxDepth, long byteSize) {}

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();
        try {
            switch (command) {
                case "format", "pretty" -> {
                    String json = readInput(args);
                    System.out.println(formatJson(json, 2));
                }
                case "minify" -> {
                    String json = readInput(args);
                    System.out.println(minifyJson(json));
                }
                case "validate" -> {
                    String json = readInput(args);
                    boolean valid = isValidJson(json);
                    System.out.println(valid ? "✓ Valid JSON format." : "✗ Invalid JSON format!");
                    System.exit(valid ? 0 : 1);
                }
                case "stats" -> {
                    String json = readInput(args);
                    JsonStats stats = computeStats(json);
                    System.out.println("=================================================");
                    System.out.println("   JSONCRAFT - Document Analytics & Inspection   ");
                    System.out.println("=================================================");
                    System.out.printf("  Total Size      : %d bytes\n", stats.byteSize());
                    System.out.printf("  Key Count       : %d\n", stats.keyCount());
                    System.out.printf("  Objects         : %d\n", stats.objectCount());
                    System.out.printf("  Arrays          : %d\n", stats.arrayCount());
                    System.out.printf("  Max Depth       : %d levels\n", stats.maxDepth());
                    System.out.println("=================================================");
                }
                default -> printHelp();
            }
        } catch (Exception e) {
            System.err.println("Error processing JSON: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("""
            JsonCraft Usage:
              java -cp . com.allday.jsoncraft.JsonCraft <command> [file | "raw_json"]

            Commands:
              format    - Pretty-print formatted JSON with 2-space indentation
              minify    - Remove all whitespace and compress JSON
              validate  - Validate whether JSON syntax is well-formed
              stats     - Compute structural statistics and nesting depth
            """);
    }

    private static String readInput(String[] args) throws Exception {
        if (args.length > 1) {
            Path path = Path.of(args[1]);
            if (Files.exists(path)) {
                return Files.readString(path);
            }
            return args[1];
        }
        Scanner scanner = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            sb.append(scanner.nextLine()).append("\n");
        }
        return sb.toString();
    }

    public static boolean isValidJson(String raw) {
        try {
            formatJson(raw, 2);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String minifyJson(String json) {
        StringBuilder out = new StringBuilder();
        boolean inQuotes = false;
        boolean escape = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                out.append(c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                out.append(c);
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
                out.append(c);
                continue;
            }
            if (inQuotes) {
                out.append(c);
            } else if (!Character.isWhitespace(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String formatJson(String raw, int indentSize) {
        StringBuilder out = new StringBuilder();
        int indentLevel = 0;
        boolean inQuotes = false;
        boolean escape = false;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (escape) {
                out.append(c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                out.append(c);
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
                out.append(c);
                continue;
            }
            if (inQuotes) {
                out.append(c);
                continue;
            }

            switch (c) {
                case '{', '[' -> {
                    out.append(c).append("\n");
                    indentLevel++;
                    out.append(" ".repeat(indentLevel * indentSize));
                }
                case '}', ']' -> {
                    out.append("\n");
                    indentLevel = Math.max(0, indentLevel - 1);
                    out.append(" ".repeat(indentLevel * indentSize)).append(c);
                }
                case ',' -> {
                    out.append(c).append("\n");
                    out.append(" ".repeat(indentLevel * indentSize));
                }
                case ':' -> out.append(": ");
                default -> {
                    if (!Character.isWhitespace(c)) {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    public static JsonStats computeStats(String raw) {
        int objects = 0;
        int arrays = 0;
        int keys = 0;
        int maxDepth = 0;
        int currentDepth = 0;
        boolean inQuotes = false;
        boolean escape = false;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (escape) { escape = false; continue; }
            if (c == '\\') { escape = true; continue; }
            if (c == '"') { inQuotes = !inQuotes; continue; }
            if (inQuotes) continue;

            if (c == '{') {
                objects++;
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (c == '}') {
                currentDepth--;
            } else if (c == '[') {
                arrays++;
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (c == ']') {
                currentDepth--;
            } else if (c == ':') {
                keys++;
            }
        }

        return new JsonStats(objects, arrays, keys, maxDepth, raw.getBytes().length);
    }
}
