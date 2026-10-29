#!/usr/bin/env bash
# F3 W1 front 1: oracle of cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate (JavaCall, read only). Same runner (runner/OracleRunner.java), same session
# emulation, same doubles runtime and same knobs as record.sh / record_rre.sh (ORACLE_WORK, ORACLE_SKIP_COMPILE, LEGACY_OVERLAY, ORACLE_SUBJECT, ORACLE_OUT,
# ORACLE_EXTRA_SOURCES, ORACLE_EXTRA_CP, JAVA_OPTS). Everything is compiled into a TEMPORARY directory (legacy/ is only read, nothing is copied into
# legacy/ or modernized/); no HANA, no network, no credential. Differences from record_rre.sh:
#  - the compiled closure is only what FSA reaches: ConsultarFechaSolicitudAporteRescate (REAL, unmodified, or its LEGACY_OVERLAY copy = mutation of the
#    goldens), CalcularFechaSolicitudAporteRescate (real), ObtenerPerfilFondoMutuo (real), ConstantesFondoMutuo and the pojo/ of the package, the
#    BcsUtils/Plataforma kit and ObtenerDiaSiguiente/Anterior/HabilMultiplesPaises (all real);
#  - ConsultarEmpresa (Plataforma) is the registered double answered by seed/doubles-base.json ("ConsultarEmpresa", IdPais 56) or by the case "colaboradores";
#  - the dead import CrearSolicitudAporteReinversion.InSolicitudesAporte (FSA:21) compiles against shims-fsa/ (empty nested type, never executed);
#  - -Doracle.fsa=true: OracleRunner runs ONLY the cases with "consultarFechaReal": true (FMM-FSA-*) and the other scripts never run them;
#  - -Doracle.repeat=N (default 20, ORACLE_REPEAT): every case is executed N times in fresh sessions (parallelStream / toMap are in the path) and the
#    dumps are compared; an unstable case is listed with the reason in $ORACLE_REPORT (default: determinism-fsa.txt next to the cases) and FSA never
#    writes the golden of an unstable case (the script exits 4 so nobody syncs it silently);
#  - the case loads seed/base-fsa.sql (fake universe) through "esquemasExtra"; schema-fsa.sql is only a MANIFEST of the objects FSA reads (every one is already
#    in schema-fmm.sql with REAL catalog types; no DDL, nothing is loaded from it);
# The legacy main() (FSA:356-373, with a real-looking JSON and PlataformaUtils.getBOLSAConnection) is compiled as part of the class and NEVER called.
#   ./record_fsa.sh [caseNamePrefix]
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
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
# the only registered double of this oracle: Plataforma ConsultarEmpresa (FSA:250-252 reads IdPais)
DS=( "$(mx "$PLP/pla/utils/ConsultarEmpresa.java")" )
$PY -I "$HERE/tools/gen_doubles.py" "$(mx "$WORK/gen")" "${DS[@]}"
mkdir -p "$WORK/gen/cl/bcs/plataforma/pla/utils"
sed 's/System.currentTimeMillis()/cl.bcs.oracle.Clock.millis()/g' "$(pick "$PLP/pla/utils/FechaDB.java")" > "$WORK/gen/cl/bcs/plataforma/pla/utils/FechaDB.java"

SRC=()
for c in ConsultarFechaSolicitudAporteRescate CalcularFechaSolicitudAporteRescate ObtenerPerfilFondoMutuo; do SRC+=("$(pick "$FMP/$c.java")"); done
for f in "$FMP"/pojo/*.java; do SRC+=("$(pick "$f")"); done
for c in SQL NamedParameterStatement NamedCallableStatement JavaCall GsonTimestampDeserializer GsonDateDeserializer Fecha BcsPojo; do SRC+=("$(pick "$UT/$c.java")"); done
for c in pla/utils/ConfiguracionSistema pla/utils/Diccionario pla/utils/PlataformaUtils pla/utils/FileLoader pla/utils/BarraProgreso pla/utils/MonedaFormato pla/RevisarRequisitosBasicos \
         pla/ObtenerDiaSiguiente pla/ObtenerDiaAnterior pla/ObtenerDiaHabilMultiplesPaises; do SRC+=("$(pick "$PLP/$c.java")"); done
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$WORK/gen" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$HERE/shims" "$HERE/shims-fsa" "$HERE/runner" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
for f in ${ORACLE_EXTRA_SOURCES:-}; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then
echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
cd "$WORK/run"
REPORT="${ORACLE_REPORT:-$BASE/determinism-fsa.txt}"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF" "-Doracle.fsa=true"
       "-Doracle.repeat=${ORACLE_REPEAT:-20}" "-Doracle.report=$(mx "$REPORT")" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
[ -n "${ORACLE_MUTANT:-}" ] && OPTS+=( "-Doracle.mutant=$ORACLE_MUTANT" )
[ -n "${ORACLE_CASES:-}" ] && OPTS+=( "-Doracle.cases=$(mx "$ORACLE_CASES")" )
RUNCP="$(mx "$WORK/classes")$SEP$CP$SEP$(mx "$FM")$SEP$(mx "$PLS")"
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$ORACLE_EXTRA_CP"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" "${ORACLE_MAIN:-OracleRunner}" "$(mx "$BASE")" "${1:-}"
