#!/usr/bin/env bash
# Legacy-side mutation check: proves the recorded goldens react to a change of the LEGACY behaviour.
#
#   ./mutation-check.sh <file relative to cl/bcs/APV/> <line> <text to replace> <replacement> [casePrefix]
#   e.g. ./mutation-check.sh CalculoRetencionTraspasos.java 175 'salida.MontoRetencion.add(MontoRetencion)' 'salida.MontoRetencion' APV-B-4
#
# 1. copies the legacy APV sources and this test resource folder into a scratch directory (NOTHING in legacy/ or in the module is touched),
# 2. applies the one-line mutation to the scratch copy of the legacy source (fails if the text is not on that line),
# 3. re-records every case (or those starting with casePrefix) with the oracle against the mutated copy (record.sh with LEGACY_APV_SRC),
# 4. prints the cases whose expected.json differs from the committed golden. At least one difference = the goldens pin that line.
# ORACLE_REUSE_CLASSES is never honoured here (the mutated source must be compiled).
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RES="$(cd "$HERE/.." && pwd)"
MODULE="$(cd "$RES/../../../.." && pwd)"
WS="$(cd "$MODULE/../../.." && pwd)"
LEGBSCL="${LEGACY_BSCL:-$WS/legacy/otr-sva-optimus-corredores/BSCL}"
LEGSRC="$LEGBSCL/APV/JAVA/BcsAPVServicios/src"
FILE="$1"; LINE="$2"; FROM="$3"; TO="$4"; PREFIX="${5:-APV-B-}"
SCR="${MUTATION_SCRATCH:-$(mktemp -d)}"
mkdir -p "$SCR/mod/src/test/resources" "$SCR/mod/src/test/java/cl/bcs/apv/familia_b"
cp -r "$RES" "$SCR/mod/src/test/resources/apv-b"
cp -r "$MODULE/src/test/java/cl/bcs/apv/familia_b/support" "$SCR/mod/src/test/java/cl/bcs/apv/familia_b/support"
cp -r "$LEGSRC" "$SCR/apv"
T="$SCR/apv/cl/bcs/APV/$FILE"
python - "$T" "$LINE" "$FROM" "$TO" <<'PY'
import sys
p, n, a, b = sys.argv[1], int(sys.argv[2]), sys.argv[3], sys.argv[4]
lines = open(p, encoding="utf-8", errors="surrogateescape", newline="").read().split("\n")
assert a in lines[n - 1], "text %r not found on line %d: %r" % (a, n, lines[n - 1])
lines[n - 1] = lines[n - 1].replace(a, b)
open(p, "w", encoding="utf-8", errors="surrogateescape", newline="").write("\n".join(lines))
PY
ORACLE_REUSE_CLASSES=0 LEGACY_BSCL="$LEGBSCL" LEGACY_APV_SRC="$SCR/apv" ORACLE_WORK="$SCR/work" bash "$SCR/mod/src/test/resources/apv-b/legacy-oracle/record.sh" "$PREFIX" > "$SCR/record.log" 2>&1 || { tail -20 "$SCR/record.log"; exit 1; }
python - "$RES/cases" "$SCR/mod/src/test/resources/apv-b/cases" "$PREFIX" <<'PY'
import os, sys
orig, mut, prefix = sys.argv[1:4]
changed = total = 0
for n in sorted(os.listdir(orig)):
    if not n.startswith(prefix):
        continue
    total += 1
    if open(os.path.join(orig, n, "expected.json"), "rb").read() != open(os.path.join(mut, n, "expected.json"), "rb").read():
        changed += 1
        print("CHANGED: " + n)
print("cases re-recorded: %d  goldens changed: %d" % (total, changed))
PY
echo "mutation $FILE:$LINE done   (scratch: $SCR)"
