#!/usr/bin/env bash
# Re-run of canary 7 of canary_rre.sh (D-F2-15 "fixed") after golden FMM-RRE-67 was added for its survivor (first run: 0 failing tests, log 7.log).
# Same scratch-copy rules as canary_rre.sh. Log: equivalence/canary/BcsFondoMutuoServicios-W1B/7b.log
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WS="$(cd "$HERE/../../../../.." && pwd)"
SRC="$WS/modernized/otr-sva-optimus-corredores"
LOGS="$WS/analysis/otr-sva-optimus-corredores/equivalence/canary/BcsFondoMutuoServicios-W1B"
: "${CANARY_SCRATCH:?}"
case "$CANARY_SCRATCH" in *modernized*|*analysis*) exit 2;; esac
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
export JAVA_TOOL_OPTIONS="-Duser.timezone=UTC -Dfile.encoding=UTF-8"
R="$CANARY_SCRATCH/root7"; rm -rf "$R"; mkdir -p "$R/BcsFondoMutuoServicios"
cp "$SRC/pom.xml" "$R/pom.xml"; cp -r "$SRC/BcsFondoMutuoServicios/pom.xml" "$SRC/BcsFondoMutuoServicios/src" "$R/BcsFondoMutuoServicios/"
X="$R/BcsFondoMutuoServicios/src/main/java/cl/bcs/fondomutuo/rescatereinversion"
sed -i 's/key.IdAgrupador = RreReglas.esCanalIngresoWebServices(c.entrada.p_IdTipoCanalIngreso) ? 0L : sol.IdAgrupador;/key.IdAgrupador = RreReglas.esCanalIngresoWebServices(c.entrada.p_IdTipoCanalIngreso) ? 0L : solicitud.IdAgrupador;/' "$X/RreRequisitos.java"
LOG="$LOGS/7b.log"
echo "== mutation 7 (re-run with FMM-RRE-67): D-F2-15 fixed, grouped amount read with the INPUT IdAgrupador" > "$LOG"
diff -r "$SRC/BcsFondoMutuoServicios/src" "$R/BcsFondoMutuoServicios/src" >> "$LOG" 2>&1
(cd "$R/BcsFondoMutuoServicios" && mvn -o -q -f pom.xml test -Dfile.encoding=UTF-8 -Dtest='RreEquivalenciaTest,RreFrescasTest' -Dsurefire.failIfNoSpecifiedTests=false) >> "$LOG" 2>&1
echo "exit $?" >> "$LOG"
echo "7b: $(grep -a -c -E '<<< (FAIL|ERROR)' "$LOG") failing tests"
