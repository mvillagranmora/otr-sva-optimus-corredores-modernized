#!/usr/bin/env bash
# Runs the NEW-side oracle against the compiled modules (BcsAPVApi/target/classes and BcsApvJavaCallAdapter/target/classes: run `mvn -o compile` or `test` first):
#   correr_nuevo.sh defecto <outDir>
#   correr_nuevo.sh ejecutar <inDir> <outDir>
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/entorno.sh"
for m in BcsAPVApi BcsApvJavaCallAdapter; do
  [ -d "$MODS/$m/target/classes/cl" ] || { echo "no compiled classes in $MODS/$m/target/classes: build the module first (mvn -o compile)" >&2; exit 3; }
done
CP="$(mx "$MODS/BcsAPVApi/target/classes")$SEP$(mx "$MODS/BcsApvJavaCallAdapter/target/classes")$SEP$GSON_JAR"
mkdir -p "$WORK/oracle-nuevo"
"$JAVA_HOME/bin/javac" -encoding UTF-8 -nowarn -d "$(mx "$WORK/oracle-nuevo")" -cp "$CP" "$(mx "$HERE/java/NuevoApvOracle.java")"
MODO="$1"; shift
ARGS=()
for a in "$@"; do ARGS+=("$(mx "$a")"); done
"$JAVA_HOME/bin/java" "${RUNOPTS[@]}" -cp "$(mx "$WORK/oracle-nuevo")$SEP$CP" NuevoApvOracle "$MODO" "$(mx "$HERE/ops.json")" "${ARGS[@]}"
