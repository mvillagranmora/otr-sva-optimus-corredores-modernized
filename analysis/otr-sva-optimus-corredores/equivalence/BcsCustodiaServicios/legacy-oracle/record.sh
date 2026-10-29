#!/usr/bin/env bash
# Oracle of cl.bcs.custodia.ConsultarFlujoCustodia (JavaCall; slice RULES_CUSTODIA_FLUJO.md CUS-FLU-01..36). Same pattern as
# BcsFondoMutuoServicios/legacy-oracle/record.sh: compiles the UNMODIFIED legacy sources into a temporary directory (NEVER inside legacy/, which
# is only read) and runs them on in-memory H2 seeded per case. No HANA, no network, no credential.
#
#   ./record.sh [caseNamePrefix]                  (re)records cases/<case>/expected.json (the goldens) from the legacy
#   ORACLE_REPEAT=5 ./record.sh [prefix]          runs every case 5 times; a case whose dumps differ is reported UNSTABLE and gets no golden
#   ORACLE_OUT=<dir> ./record.sh                  writes <dir>/<case>.json instead of the goldens (re-recording check / mutation check)
#   LEGACY_OVERLAY=<dir> ORACLE_OUT=<dir> ...     compiles a MUTATED copy (same file name) instead of the legacy source (proves the goldens can fail)
#   ORACLE_WORK=<dir> | ORACLE_SKIP_COMPILE=1     scratch dir / reuse the compiled classes
#
# REAL legacy sources used AS IS: cl.bcs.custodia.ConsultarFlujoCustodia; BcsUtils SQL, NamedParameterStatement, NamedCallableStatement, JavaCall,
# Gson{Timestamp,Date}Deserializer, Fecha, BcsPojo; Plataforma ConfiguracionSistema, Diccionario, PlataformaUtils (MensajeSalida), FileLoader,
# BarraProgreso, ConsultarEmpresa, ObtenerDiaSiguiente (+ the clock-pinned copy of FechaDB, as FondoMutuo).
# GENERATED from the real sources: shim cl.bcs.utils.Utils (tools/gen_utils.py: verbatim methods + H2 getSecuencia) and the REGISTERED DOUBLE
# cl.bcs.custodia.utils.ValorizarInstrumento (tools/gen_doubles.py --drop-helpers: legacy declarations, logica answered by the case script).
# Hand written: shims/ (datadog Trace, dbcp2 BasicDataSource compile-only, cl.bcs.utils.Log no-op), h2/ (dialect bridge, HANA functions,
# MatrizUso witness, case runner + dump: SINGLE SOURCE copied into the module tests by sync.py), runner/ (recorder, Doubles).
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"                       # .../equivalence/BcsCustodiaServicios
WS="$(cd "$BASE/../../../.." && pwd)"                # workspace root
LEG="$WS/legacy/otr-sva-optimus-corredores/BSCL"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
OVERLAY="${LEGACY_OVERLAY:-}"
PY="${PYTHON_CMD:-py -3}"

SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
CP=""
for j in "com/h2database/h2/2.4.240/h2-2.4.240.jar" "com/google/code/gson/gson/2.13.2/gson-2.13.2.jar" \
         "commons-io/commons-io/2.11.0/commons-io-2.11.0.jar" "commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar" \
         "commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar" "commons-logging/commons-logging/1.2/commons-logging-1.2.jar" \
         "commons-lang/commons-lang/2.6/commons-lang-2.6.jar" "commons-configuration/commons-configuration/1.2/commons-configuration-1.2.jar" \
         "org/apache/logging/log4j/log4j-api/2.24.3/log4j-api-2.24.3.jar"; do
  [ -f "$M2/$j" ] || { echo "missing jar $M2/$j" >&2; exit 3; }
  CP="${CP:+$CP$SEP}$(mx "$M2/$j")"
done
pick() { # the mutated copy when LEGACY_OVERLAY has one with the same file name
  local f="$1"
  if [ -n "$OVERLAY" ] && [ -f "$OVERLAY/$(basename "$f")" ]; then echo "$OVERLAY/$(basename "$f")"; else echo "$f"; fi
}

CUSS="$LEG/Custodia/JAVA/BcsCustodiaServicios/src"; CUS="$CUSS/cl/bcs/custodia"
PLS="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"; PLP="$PLS/cl/bcs/plataforma"
UT="$LEG/Plataforma/JAVA/BcsUtils/src/cl/bcs/utils"

echo "== generating shim Utils and the ValorizarInstrumento double into $WORK/gen"
rm -rf "$WORK/gen"; mkdir -p "$WORK/gen" "$WORK/classes" "$WORK/run"
$PY -I "$HERE/tools/gen_utils.py" "$(mx "$UT/Utils.java")" "$(mx "$WORK/gen")"
$PY -I "$HERE/tools/gen_doubles.py" --drop-helpers "$(mx "$WORK/gen")" "$(mx "$CUS/utils/ValorizarInstrumento.java")"
mkdir -p "$WORK/gen/cl/bcs/plataforma/pla/utils"
sed 's/System.currentTimeMillis()/cl.bcs.oracle.Clock.millis()/g' "$(pick "$PLP/pla/utils/FechaDB.java")" > "$WORK/gen/cl/bcs/plataforma/pla/utils/FechaDB.java"

SRC=( "$(pick "$CUS/ConsultarFlujoCustodia.java")" )
for c in SQL NamedParameterStatement NamedCallableStatement JavaCall GsonTimestampDeserializer GsonDateDeserializer Fecha BcsPojo; do SRC+=("$(pick "$UT/$c.java")"); done
for c in pla/utils/ConfiguracionSistema pla/utils/Diccionario pla/utils/PlataformaUtils pla/utils/FileLoader pla/utils/BarraProgreso \
         pla/utils/ConsultarEmpresa pla/ObtenerDiaSiguiente; do SRC+=("$(pick "$PLP/$c.java")"); done
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$WORK/gen" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$HERE/shims" "$HERE/h2" "$HERE/runner" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then
echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
cd "$WORK/run"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Duser.country=US" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_REPEAT:-}" ] && OPTS+=( "-Doracle.repeat=$ORACLE_REPEAT" )
# the legacy loads its SQL with loadResourceToString("cl/bcs/.../sql/x.sql"): the legacy src roots (read only) are on the RUN classpath
RUNCP="$(mx "$WORK/classes")$SEP$CP$SEP$(mx "$CUSS")$SEP$(mx "$PLS")"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" FlujoOracleRunner "$(mx "$BASE")" "${1:-}"
