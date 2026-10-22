package com.allday.envvault;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * EnvVault - Secure Environment Secrets Manager & Leak Detector.
 * Functional Java 21 tool using AES-256-GCM, PBKDF2, Streams, and Records.
 */
public class EnvVault {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;

    public record SecretLeak(String key, String ruleDescription, String preview) {}

    public static void main(String[] args) {
        if (args.length < 2) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();
        Path targetFile = Path.of(args[1]);

        try {
            switch (command) {
                case "encrypt" -> {
                    String password = args.length > 2 ? args[2] : promptPassword("Enter master encryption password: ");
                    byte[] plaintext = Files.readAllBytes(targetFile);
                    byte[] encrypted = encrypt(plaintext, password.toCharArray());
                    Path outPath = Path.of(targetFile.toString() + ".vault");
                    Files.write(outPath, encrypted);
                    System.out.println("✓ Encrypted successfully to: " + outPath.toAbsolutePath());
                }
                case "decrypt" -> {
                    String password = args.length > 2 ? args[2] : promptPassword("Enter master decryption password: ");
                    byte[] ciphertext = Files.readAllBytes(targetFile);
                    byte[] decrypted = decrypt(ciphertext, password.toCharArray());
                    String originalName = targetFile.toString().replaceAll("\\.vault$", ".decrypted");
                    Path outPath = Path.of(originalName);
                    Files.write(outPath, decrypted);
                    System.out.println("✓ Decrypted successfully to: " + outPath.toAbsolutePath());
                }
                case "audit", "scan" -> {
                    List<String> lines = Files.readAllLines(targetFile);
                    List<SecretLeak> leaks = auditSecrets(lines);
                    System.out.println("=================================================");
                    System.out.println("   ENVVAULT - Security Audit & Secret Leak Scan  ");
                    System.out.println("=================================================");
                    System.out.printf("Audited file: %s (%d lines)\n", targetFile, lines.size());
                    if (leaks.isEmpty()) {
                        System.out.println("✓ No high-entropy or plain-text secrets detected!");
                    } else {
                        System.out.printf("⚠ Found %d potential risk(s):\n\n", leaks.size());
                        leaks.forEach(l -> System.out.printf("  [%s] %s -> %s\n", l.ruleDescription(), l.key(), l.preview()));
                    }
                    System.out.println("=================================================");
                }
                case "diff" -> {
                    if (args.length < 3) {
                        System.err.println("Usage: diff <.env> <.env.example>");
                        return;
                    }
                    Path exampleFile = Path.of(args[2]);
                    compareEnvFiles(targetFile, exampleFile);
                }
                default -> printHelp();
            }
        } catch (Exception e) {
            System.err.println("EnvVault error: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("""
            EnvVault Usage:
              java -cp . com.allday.envvault.EnvVault <command> <file> [options]

            Commands:
              encrypt <file> [password]  - Encrypt .env using AES-256-GCM with PBKDF2
              decrypt <vault> [password] - Decrypt .env.vault to plaintext
              audit <file>               - Scan file for plain-text API keys/tokens
              diff <.env> <.env.example> - Compare keys between actual and example env
            """);
    }

    private static String promptPassword(String prompt) {
        System.out.print(prompt);
        return new Scanner(System.in).nextLine();
    }

    public static byte[] encrypt(byte[] plaintext, char[] password) throws Exception {
        byte[] salt = new byte[16];
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        random.nextBytes(iv);

        SecretKey secretKey = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] cipherText = cipher.doFinal(plaintext);

        // Payload: [Salt (16)] + [IV (12)] + [Ciphertext + Tag]
        byte[] payload = new byte[salt.length + iv.length + cipherText.length];
        System.arraycopy(salt, 0, payload, 0, salt.length);
        System.arraycopy(iv, 0, payload, salt.length, iv.length);
        System.arraycopy(cipherText, 0, payload, salt.length + iv.length, cipherText.length);
        return payload;
    }

    public static byte[] decrypt(byte[] payload, char[] password) throws Exception {
        byte[] salt = Arrays.copyOfRange(payload, 0, 16);
        byte[] iv = Arrays.copyOfRange(payload, 16, 16 + GCM_IV_LENGTH);
        byte[] cipherText = Arrays.copyOfRange(payload, 16 + GCM_IV_LENGTH, payload.length);

        SecretKey secretKey = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(cipherText);
    }

    private static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    public static List<SecretLeak> auditSecrets(List<String> lines) {
        Pattern awsPattern = Pattern.compile("(?i)AKIA[0-9A-Z]{16}");
        Pattern gitHubPattern = Pattern.compile("(?i)gh[pousr]_[0-9a-zA-Z]{36}");
        Pattern genericKey = Pattern.compile("(?i)(api_key|secret|token|password|auth)\\s*=\\s*['\"]?([^'\"\\s]{8,})['\"]?");

        return lines.stream()
                .filter(l -> !l.isBlank() && !l.startsWith("#"))
                .flatMap(line -> {
                    List<SecretLeak> leaks = new ArrayList<>();
                    if (awsPattern.matcher(line).find()) {
                        leaks.add(new SecretLeak(line.split("=")[0].trim(), "AWS Access Key Pattern", mask(line)));
                    } else if (gitHubPattern.matcher(line).find()) {
                        leaks.add(new SecretLeak(line.split("=")[0].trim(), "GitHub Token Pattern", mask(line)));
                    } else {
                        var m = genericKey.matcher(line);
                        if (m.find()) {
                            leaks.add(new SecretLeak(m.group(1), "High-Entropy Plaintext Secret", mask(m.group(2))));
                        }
                    }
                    return leaks.stream();
                })
                .toList();
    }

    public static void compareEnvFiles(Path envPath, Path examplePath) throws Exception {
        Set<String> envKeys = extractKeys(Files.readAllLines(envPath));
        Set<String> exampleKeys = extractKeys(Files.readAllLines(examplePath));

        Set<String> missingInEnv = exampleKeys.stream()
                .filter(k -> !envKeys.contains(k))
                .collect(Collectors.toSet());

        Set<String> extraInEnv = envKeys.stream()
                .filter(k -> !exampleKeys.contains(k))
                .collect(Collectors.toSet());

        System.out.println("=================================================");
        System.out.println("   ENVVAULT - Environment Keys Difference Check  ");
        System.out.println("=================================================");
        System.out.printf("Missing in %s (found in example): %s\n", envPath.getFileName(), missingInEnv.isEmpty() ? "None (OK)" : missingInEnv);
        System.out.printf("Extra in %s (not in example): %s\n", envPath.getFileName(), extraInEnv.isEmpty() ? "None" : extraInEnv);
        System.out.println("=================================================");
    }

    private static Set<String> extractKeys(List<String> lines) {
        return lines.stream()
                .map(String::trim)
                .filter(l -> !l.startsWith("#") && l.contains("="))
                .map(l -> l.split("=")[0].trim())
                .collect(Collectors.toSet());
    }

    private static String mask(String value) {
        if (value.length() <= 6) return "******";
        return value.substring(0, 3) + "..." + value.substring(value.length() - 3);
    }
}
