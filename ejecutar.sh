#!/bin/sh
# Compila y abre el Casino Broadway sin IDE (macOS / Linux). Requiere JDK 17 o superior.
cd "$(dirname "$0")"
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java") && java -cp bin broadway.Main
