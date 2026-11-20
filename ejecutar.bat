@echo off
rem Launch with an existing JavaFX SDK on Windows. Requires JDK 17 or later.
setlocal
cd /d "%~dp0"
if not defined JAVAFX_LIB (
  echo Define JAVAFX_LIB con la carpeta lib de un SDK JavaFX 21 para Windows.
  exit /b 1
)
if not exist bin mkdir bin
set "JAVAC=javac"
set "JAVA=java"
if defined JAVA_HOME (
  set "JAVAC=%JAVA_HOME%\bin\javac.exe"
  set "JAVA=%JAVA_HOME%\bin\java.exe"
)
"%JAVAC%" --release 17 -encoding UTF-8 --module-path "%JAVAFX_LIB%" --add-modules javafx.controls --source-path src -d bin src/Main.java
if errorlevel 1 exit /b 1
"%JAVA%" --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -cp "bin;src" Main
