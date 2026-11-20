@echo off
rem Compila y abre el Casino Broadway sin IDE (Windows). Requiere JDK 17 o superior.
cd /d "%~dp0"
if not exist bin mkdir bin
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d bin @fuentes.txt
if errorlevel 1 (
  echo No se pudo compilar. Revisa que tengas instalado un JDK 17 o superior.
  del fuentes.txt
  pause
  exit /b 1
)
del fuentes.txt
java -cp bin broadway.Main
