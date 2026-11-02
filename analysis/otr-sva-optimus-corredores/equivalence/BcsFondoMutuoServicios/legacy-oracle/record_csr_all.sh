#!/usr/bin/env bash
# F3 W3 T4-A: records the goldens of EVERY FMM-CSR case with the oracle of record_csr.sh, in parallel shards (one JVM per two-digit prefix of the case number: FMM-CSR-00, -01, ...), each
# with its OWN copy of the compiled oracle (record_csr.sh recreates its work directory), and merges the per-shard stability reports into determinism-csr.txt (cases x 20 runs, 0 unstable).
# The two lots of 2.000 aportes take about two minutes per run; the whole recording with 20 runs per case takes a few hours of wall time on 8 shards.
#   ./record_csr_all.sh [repeats=20] [shards=8]        (ORACLE_WORK = scratch dir, default a temporary one; JAVA_HOME as record_csr.sh)
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
REPEAT="${1:-20}"
SHARDS="${2:-8}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
PY="${PYTHON_CMD:-py -3}"
mkdir -p "$WORK/shards"
echo "== compiling the oracle once into $WORK/base"
ORACLE_WORK="$WORK/base" ORACLE_REPEAT=1 ORACLE_CASES="$WORK/none" ORACLE_REPORT="$WORK/none.txt" bash "$HERE/record_csr.sh" FMM-CSR-000 > "$WORK/compile.log" 2>&1 || true   # no case has that prefix: only the compilation matters
[ -d "$WORK/base/classes" ] || { echo "oracle did not compile; see $WORK/compile.log" >&2; exit 3; }
ls "$BASE/cases-csr" | sed -E 's/^(FMM-CSR-[0-9]{2}).*/\1/' | sort -u > "$WORK/prefijos.txt"
export HERE BASE REPEAT WORK
< "$WORK/prefijos.txt" xargs -P "$SHARDS" -I{} bash -c '
  rm -rf "$WORK/w/{}"; mkdir -p "$WORK/w"; cp -r "$WORK/base" "$WORK/w/{}"
  ORACLE_WORK="$WORK/w/{}" ORACLE_SKIP_COMPILE=1 ORACLE_REPEAT="$REPEAT" ORACLE_REPORT="$WORK/shards/{}.txt" bash "$HERE/record_csr.sh" {} > "$WORK/shards/{}.log" 2>&1
  echo "{} exit $?"'
$PY -I "$HERE/tools/merge_reports_csr.py" "$WORK/shards" "$BASE/determinism-csr.txt" "$REPEAT"
