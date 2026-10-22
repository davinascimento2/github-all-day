package com.allday;

import java.util.Arrays;

/**
 * AllDay - Master CLI Dispatcher for 10 Functional Java Developer Tools.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("--help")) {
            printBanner();
            return;
        }

        String tool = args[0].toLowerCase();
        String[] toolArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (tool) {
            case "gitpulse" -> com.allday.gitpulse.GitPulse.main(toolArgs);
            case "portwatch" -> com.allday.portwatch.PortWatch.main(toolArgs);
            case "jsoncraft" -> com.allday.jsoncraft.JsonCraft.main(toolArgs);
            case "envvault" -> com.allday.envvault.EnvVault.main(toolArgs);
            case "filejanitor" -> com.allday.filejanitor.FileJanitor.main(toolArgs);
            case "logpulse" -> com.allday.logpulse.LogPulse.main(toolArgs);
            case "quickhttp" -> com.allday.quickhttp.QuickHttp.main(toolArgs);
            case "cronparser" -> com.allday.cronparser.CronParser.main(toolArgs);
            case "textforge" -> com.allday.textforge.TextForge.main(toolArgs);
            case "syspulse" -> com.allday.syspulse.SysPulse.main(toolArgs);
            default -> {
                System.err.println("Unknown tool: '" + tool + "'");
                printBanner();
            }
        }
    }

    private static void printBanner() {
        System.out.println("""
            ========================================================================
                      GITHUB ALL DAY - SUÍTE DE 10 FERRAMENTAS JAVA FUNCIONAL
            ========================================================================
            Uso: java -jar allday.jar <ferramenta> [argumentos...]

            Ferramentas disponíveis:
              1. gitpulse     - Análise de produtividade, commits e heatmap Git
              2. portwatch    - Scanner assíncrono de portas TCP e integridade
              3. jsoncraft    - Formatador, minificador e validador JSON sem dependências
              4. envvault     - Criptografia AES-GCM 256 de .env e detector de vazamentos
              5. filejanitor  - Localizador de duplicatas por SHA-256 e limpeza de builds
              6. logpulse     - Auditor de logs (Spring/Nginx), taxa de erros e exceções
              7. quickhttp    - Benchmark e teste de carga HTTP com percentis de latência
              8. cronparser   - Tradutor e preditor de expressões Cron
              9. textforge    - Métricas de texto, tokens e formatador de tabelas Markdown
              10. syspulse    - Diagnóstico de CPU, memória heap JVM e discos do sistema
            ========================================================================
            """);
    }
}
