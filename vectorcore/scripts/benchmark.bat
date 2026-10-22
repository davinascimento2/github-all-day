@echo off
set JDK_BIN=%LOCALAPPDATA%\Programs\jdk-21\bin
if not exist "%JDK_BIN%\java.exe" set JDK_BIN=java

"%JDK_BIN%\java.exe" -jar target\vectorcore.jar benchmark 5000 32
pause
