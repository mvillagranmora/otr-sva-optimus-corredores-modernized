#!/usr/bin/env bash
# verify 2026-10-27 (v27b): regenerates the fresh inputs, records the UNMODIFIED legacy with the front's oracle (output only under verify20261027/fresh/<front>/),
# runs the new code through the module harness and writes the canonical texts. Nothing outside verify20261027/ is written (the oracle compiles into verify20261027/tmp).
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EQ="$(cd "$HERE/.." && pwd)"
export JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot" PYTHONUTF8=1
PATH="$JAVA_HOME/bin:$PATH"
cd "$HERE"
py -3 v27b-gen_fresh.py
CP="$(cat tmp/v27b-cp.txt)"
for f in ifm:ifm sfm:sfm ws:sfm; do
  fr=${f%%:*}; sc=${f##*:}
  mkdir -p "$HERE/tmp/work-$fr"
  ORACLE_WORK="$HERE/tmp/work-$fr" ORACLE_CASES="$HERE/fresh/$fr/cases" ORACLE_OUT="$HERE/fresh/$fr/legacy-dumps" ORACLE_REPORT="$HERE/fresh/$fr/determinism-v27b.txt" \
    bash "$EQ/legacy-oracle/record_$sc.sh" > "$HERE/fresh/$fr/oracle-run.log" 2>&1 || { echo "oracle $fr failed"; tail -20 "$HERE/fresh/$fr/oracle-run.log"; exit 1; }
  "$JAVA_HOME/bin/java" -Duser.timezone=UTC -Duser.language=en -Dfile.encoding=UTF-8 -cp "tmp/cls;$CP" Volcar27b fresh "$fr" "$HERE/fresh/$fr/cases" "$HERE/fresh/$fr/legacy-dumps" "$HERE/fresh/$fr/out"
done
