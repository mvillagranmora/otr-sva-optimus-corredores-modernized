#!/usr/bin/env bash
# Legacy-side mutation check: proves the recorded goldens react to a change of the LEGACY behaviour.
#
#   ./mutation-check.sh <file relative to cl/bcs/tesoreria/> <line> <text to replace> <replacement> [casePrefix]
#   e.g. ./mutation-check.sh dep/GestionDeDepositos.java 366 '" TES.DepositoDetalle"' '"TES.DepositoDetalle"' TES-B-24
#
# 1. copies the legacy Tesoreria sources and this test resource folder into a scratch directory (NOTHING in legacy/ or in the module is touched),
# 2. applies the one-line mutation to the scratch copy of the legacy source (fails if the text is not on that line),
# 3. re-records every case (or those starting with casePrefix) with the oracle against the mutated copy,
# 4. prints the cases whose expected.json differs from the committed golden. At least one difference = the goldens pin that line.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RES="$(cd "$HERE/.." && pwd)"
MODULE="$(cd "$RES/../../../.." && pwd)"
WS="$(cd "$MODULE/../../.." && pwd)"
LEGSRC="$WS/legacy/otr-sva-optimus-corredores/BSCL/Tesoreria/JAVA/BcsTesoreriaServicios/src"
FILE="$1"; LINE="$2"; FROM="$3"; TO="$4"; PREFIX="${5:-TES-B-}"
SCR="$(mktemp -d)"
mkdir -p "$SCR/mod/src/test/resources" "$SCR/mod/src/test/java/cl/bcs/tesoreria/familia_b"
cp -r "$RES" "$SCR/mod/src/test/resources/tesoreria-b"
cp -r "$MODULE/src/test/java/cl/bcs/tesoreria/familia_b/support" "$SCR/mod/src/test/java/cl/bcs/tesoreria/familia_b/support"
cp -r "$LEGSRC" "$SCR/tes"
T="$SCR/tes/cl/bcs/tesoreria/$FILE"
python - "$T" "$LINE" "$FROM" "$TO" <<'PY'
import sys
p, n, a, b = sys.argv[1], int(sys.argv[2]), sys.argv[3], sys.argv[4]
lines = open(p, encoding="utf-8", errors="surrogateescape", newline="").read().split("\n")
assert a in lines[n - 1], "text %r not found on line %d: %r" % (a, n, lines[n - 1])
lines[n - 1] = lines[n - 1].replace(a, b)
open(p, "w", encoding="utf-8", errors="surrogateescape", newline="").write("\n".join(lines))
PY
LEGACY_TES_SRC="$SCR/tes" LEGACY_BSCL="$WS/legacy/otr-sva-optimus-corredores/BSCL" ORACLE_WORK="$SCR/work" bash "$SCR/mod/src/test/resources/tesoreria-b/legacy-oracle/record.sh" "$PREFIX" > "$SCR/record.log" 2>&1 || { tail -20 "$SCR/record.log"; exit 1; }
CHANGED=0; TOTAL=0
for d in "$RES"/cases/${PREFIX}*; do
  n="$(basename "$d")"; TOTAL=$((TOTAL+1))
  if ! cmp -s "$d/expected.json" "$SCR/mod/src/test/resources/tesoreria-b/cases/$n/expected.json"; then CHANGED=$((CHANGED+1)); echo "CHANGED: $n"; fi
done
echo "mutation $FILE:$LINE  cases re-recorded: $TOTAL  goldens changed: $CHANGED   (scratch: $SCR)"
