#!/usr/bin/env bash
# F3 W1 front 9, part B (the CHAIN): oracle of cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo (CPF) running over the REAL, unmodified ConsultarCustodiaPortafolioFFMM (the custody
# calculation it calls in process), both compiled from legacy/ into a TEMPORARY directory (legacy/ is only read; nothing is copied into legacy/ or modernized/); no HANA, no network, no
# credential. Same runner (runner/OracleRunner.java, NOT modified), same session emulation, same doubles runtime and same knobs as record_cpf.sh / record_ffmm.sh (ORACLE_WORK,
# ORACLE_SKIP_COMPILE, LEGACY_OVERLAY, LEGACY_OVERLAY_RES, ORACLE_SUBJECT, ORACLE_OUT, ORACLE_EXTRA_SOURCES, ORACLE_EXTRA_CP, ORACLE_CASES, ORACLE_REPEAT, ORACLE_REPORT, JAVA_OPTS, ORACLE_FJ_PARALLELISM).
# Differences from record_cpf.sh (the CPF front, whose goldens use a DOUBLE of the custody calculation and are NOT touched):
#  - ConsultarCustodiaPortafolioFFMM is compiled REAL (no generated double of it): CPF calls it in process with the same connection, exactly as in production;
#  - the registered doubles are only the collaborators outside the module: CalcularParidad, EntregarPreciosTasas and ConsultarEmpresa (the last one is called TWICE per request: first by FFMM,
#    then by CPF, D-F3A-07: both are answered by the case script);
#  - the cases live in cases-cpfchain/ (NOT cases-cpf/), passed with -Doracle.cases: no other record script can run them or touch their goldens;
#  - the cases load the SCHEMA AND SEED OF CPF (schema-cpf.sql, seed/base-cpf.sql: byte for byte the ones of the CPF front, with the MAP / TO_DECIMAL aliases of runner-cpf/H2FnCpf) and THEN
#    schema-cpfchain.sql / seed/base-cpfchain.sql (the custody tables CUS.Cartera, HIS.Cartera, GTA.Garantia, HIS.Garantia, the views FFMM joins and the columns CPF's stand-ins lack), through "esquemasExtra";
#  - -Doracle.repeat=N (default 20, ORACLE_REPEAT): every case runs N times in fresh sessions (parallelStream and two HashMaps are in the path) and the dumps are compared; an unstable case is listed in
#    $ORACLE_REPORT (default determinism-cpfchain.txt) and gets NO golden (exit 4).
# The legacy main() of the classes (real-looking ids, PlataformaUtils.getDESAConnection / getPRESURAConnection) is compiled and NEVER called (RULE-076).
#   ./record_cpfchain.sh [caseNamePrefix]
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
LEG="$WS/legacy/otr-sva-optimus-corredores/BSCL"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
OVERLAY="${LEGACY_OVERLAY:-}"
OVERLAY_RES="${LEGACY_OVERLAY_RES:-}"
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
pick() {
  local f="$1"
  if [ -n "$OVERLAY" ] && [ -f "$OVERLAY/$(basename "$f")" ]; then echo "$OVERLAY/$(basename "$f")"; else echo "$f"; fi
}

FM="$LEG/FondoMutuo/JAVA/BcsFondoMutuoServicios/src"; FMP="$FM/cl/bcs/fondomutuo"
PLS="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"; PLP="$PLS/cl/bcs/plataforma"
UT="$LEG/Plataforma/JAVA/BcsUtils/src/cl/bcs/utils"

echo "== generating shim Utils and doubles into $WORK/gen"
rm -rf "$WORK/gen"; mkdir -p "$WORK/gen" "$WORK/classes" "$WORK/run"
$PY -I "$HERE/tools/gen_utils.py" "$(mx "$UT/Utils.java")" "$(mx "$WORK/gen")"
# the registered doubles (generated from the REAL sources, never editing legacy/): the two Plataforma calls of CalcularMontoNumeroCuota / CPF and ConsultarEmpresa. NO double of FFMM: it is REAL
DS=( "$(mx "$PLP/mer/CalcularParidad.java")" "$(mx "$PLP/tas/EntregarPreciosTasas.java")" "$(mx "$PLP/pla/utils/ConsultarEmpresa.java")" )
$PY -I "$HERE/tools/gen_doubles.py" "$(mx "$WORK/gen")" "${DS[@]}"
mkdir -p "$WORK/gen/cl/bcs/plataforma/pla/utils"
sed 's/System.currentTimeMillis()/cl.bcs.oracle.Clock.millis()/g' "$(pick "$PLP/pla/utils/FechaDB.java")" > "$WORK/gen/cl/bcs/plataforma/pla/utils/FechaDB.java"

SRC=()
for c in ConsultarCustodiaPortafolioFondoMutuo ConsultarCustodiaPortafolioFFMM CalcularMontoNumeroCuota; do SRC+=("$(pick "$FMP/$c.java")"); done
for f in "$FMP"/pojo/*.java; do SRC+=("$(pick "$f")"); done
for c in SQL NamedParameterStatement NamedCallableStatement JavaCall GsonTimestampDeserializer GsonDateDeserializer Fecha BcsPojo; do SRC+=("$(pick "$UT/$c.java")"); done
for c in pla/utils/ConfiguracionSistema pla/utils/Diccionario pla/utils/PlataformaUtils pla/utils/FileLoader pla/utils/BarraProgreso pla/utils/MonedaFormato pla/RevisarRequisitosBasicos \
         pla/ObtenerDiaSiguiente pla/ObtenerDiaAnterior pla/ObtenerDiaHabilMultiplesPaises; do SRC+=("$(pick "$PLP/$c.java")"); done
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$WORK/gen" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$HERE/shims" "$HERE/shims-cpfchain" "$HERE/runner" "$HERE/runner-cpf" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
for f in ${ORACLE_EXTRA_SOURCES:-}; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then
echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
cd "$WORK/run"
REPORT="${ORACLE_REPORT:-$BASE/determinism-cpfchain.txt}"
# D-F3A-04: the legacy assigns the IdInterno of the solicitudes inside parallelStream().forEach. The JVM runs with ForkJoinPool.common.parallelism=${ORACLE_FJ_PARALLELISM:-0} (no worker threads: the
# caller runs every leaf) so that the recorded dump never depends on the number of cores or on thread timing (with the default pool 1 case of 163 flaked in 19 re-runs). The assignment pattern
# is the same with 0, 1 and 2 workers (2 rows: 3,2; 3 rows: 4,3,2; 4 rows: 5,4,2,3 = the JDK's splitting order, not the list order): the tests compare those ids modulo renaming.
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF"
       "-Djava.util.concurrent.ForkJoinPool.common.parallelism=${ORACLE_FJ_PARALLELISM:-0}"
       "-Doracle.repeat=${ORACLE_REPEAT:-20}" "-Doracle.report=$(mx "$REPORT")" "-Doracle.cases=$(mx "${ORACLE_CASES:-$BASE/cases-cpfchain}")" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
[ -n "${ORACLE_MUTANT:-}" ] && OPTS+=( "-Doracle.mutant=$ORACLE_MUTANT" )
RUNCP="$(mx "$WORK/classes")$SEP$CP"
[ -n "$OVERLAY_RES" ] && RUNCP="$RUNCP$SEP$(mx "$OVERLAY_RES")"
RUNCP="$RUNCP$SEP$(mx "$FM")$SEP$(mx "$PLS")"
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$ORACLE_EXTRA_CP"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" "${ORACLE_MAIN:-OracleRunner}" "$(mx "$BASE")" "${1:-}"
