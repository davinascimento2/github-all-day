package com.allday.textforge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TextForge - Text Transformation, Token Estimator & Markdown Formatter.
 * Functional Java 21 implementation using Streams, Pattern, and Records.
 */
public class TextForge {

    public record TextMetrics(
            int characterCount,
            int wordCount,
            int lineCount,
            int estimatedTokens,
            double readingTimeMinutes
    ) {}

    public static void main(String[] args) {
        if (args.length < 2) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();
        String input = args[1];

        try {
            if (Files.exists(Path.of(input))) {
                input = Files.readString(Path.of(input));
            }

            switch (command) {
                case "stats" -> {
                    TextMetrics metrics = analyze(input);
                    System.out.println("=================================================");
                    System.out.println("   TEXTFORGE - Text Analytics & Token Metrics    ");
                    System.out.println("=================================================");
                    System.out.printf("  Characters     : %d\n", metrics.characterCount());
                    System.out.printf("  Words          : %d\n", metrics.wordCount());
                    System.out.printf("  Lines          : %d\n", metrics.lineCount());
                    System.out.printf("  Tokens (approx): ~%d tokens\n", metrics.estimatedTokens());
                    System.out.printf("  Reading Time   : %.1f min (at 200 wpm)\n", metrics.readingTimeMinutes());
                    System.out.println("=================================================");
                }
                case "case" -> {
                    String targetCase = args.length > 2 ? args[2].toLowerCase() : "camel";
                    System.out.println(convertCase(input, targetCase));
                }
                case "slug" -> System.out.println(slugify(input));
                case "table" -> System.out.println(formatMarkdownTable(input));
                default -> printHelp();
            }
        } catch (Exception e) {
            System.err.println("TextForge error: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("""
            TextForge Usage:
              java -cp . com.allday.textforge.TextForge <command> <text | file> [options]

            Commands:
              stats <text|file>              - Character, word, token & reading time metrics
              case <text> <camel|snake|kebab|pascal|constant> - Convert naming casing
              slug <text>                    - Convert text into clean URL slug
              table <markdown_table_file>    - Perfectly align Markdown table columns
            """);
    }

    public static TextMetrics analyze(String text) {
        int chars = text.length();
        int lines = text.split("\r\n|\r|\n").length;
        String[] words = text.trim().isEmpty() ? new String[0] : text.trim().split("\\s+");
        int wordCount = words.length;
        int tokens = (int) Math.round(chars / 4.0);
        double readingTime = wordCount / 200.0;
        return new TextMetrics(chars, wordCount, lines, tokens, readingTime);
    }

    public static String convertCase(String text, String target) {
        String[] parts = text.split("[\\s_\\-]+");
        List<String> cleanWords = Arrays.stream(parts)
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .map(String::toLowerCase)
                .toList();

        return switch (target) {
            case "snake" -> String.join("_", cleanWords);
            case "kebab" -> String.join("-", cleanWords);
            case "constant" -> cleanWords.stream().map(String::toUpperCase).collect(Collectors.joining("_"));
            case "pascal" -> cleanWords.stream().map(TextForge::capitalize).collect(Collectors.joining());
            case "camel" -> {
                if (cleanWords.isEmpty()) yield "";
                yield cleanWords.get(0) + cleanWords.stream().skip(1).map(TextForge::capitalize).collect(Collectors.joining());
            }
            default -> text;
        };
    }

    public static String slugify(String text) {
        return Pattern.compile("[^a-zA-Z0-9]+")
                .matcher(text.trim().toLowerCase())
                .replaceAll("-")
                .replaceAll("^-|-$", "");
    }

    public static String formatMarkdownTable(String markdown) {
        List<String> lines = Arrays.stream(markdown.split("\r\n|\r|\n"))
                .map(String::trim)
                .filter(l -> l.startsWith("|") && l.endsWith("|"))
                .toList();

        if (lines.isEmpty()) return markdown;

        List<List<String>> matrix = lines.stream()
                .map(line -> Arrays.stream(line.split("\\|"))
                        .skip(1)
                        .map(String::trim)
                        .toList())
                .toList();

        int numCols = matrix.stream().mapToInt(List::size).max().orElse(0);
        int[] maxLens = new int[numCols];

        for (List<String> row : matrix) {
            for (int c = 0; c < row.size(); c++) {
                if (!row.get(c).matches("^-+$")) {
                    maxLens[c] = Math.max(maxLens[c], row.get(c).length());
                }
            }
        }

        StringBuilder out = new StringBuilder();
        for (int r = 0; r < matrix.size(); r++) {
            List<String> row = matrix.get(r);
            out.append("| ");
            for (int c = 0; c < numCols; c++) {
                String val = c < row.size() ? row.get(c) : "";
                if (val.matches("^-+$") || (r == 1 && val.contains("-"))) {
                    out.append("-".repeat(Math.max(3, maxLens[c]))).append(" | ");
                } else {
                    out.append(String.format("%-" + maxLens[c] + "s", val)).append(" | ");
                }
            }
            out.append("\n");
        }
        return out.toString().trim();
    }

    private static String capitalize(String str) {
        if (str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
