package com.allday.cronparser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * CronParser - Cron Expression Human Translator & Next-Run Predictor.
 * Functional Java 21 implementation using LocalDateTime, Streams, and Records.
 */
public class CronParser {

    public record CronSchedule(
            String expression,
            Set<Integer> minutes,
            Set<Integer> hours,
            Set<Integer> daysOfMonth,
            Set<Integer> months,
            Set<Integer> daysOfWeek
    ) {}

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        String cronExpr = args.length > 0 ? String.join(" ", args) : "*/15 9-18 * * 1-5";

        System.out.println("=================================================");
        System.out.println("   CRONPARSER - Cron Translator & Next Triggers  ");
        System.out.println("=================================================");
        System.out.println("Expression: " + cronExpr);

        try {
            CronSchedule schedule = parse(cronExpr);
            System.out.println("\n[HUMAN TRANSLATION]");
            System.out.println("  " + describe(schedule));

            System.out.println("\n[NEXT 5 SCHEDULED RUNS]");
            List<LocalDateTime> nextRuns = computeNextRuns(schedule, LocalDateTime.now(), 5);
            nextRuns.forEach(dt -> System.out.println("  ✓ " + dt.format(FORMATTER)));

            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("Invalid cron expression: " + e.getMessage());
        }
    }

    public static CronSchedule parse(String expression) {
        String[] parts = expression.trim().split("\\s+");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Expected 5 fields (minute hour dom month dow), got " + parts.length);
        }

        Set<Integer> minutes = parseField(parts[0], 0, 59);
        Set<Integer> hours = parseField(parts[1], 0, 23);
        Set<Integer> doms = parseField(parts[2], 1, 31);
        Set<Integer> months = parseField(parts[3], 1, 12);
        Set<Integer> dows = parseField(parts[4], 0, 7)
                .stream().map(d -> d == 7 ? 0 : d) // 7 is Sunday, map to 0
                .collect(Collectors.toSet());

        return new CronSchedule(expression, minutes, hours, doms, months, dows);
    }

    private static Set<Integer> parseField(String field, int min, int max) {
        Set<Integer> values = new TreeSet<>();
        String[] segments = field.split(",");

        for (String seg : segments) {
            if (seg.equals("*")) {
                IntStream.rangeClosed(min, max).forEach(values::add);
            } else if (seg.startsWith("*/")) {
                int step = Integer.parseInt(seg.substring(2));
                for (int i = min; i <= max; i += step) values.add(i);
            } else if (seg.contains("-")) {
                String[] range = seg.split("-");
                int start = Integer.parseInt(range[0]);
                int end = Integer.parseInt(range[1]);
                IntStream.rangeClosed(start, end).forEach(values::add);
            } else {
                values.add(Integer.parseInt(seg));
            }
        }
        return values;
    }

    public static String describe(CronSchedule s) {
        StringBuilder sb = new StringBuilder();
        sb.append("Runs at ");
        if (s.minutes().size() == 60) sb.append("every minute ");
        else sb.append("minute(s) ").append(s.minutes()).append(" ");

        if (s.hours().size() == 24) sb.append("past every hour ");
        else sb.append("during hour(s) ").append(s.hours()).append(" ");

        if (s.daysOfWeek().size() < 7) {
            sb.append("on day-of-week ").append(s.daysOfWeek()).append(" ");
        }
        return sb.toString().trim() + ".";
    }

    public static List<LocalDateTime> computeNextRuns(CronSchedule schedule, LocalDateTime start, int limit) {
        List<LocalDateTime> results = new ArrayList<>();
        LocalDateTime current = start.withSecond(0).withNano(0).plusMinutes(1);

        int safetyMaxMinutes = 60 * 24 * 365; // Look ahead up to 1 year
        int minutesChecked = 0;

        while (results.size() < limit && minutesChecked < safetyMaxMinutes) {
            if (matches(schedule, current)) {
                results.add(current);
            }
            current = current.plusMinutes(1);
            minutesChecked++;
        }
        return results;
    }

    private static boolean matches(CronSchedule s, LocalDateTime dt) {
        int dow = dt.getDayOfWeek().getValue() % 7; // Sunday = 0
        return s.months().contains(dt.getMonthValue())
                && s.daysOfMonth().contains(dt.getDayOfMonth())
                && s.daysOfWeek().contains(dow)
                && s.hours().contains(dt.getHour())
                && s.minutes().contains(dt.getMinute());
    }
}
