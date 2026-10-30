#!/usr/bin/env bash
# Runs the legacy oracle (needs compilar_legacy.sh first):   correr_legacy.sh <defecto|entradas|ejecutar> <args of LegacyApvOracle after ops.json>
#   correr_legacy.sh defecto  <outDir>               default instances -> outDir/<Caso>/{entrada,salida}.json
#   correr_legacy.sh entradas <inDir>                fresh inputs -> inDir/<id>.json + index.json
#   correr_legacy.sh ejecutar <inDir> <outDir>       legacy gson.fromJson + gson.toJson of every input -> outDir/<id>/out.json
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/entorno.sh"
OPS="$HERE/ops.json"
CP="$(classpath_legacy)"
MODO="$1"; shift
ARGS=()
for a in "$@"; do ARGS+=("$(mx "$a")"); done
"$JAVA_HOME/bin/java" "${RUNOPTS[@]}" -cp "$(mx "$WORK/oracle-legacy")$SEP$(mx "$WORK/legacy-build/classes")$SEP$CP" LegacyApvOracle "$MODO" "$(mx "$OPS")" "${ARGS[@]}"
