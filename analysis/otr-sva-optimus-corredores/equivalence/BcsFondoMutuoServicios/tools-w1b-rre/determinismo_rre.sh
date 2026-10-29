#!/usr/bin/env bash
# F2 W1 front 2, plan 7 (determinism N = 20): records every FMM-RRE case 20 more times from the unmodified legacy (record_rre.sh, ONE JVM per run, the same
# compiled oracle) into tools-w1b-rre/determinismo/run-NN/ (ORACLE_OUT: the goldens are never touched) and compares each dump byte for byte with
# cases/<id>/expected.json. Result: tools-w1b-rre/determinismo/RESULTADO.txt
#   ORACLE_WORK=<compiled work dir> tools-w1b-rre/determinismo_rre.sh [N]
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
N="${1:-20}"; PREF="${2:-FMM-RRE}"
OUT="$HERE/determinismo${3:-}"
rm -rf "$OUT"; mkdir -p "$OUT"
for i in $(seq -w 1 "$N"); do
  ORACLE_SKIP_COMPILE=1 ORACLE_OUT="$OUT/run-$i" bash "$BASE/legacy-oracle/record_rre.sh" "$PREF" > "$OUT/run-$i.log" 2>&1
done
{
  echo "runs: $N"
  total=0; dif=0
  for d in "$BASE"/cases/"$PREF"*; do
    id="$(basename "$d")"; distintos=0
    for r in "$OUT"/run-*/; do
      total=$((total+1))
      if ! cmp -s "$d/expected.json" "$r/$id.json"; then distintos=$((distintos+1)); dif=$((dif+1)); fi
    done
    echo "$id: $distintos of $N differ"
  done
  echo "TOTAL comparisons: $total, different: $dif"
} > "$OUT/RESULTADO.txt"
tail -1 "$OUT/RESULTADO.txt"
