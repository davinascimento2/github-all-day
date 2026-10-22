package com.allday.syspulse;

import java.io.File;
import java.lang.management.*;
import java.util.List;

/**
 * SysPulse - System & JVM Runtime Diagnostics Snapshot CLI.
 * Functional Java 21 implementation using ManagementFactory, Streams, and Records.
 */
public class SysPulse {

    public record MemorySnapshot(long usedMb, long freeMb, long maxMb, double percentUsed) {}
    public record DiskSnapshot(String drive, long totalGb, long freeGb, double percentUsed) {}
    public record SystemSnapshot(
            int availableProcessors,
            double systemLoad,
            MemorySnapshot jvmHeap,
            int threadCount,
            List<DiskSnapshot> disks
    ) {
        public String toJson() {
            return String.format("""
                {
                  "availableProcessors": %d,
                  "systemLoad": %.2f,
                  "threadCount": %d,
                  "jvmHeap": {
                    "usedMb": %d,
                    "freeMb": %d,
                    "maxMb": %d,
                    "percentUsed": %.1f
                  }
                }""",
                    availableProcessors, systemLoad, threadCount,
                    jvmHeap.usedMb, jvmHeap.freeMb, jvmHeap.maxMb, jvmHeap.percentUsed);
        }
    }

    public static void main(String[] args) {
        boolean jsonOutput = args.length > 0 && args[0].equalsIgnoreCase("--json");

        SystemSnapshot snapshot = capture();
        if (jsonOutput) {
            System.out.println(snapshot.toJson());
            return;
        }

        System.out.println("=================================================");
        System.out.println("   SYSPULSE - System & JVM Diagnostics Snapshot  ");
        System.out.println("=================================================");
        System.out.printf("  CPU Logical Cores: %d\n", snapshot.availableProcessors());
        System.out.printf("  Active JVM Threads: %d\n", snapshot.threadCount());
        System.out.printf("  System Load Avg   : %.2f\n\n", snapshot.systemLoad());

        System.out.println("[JVM HEAP MEMORY]");
        System.out.printf("  Used: %d MB | Free: %d MB | Max: %d MB (%.1f%% utilized)\n\n",
                snapshot.jvmHeap().usedMb(), snapshot.jvmHeap().freeMb(),
                snapshot.jvmHeap().maxMb(), snapshot.jvmHeap().percentUsed());

        System.out.println("[STORAGE DISK DRIVES]");
        snapshot.disks().forEach(d -> System.out.printf("  Drive [%s] - Total: %d GB | Free: %d GB (%.1f%% used)\n",
                d.drive(), d.totalGb(), d.freeGb(), d.percentUsed()));
        System.out.println("=================================================");
    }

    public static SystemSnapshot capture() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Runtime runtime = Runtime.getRuntime();

        long maxMb = runtime.maxMemory() / (1024 * 1024);
        long totalMb = runtime.totalMemory() / (1024 * 1024);
        long freeMb = runtime.freeMemory() / (1024 * 1024);
        long usedMb = totalMb - freeMb;
        double heapPct = maxMb > 0 ? (usedMb * 100.0 / maxMb) : 0.0;

        List<DiskSnapshot> disks = List.of(File.listRoots()).stream()
                .filter(File::canRead)
                .map(root -> {
                    long totalGb = root.getTotalSpace() / (1024 * 1024 * 1024);
                    long freeGb = root.getFreeSpace() / (1024 * 1024 * 1024);
                    long usedGb = totalGb - freeGb;
                    double pct = totalGb > 0 ? (usedGb * 100.0 / totalGb) : 0.0;
                    return new DiskSnapshot(root.getAbsolutePath(), totalGb, freeGb, pct);
                })
                .toList();

        return new SystemSnapshot(
                os.getAvailableProcessors(),
                os.getSystemLoadAverage(),
                new MemorySnapshot(usedMb, freeMb, maxMb, heapPct),
                threadBean.getThreadCount(),
                disks
        );
    }
}
