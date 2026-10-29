#!/usr/bin/env bash
# verify 2026-10-29 (v29): records the UNMODIFIED legacy over the fresh Normal inputs, 20 repetitions; output only under verify20261027/fresh/nor/ (compiles into verify20261027/tmp/v29nor).
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot" PYTHONUTF8=1
mkdir -p "$HERE/tmp/v29nor/work" "$HERE/fresh/nor/legacy-dumps"
ORACLE_WORK="$HERE/tmp/v29nor/work" ORACLE_CASES="$HERE/fresh/nor/cases" ORACLE_OUT="$HERE/fresh/nor/legacy-dumps" ORACLE_REPORT="$HERE/fresh/nor/determinism-fresh.txt" ORACLE_REPEAT=20 \
  bash "$HERE/v29-record_nor.sh" VF-NOR > "$HERE/fresh/nor/oracle-run.log" 2>&1
echo "exit $?" >> "$HERE/fresh/nor/oracle-run.log"
