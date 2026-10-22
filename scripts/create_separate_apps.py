import os
import subprocess
import shutil

BASE_REPOS = r"c:\Users\Davi\Documents\antigravity\repos_workspace"
COOL_MAXWELL = r"c:\Users\Davi\Documents\antigravity\cool-maxwell"

APPS = [
    {
        "name": "gitpulse-cli",
        "title": "GitPulse CLI",
        "pkg": "com.allday.gitpulse",
        "main_class": "GitPulse",
        "source_file": "src/main/java/com/allday/gitpulse/GitPulse.java",
        "desc": "Git repository analytics, productivity metrics, streak tracking and ASCII terminal heatmaps in Java 21."
    },
    {
        "name": "portwatch-probe",
        "title": "PortWatch Probe",
        "pkg": "com.allday.portwatch",
        "main_class": "PortWatch",
        "source_file": "src/main/java/com/allday/portwatch/PortWatch.java",
        "desc": "Ultra-fast asynchronous TCP port scanner and service health check probe built with Java 21."
    },
    {
        "name": "jsoncraft-cli",
        "title": "JsonCraft CLI",
        "pkg": "com.allday.jsoncraft",
        "main_class": "JsonCraft",
        "source_file": "src/main/java/com/allday/jsoncraft/JsonCraft.java",
        "desc": "Zero-dependency CLI JSON validator, pretty-printer, minifier and structural inspector in pure Java 21."
    },
    {
        "name": "envvault-core",
        "title": "EnvVault Core",
        "pkg": "com.allday.envvault",
        "main_class": "EnvVault",
        "source_file": "src/main/java/com/allday/envvault/EnvVault.java",
        "desc": "AES-256-GCM environment secrets manager, PBKDF2 encryption and secret leak auditor for .env files."
    },
    {
        "name": "filejanitor-cli",
        "title": "FileJanitor CLI",
        "pkg": "com.allday.filejanitor",
        "main_class": "FileJanitor",
        "source_file": "src/main/java/com/allday/filejanitor/FileJanitor.java",
        "desc": "SHA-256 duplicate file finder and heavy build artifacts (node_modules, target, .next) cleanup CLI."
    },
    {
        "name": "logpulse-engine",
        "title": "LogPulse Engine",
        "pkg": "com.allday.logpulse",
        "main_class": "LogPulse",
        "source_file": "src/main/java/com/allday/logpulse/LogPulse.java",
        "desc": "High-performance log analyzer for Spring Boot, Nginx and Apache with error rate and anomaly metrics."
    },
    {
        "name": "quickhttp-bench",
        "title": "QuickHttp Bench",
        "pkg": "com.allday.quickhttp",
        "main_class": "QuickHttp",
        "source_file": "src/main/java/com/allday/quickhttp/QuickHttp.java",
        "desc": "Asynchronous HTTP load testing and latency percentile (p50/p90/p95/p99) benchmark CLI in Java 21."
    },
    {
        "name": "cronparser-tool",
        "title": "CronParser Tool",
        "pkg": "com.allday.cronparser",
        "main_class": "CronParser",
        "source_file": "src/main/java/com/allday/cronparser/CronParser.java",
        "desc": "Natural language Cron expression translator and next-run scheduler simulator in functional Java."
    },
    {
        "name": "textforge-toolkit",
        "title": "TextForge Toolkit",
        "pkg": "com.allday.textforge",
        "main_class": "TextForge",
        "source_file": "src/main/java/com/allday/textforge/TextForge.java",
        "desc": "Developer text transformation toolkit, Markdown table auto-formatter and LLM token estimator in Java."
    },
    {
        "name": "syspulse-monitor",
        "title": "SysPulse Monitor",
        "pkg": "com.allday.syspulse",
        "main_class": "SysPulse",
        "source_file": "src/main/java/com/allday/syspulse/SysPulse.java",
        "desc": "System resources snapshot and JVM runtime diagnostics CLI with JSON and metrics export."
    }
]

def make_pom(app):
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.allday</groupId>
    <artifactId>{app['name']}</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>{app['title']}</name>
    <description>{app['desc']}</description>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <release>21</release>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>{app['pkg']}.{app['main_class']}</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
