@echo off
set JDK_BIN=%LOCALAPPDATA%\Programs\jdk-21\bin
if not exist "%JDK_BIN%\java.exe" set JDK_BIN=java

if not exist "target\vectorcore.jar" (
    echo Compilando VectorCore...
    if not exist "target\classes" mkdir target\classes
    dir /s /b src\main\java\*.java > target\sources.txt
    "%JDK_BIN%\javac.exe" -d target\classes @target\sources.txt
    "%JDK_BIN%\jar.exe" --create --file target\vectorcore.jar --main-class com.vectorcore.VectorCoreApp -C target\classes .
)

echo Iniciando VectorCore Dashboard na porta 8080...
start http://localhost:8080
"%JDK_BIN%\java.exe" -jar target\vectorcore.jar server 8080
pause
