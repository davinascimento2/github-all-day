@echo off
echo ========================================================================
echo   Compilando GitHub All Day - Suíte 10 Ferramentas Java 21
echo ========================================================================

set JDK_BIN=%LOCALAPPDATA%\Programs\jdk-21\bin
if not exist "%JDK_BIN%\javac.exe" (
    set JDK_BIN=javac
)

if not exist "target\classes" mkdir target\classes

echo Compilando fontes Java...
dir /s /b src\main\java\*.java > target\sources.txt
"%JDK_BIN%\javac.exe" -d target\classes @target\sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo Erro na compilacao!
    exit /b 1
)

echo Empacotando JAR executavel...
"%JDK_BIN%\jar.exe" --create --file target\allday.jar --main-class com.allday.Main -C target\classes .

echo Testando execucao...
"%JDK_BIN%\java.exe" -jar target\allday.jar help

echo ========================================================================
echo   Build concluido com sucesso em target\allday.jar!
echo ========================================================================
pause
