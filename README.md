# 🚀 GitHub All Day - Ecossistema de Ferramentas & Flagship AI Engine

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Status](https://img.shields.io/badge/Activity-Daily%20Pulse%20Automated-brightgreen?style=for-the-badge&logo=githubactions&logoColor=white)](.github/workflows/daily-pulse.yml)
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero%20External-blue?style=for-the-badge)](#)
[![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)](LICENSE)

<!-- LAST_PULSE_START -->⭐ Last Active Pulse: `2026-10-25 2026-10-25T15:43:58Z` <!-- LAST_PULSE_END -->

Este ecossistema reúne um **motor emblemático de Inteligência Artificial / Vector Database** e uma suíte completa de **10 ferramentas utilitárias funcionais para desenvolvedores**, construídas em **Java 21 LTS** puro (sem dependências externas) com pipelines de CI/CD automatizados.

---

## ⚡ Flagship Project: [VectorCore Engine](./vectorcore)
Um banco de dados vetorial em memória ultra-rápido para **RAG (Retrieval-Augmented Generation)**:
- **Navegação HNSW Multi-Camadas**: Busca aproximada (ANN) em $\mathcal{O}(\log N)$ com mais de **4.100 QPS** e **99.5% de recall**.
- **Busca Híbrida com BM25**: Combina embeddings semânticos densos com palavras-chave exatas via **Reciprocal Rank Fusion (RRF)**.
- **Dashboard Web & REST API**: Interface visual dark-mode embutida na porta 8080 (`com.sun.net.httpserver` com Virtual Threads).
- **Persistência Binária Ultra-Rápida**: Snapshots compactos em disco.

```bash
# Executar o VectorCore Dashboard
java -jar vectorcore/target/vectorcore.jar server 8080
```

---

## 🌐 Repositórios Independentes Publicados no GitHub

Cada aplicação abaixo possui seu próprio repositório dedicado, código-fonte modular, documentação e workflow de GitHub Actions:

| # | Repositório no GitHub | Finalidade / O que faz | Status |
|---|---|---|---|
| 1 | [**`gitpulse-cli`**](https://github.com/davinascimento2/gitpulse-cli) | Analisador de histórico Git, cálculo de sequências de commits (streaks) e heatmap no terminal. | ![Active](https://img.shields.io/badge/Public-Active-green) |
| 2 | [**`portwatch-probe`**](https://github.com/davinascimento2/portwatch-probe) | Scanner assíncrono de portas TCP e resolvedor de serviços de banco/web. | ![Active](https://img.shields.io/badge/Public-Active-green) |
| 3 | [**`jsoncraft-cli`**](https://github.com/davinascimento2/jsoncraft-cli) | Formatador (pretty/minify), validador e analisador estrutural de JSON em Java puro. | ![Active](https://img.shields.io/badge/Public-Active-green) |
| 4 | [**`envvault-core`**](https://github.com/davinascimento2/envvault-core) | Criptografia militar AES-GCM 256 de `.env`, diff contra `.env.example` e detector de vazamentos. | ![Active](https://img.shields.io/badge/Public-Active-green) |
| 5 | [**`filejanitor-cli`**](https://github.com/davinascimento2/filejanitor-cli) | Localizador de arquivos duplicados por hash SHA-256 e limpador de pastas pesadas de build. | ![Active](https://img.shields.io/badge/Public-Active-green) |
| 6 | [**`logpulse-engine`**](https://github.com/davinascimento2/logpulse-engine) | Auditor de arquivos de log (Spring Boot, Nginx, Apache), taxas de erro e ranking de exceções. | ![Active](https://img.shields.io/badge/Public-Active-green) |
| 7 | [**`quickhttp-bench`**](https://github.com/davinascimento2/quickhttp-bench) | Utilitário CLI para testes de carga e benchmarks HTTP com cálculo de percentis de latência. | ![Active](https://img.shields.io/badge/Public-Active-green) |
| 8 | [**`cronparser-tool`**](https://github.com/davinascimento2/cronparser-tool) | Tradutor de sintaxe Cron para linguagem natural e simulador dos próximos horários de disparo. | ![Active](https://img.shields.io/badge/Public-Active-green) |
| 9 | [**`textforge-toolkit`**](https://github.com/davinascimento2/textforge-toolkit) | Utilitário de formatação de tabelas Markdown, conversão de case e estimador de tokens de IA. | ![Active](https://img.shields.io/badge/Public-Active-green) |
| 10 | [**`syspulse-monitor`**](https://github.com/davinascimento2/syspulse-monitor) | Diagnóstico instantâneo de recursos do PC e JVM (CPU, heap, threads e discos) com exportação JSON. | ![Active](https://img.shields.io/badge/Local-Ready-blue) |

---

## ⚡ Como Executar a Suíte Unificada Localmente

Se preferir rodar todas as 10 ferramentas a partir deste repositório central:

```bash
# Compilar todas as ferramentas
.\scripts\build-all.bat

# Executar qualquer ferramenta através do dispatcher unificado
java -jar target/allday.jar <nome-da-ferramenta> [argumentos...]
```

---

## 👤 Autor
Desenvolvido por **Davi Nascimento**
- GitHub: [@davinascimento2](https://github.com/davinascimento2)
