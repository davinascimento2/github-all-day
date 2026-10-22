# 🚀 GitHub All Day

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Status](https://img.shields.io/badge/Activity-Daily%20Pulse%20Automated-brightgreen?style=for-the-badge&logo=githubactions&logoColor=white)](.github/workflows/daily-pulse.yml)
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero%20External-blue?style=for-the-badge)](#)
[![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)](LICENSE)

<!-- LAST_PULSE_START -->⭐ Last Active Pulse: `2026-10-22 16:10:00Z` <!-- LAST_PULSE_END -->

Uma suíte de **10 ferramentas utilitárias funcionais para o dia a dia**, desenvolvidas em **Java 21 LTS** puro (sem dependências pesadas externas), acompanhada de automação para manter seu perfil do GitHub sempre **ativo, produtivo e com histórico de commits diários**.

---

## 🛠️ Catálogo das 10 Ferramentas Java

Todas as ferramentas podem ser executadas individualmente ou por meio do disparador mestre unificado:

```bash
java -jar target/allday.jar <ferramenta> [argumentos...]
```

| # | Ferramenta | Pacote | O que faz no seu dia a dia |
|---|---|---|---|
| 1 | **`gitpulse`** | `com.allday.gitpulse` | Analisa repositórios Git, calcula sequências de commits (streaks), gera heatmaps horários no terminal e estatísticas de autores. |
| 2 | **`portwatch`** | `com.allday.portwatch` | Scanner assíncrono de portas TCP e resolvedor de serviços para verificar saúde de portas de desenvolvimento (Postgres, Redis, React, etc.). |
| 3 | **`jsoncraft`** | `com.allday.jsoncraft` | Validador, formatador (pretty-print/minify) e analisador de estrutura e profundidade JSON em milissegundos. |
| 4 | **`envvault`** | `com.allday.envvault` | Criptografia militar AES-GCM 256 de arquivos `.env` com derivação PBKDF2, diff contra `.env.example` e detector de vazamento de chaves API. |
| 5 | **`filejanitor`** | `com.allday.filejanitor` | Localizador de arquivos duplicados usando hash SHA-256 e auditor/limpador de pastas pesadas de build (`node_modules`, `target`, `.next`). |
| 6 | **`logpulse`** | `com.allday.logpulse` | Leitor de arquivos de log de backend (Spring Boot, Nginx, Apache), cálculo de taxas de erro 4xx/5xx e ranking de exceções mais frequentes. |
| 7 | **`quickhttp`** | `com.allday.quickhttp` | Utilitário CLI para testes de carga e benchmarks HTTP com cálculo de percentis de latência (p50, p90, p95, p99) e req/s. |
| 8 | **`cronparser`** | `com.allday.cronparser` | Tradutor de sintaxe Cron para linguagem natural e simulador dos próximos 5 a 10 horários exatos de disparo. |
| 9 | **`textforge`** | `com.allday.textforge` | Conversor de nomenclatura (camelCase, snake_case, kebab-case), alinhador automático de tabelas Markdown e estimador de tokens de IA. |
| 10 | **`syspulse`** | `com.allday.syspulse` | Diagnóstico instantâneo de recursos do computador e da JVM (CPU, memória heap, threads ativas e unidades de disco) com exportação JSON. |

---

## ⚡ Exemplos Rápidos de Uso

### 1. Analisar histórico do Git (GitPulse)
```bash
java -jar target/allday.jar gitpulse .
```

### 2. Escanear portas locais (PortWatch)
```bash
java -jar target/allday.jar portwatch 127.0.0.1 3000 8080
```

### 3. Criptografar e Proteger `.env` (EnvVault)
```bash
# Criptografar
java -jar target/allday.jar envvault encrypt .env "MinhaSenhaForte"

# Auditar se há tokens vazando no código
java -jar target/allday.jar envvault audit .env
```

### 4. Testar latência de API (QuickHttp)
```bash
java -jar target/allday.jar quickhttp https://api.github.com 30 5
```

### 5. Explicar Cron Job (CronParser)
```bash
java -jar target/allday.jar cronparser "*/15 8-18 * * 1-5"
```

---

## 📈 Automação de Commits e Perfil Ativo

O projeto conta com **duas camadas de automação** para manter o GitHub sempre com atividade consistente:

1. **GitHub Actions (Cron na Nuvem)**:
   - Configurado em `.github/workflows/daily-pulse.yml`.
   - Executa diariamente às **12:00 UTC** direto nos servidores do GitHub.
   - Compila a suíte, executa benchmarks e registra o log diário em `stats/`, commitando de volta no repositório.
   - Funciona mesmo com o computador desligado!

2. **Script Local (PowerShell)**:
   - Localizado em `scripts/daily-commit.ps1`.
   - Pode ser acionado a qualquer momento ou agendado no Agendador de Tarefas do Windows / Antigravity `/schedule`.

---

## 📦 Como Compilar

### Opção 1: Script Rápido (Windows)
Execute `scripts/build-all.bat` ou pelo PowerShell:
```powershell
.\scripts\build-all.bat
```

### Opção 2: Linha de comando direta
```bash
javac -d target/classes $(find src/main/java -name "*.java")
jar --create --file target/allday.jar --main-class com.allday.Main -C target/classes .
```

---

## 👤 Autor
Desenvolvido por **Davi Nascimento**
- GitHub: [@davinascimento2](https://github.com/davinascimento2)
