#!/bin/sh
# Launch with an existing JavaFX SDK or the matching artifacts in the local Maven cache.
set -eu
cd "$(dirname "$0")"

javafx_modules=${JAVAFX_LIB:-}
if [ -z "$javafx_modules" ] && [ -r /usr/share/openjfx/lib/javafx.controls.jar ]; then
    javafx_modules=/usr/share/openjfx/lib
fi
if [ -z "$javafx_modules" ]; then
    case "$(uname -s):$(uname -m)" in
        Linux:x86_64) platform=linux ;;
        Linux:aarch64) platform=linux-aarch64 ;;
        Darwin:x86_64) platform=mac ;;
        Darwin:arm64) platform=mac-aarch64 ;;
        *) platform= ;;
    esac
    if [ -n "$platform" ]; then
        repository=${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}/org/openjfx
        version=21.0.6
        base="$repository/javafx-base/$version/javafx-base-$version-$platform.jar"
        graphics="$repository/javafx-graphics/$version/javafx-graphics-$version-$platform.jar"
        controls="$repository/javafx-controls/$version/javafx-controls-$version-$platform.jar"
        if [ -r "$base" ] && [ -r "$graphics" ] && [ -r "$controls" ]; then
            javafx_modules="$base:$graphics:$controls"
        fi
    fi
fi
if [ -z "$javafx_modules" ]; then
    printf '%s\n' 'Define JAVAFX_LIB con la carpeta lib de un SDK JavaFX 21 compatible con tu equipo.' >&2
    exit 1
fi

mkdir -p bin
java_bin=${JAVA_HOME:+$JAVA_HOME/bin/}
"${java_bin}javac" --release 17 -encoding UTF-8 --module-path "$javafx_modules" --add-modules javafx.controls --source-path src -d bin src/Main.java
exec "${java_bin}java" --module-path "$javafx_modules" --add-modules javafx.controls -cp "bin:src" Main
