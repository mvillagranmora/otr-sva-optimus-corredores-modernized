#!/usr/bin/env bash
# Legacy-side mutation check: proves the recorded goldens react to a change of the LEGACY behaviour.
#
#   ./mutation-check.sh <file relative to cl/bcs/APV/> <line> <text to replace> <replacement> [casePrefix]
#   e.g. ./mutation-check.sh CalculoRetencionTraspasos.java 175 'salida.MontoRetencion.add(MontoRetencion)' 'salida.MontoRetencion' APV-C-4
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
FILE="$1"; LINE="$2"; FROM="$3"; TO="$4"; PREFIX="${5:-APV-C-}"
SCR="${MUTATION_SCRATCH:-$(mktemp -d)}"
mkdir -p "$SCR/mod/src/test/resources" "$SCR/mod/src/test/java/cl/bcs/apv/familia_c"
cp -r "$RES" "$SCR/mod/src/test/resources/apv-c"
cp -r "$MODULE/src/test/java/cl/bcs/apv/familia_c/support" "$SCR/mod/src/test/java/cl/bcs/apv/familia_c/support"
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
ORACLE_REUSE_CLASSES=0 LEGACY_BSCL="$LEGBSCL" LEGACY_APV_SRC="$SCR/apv" ORACLE_WORK="$SCR/work" bash "$SCR/mod/src/test/resources/apv-c/legacy-oracle/record.sh" "$PREFIX" > "$SCR/record.log" 2>&1 || { tail -20 "$SCR/record.log"; exit 1; }
# one python process compares all goldens (a bash loop spawning cmp per case takes minutes on Windows)
python - "$RES/cases" "$SCR/mod/src/test/resources/apv-c/cases" "$PREFIX" "$FILE" "$LINE" "$SCR" <<'PY'
import glob, os, sys
orig, mut, prefix, f, line, scr = sys.argv[1:7]
total = changed = 0
for d in sorted(glob.glob(os.path.join(orig, prefix + "*"))):
    n = os.path.basename(d)
    total += 1
    a = open(os.path.join(d, "expected.json"), "rb").read()
    p = os.path.join(mut, n, "expected.json")
    if not os.path.exists(p) or open(p, "rb").read() != a:
        changed += 1
        print("CHANGED: " + n)
print("mutation %s:%s  cases re-recorded: %d  goldens changed: %d   (scratch: %s)" % (f, line, total, changed, scr))
PY
