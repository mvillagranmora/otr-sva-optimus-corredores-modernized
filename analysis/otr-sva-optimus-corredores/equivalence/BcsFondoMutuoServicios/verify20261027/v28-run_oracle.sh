#!/usr/bin/env bash
# verify 2026-10-28 (v28): records the UNMODIFIED legacy over the fresh inputs of one front (ffmm | cpfchain), 20 repetitions; output only under verify20261027/fresh/<front>/ (compiles into verify20261027/tmp).
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EQ="$(cd "$HERE/.." && pwd)"
fr="$1"
export JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot" PYTHONUTF8=1
mkdir -p "$HERE/tmp/work28-$fr" "$HERE/fresh/$fr/legacy-dumps"
ORACLE_WORK="$HERE/tmp/work28-$fr" ORACLE_CASES="$HERE/fresh/$fr/cases" ORACLE_OUT="$HERE/fresh/$fr/legacy-dumps" ORACLE_REPORT="$HERE/fresh/$fr/determinism-fresh.txt" ORACLE_REPEAT=20 \
  bash "$EQ/legacy-oracle/record_$fr.sh" > "$HERE/fresh/$fr/oracle-run.log" 2>&1
echo "exit $?" >> "$HERE/fresh/$fr/oracle-run.log"