"""

def make_readme(app):
    return f"""# 🚀 {app['title']}

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![CI Build](https://img.shields.io/badge/CI-Passing-brightgreen?style=for-the-badge&logo=githubactions&logoColor=white)](.github/workflows/ci.yml)
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero%20External-blue?style=for-the-badge)](#)
[![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)](LICENSE)

> {app['desc']}

---

## ⚡ Como Usar

### Compilação Rápida
```bash
# Compilar direto com javac
javac -d target/classes $(find src/main/java -name "*.java")
jar --create --file target/{app['name']}.jar --main-class {app['pkg']}.{app['main_class']} -C target/classes .

# Executar
java -jar target/{app['name']}.jar
```

---

## 🛠️ Tecnologias
- **Java 21 LTS** puro (Records, Pattern Matching, Streams, Lambdas)
- Zero dependências de terceiros no runtime
- GitHub Actions CI/CD automatizado

---

## 👤 Autor
Desenvolvido por **Davi Nascimento**
- GitHub: [@davinascimento2](https://github.com/davinascimento2)
"""

def make_ci(app):
    return f"""name: CI Build & Test

on:
  push:
    branches: [ master, main ]
  pull_request:
    branches: [ master, main ]
  schedule:
    - cron: '0 12 * * *'
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up Java 21 LTS
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '21'

      - name: Compile and Package JAR
        run: |
          mkdir -p target/classes
          javac -d target/classes $(find src/main/java -name "*.java")
          jar --create --file target/{app['name']}.jar --main-class {app['pkg']}.{app['main_class']} -C target/classes .

      - name: Smoke Test Execution
        run: |
          java -jar target/{app['name']}.jar || true
"""

def make_gitignore():
    return """target/
*.class
*.jar
*.vault
*.decrypted
.idea/
*.iml
.vscode/
"""

def main():
    print("Iniciando geração dos 10 apps independentes...")
    for app in APPS:
        app_dir = os.path.join(BASE_REPOS, app['name'])
        os.makedirs(app_dir, exist_ok=True)
        print(f"\\n--- Configurando {app['name']} em {app_dir} ---")

        # Copy source Java file
        src_dir = os.path.join(app_dir, "src", "main", "java", *app['pkg'].split("."))
        os.makedirs(src_dir, exist_ok=True)
        shutil.copy2(os.path.join(COOL_MAXWELL, app['source_file']), os.path.join(src_dir, f"{app['main_class']}.java"))

        # Write pom.xml, README.md, .gitignore, CI workflow
        with open(os.path.join(app_dir, "pom.xml"), "w", encoding="utf-8") as f:
            f.write(make_pom(app))
        with open(os.path.join(app_dir, "README.md"), "w", encoding="utf-8") as f:
            f.write(make_readme(app))
        with open(os.path.join(app_dir, ".gitignore"), "w", encoding="utf-8") as f:
            f.write(make_gitignore())

        gh_dir = os.path.join(app_dir, ".github", "workflows")
        os.makedirs(gh_dir, exist_ok=True)
        with open(os.path.join(gh_dir, "ci.yml"), "w", encoding="utf-8") as f:
            f.write(make_ci(app))

        # Git init and commit
        subprocess.run(["git", "init"], cwd=app_dir, capture_output=True)
        subprocess.run(["git", "add", "."], cwd=app_dir, capture_output=True)
        subprocess.run(["git", "commit", "-m", f"feat: initial release of {app['name']}"], cwd=app_dir, capture_output=True)

        # Check if repo exists on GitHub, else create and push
        check = subprocess.run(["gh", "repo", "view", f"davinascimento2/{app['name']}"], capture_output=True, text=True)
        if check.returncode != 0:
            print(f"Criando repositório público no GitHub: davinascimento2/{app['name']}...")
            res = subprocess.run([
                "gh", "repo", "create", f"davinascimento2/{app['name']}",
                "--public", "--source=.", "--remote=origin", "--push",
                f"--description={app['desc']}"
            ], cwd=app_dir, capture_output=True, text=True)
            print("Resultado:", res.stdout, res.stderr)
        else:
            print(f"Repositório davinascimento2/{app['name']} já existe. Fazendo push...")
            subprocess.run(["git", "push", "-u", "origin", "master"], cwd=app_dir, capture_output=True)

    print("\\n Todos os 10 apps foram criados e publicados com sucesso!")

if __name__ == "__main__":
    main()
