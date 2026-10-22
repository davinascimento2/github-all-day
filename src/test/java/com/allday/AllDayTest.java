package com.allday;

import com.allday.cronparser.CronParser;
import com.allday.envvault.EnvVault;
import com.allday.jsoncraft.JsonCraft;
import com.allday.portwatch.PortWatch;
import com.allday.textforge.TextForge;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AllDayTest {

    @Test
    void testCronParser() {
        var schedule = CronParser.parse("*/15 9-17 * * 1-5");
        assertNotNull(schedule);
        assertTrue(schedule.minutes().contains(0));
        assertTrue(schedule.minutes().contains(15));
        assertTrue(schedule.minutes().contains(30));
        assertTrue(schedule.minutes().contains(45));

        List<LocalDateTime> next = CronParser.computeNextRuns(schedule, LocalDateTime.of(2026, 1, 1, 8, 0), 2);
        assertEquals(2, next.size());
    }

    @Test
    void testJsonCraft() {
        String raw = "{\"name\":\"AllDay\",\"tools\":10,\"active\":true}";
        assertTrue(JsonCraft.isValidJson(raw));
        String formatted = JsonCraft.formatJson(raw, 2);
        assertTrue(formatted.contains("\n"));
        var stats = JsonCraft.computeStats(raw);
        assertEquals(1, stats.objectCount());
        assertEquals(3, stats.keyCount());
    }

    @Test
    void testEnvVaultEncryption() throws Exception {
        String secret = "DATABASE_URL=postgres://user:pass@localhost:5432/db";
        char[] password = "MasterStrongPassword123!".toCharArray();

        byte[] encrypted = EnvVault.encrypt(secret.getBytes(), password);
        assertNotNull(encrypted);

        byte[] decrypted = EnvVault.decrypt(encrypted, password);
        assertEquals(secret, new String(decrypted));
    }

    @Test
    void testTextForge() {
        assertEquals("hello_world", TextForge.convertCase("Hello World", "snake"));
        assertEquals("hello-world", TextForge.convertCase("Hello World", "kebab"));
        assertEquals("helloWorld", TextForge.convertCase("hello_world", "camel"));
        assertEquals("HelloWorld", TextForge.convertCase("hello_world", "pascal"));

        var metrics = TextForge.analyze("Java 21 functional tools suite for daily productivity.");
        assertEquals(8, metrics.wordCount());
        assertTrue(metrics.estimatedTokens() > 0);
    }

    @Test
    void testPortWatchServiceGuess() {
        assertEquals("HTTPS Secure Web", PortWatch.guessService(443));
        assertEquals("PostgreSQL Database", PortWatch.guessService(5432));
        assertEquals("Redis In-Memory Cache", PortWatch.guessService(6379));
    }
}
