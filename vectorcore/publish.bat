@echo off
echo Publicando VectorCore no GitHub...
gh repo create davinascimento2/vectorcore --public --source=. --remote=origin --push --description "Lightweight in-memory vector database and HNSW hybrid search engine in Java 21."
if %ERRORLEVEL% EQU 0 (
    echo [OK] Repositorio VectorCore publicado com sucesso no GitHub!
) else (
    echo [AVISO] Limite temporario do GitHub ainda ativo. Aguarde alguns minutos e tente novamente.
)
pause
