#!/usr/bin/env bash
# Step 4B: dump of the NEW Manejador (module classes in target/classes) through the oracle, same cases and same dump as the goldens.
#   ./run4b.sh <outDir> [casePrefix]        (default prefix FMM-MAN)       needs: mvn compile of the module done (target/classes)
#   ORACLE_RERECORD=1 ./run4b.sh ...        re-records the goldens of the prefix FROM THE LEGACY first (record.sh), never by hand
# The new Manejador runs over the UNMODIFIED legacy Normal and the oracle doubles (cl.bcs.oracle.ManejadorNuevoSubject, runner4b/).
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
OUT="$1"; PREFIX="${2:-FMM-MAN}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
export ORACLE_WORK="$WORK" JAVA_HOME="$JH"
SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
MOD="${MODULE_CLASSES:-$WS/modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/target/classes}"
[ -d "$MOD/cl/bcs/fondomutuo/manejador" ] || { echo "module not compiled: $MOD" >&2; exit 3; }

if [ -n "${ORACLE_RERECORD:-}" ]; then "$HERE/record.sh" "$PREFIX"; fi
# 1) compile the oracle (legacy dump of the prefix into a scratch dir: also a determinism check of the goldens)
ORACLE_OUT="$WORK/legacy-dump" "$HERE/record.sh" "$PREFIX" > "$WORK/legacy-run.log"
# 2) compile the subject (JDK 25) next to the oracle classes
CP="$(mx "$WORK/classes")"
for j in "com/google/code/gson/gson/2.13.2/gson-2.13.2.jar" "org/springframework/spring-context/7.0.9/spring-context-7.0.9.jar" \
         "org/springframework/spring-beans/7.0.9/spring-beans-7.0.9.jar" "org/springframework/spring-core/7.0.9/spring-core-7.0.9.jar"; do
  CP="$CP$SEP$(mx "$M2/$j")"
done
CP="$CP$SEP$(mx "$MOD")"
: > "$WORK/sources4b.txt"
find "$HERE/runner4b" -name "*.java" | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources4b.txt"; done
"$JH/bin/javac" --release 25 -encoding UTF-8 -proc:none -nowarn -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources4b.txt")"
# 3) run the NEW subject (classes already compiled: ORACLE_SKIP_COMPILE keeps $WORK/classes with the subject)
mkdir -p "$OUT"
ORACLE_SKIP_COMPILE=1 ORACLE_SUBJECT=cl.bcs.oracle.ManejadorNuevoSubject ORACLE_OUT="$OUT" ORACLE_EXTRA_CP="$MOD" \
  JAVA_OPTS="${JAVA_OPTS:-}" "$HERE/record.sh" "$PREFIX"
echo "legacy dump: $WORK/legacy-dump ; new dump: $OUT"
